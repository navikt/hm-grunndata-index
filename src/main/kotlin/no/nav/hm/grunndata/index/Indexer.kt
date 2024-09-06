package no.nav.hm.grunndata.index


import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import java.io.StringReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch._types.mapping.TypeMapping
import org.opensearch.client.opensearch.core.BulkRequest
import org.opensearch.client.opensearch.core.BulkResponse
import org.opensearch.client.opensearch.core.DeleteRequest
import org.opensearch.client.opensearch.core.DeleteResponse
import org.opensearch.client.opensearch.indices.CreateIndexRequest
import org.opensearch.client.opensearch.indices.ExistsRequest
import org.opensearch.client.opensearch.indices.GetAliasRequest
import org.opensearch.client.opensearch.indices.IndexSettings
import org.opensearch.client.opensearch.indices.UpdateAliasesRequest
import org.opensearch.client.opensearch.indices.update_aliases.ActionBuilders.add
import org.slf4j.LoggerFactory


@Singleton
class Indexer(private val client: OpenSearchClient,
              private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(Indexer::class.java)
    }

    fun initIndex(indexName: String, settings: String?=null, mapping: String?=null) {
        val request = ExistsRequest.Builder().index(indexName).build()
        if (!client.indices().exists(request).value()) {
            if (createIndex(indexName, settings, mapping))
                LOG.info("$indexName has been created")
            else
                LOG.error("Failed to create $indexName")
        }
    }

    fun updateAlias(indexName: String, aliasName: String): Boolean {
        val request = UpdateAliasesRequest.Builder().apply {
            add().alias(aliasName).index(indexName)
        }.build()
        LOG.info("updateAlias for alias $aliasName and pointing to $indexName ")
        return client.indices().updateAliases(request).acknowledged()
    }

    fun getAlias(aliasName: String)
        = client.indices().getAlias(GetAliasRequest.Builder().name(aliasName).build()).result().keys


    fun createIndex(indexName: String, settings: String?=null, mapping: String?=null): Boolean {
        val mapper = client._transport().jsonpMapper()
        val createIndexRequest = CreateIndexRequest.Builder().index(indexName)
        settings?.let {
            val settingsParser = mapper.jsonProvider().createParser(StringReader(settings))
            val indexSettings = IndexSettings._DESERIALIZER.deserialize(settingsParser, mapper)
            createIndexRequest.settings(indexSettings)
        }
        mapping?.let {
            val mappingsParser = mapper.jsonProvider().createParser(StringReader(mapping))
            val typeMapping = TypeMapping._DESERIALIZER.deserialize(mappingsParser, mapper)
            createIndexRequest.mappings(typeMapping)
        }
        return client.indices().create(createIndexRequest.build()).acknowledged()!!
    }

    fun index(doc: SearchDoc, indexName: String): BulkResponse {
        return index(listOf(doc), indexName)
    }

    fun index(docs: List<SearchDoc>, indexName: String): BulkResponse {
        val bulkRequest = BulkRequest.Builder()
        docs.forEach { doc ->
            bulkRequest.operations {
                op -> op.index {
                    index -> index.index(indexName).id(doc.id).document(doc)
                }
            }
        }
        return try {
            client.bulk(bulkRequest.build())
        }
        catch (e: Exception) {
            LOG.error("Failed to index $docs to $indexName", e)
            throw e
        }
    }

    fun delete(id: String, indexName: String): DeleteResponse {
        val request = DeleteRequest.Builder().index(indexName).id(id)
        return client.delete(request.build())
    }

    fun indexExists(indexName: String):Boolean =
        client.indices().exists(ExistsRequest.Builder().index(indexName).build()).value()

    fun initAlias(aliasName: String, settings: String?=null, mapping: String?=null) {
        val alias = getAlias(aliasName)
        if (alias.isEmpty()) {
            LOG.warn("alias $aliasName is not pointing any index")
            val indexName = "${aliasName}_${LocalDate.now()}"
            LOG.warn("Creating index $indexName")
            createIndex(indexName,settings, mapping)
            updateAlias(indexName, aliasName)
        }
        else
           LOG.info("alias $aliasName is pointing to ${alias.elementAt(0)}")
    }

}

fun createIndexName(type: IndexType): String = "${type.name}_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))}"

enum class IndexType {
    products, external_products, suppliers, agreements, news
}

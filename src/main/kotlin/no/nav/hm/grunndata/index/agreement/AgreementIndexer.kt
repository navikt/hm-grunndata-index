package no.nav.hm.grunndata.index.agreement

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.createIndexName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import org.opensearch.client.opensearch.core.BulkResponse

@Singleton
class AgreementIndexer(private val indexer: Indexer,
                       private val agreementGdbApiClient: AgreementGdbApiClient,
                       @Value("\${agreements.aliasName}") private val aliasName: String) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexer::class.java)
        private val settings = AgreementIndexer::class.java
            .getResource("/opensearch/agreements_settings.json")?.readText()
        private val mapping = AgreementIndexer::class.java
            .getResource("/opensearch/agreements_mapping.json")?.readText()
    }

    init {
        try {
            initAlias()
        } catch (e: Exception) {
            LOG.error("OpenSearch might not be ready ${e.message}, will wait 10s and retry")
            Thread.sleep(10000)
            initAlias()
        }
    }

    fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexType.agreements)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName)
        }
        var updated =  LocalDateTime.now().minusYears(30)
        var page = agreementGdbApiClient.findAgreements(params = mapOf("updatedAfter" to updated.toString()),
            size=1000, page = 0, sort="updated,asc")
        var lastId: UUID? = null
        while(page.numberOfElements>0) {
            val agreements = page.content.map { it.toDoc() }.filter {  it.status != AgreementStatus.DELETED }
            LOG.info("indexing ${agreements.size} agreements to $indexName")
            if (agreements.isNotEmpty()) index(agreements, indexName)
            val last = page.last()
            if (updated.equals(last.updated) && last.id == lastId) {
                LOG.info("Last updated ${last.updated} ${last.id} is the same, increasing last updated")
                updated = updated.plusNanos(1000000)
            }
            else {
                lastId = last.id
                updated = last.updated
            }
            page = agreementGdbApiClient.findAgreements(params = mapOf("updatedAfter" to updated.toString()),
                size=1000, page = 0, sort="updated,asc")
        }
        if (alias) {
           updateAlias(indexName)
        }
    }

    fun index(docs: List<AgreementDoc>): BulkResponse = indexer.index(docs, aliasName)

    fun index(doc: AgreementDoc): BulkResponse = indexer.index(listOf(doc), aliasName)

    fun index(doc: AgreementDoc, indexName: String): BulkResponse = indexer.index(listOf(doc), indexName)

    fun index(docs: List<AgreementDoc>, indexName: String): BulkResponse = indexer.index(docs,indexName)

    fun createIndex(indexName: String): Boolean = indexer.createIndex(indexName, settings, mapping)

    fun updateAlias(indexName: String): Boolean = indexer.updateAlias(indexName,aliasName)

    fun indexExists(indexName: String): Boolean = indexer.indexExists(indexName)

    fun initAlias() = indexer.initAlias(aliasName, settings, mapping)

    fun getAlias() = indexer.existsAlias(aliasName)
}

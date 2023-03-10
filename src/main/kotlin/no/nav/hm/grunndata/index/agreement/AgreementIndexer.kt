package no.nav.hm.grunndata.index.agreement

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.Indexer
import org.opensearch.action.bulk.BulkResponse
import org.slf4j.LoggerFactory

@Singleton
class AgreementIndexer(private val indexer: Indexer,
                       @Value("\${agreements.aliasName}") private val aliasName: String,
                       @Value("\${agreements.indexName}") private val indexName: String ) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexer::class.java)
    }

    init {
        try {
            indexer.initIndex(indexName)
            indexer.initAlias(aliasName,indexName)
        } catch (e: Exception) {
            LOG.error("OpenSearch might not be ready ${e.message}, will wait 10s and retry")
            Thread.sleep(10000)
            indexer.initIndex(indexName)
            indexer.initAlias(aliasName,indexName)
        }
    }

    fun index(docs: List<AgreementDoc>): BulkResponse {
        return indexer.index(docs, aliasName)
    }

    fun index(doc: AgreementDoc): BulkResponse {
        return indexer.index(listOf(doc), aliasName)
    }

    fun index(doc: AgreementDoc, indexName: String): BulkResponse {
        return indexer.index(listOf(doc), indexName)
    }

    fun index(docs: List<AgreementDoc>, indexName: String): BulkResponse {
        return indexer.index(docs,indexName)
    }

    fun createIndex(indexName: String): Boolean = indexer.createIndex(indexName)

    fun updateAlias(indexName: String): Boolean = indexer.updateAlias(indexName,aliasName)

    fun indexExists(indexName: String): Boolean = indexer.indexExists(indexName)
}

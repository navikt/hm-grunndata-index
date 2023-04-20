package no.nav.hm.grunndata.index.supplier

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.product.ProductIndexer
import org.opensearch.action.bulk.BulkResponse
import org.slf4j.LoggerFactory

@Singleton
class SupplierIndexer(private val indexer: Indexer,
                      @Value("\${suppliers.aliasName}") private val aliasName: String) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexer::class.java)
        private val settings = ProductIndexer::class.java
            .getResource("/opensearch/suppliers_settings.json")?.readText()
        private val mapping = ProductIndexer::class.java
            .getResource("/opensearch/suppliers_mapping.json")?.readText()
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

    fun index(docs: List<SupplierDoc>): BulkResponse = indexer.index(docs, aliasName)

    fun index(doc: SupplierDoc): BulkResponse = indexer.index(listOf(doc), aliasName)

    fun index(doc: SupplierDoc, indexName: String): BulkResponse = indexer.index(listOf(doc), indexName)


    fun index(docs: List<SupplierDoc>, indexName: String): BulkResponse = indexer.index(docs,indexName)


    fun createIndex(indexName: String): Boolean = indexer.createIndex(indexName, settings, mapping)

    fun updateAlias(indexName: String): Boolean = indexer.updateAlias(indexName,aliasName)

    fun indexExists(indexName: String): Boolean = indexer.indexExists(indexName)

    fun initAlias() = indexer.initAlias(aliasName, settings, mapping)

    fun getAlias() = indexer.getAlias(aliasName)
}

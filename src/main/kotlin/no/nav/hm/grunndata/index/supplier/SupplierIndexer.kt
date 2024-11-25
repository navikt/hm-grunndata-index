package no.nav.hm.grunndata.index.supplier

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.createIndexName
import no.nav.hm.grunndata.index.product.ProductIndexer
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch.core.BulkResponse

import org.slf4j.LoggerFactory

@Singleton
class SupplierIndexer(private val supplierGdbApiClient: SupplierGdbApiClient,
                      @Value("\${suppliers.aliasName}") private val aliasName: String,
                      private val client: OpenSearchClient
): Indexer(client, settings, mapping, aliasName) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexer::class.java)
        private val settings = SupplierIndexer::class.java
            .getResource("/opensearch/suppliers_settings.json")!!.readText()
        private val mapping = SupplierIndexer::class.java
            .getResource("/opensearch/suppliers_mapping.json")!!.readText()
    }


    fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexType.suppliers)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, settings, mapping)
        }
        val page = supplierGdbApiClient.findSuppliers(size=5000, page = 0, sort="updated,asc")
        val suppliers = page.content.map { it.toDoc() }
        LOG.info("indexing ${suppliers.size} suppliers to $indexName")
        index(suppliers, indexName)
        if (alias) {
           updateAlias(indexName)
        }
    }

}

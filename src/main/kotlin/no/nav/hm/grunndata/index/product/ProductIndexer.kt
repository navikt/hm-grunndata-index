package no.nav.hm.grunndata.index.product

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.agreement.AgreementLabels
import no.nav.hm.grunndata.index.createIndexName
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.opensearch.action.admin.indices.alias.get.GetAliasesRequest
import org.opensearch.action.bulk.BulkResponse
import org.opensearch.client.RequestOptions
import org.opensearch.rest.RestStatus
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Singleton
class ProductIndexer(private val indexer: Indexer,
                     @Value("\${products.aliasName}") private val aliasName: String,
                     private val gdbApiClient: GdbApiClient,
                     private val isoCategoryService: IsoCategoryService,
                     private val agreementLabels: AgreementLabels) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexer::class.java)
        private val settings = ProductIndexer::class.java
            .getResource("/opensearch/products_settings.json")?.readText()
        private val mapping = ProductIndexer::class.java
            .getResource("/opensearch/products_mapping.json")?.readText()
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
        val indexName = createIndexName(IndexType.products)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName)
        }
        val dateString =  LocalDateTime.now().minusYears(30).toString()
        var page = gdbApiClient.findProducts(params = mapOf("updated" to dateString),
            size=1000, page = 0, sort="updated,asc")
        while(page.pageNumber<page.totalPages) {
            if (page.numberOfElements>0) {

                val products = page.content
                    .filter { it.status != ProductStatus.DELETED }
                    .map { it.toDoc(isoCategoryService, agreementLabels) }
                LOG.info("indexing ${products.size} products to $indexName")
                if (products.isNotEmpty()) index(products, indexName)
            }
            page = gdbApiClient.findProducts(params = mapOf("updated" to dateString),
                size=1000, page = page.pageNumber+1, sort="updated,asc")
        }
        if (alias) {
            updateAlias(indexName)
        }
    }

    fun index(docs: List<ProductDoc>): BulkResponse = indexer.index(docs, aliasName)


    fun index(doc: ProductDoc): BulkResponse = indexer.index(listOf(doc), aliasName)


    fun index(doc: ProductDoc, indexName: String): BulkResponse =
        indexer.index(listOf(doc), indexName)


    fun index(docs: List<ProductDoc>, indexName: String): BulkResponse =
        indexer.index(docs,indexName)


    fun createIndex(indexName: String): Boolean = indexer.createIndex(indexName, settings, mapping)

    fun updateAlias(indexName: String): Boolean = indexer.updateAlias(indexName,aliasName)

    fun getAlias() = indexer.getAlias(aliasName)

    fun indexExists(indexName: String): Boolean = indexer.indexExists(indexName)

    fun initAlias() = indexer.initAlias(aliasName, settings, mapping)

}

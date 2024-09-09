package no.nav.hm.grunndata.index.product

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.createIndexName
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.opensearch.client.opensearch.core.BulkResponse
import org.opensearch.client.opensearch.core.DeleteResponse

import org.slf4j.LoggerFactory

@Singleton
class ProductIndexer(private val indexer: Indexer,
                     @Value("\${products.aliasName}") private val aliasName: String,
                     private val gdbApiClient: GdbApiClient,
                     private val isoCategoryService: IsoCategoryService) {

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
        var updated =  LocalDateTime.now().minusYears(30)
        var page = gdbApiClient.findProducts(updated = updated.toString(),
            size=3000, page = 0, sort="updated,asc")
        var lastId: UUID? = null
        while(page.numberOfElements>0) {
            val products = page.content
                .map { it.toDoc(isoCategoryService) }.filter {
                    it.status != ProductStatus.DELETED
                }
            LOG.info("indexing ${products.size} products to $indexName")
            if (products.isNotEmpty()) index(products, indexName)
            val last = page.last()
            if (updated.equals(last.updated) && last.id == lastId) {
                LOG.info("Last updated ${last.updated} ${last.id} is the same, increasing last updated")
                updated = updated.plusNanos(1000000)
            }
            else {
                lastId = last.id
                updated = last.updated
            }
            LOG.info("updated is now: $updated")
            page = gdbApiClient.findProducts(updated=updated.toString(),
                size=3000, page = 0, sort="updated,asc")
        }
        if (alias) {
            updateAlias(indexName)
        }
    }

    fun reIndexBySupplierId(supplierId: UUID) {
        val page = gdbApiClient.findProductsBySupplierId(supplierId = supplierId,
            size=3000, page = 0, sort="updated,asc")
        if (page.numberOfElements>0) {
            val products = page.content.map { it.toDoc(isoCategoryService)}.filter {
                it.status != ProductStatus.DELETED
            }
            LOG.info("indexing ${products.size} products to $aliasName")
            index(products)
        }
    }

    fun reIndexBySeriesId(seriesId: UUID) {
        val page = gdbApiClient.findProductsBySeriesId(seriesUUID = seriesId,
            size=3000, page = 0, sort="updated,asc")
        if (page.numberOfElements>0) {
            val products = page.content.map { it.toDoc(isoCategoryService)}.filter {
                it.status != ProductStatus.DELETED
            }
            LOG.info("indexing ${products.size} products to $aliasName")
            index(products)
        }
    }


    fun index(docs: List<ProductDoc>): BulkResponse = indexer.index(docs, aliasName)


    fun index(doc: ProductDoc): BulkResponse = indexer.index(listOf(doc), aliasName)


    fun index(doc: ProductDoc, indexName: String): BulkResponse =
        indexer.index(listOf(doc), indexName)


    fun index(docs: List<ProductDoc>, indexName: String): BulkResponse =
        indexer.index(docs,indexName)

    fun delete(uuid: UUID): DeleteResponse = indexer.delete(uuid.toString(), aliasName)

    fun createIndex(indexName: String): Boolean = indexer.createIndex(indexName, settings, mapping)

    fun updateAlias(indexName: String): Boolean = indexer.updateAlias(indexName,aliasName)

    fun getAlias() = indexer.existsAlias(aliasName)

    fun indexExists(indexName: String): Boolean = indexer.indexExists(indexName)

    fun initAlias() = indexer.initAlias(aliasName, settings, mapping)



}

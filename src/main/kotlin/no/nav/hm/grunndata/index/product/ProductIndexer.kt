package no.nav.hm.grunndata.index.product

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.createIndexName
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.opensearch.client.opensearch.core.BulkResponse
import org.opensearch.client.opensearch.core.DeleteResponse

import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.UUID
import org.opensearch.client.opensearch.OpenSearchClient

@Singleton
class ProductIndexer(
    @Value("\${products.aliasName}") private val aliasName: String,
    private val gdbApiClient: GdbApiClient,
    private val isoCategoryService: IsoCategoryService,
    private val client: OpenSearchClient
):Indexer(client, settings, mapping, aliasName) {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexer::class.java)
        private val settings = ProductIndexer::class.java
            .getResource("/opensearch/products_settings.json")!!.readText()
        private val mapping = ProductIndexer::class.java
            .getResource("/opensearch/products_mapping.json")!!.readText()
    }

    fun count() = docCount()

    fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexType.products)
        if (!indexExists(indexName)) {
            createIndex(indexName, settings, mapping)
        }
        var updated = LocalDateTime.now().minusYears(1000)
        var page = gdbApiClient.findProducts(
            updated = updated.toString(),
            size = 3000, page = 0, sort = "updated,asc"
        )
        var lastId: UUID? = null
        while (page.numberOfElements > 0) {
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
            } else {
                lastId = last.id
                updated = last.updated
            }
            LOG.info("updated is now: $updated")
            page = gdbApiClient.findProducts(
                updated = updated.toString(),
                size = 3000, page = 0, sort = "updated,asc"
            )
        }
        if (alias) {
            updateAlias(indexName = indexName)
        }
    }

    fun reIndexBySupplierId(supplierId: UUID) {
        val page = gdbApiClient.findProductsBySupplierId(
            supplierId = supplierId,
            size = 3000, page = 0, sort = "updated,asc"
        )
        if (page.numberOfElements > 0) {
            val products = page.content.map { it.toDoc(isoCategoryService) }.filter {
                it.status != ProductStatus.DELETED
            }
            LOG.info("indexing ${products.size} products to $aliasName")
            index(products, aliasName)
        }
    }

    fun reIndexBySeriesId(seriesId: UUID) {
        val page = gdbApiClient.findProductsBySeriesId(
            seriesUUID = seriesId,
            size = 3000, page = 0, sort = "updated,asc"
        )
        if (page.numberOfElements > 0) {
            val products = page.content.map { it.toDoc(isoCategoryService) }.filter {
                it.status != ProductStatus.DELETED
            }
            LOG.info("indexing ${products.size} products to $aliasName")
            index(products, aliasName)
        }
    }

    fun reIndexByIsoCategory(iso: String) {
        val page = gdbApiClient.findProductsByIsoCategory(iso, 3000, 0, "updated,asc")
        if (page.numberOfElements > 0) {
            val products = page.content.map { it.toDoc(isoCategoryService) }.filter {
                it.status != ProductStatus.DELETED
            }
            LOG.info("indexing ${products.size} products to $aliasName")
            index(products, aliasName)
        }
    }

}

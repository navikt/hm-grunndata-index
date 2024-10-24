package no.nav.hm.grunndata.index.alternative_product

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.createIndexName
import no.nav.hm.grunndata.index.product.GdbApiClient
import no.nav.hm.grunndata.index.product.IsoCategoryService
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.opensearch.action.bulk.BulkResponse
import org.opensearch.action.delete.DeleteResponse
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*


@Singleton
class AlternativeProductIndexer(
    private val indexer: Indexer,
    @Value("\${alternative_products.aliasName}") private val aliasName: String,
    private val gdbApiClient: GdbApiClient,
    private val isoCategoryService: IsoCategoryService,
    private val techLabelService: TechLabelService,
    private val alternativProdukterClient: AlternativProdukterClient
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductIndexer::class.java)
        private val settings = AlternativeProductIndexer::class.java
            .getResource("/opensearch/alternative_products_settings.json")?.readText()
        private val mapping = AlternativeProductIndexer::class.java
            .getResource("/opensearch/alternative_products_mapping.json")?.readText()
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
        val indexName = createIndexName(IndexType.alternative_products)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName)
        }
        var updated =  LocalDateTime.now().minusYears(30)
        var page = gdbApiClient.findProducts(updated = updated.toString(),
            size=3000, page = 0, sort="updated,asc")
        var lastId: UUID? = null
        while(page.numberOfElements>0) {
            val products = page.content.filter {
                it.status != ProductStatus.DELETED && it.hmsArtNr != null
            }.map { it.toDoc(isoCategoryService, techLabelService, alternativProdukterClient)}
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

    fun reIndexByIsoCategory(isoCategory: String) {
        val page = gdbApiClient.findProductsByIsoCategory(isoCategory = isoCategory,
            size=3000, page = 0, sort="updated,asc")
        if (page.numberOfElements>0) {
            val products = page.content.filter {
                it.status != ProductStatus.DELETED && it.hmsArtNr != null
            }.map { it.toDoc(isoCategoryService, techLabelService, alternativProdukterClient)}
            LOG.info("indexing ${products.size} products to $aliasName")
            index(products)
        }
    }

    fun index(docs: List<AlternativeProductDoc>): BulkResponse = indexer.index(docs, aliasName)

    fun index(doc: AlternativeProductDoc): BulkResponse = indexer.index(listOf(doc), aliasName)

    fun index(doc: AlternativeProductDoc, indexName: String): BulkResponse =
        indexer.index(listOf(doc), indexName)

    fun index(docs: List<AlternativeProductDoc>, indexName: String): BulkResponse =
        indexer.index(docs,indexName)

    fun delete(uuid: UUID): DeleteResponse = indexer.delete(uuid.toString(), aliasName)

    fun createIndex(indexName: String): Boolean = indexer.createIndex(indexName, settings, mapping)

    fun updateAlias(indexName: String): Boolean = indexer.updateAlias(indexName,aliasName)

    fun getAlias() = indexer.getAlias(aliasName)

    fun indexExists(indexName: String): Boolean = indexer.indexExists(indexName)

    fun initAlias() = indexer.initAlias(aliasName, settings, mapping)
}

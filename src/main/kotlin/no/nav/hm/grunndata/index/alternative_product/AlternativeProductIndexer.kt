package no.nav.hm.grunndata.index.alternative_product

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.createIndexName
import no.nav.hm.grunndata.index.GdbApiClient
import no.nav.hm.grunndata.index.product.IsoCategoryService
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.slf4j.LoggerFactory
import org.opensearch.client.opensearch.OpenSearchClient



@Singleton
class AlternativeProductIndexer(
    @Value("\${alternative_products.aliasName}") private val aliasName: String,
    private val gdbApiClient: GdbApiClient,
    private val isoCategoryService: IsoCategoryService,
    private val techLabelService: TechLabelService,
    private val alternativProdukterClient: AlternativProdukterClient,
    private val client: OpenSearchClient
): Indexer(client, settings, mapping, aliasName) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductIndexer::class.java)
        private val settings = AlternativeProductIndexer::class.java
            .getResource("/opensearch/alternative_products_settings.json")!!.readText()
        private val mapping = AlternativeProductIndexer::class.java
            .getResource("/opensearch/alternative_products_mapping.json")!!.readText()
        val excludeIsos = setOf(
            // Sitteputer
            "18100601",
        )
    }



    fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexType.alternative_products)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, settings, mapping)
        }
        reIndexAllByIsoCategory()
        if (alias) {
            updateAlias(indexName)
        }
    }

    fun reIndexByIsoCategory(isoCategory: String): Int {
        val page = gdbApiClient.findProducts(isoCategory = isoCategory,
            size=5000, page = 0, sort="updated,asc", accessory = false, sparePart = false)
        if (page.numberOfElements>0) {
            val products = page.content.filter {
                it.status != ProductStatus.DELETED && it.hmsArtNr != null
            }.map { it.toDoc(isoCategoryService, techLabelService, alternativProdukterClient)}
            LOG.info("indexing ${products.size} products to $aliasName")
            //index(products)
            return products.size
        }
        return 0
    }

    fun reIndexAllByIsoCategory() {
        val isos = gdbApiClient.findDistinctIsoCategoryThatHasHmsnr()
        LOG.info("Got ${isos.size} isoCategories")
        var total = 0
        isos.forEach {
            if (!excludeIsos.contains(it)) {
                LOG.info("Reindexing isoCategory: $it")
                val count = reIndexByIsoCategory(it)
                total += count
            }
        }
        LOG.info("Total products indexed: $total")
    }

}

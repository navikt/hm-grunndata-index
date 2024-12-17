package no.nav.hm.grunndata.index.alternative_product

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.createIndexName
import no.nav.hm.grunndata.index.product.GdbApiClient
import no.nav.hm.grunndata.index.product.IsoCategoryService
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
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
        val isos = setOf(
            // løfteplattform
            "12360401",
            "12360301",
            "12361202",
            "12361501",
            // stoler med oppreisningsfunksjon
            "18091501",
            "18091502",
            // Kalendere, dagsplanleggere og tidtakere
            "22271501",
            "22271201",
            // varmehjelpemidler
            "09061501",
            "09062101",
            // Arbeidsstoler
            "18090302",
            "18090301",
            "18090701",
            "18092101",
            "18030602",
            "18030601",
            // Synstekniske hjelpemidler
            "22031803",
            "22031801",
            "22031802",
            "22031804",
            "22390502",
            "22390601",
            "22180301",
            "22302101",
            "22391201",
            "22030901",
            // Ganghjelpemidler
            "12060602",
            "12060603",
            "12061202",
            "12061201",
            "12181202",
            "12060604",
            "12060603",
            "12060901",
            "04481503",
            "04481501",
            "12030601",
            // Manuelle rullestoler
            "12220301",
            "12220302",
            "12220303",
            "12220304",
            "12240902",
            "12240901",
            // Elektriske rullestoler
            "12230301",
            "12230303",
            "12230601",
            "12230602",
            "12230603",
            // Vogner
            "12271002",
            "30093604",
            "30093602",
            "30093603",
            "12270701",
            // Kjøreramper
            "18301505",
            "18301502",
            "18301801",
            "18301802",
            "18301804",
            "18301806",
            // Ståstativ
            "04481504",
            "05360301"
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

    fun reIndexAllByIsoCategory() {
        isos.forEach {
            LOG.info("Reindexing isoCategory: $it")
            reIndexByIsoCategory(it)
        }
    }

}

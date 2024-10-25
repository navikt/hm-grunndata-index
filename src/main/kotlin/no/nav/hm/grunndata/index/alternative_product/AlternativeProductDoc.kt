package no.nav.hm.grunndata.index.alternative_product

import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.grunndata.index.SearchDoc
import no.nav.hm.grunndata.index.product.IsoCategoryService
import java.time.LocalDateTime
import no.nav.hm.grunndata.index.product.AgreementInfoDoc
import no.nav.hm.grunndata.index.product.AttributesDoc
import no.nav.hm.grunndata.index.product.MediaDoc
import no.nav.hm.grunndata.index.product.ProductSupplier
import no.nav.hm.grunndata.index.product.toDoc

data class AlternativeProductDoc(
    override val id: String,
    val supplier: ProductSupplier,
    val title: String,
    val articleName: String,
    val attributes: AttributesDoc,
    val status: ProductStatus,
    val hmsArtNr: String? = null,
    val supplierRef: String,
    val isoCategory: String,
    val isoCategoryTitle: String? = null,
    val isoCategoryTitleShort: String? = null,
    val isoCategoryText: String? = null,
    val isoCategoryTextShort: String? = null,
    val seriesId: String? = null,
    val data: List<TechData> = emptyList(),
    val media: List<MediaDoc> = emptyList(),
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val expired: LocalDateTime,
    val agreements: List<AgreementInfoDoc> = emptyList(),
    val hasAgreement: Boolean = false,
    val alternativeFor: Set<String> = emptySet(),
    val wareHouseStock: List<WareHouseStockDoc> = emptyList(),
    val filters: Map<String, Any> = emptyMap()
) : SearchDoc

data class WareHouseStock(
    val erPåLager: Boolean,
    val organisasjons_id: Long,
    val organisasjons_navn: String,
    val artikkelnummer: String,
    val artikkelid: Long,
    val fysisk: Int,
    val tilgjengeligatt: Int,
    val tilgjengeligroo: Int,
    val tilgjengelig: Int,
    val behovsmeldt: Int,
    val reservert: Int,
    val restordre: Int,
    val bestillinger: Int,
    val anmodning: Int,
    val intanmodning: Int,
    val forsyning: Int,
    val sortiment: Boolean,
    val lagervare: Boolean,
    val minmax: Boolean
)

data class WareHouseStockDoc(
    val location: String,
    val available: Int,
    val reserved: Int,
    val needNotified: Int,
    val minmax: Boolean,
    val updated: LocalDateTime = LocalDateTime.now()
)

fun WareHouseStock.toDoc(): WareHouseStockDoc = WareHouseStockDoc(
    location = organisasjons_navn.substring(4),
    available = tilgjengelig,
    reserved = reservert,
    needNotified = behovsmeldt,
    minmax = minmax
)

fun ProductRapidDTO.toDoc(
    isoCategoryService: IsoCategoryService,
    techLabelService: TechLabelService,
    alternativProdukterClient: AlternativProdukterClient
): AlternativeProductDoc = try {
    val (onlyActiveAgreements, previousAgreements) =
        agreements.partition {
            it.published!!.isBefore(LocalDateTime.now())
                    && it.expired.isAfter(LocalDateTime.now()) && it.status == ProductAgreementStatus.ACTIVE
        }
    val alternativeProdukterResponse = alternativProdukterClient.fetchAlternativProdukter(hmsArtNr!!)
    val iso = isoCategoryService.lookUpCode(isoCategory)
    AlternativeProductDoc(id = id.toString(),
        supplier = ProductSupplier(
            id = supplier.id.toString(), identifier = supplier.identifier, name = supplier.name
        ),
        title = title,
        articleName = articleName,
        attributes = attributes.toDoc(),
        status = status,
        hmsArtNr = hmsArtNr,
        supplierRef = supplierRef,
        isoCategory = isoCategory,
        isoCategoryTitle = iso?.isoTitle,
        isoCategoryTitleShort = iso?.isoTitleShort,
        isoCategoryText = iso?.isoText,
        isoCategoryTextShort = iso?.isoTextShort,
        seriesId = seriesUUID.toString(),
        data = techData,
        media = media.map { it.toDoc() }.sortedBy { it.priority },
        created = created,
        updated = updated,
        expired = expired,
        agreements = onlyActiveAgreements.map { it.toDoc() },
        hasAgreement = onlyActiveAgreements.isNotEmpty(),
        alternativeFor = alternativeProdukterResponse.alternatives.map { it }.toSet(),
        wareHouseStock = alternativeProdukterResponse.original.warehouseStock.map { it.toDoc()},
        filters = techData.mapNotNull { data ->
            techLabelService.fetchLabelByIsoCodeLabel(isoCategory, data.key)?.let {
                it.systemLabel to data.value
            }
        }.toMap()
    )
} catch (e: Exception) {
    println(isoCategory)
    throw e
}


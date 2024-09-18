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
    val identifier: String,
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
    val filters: Map<String, Any> = emptyMap()
) : SearchDoc


fun ProductRapidDTO.toDoc(isoCategoryService: IsoCategoryService, techLabelService: TechLabelService): AlternativeProductDoc = try {
    val onlyActiveAgreements =
        agreements.filter { it.published!!.isBefore(LocalDateTime.now())
                && it.expired.isAfter(LocalDateTime.now()) && it.status == ProductAgreementStatus.ACTIVE}
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
        identifier = identifier,
        supplierRef = supplierRef,
        isoCategory = isoCategory,
        isoCategoryTitle = iso?.isoTitle,
        isoCategoryTitleShort = iso?.isoTitleShort,
        isoCategoryText = iso?.isoText,
        isoCategoryTextShort = iso?.isoTextShort,
        seriesId = seriesIdentifier ?: seriesUUID.toString(),
        data = techData,
        media = media.map { it.toDoc() }.sortedBy { it.priority },
        created = created,
        updated = updated,
        expired = expired,
        agreements = onlyActiveAgreements.map { it.toDoc() },
        hasAgreement = onlyActiveAgreements.isNotEmpty(),
        filters = techData.mapNotNull { data -> techLabelService.fetchLabelByIsoCodeLabel(isoCategory, data.key)?.let {
            it.systemLabel to data.value
        }}.toMap()
    )
} catch (e: Exception) {
    println(isoCategory)
    throw e
}


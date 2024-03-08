package no.nav.hm.grunndata.index.product

import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.grunndata.index.SearchDoc
import no.nav.hm.grunndata.index.agreement.AgreementLabels
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

data class ProductDoc(
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
    val isoCategoryTitle: String?,
    val isoCategoryTitleShort: String?,
    val isoCategoryText: String?,
    val isoCategoryTextShort: String?,
    val isoSearchTag: List<String>?,
    val accessory: Boolean = false,
    val sparePart: Boolean = false,
    val seriesId: String? = null,
    val data: List<TechData> = emptyList(),
    val media: List<MediaDoc> = emptyList(),
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val expired: LocalDateTime,
    val createdBy: String,
    val updatedBy: String,
    val filters: TechDataFilters,
    val agreements: List<AgreementInfoDoc> = emptyList(),
    val hasAgreement: Boolean = false,
) : SearchDoc


data class AgreementInfoDoc(
    val id: UUID,
    val identifier: String? = null,
    val title: String? = null,
    val label: String,
    val rank: Int,
    val postNr: Int,
    val postIdentifier: String? = null,
    val postTitle: String? = null,
    val postId: UUID? = null,
    val refNr: String? = null,
    val reference: String,
    val expired: LocalDateTime,
)

data class AttributesDoc(
    val manufacturer: String? = null,
    val compatibleWith: CompatibleWith? = null,
    val keywords: List<String>? = null,
    val series: String? = null,
    val shortdescription: String? = null,
    val text: String? = null,
    val url: String? = null,
    val bestillingsordning: Boolean? = null,
    val digitalSoknad: Boolean? = null,
    val sortimentKategori: String? = null,
    val pakrevdGodkjenningskurs: PakrevdGodkjenningskurs? = null,
    val produkttype: Produkttype? = null,
    val tenderId: String? = null,
    val hasTender: Boolean? = null
)

data class MediaDoc(
    val uri: String,
    val priority: Int = 1,
    val type: MediaType = MediaType.IMAGE,
    val text: String? = null,
    val source: MediaSourceType = MediaSourceType.HMDB
)

data class TechDataFilters(
    val fyllmateriale: String?=null,
    val setebreddeMaksCM: Int??=null,
    val setebreddeMinCM: Int??=null,
    val brukervektMinKG: Int??=null,
    val materialeTrekk: String??=null,
    val setedybdeMinCM: Int??=null,
    val setedybdeMaksCM: Int??=null,
    val setehoydeMaksCM: Int??=null,
    val setehoydeMinCM: Int??=null,
    val totalVektKG: Int??=null,
    val lengdeCM: Int??=null,
    val breddeCM: Int??=null,
    val beregnetBarn: String??=null,
    val brukervektMaksKG: Int??=null
)

data class ProductSupplier(val id: String, val identifier: String, val name: String)

fun ProductRapidDTO.toDoc(isoCategoryService: IsoCategoryService): ProductDoc = try {
    val onlyActiveAgreements =
        agreements.filter { it.published!!.isBefore(LocalDateTime.now()) && it.expired.isAfter(LocalDateTime.now()) }
    val iso = isoCategoryService.lookUpCode(isoCategory)
    ProductDoc(id = id.toString(),
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
        isoSearchTag = isoCategoryService.getHigherLevelsInBranch(isoCategory).map { it.searchWords }.flatten(),
        accessory = accessory,
        sparePart = sparePart,
        seriesId = seriesIdentifier ?: seriesId, // backovercompatible with hmdbIdentifier
        data = techData,
        media = media.map { it.toDoc() }.sortedBy { it.priority },
        created = created,
        updated = updated,
        expired = expired,
        createdBy = createdBy,
        updatedBy = updatedBy,
        agreements = onlyActiveAgreements.map { it.toDoc() },
        hasAgreement = onlyActiveAgreements.isNotEmpty(),
        filters = mapTechDataFilters(techData))


} catch (e: Exception) {
    println(isoCategory)
    throw e
}

private fun AgreementInfo.toDoc(): AgreementInfoDoc = AgreementInfoDoc(
    id = id,
    identifier = identifier,
    title = title,
    label = AgreementLabels.matchTitleToLabel(title ?: "Annet"),
    rank = rank,
    postNr = postNr,
    postIdentifier = postIdentifier,
    postTitle = postTitle,
    postId = postId,
    refNr = refNr,
    reference = reference,
    expired = expired
)

private fun Attributes.toDoc(): AttributesDoc {
    return AttributesDoc(
        manufacturer = manufacturer,
        keywords = keywords,
        series = series,
        shortdescription = shortdescription,
        text = text,
        url = url,
        bestillingsordning = bestillingsordning,
        digitalSoknad = digitalSoknad,
        sortimentKategori = sortimentKategori,
        pakrevdGodkjenningskurs = pakrevdGodkjenningskurs,
        produkttype = produkttype,
        tenderId = tenderId,
        hasTender = hasTender,
        compatibleWith = compatibleWidth
    )
}


fun MediaInfo.toDoc(): MediaDoc = MediaDoc(
    uri = uri, priority = priority, type = type, text = text, source = source
)

fun mapTechDataFilters(data: List<TechData>): TechDataFilters {
    try {

        var fyllmateriale: String? = null
        var setebreddeMaksCM: Int? = null
        var setebreddeMinCM: Int? = null
        var brukervektMinKG: Int? = null
        var materialeTrekk: String? = null
        var setedybdeMinCM: Int? = null
        var setedybdeMaksCM: Int? = null
        var setehoydeMaksCM: Int? = null
        var setehoydeMinCM: Int? = null
        var totalVektKG: Int? = null
        var lengdeCM: Int? = null
        var breddeCM: Int? = null
        var beregnetBarn: String? = null
        var brukervektMaksKG: Int? = null
        data.forEach {
            when (it.key) {
                "Fyllmateriale" -> fyllmateriale = it.value
                "Setebredde maks" -> setebreddeMaksCM = it.value.decimalToInt()
                "Setebredde min" -> setebreddeMinCM = it.value.decimalToInt()
                "Brukervekt min" -> brukervektMinKG = it.value.decimalToInt()
                "Materiale i trekk" -> materialeTrekk = it.value
                "Setedybde min" -> setedybdeMinCM = it.value.decimalToInt()
                "Setedybde maks" -> setedybdeMaksCM = it.value.decimalToInt()
                "Setehøyde maks" -> setehoydeMaksCM = it.value.decimalToInt()
                "Setehøyde min" -> setehoydeMinCM = it.value.decimalToInt()
                "Totalvekt" -> totalVektKG = it.value.decimalToInt()
                "Lengde" -> lengdeCM = it.value.decimalToInt()
                "Bredde" -> breddeCM = it.value.decimalToInt()
                "Beregnet på barn" -> beregnetBarn = it.value
                "Brukervekt maks" -> brukervektMaksKG = it.value.decimalToInt()
            }
        }
        return TechDataFilters(
            fyllmateriale = fyllmateriale,
            setebreddeMaksCM = setebreddeMaksCM,
            setebreddeMinCM = setebreddeMinCM,
            brukervektMinKG = brukervektMinKG,
            materialeTrekk = materialeTrekk,
            setedybdeMinCM = setedybdeMinCM,
            setedybdeMaksCM = setedybdeMaksCM,
            setehoydeMaksCM = setehoydeMaksCM,
            setehoydeMinCM = setehoydeMinCM,
            totalVektKG = totalVektKG,
            lengdeCM = lengdeCM,
            breddeCM = breddeCM,
            beregnetBarn = beregnetBarn,
            brukervektMaksKG = brukervektMaksKG
        )
    }
    catch (e: Exception) {
        println("Error mapping techdatafilters ${e.message}")
        return TechDataFilters()
    }
}


private fun String.decimalToInt(): Int? = substringBeforeLast(".").toInt()

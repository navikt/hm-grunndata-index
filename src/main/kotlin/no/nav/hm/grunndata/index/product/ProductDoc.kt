package no.nav.hm.grunndata.index.product

import no.nav.hm.grunndata.rapid.dto.*
import no.nav.hm.grunndata.index.SearchDoc
import no.nav.hm.grunndata.index.agreement.AgreementLabels
import java.time.LocalDateTime
import java.util.*

data class ProductDoc (
    override val id: String,
    val supplier: ProductSupplier,
    val title: String,
    val articleName:String,
    val attributes: AttributesDoc,
    val status: ProductStatus,
    val hmsArtNr: String?=null,
    val identifier: String,
    val supplierRef: String,
    val isoCategory: String,
    val isoCategoryTitle: String?,
    val isoCategoryText: String?,
    val accessory: Boolean = false,
    val sparePart: Boolean = false,
    val seriesId: String?=null,
    val data: List<TechData> = emptyList(),
    val media: List<MediaDoc> = emptyList(),
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val expired: LocalDateTime,
    val createdBy: String,
    val updatedBy: String,
    val filters: TechDataFilters,
    val agreementInfo: AgreementInfoDoc?,
    val agreements: List<AgreementInfoDoc> = emptyList(),
    val hasAgreement: Boolean = false,
): SearchDoc


data class AgreementInfoDoc (
    val id: UUID,
    val identifier: String?=null,
    val title: String?=null,
    val label: String,
    val rank: Int,
    val postNr: Int,
    val postIdentifier: String?=null,
    val postTitle: String?=null,
    val reference: String,
    val expired: LocalDateTime,
)

data class AttributesDoc (
    val manufacturer: String? = null,
    val compatibleWith: CompatibleWith? = null,
    val keywords: List<String>? = null,
    val series: String? = null,
    val shortdescription: String? = null,
    val text: String? = null,
    val url: String? = null,
    val bestillingsordning: Boolean? = null,
    val tenderId: String? = null,
    val hasTender: Boolean? = null
)

data class MediaDoc (
    val uri:    String,
    val priority: Int = 1,
    val type: MediaType = MediaType.IMAGE,
    val text:   String?=null,
    val source: MediaSourceType = MediaSourceType.HMDB
)

data class TechDataFilters(val fyllmateriale:String?, val setebreddeMaksCM: Int?, val setebreddeMinCM: Int?,
                           val brukervektMinKG: Int?, val materialeTrekk:String?, val setedybdeMinCM:Int?,
                           val setedybdeMaksCM: Int?, val setehoydeMaksCM:Int?, val setehoydeMinCM: Int?,
                           val totalVektKG: Int?, val lengdeCM: Int?, val breddeCM: Int?, val beregnetBarn: String?,
                           val brukervektMaksKG: Int?)

data class ProductSupplier(val id: String, val identifier: String, val name: String)

fun ProductRapidDTO.toDoc(isoCategoryService: IsoCategoryService) : ProductDoc = try { ProductDoc (
    id = id.toString(),
    supplier = ProductSupplier(id = supplier.id.toString(), identifier = supplier.identifier,
        name = supplier.name),
    title = title,
    articleName = articleName,
    attributes = attributes.toDoc(),
    status = status,
    hmsArtNr = hmsArtNr,
    identifier = identifier,
    supplierRef = supplierRef,
    isoCategory = isoCategory,
    isoCategoryTitle = isoCategoryService.lookUpCode(isoCategory)?.isoTitle,
    isoCategoryText = isoCategoryService.lookUpCode(isoCategory)?.isoText,
    accessory = accessory,
    sparePart = sparePart,
    seriesId = seriesIdentifier,
    data = techData,
    media = media.map { it.toDoc() }.sortedBy { it.priority },
    created = created,
    updated = updated,
    expired = expired,
    createdBy = createdBy,
    updatedBy = updatedBy,
    agreementInfo = agreementInfo?.toDoc(),
    agreements = agreements.map { it.toDoc()},
    hasAgreement = hasAgreement,
    filters = mapTechDataFilters(techData)) }
    catch (e: Exception) {
        println(isoCategory)
        throw e
    }

private fun AgreementInfo.toDoc(): AgreementInfoDoc = AgreementInfoDoc(
    id = id, identifier = identifier, title= title, label = AgreementLabels.matchTitleToLabel(title?:"Annet"),
    rank= rank, postNr = postNr, postIdentifier = postIdentifier, postTitle = postTitle, reference = reference,
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
        tenderId = tenderId,
        hasTender = hasTender,
        compatibleWith = compatibleWidth
    )
}


fun MediaInfo.toDoc() : MediaDoc = MediaDoc (
    uri = uri,
    priority = priority,
    type = type,
    text = text,
    source = source
)

fun mapTechDataFilters(data: List<TechData>): TechDataFilters {
    var fyllmateriale:String? = null
    var setebreddeMaksCM: Int? = null
    var setebreddeMinCM: Int? = null
    var brukervektMinKG: Int? = null
    var materialeTrekk:String? = null
    var setedybdeMinCM:Int? = null
    var setedybdeMaksCM: Int? = null
    var setehoydeMaksCM:Int? = null
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
    return TechDataFilters(fyllmateriale = fyllmateriale, setebreddeMaksCM = setebreddeMaksCM,
        setebreddeMinCM = setebreddeMinCM, brukervektMinKG = brukervektMinKG, materialeTrekk = materialeTrekk,
        setedybdeMinCM = setedybdeMinCM, setedybdeMaksCM = setedybdeMaksCM, setehoydeMaksCM = setehoydeMaksCM,
        setehoydeMinCM = setehoydeMinCM, totalVektKG = totalVektKG, lengdeCM = lengdeCM, breddeCM = breddeCM,
        beregnetBarn = beregnetBarn, brukervektMaksKG = brukervektMaksKG)
}



private fun String.decimalToInt(): Int? = substringBeforeLast(".").toInt()

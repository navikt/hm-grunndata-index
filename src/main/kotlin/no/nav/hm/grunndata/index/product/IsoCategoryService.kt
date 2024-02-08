package no.nav.hm.grunndata.index.product

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@Singleton
class IsoCategoryService(gdbApiClient: GdbApiClient) {

    private val isoCategories: Map<String, IsoCategoryDTO> =
        gdbApiClient.retrieveIsoCategories().associateBy { it.isoCode }

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategoryService::class.java)
    }

    init {
        LOG.info("Got isoCategories: ${isoCategories.size}")
    }

    fun lookUpCode(isoCode: String): IsoCategoryDTO? = isoCategories[isoCode]

    fun retrieveAllCategories(): List<IsoCategoryDTO> = isoCategories.values.toList()

    fun getHigherLevelsInBranch(isoCode: String): List<IsoCategoryDTO> {
        val cat = isoCategories[isoCode]
        if (cat==null) LOG.error("IsoCode: $isoCode not found!")
        return isoCategories.values.filter { isoCode.startsWith(it.isoCode) }
    }

}

data class IsoCategoryDTO(
    val id: UUID,
    val isoCode: String,
    val isoTitle: String,
    val isoTitleShort: String?=null,
    val isoText: String,
    val isoTextShort: String?=null,
    val isoTranslations: IsoTranslationsDTO?=null,
    val isoLevel: Int,
    val isActive: Boolean = true,
    val showTech: Boolean = true,
    val allowMulti: Boolean = true,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val searchWords: List<String> = emptyList()
)

data class IsoTranslationsDTO(
    val titleEn: String?=null,
    val textEn: String?=null,
)
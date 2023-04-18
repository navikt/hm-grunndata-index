package no.nav.hm.grunndata.index.product

import jakarta.inject.Singleton
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import org.slf4j.LoggerFactory

@Singleton
class IsoCategoryService(private val gdbApiClient: GdbApiClient) {

    private val isoCategories: Map<String, IsoCategoryDTO> =
        gdbApiClient.retrieveIsoCategories().associateBy { it.isoCode }

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategoryService::class.java)
    }

    init {
        LOG.info("Iso categories initialized with size: ${isoCategories.size}")
    }

    fun lookUpCode(isoCode: String): IsoCategoryDTO? = isoCategories[isoCode]

    fun retrieveAllCategories(): List<IsoCategoryDTO> = isoCategories.values.toList()

}

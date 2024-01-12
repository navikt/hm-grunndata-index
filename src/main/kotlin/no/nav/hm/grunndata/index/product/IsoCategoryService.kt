package no.nav.hm.grunndata.index.product

import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import org.slf4j.LoggerFactory

@Singleton
class IsoCategoryService(gdbApiClient: GdbApiClient) {

    private val isoCategories: Map<String, IsoCategoryDTO> =
        runBlocking {
            // Dispatchers.IO fixes: You are trying to run a BlockingHttpClient operation on a netty event loop thread.
            // This is a common cause for bugs: Event loops should never be blocked. You can either mark your
            // controller as @ExecuteOn(TaskExecutors.BLOCKING), or use the reactive HTTP client to resolve
            // this bug. There is also a configuration option to disable this check if you are certain a
            // blocking operation is fine here.
            withContext(Dispatchers.IO) {
                gdbApiClient.retrieveIsoCategories().associateBy { it.isoCode }
            }
        }

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategoryService::class.java)
    }

    init {
        LOG.info("Got isoCategories: ${isoCategories.size}")
    }

    fun lookUpCode(isoCode: String): IsoCategoryDTO? = isoCategories[isoCode]

    fun retrieveAllCategories(): List<IsoCategoryDTO> = isoCategories.values.toList()

}

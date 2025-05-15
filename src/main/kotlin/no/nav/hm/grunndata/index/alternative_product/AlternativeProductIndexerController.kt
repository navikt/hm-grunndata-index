package no.nav.hm.grunndata.index.alternative_product

import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.slf4j.LoggerFactory

@Controller("/internal/index/alternative_products")
@ExecuteOn(TaskExecutors.BLOCKING)
class AlternativeProductIndexerController(private val alternativeProductIndexer: AlternativeProductIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductIndexerController::class.java)
    }

    @Post("/")
    fun indexAlternativeProducts(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        alternativeProductIndexer.reIndex(alias)
    }

    @Post("/hmsNr/{hmsNr}")
    fun indexAlternativeProductsByHmsNr(hmsNr: String) {
        LOG.info("reIndex alternative products by hmsNr: $hmsNr")
        alternativeProductIndexer.reIndexByHmsNr(hmsNr)
    }

    @Post("/isoCategory/{isoCategory}")
    fun indexAlternativeProductsByCategory(isoCategory:String) {
        alternativeProductIndexer.reIndexByIsoCategory(isoCategory)
    }

    @Put("/alias/{indexName}")
    fun aliasAlternativeProducts(indexName: String) {
        alternativeProductIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = alternativeProductIndexer.getAlias().toJsonString()

    @Get("/count")
    fun count() = alternativeProductIndexer.docCount()

}

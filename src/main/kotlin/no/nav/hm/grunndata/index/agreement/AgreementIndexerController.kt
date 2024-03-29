package no.nav.hm.grunndata.index.agreement

import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.slf4j.LoggerFactory

@Controller("/internal/index/agreements")
@ExecuteOn(TaskExecutors.BLOCKING)
class AgreementIndexerController(private val agreementIndexer: AgreementIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexerController::class.java)
    }

    @Post("/")
    fun indexAgreements(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        agreementIndexer.reIndex(alias)
    }

    @Put("/alias/{indexName}")
    fun aliasAgreements(indexName: String) {
        agreementIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = agreementIndexer.getAlias()
}

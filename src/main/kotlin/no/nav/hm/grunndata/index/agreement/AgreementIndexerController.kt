package no.nav.hm.grunndata.index.agreement

import io.micronaut.http.annotation.*
import no.nav.hm.grunndata.index.product.ProductIndexerController
import no.nav.hm.grunndata.index.product.toDoc
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Controller("/internal/index/agreements")
class AgreementIndexerController(private val agreementGdbApiClient: AgreementGdbApiClient,
                                 private val agreementIndexer: AgreementIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexerController::class.java)
    }

    @Post("/{indexName}")
    fun indexAgreements(@PathVariable indexName: String, @QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        if (!agreementIndexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            agreementIndexer.createIndex(indexName)
        }
        val dateString =  LocalDateTime.now().minusYears(30).toString()
        var page = agreementGdbApiClient.findAgreements(params = mapOf("updated" to dateString),
            size=1000, page = 0, sort="updated,asc")
        while(page.pageNumber<page.totalPages) {
            if (page.numberOfElements>0) {
                val agreements = page.content.map { it.toDoc() }
               LOG.info("indexing ${agreements.size} agreements to $indexName")
                agreementIndexer.index(agreements, indexName)
            }
            page = agreementGdbApiClient.findAgreements(params = mapOf("updated" to dateString),
                size=1000, page = page.pageNumber+1, sort="updated,asc")
        }
        if (alias) {
            agreementIndexer.updateAlias(indexName)
        }
    }

    @Put("/alias/{indexName}")
    fun aliasAgreements(indexName: String) {
        agreementIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = agreementIndexer.getAlias()
}

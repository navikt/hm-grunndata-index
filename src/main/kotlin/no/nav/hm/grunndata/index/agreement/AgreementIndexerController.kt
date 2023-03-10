package no.nav.hm.grunndata.index.agreement

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import org.slf4j.LoggerFactory

@Controller("/internal/index/agreements")
class AgreementIndexerController(private val agreementGdbApiClient: AgreementGdbApiClient,
                                 private val agreementIndexer: AgreementIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexerController::class.java)
    }

    @Post("/{indexName}")
    fun indexAgreements(indexName: String) {
        val agreements = agreementGdbApiClient.findAgreements(size=1000, number=0).content.map { it.toDoc() }
        LOG.info("indexing ${agreements.size} agreements to $indexName")
        agreementIndexer.index(agreements, indexName)
    }

    @Put("/alias/{indexName}")
    fun aliasAgreements(indexName: String) {
        agreementIndexer.updateAlias(indexName)
    }

}
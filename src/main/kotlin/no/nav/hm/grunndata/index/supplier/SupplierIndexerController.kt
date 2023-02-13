package no.nav.hm.grunndata.index.supplier

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import org.slf4j.LoggerFactory

@Controller("/internal/index/agreements")
class SupplierIndexerController(private val supplierGdbApiClient: SupplierGdbApiClient,
                                private val supplierIndexer: SupplierIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexerController::class.java)
    }

    @Post("/{?indexName}")
    fun indexAgreements(@QueryValue indexName: String) {
        val suppliers = supplierGdbApiClient.findSuppliers().content.map { it.toDoc() }
        LOG.info("indexing ${suppliers.size} agreements to $indexName")
        supplierIndexer.index(suppliers, indexName)
    }


}
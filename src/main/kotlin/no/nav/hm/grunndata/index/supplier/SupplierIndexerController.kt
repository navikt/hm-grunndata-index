package no.nav.hm.grunndata.index.supplier

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import org.slf4j.LoggerFactory

@Controller("/internal/index/suppliers")
class SupplierIndexerController(private val supplierGdbApiClient: SupplierGdbApiClient,
                                private val supplierIndexer: SupplierIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexerController::class.java)
    }

    @Post("/{?indexName}")
    fun indexSuppliers(@QueryValue indexName: String) {
        val page = supplierGdbApiClient.findSuppliers(size=1000, number = 0, sort="updated,asc")
        val suppliers = page.content.map { it.toDoc() }
        LOG.info("indexing ${suppliers.size} suppliers to $indexName")
        supplierIndexer.index(suppliers, indexName)
    }


}
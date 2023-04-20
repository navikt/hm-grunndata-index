package no.nav.hm.grunndata.index.supplier

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import org.slf4j.LoggerFactory

@Controller("/internal/index/suppliers")
class SupplierIndexerController(private val supplierGdbApiClient: SupplierGdbApiClient,
                                private val supplierIndexer: SupplierIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexerController::class.java)
    }

    @Post("/{indexName}")
    fun indexSuppliers(indexName: String) {
        val page = supplierGdbApiClient.findSuppliers(size=1000, page = 0, sort="updated,asc")
        val suppliers = page.content.map { it.toDoc() }
        LOG.info("indexing ${suppliers.size} suppliers to $indexName")
        supplierIndexer.index(suppliers, indexName)
    }

    @Put("/alias/{indexName}")
    fun aliasSuppliers(indexName: String) {
        supplierIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = supplierIndexer.getAlias()
}

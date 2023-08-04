package no.nav.hm.grunndata.index.supplier

import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory

@Controller("/internal/index/suppliers")
class SupplierIndexerController(private val supplierIndexer: SupplierIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexerController::class.java)
    }

    @Post("/")
    fun indexSuppliers(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        supplierIndexer.reIndex(alias)
    }

    @Put("/alias/{indexName}")
    fun aliasSuppliers(indexName: String) {
        supplierIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = supplierIndexer.getAlias()
}

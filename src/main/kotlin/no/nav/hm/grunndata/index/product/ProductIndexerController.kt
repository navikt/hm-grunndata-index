package no.nav.hm.grunndata.index.product

import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory

@Controller("/internal/index/products")
// @ExecuteOn(TaskExecutors.BLOCKING)
class ProductIndexerController(private val productIndexer: ProductIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexerController::class.java)
    }

    @Post("/")
    fun indexProducts(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        productIndexer.reIndex(alias)
    }

    @Put("/alias/{indexName}")
    fun aliasProducts(indexName: String) {
        productIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = productIndexer.getAlias()


}

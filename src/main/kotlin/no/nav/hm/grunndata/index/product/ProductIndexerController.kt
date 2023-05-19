package no.nav.hm.grunndata.index.product

import io.micronaut.http.annotation.*
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Controller("/internal/index/products")
class ProductIndexerController(private val gdbApiClient: GdbApiClient,
                               private val productIndexer: ProductIndexer,
                               private val isoCategoryService: IsoCategoryService) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexerController::class.java)
    }

    @Post("/{indexName}")
    fun indexProducts(@PathVariable indexName: String,
                      @QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        if (!productIndexer.indexExists(indexName)) {
            LOG.info("creating index $indexName")
            productIndexer.createIndex(indexName)
        }
        val dateString =  LocalDateTime.now().minusYears(30).toString()
        var page = gdbApiClient.findProducts(params = mapOf("updated" to dateString),
            size=1000, page = 0, sort="updated,asc")
        while(page.pageNumber<page.totalPages) {
            if (page.numberOfElements>0) {

                val products = page.content
                    .filter { it.status != ProductStatus.DELETED }
                    .map { it.toDoc(isoCategoryService) }
                LOG.info("indexing ${products.size} products to $indexName")
                productIndexer.index(products, indexName)
            }
            page = gdbApiClient.findProducts(params = mapOf("updated" to dateString),
                size=1000, page = page.pageNumber+1, sort="updated,asc")
        }
        if (alias) {
            productIndexer.updateAlias(indexName)
        }
    }

    @Put("/alias/{indexName}")
    fun aliasProducts(indexName: String) {
        productIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = productIndexer.getAlias()

}

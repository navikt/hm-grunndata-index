package no.nav.hm.grunndata.index.product

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@Controller("/internal/index/products")
class ProductIndexerController(private val productGdbApiClient: ProductGdbApiClient,
                               private val productIndexer: ProductIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexerController::class.java)
    }

    @Post("/{?indexName}")
    fun indexProducts(@QueryValue indexName: String) {
        val dateString =  LocalDateTime.now().minusYears(15).toString()
        var page = productGdbApiClient.findProducts(params = mapOf("updated" to dateString),
            size=1000, page = 0, sort="updated,asc")
        while(page.pageNumber<page.totalPages) {
            if (page.numberOfElements>0) {
                val products = page.content.map { it.toDoc() }
                LOG.info("indexing ${products.size} products to $indexName")
                productIndexer.index(products, indexName)
            }
            page = productGdbApiClient.findProducts(params = mapOf("updated" to dateString),
                size=1000, page = page.pageNumber+1, sort="updated,asc")
        }
    }

    @Put("/alias/{?indexName}")
    fun aliasProducts(@QueryValue indexName: String) {
        productIndexer.updateAlias(indexName)
    }

}
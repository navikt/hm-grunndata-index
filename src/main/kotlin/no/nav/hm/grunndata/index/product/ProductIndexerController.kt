package no.nav.hm.grunndata.index.product

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import org.slf4j.LoggerFactory

@Controller("/internal/index/products")
class ProductIndexerController(private val productGdbApiClient: ProductGdbApiClient,
                               private val productIndexer: ProductIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexerController::class.java)
    }

    @Post("/{?indexName}")
    fun indexProducts(@QueryValue indexName: String) {
        var page = productGdbApiClient.findProducts(params = mapOf("updated" to "2011-01-01T00:00:00"),
            size=1000, page = 0)
        while(page.pageNumber<page.totalPages) {
            println("page: ${page.pageNumber} Totalpage: ${page.totalPages} " +
                    "Totalsize: ${page.totalSize} number: ${page.numberOfElements}")
            if (page.numberOfElements>0) {
                val products = page.content.map { it.toDoc() }
                LOG.info("indexing ${products.size} products to $indexName")
                productIndexer.index(products, indexName)
            }
            page = productGdbApiClient.findProducts(params = mapOf("updated" to "2010-01-01T00:00:00"),
                size=1000, page = page.pageNumber+1)
        }
    }

}
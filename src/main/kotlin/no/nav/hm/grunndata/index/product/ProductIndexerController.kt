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
        val products = productGdbApiClient.findProducts().content.map { it.toDoc() }
        LOG.info("indexing ${products.size} products to $indexName")
        productIndexer.index(products, indexName)
    }


}
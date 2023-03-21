package no.nav.hm.grunndata.index.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class ProductGdbApiClientTest(private val productGdbApiClient: ProductGdbApiClient,
                              private val objectMapper: ObjectMapper,
                              private val isoCategory: IsoCategory) {


    //@Test ignore integration test
    fun findGdbProducts() {
        val dateString =  LocalDateTime.now().minusYears(15).toString()
        var page = productGdbApiClient.findProducts(params = mapOf("updated" to dateString),
            size=1000, page = 0, sort="updated,asc")
        while(page.pageNumber<page.totalPages) {
            if (page.numberOfElements>0) {
                val products = page.content.map { it.toDoc(isoCategory) }
                println(products.size)
            }
            page = productGdbApiClient.findProducts(params = mapOf("updated" to dateString),
                size=1000, page = page.pageNumber+1, sort="updated,asc")
        }
    }

}

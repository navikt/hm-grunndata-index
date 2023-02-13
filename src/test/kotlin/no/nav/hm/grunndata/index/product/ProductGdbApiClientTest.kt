package no.nav.hm.grunndata.index.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class ProductGdbApiClientTest(private val productGdbApiClient: ProductGdbApiClient,
                              private val objectMapper: ObjectMapper) {


    //@Test
    fun findGdbProducts() {
        val page = productGdbApiClient.findProducts(params = mapOf( "updated" to LocalDateTime.now().minusYears(15).toString()),
            size = 1000, page = 0, sort = "updated,asc")
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(page))
    }

}
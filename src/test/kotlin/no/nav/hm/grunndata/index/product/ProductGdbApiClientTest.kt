package no.nav.hm.grunndata.index.product

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.index.agreement.AgreementLabels
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class ProductGdbApiClientTest(private val gdbApiClient: GdbApiClient,
                              private val isoCategoryService: IsoCategoryService) {


    //@Test //ignore integration test
    fun findGdbProducts() {
        val dateString =  LocalDateTime.now().minusYears(15).toString()
        var page = gdbApiClient.findProducts(updated = dateString,
            size=1000, page = 0, sort="updated,asc")
        while(page.pageNumber<page.totalPages) {
            if (page.numberOfElements>0) {
                val products = page.content.map { it.toDoc(isoCategoryService) }
                println(products.size)
            }
            page = gdbApiClient.findProducts(updated=dateString,
                size=1000, page = page.pageNumber+1, sort="updated,asc")
        }
    }

}

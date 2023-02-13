package no.nav.hm.grunndata.index.product

import io.micronaut.data.model.Page
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import no.nav.hm.grunndata.dto.ProductDTO

@Client("\${grunndata.db.url:`http://localhost:8888`}/api/v1/products")
interface ProductGdbApiClient {

    @Get(uri="/", consumes = [APPLICATION_JSON])
    fun findProducts(params: Map<String, String>?=null, @QueryValue("size") size: Int? = null,
                     @QueryValue("page") page: Int?=null, @QueryValue("sort") sort: String? = null): Page<ProductDTO>

}
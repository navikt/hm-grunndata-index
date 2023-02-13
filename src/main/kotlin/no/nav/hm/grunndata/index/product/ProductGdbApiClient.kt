package no.nav.hm.grunndata.index.product

import io.micronaut.context.annotation.Value
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import no.nav.hm.grunndata.dto.ProductDTO
import java.util.HashMap

@Client("\${grunndata.db.url:`http://localhost:8888}")
interface ProductGdbApiClient {

    @Get(uri="/api/v1/products", consumes = [APPLICATION_JSON])
    fun findProducts(params: HashMap<String, String>?, @QueryValue("size") size: Int? = null,
                     @QueryValue("number") number: Int?=null, @QueryValue("sort") sort: String? = null): Page<ProductDTO>

}
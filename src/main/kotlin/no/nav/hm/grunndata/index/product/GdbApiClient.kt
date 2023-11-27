package no.nav.hm.grunndata.index.product

import io.micronaut.data.model.Page
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO

@Client("\${grunndata.db.url}")
interface GdbApiClient {

    @Get(uri="/api/v1/products", consumes = [APPLICATION_JSON])
    fun findProducts(@QueryValue("updated") updated: String? = null,
                     @QueryValue("size") size: Int? = null,
                     @QueryValue("page") page: Int?=null,
                     @QueryValue("sort") sort: String? = null): Page<ProductRapidDTO>

    @Get(uri="/api/v1/isocategories", consumes = [APPLICATION_JSON])
    fun retrieveIsoCategories(): List<IsoCategoryDTO>

}

package no.nav.hm.grunndata.index.supplier

import io.micronaut.data.model.Page
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import no.nav.hm.grunndata.dto.SupplierDTO

@Client("\${grunndata.db.url}/api/v1/suppliers")
interface SupplierGdbApiClient {

    @Get(uri="/", consumes = [MediaType.APPLICATION_JSON])
    fun findSuppliers(params: Map<String, String>?=null,
                      @QueryValue("size") size: Int? = null,
                      @QueryValue("page") number: Int?=null,
                      @QueryValue("sort") sort: String? = null): Page<SupplierDTO>

}


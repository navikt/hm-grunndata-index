package no.nav.hm.grunndata.index.agreement

import io.micronaut.data.model.Page
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import no.nav.hm.grunndata.dto.AgreementDTO
import no.nav.hm.grunndata.dto.SupplierDTO

@Client("\${grunndata.db.url:`http://localhost:8888`}/api/v1/agreements")
interface AgreementGdbApiClient {

    @Get(uri="/", consumes = [MediaType.APPLICATION_JSON])
    fun findAgreements(params: Map<String, String>?=null, @QueryValue("size") size: Int? = null,
                     @QueryValue("page") number: Int?=null, @QueryValue("sort") sort: String? = null): Page<AgreementDTO>

}


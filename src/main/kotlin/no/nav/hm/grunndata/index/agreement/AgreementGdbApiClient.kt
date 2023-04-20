package no.nav.hm.grunndata.index.agreement

import io.micronaut.data.model.Page
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import no.nav.hm.grunndata.rapid.dto.AgreementDTO

@Client("\${grunndata.db.url}")
interface AgreementGdbApiClient {

    @Get(uri="/api/v1/agreements", consumes = [MediaType.APPLICATION_JSON])
    fun findAgreements(params: Map<String, String>?=null,
                       @QueryValue("size") size: Int? = null,
                       @QueryValue("page") page: Int?=null,
                       @QueryValue("sort") sort: String? = null): Page<AgreementDTO>

}


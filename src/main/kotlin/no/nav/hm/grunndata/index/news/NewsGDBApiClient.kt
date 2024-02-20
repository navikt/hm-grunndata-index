package no.nav.hm.grunndata.index.news

import io.micronaut.data.model.Page
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import no.nav.hm.grunndata.rapid.dto.NewsDTO

interface NewsGDBApiClient {

    @Get(uri="/api/v1/news", consumes = [MediaType.APPLICATION_JSON])
    fun findNews(params: Map<String, String>?=null,
                      @QueryValue("size") size: Int? = null,
                      @QueryValue("page") page: Int?=null,
                      @QueryValue("sort") sort: String? = null): Page<NewsDTO>

}
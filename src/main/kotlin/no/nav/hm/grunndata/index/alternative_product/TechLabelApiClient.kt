package no.nav.hm.grunndata.index.alternative_product

import io.micronaut.data.model.Page
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.util.UUID
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO

@Client("\${grunndata.register.url}")
interface TechLabelApiClient {

    @Get(uri = "/api/v1/products", consumes = [APPLICATION_JSON])
    fun fetchAllTechLabel(): Map<String, List<TechLabelDTO>>

}

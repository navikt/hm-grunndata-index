package no.nav.hm.grunndata.index.alternative_product

import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client


@Client("\${grunndata.alternativprodukter.url}")
interface AlternativProdukterClient {

    @Get(uri = "/alternativ/{hmsNr}", consumes = [APPLICATION_JSON])
    fun fetchAlterntivProdukter(hmsNr: String): AlternativeProductsResponse

}

data class AlternativeProductsResponse(val original: ProductStock, val alternatives: List<ProductStock> = emptyList())

data class ProductStock(val hmsArtNr: String, val warehouseStock: List<WareHouseStock> = emptyList())



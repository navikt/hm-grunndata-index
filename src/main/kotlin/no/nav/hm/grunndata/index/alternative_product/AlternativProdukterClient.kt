package no.nav.hm.grunndata.index.alternative_product

import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client


@Client("\${grunndata.alternativprodukter.url}")
interface AlternativProdukterClient {

    @Get(uri = "/alternativ/stock-alternatives/{hmsNr}", consumes = [APPLICATION_JSON])
    fun fetchAlterntivProdukter(hmsNr: String): ProductStockAlternatives

}

data class ProductStock(val hmsArtNr: String, val warehouseStock: List<WareHouseStock> = emptyList())

data class ProductStockAlternatives(val original: ProductStock, val alternatives: List<String> = emptyList())


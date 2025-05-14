package no.nav.hm.grunndata.index.alternative_product

import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime
import java.util.UUID


@Client("\${grunndata.alternativprodukter.url}")
interface AlternativProdukterClient {

    @Get(uri = "/alternativ/stock-alternatives/{hmsNr}", consumes = [APPLICATION_JSON])
    fun fetchAlternativProdukter(hmsNr: String): ProductStockAlternatives

}

data class ProductStockDTO(
    val id: UUID,
    val hmsArtnr: String,
    val status: ProductStockStatus,
    val stockQuantity: List<StockQuantity>,
    val updated: LocalDateTime = LocalDateTime.now()
)

data class StockQuantity(
    val inStock: Boolean,
    val amountInStock: Int,
    val location: String,
    val available: Int,
    val reserved: Int,
    val needNotified: Int,
    val orders: Int,
    val request: Int,
    val minmax: Boolean,
)

enum class ProductStockStatus {
    ACTIVE, INACTIVE
}

data class ProductStockAlternatives(val original: ProductStockDTO, val alternatives: List<String> = emptyList())


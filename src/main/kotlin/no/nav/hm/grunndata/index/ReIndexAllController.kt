package no.nav.hm.grunndata.index

import io.micronaut.http.annotation.Controller

import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import no.nav.hm.grunndata.index.agreement.AgreementIndexer
import no.nav.hm.grunndata.index.product.ProductIndexer
import no.nav.hm.grunndata.index.supplier.SupplierIndexer

@Controller("/internal/index/all")
class ReIndexAllController(private val productIndexer: ProductIndexer,
                           private val supplierIndexer: SupplierIndexer,
                           private val agreementIndexer: AgreementIndexer) {

    @Post("/")
    fun indexProducts(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        supplierIndexer.reIndex(alias)
        agreementIndexer.reIndex(alias)
        productIndexer.reIndex(alias)
    }

}
package no.nav.hm.grunndata.index

import io.micronaut.http.annotation.Controller

import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.nav.hm.grunndata.index.agreement.AgreementIndexer
import no.nav.hm.grunndata.index.product.ProductIndexer
import no.nav.hm.grunndata.index.supplier.SupplierIndexer

@Controller("/internal/index/all")
class ReIndexAllController(private val productIndexer: ProductIndexer,
                           private val supplierIndexer: SupplierIndexer,
                           private val agreementIndexer: AgreementIndexer) {

    @Post("/")
    fun indexProducts(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        runBlocking {
            // Dispatchers.IO fixes: You are trying to run a BlockingHttpClient operation on a netty event loop thread.
            // This is a common cause for bugs: Event loops should never be blocked. You can either mark your
            // controller as @ExecuteOn(TaskExecutors.BLOCKING), or use the reactive HTTP client to resolve
            // this bug. There is also a configuration option to disable this check if you are certain a
            // blocking operation is fine here.
            withContext(Dispatchers.IO) {
                supplierIndexer.reIndex(alias)
                agreementIndexer.reIndex(alias)
                productIndexer.reIndex(alias)
            }
        }
    }

}
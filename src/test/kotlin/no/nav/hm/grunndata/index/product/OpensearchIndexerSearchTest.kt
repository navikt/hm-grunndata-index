package no.nav.hm.grunndata.index.product

import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.index.supplier.SupplierDoc
import no.nav.hm.grunndata.index.supplier.SupplierIndexer
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

@MicronautTest
class OpensearchIndexerSearchTest(private val osContainer: OSContainer,
                                  private val supplierIndexer: SupplierIndexer,
                                  ) {

    companion object {
        private val LOG = LoggerFactory.getLogger(OpensearchIndexerSearchTest::class.java)
    }

    @Test
    fun integrationTestIndexerAndSearch() {
        osContainer.shouldNotBeNull()
        supplierIndexer.shouldNotBeNull()
        val supplierId = UUID.randomUUID().toString()
        val doc = SupplierDoc(
            id = supplierId,
            identifier = supplierId,
            status = SupplierStatus.ACTIVE,
            name = "Supplier 1",
            address = "address",
            postNr = "postNr",
            postLocation = "postLocation",
            countryCode = "countryCode",
            email = "email@email.no",
            phone = "12345678",
            homepage = "homepage",
            createdBy = "ME",
            updatedBy = "ME",
            created = LocalDateTime.now(),
            updated = LocalDateTime.now()
        )
        val response = supplierIndexer.index(doc)
        LOG.info("OS running: ${osContainer.container.httpHostAddress}")
        response.shouldNotBeNull()
        LOG.info("Response: ${response.toJsonString()}")

    }


}
package no.nav.hm.grunndata.index.product

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.index.supplier.SupplierDoc
import no.nav.hm.grunndata.index.supplier.SupplierIndexer
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import no.nav.hm.grunndata.rapid.dto.TechData
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

@MicronautTest
class OpensearchIndexerSearchTest(private val osContainer: OSContainer,
                                  private val supplierIndexer: SupplierIndexer,
                                  private val productIndexer: ProductIndexer
                                  ) {

    companion object {
        private val LOG = LoggerFactory.getLogger(OpensearchIndexerSearchTest::class.java)
    }

    @Test
    fun testSupplierIndexer() {
        osContainer.shouldNotBeNull()
        supplierIndexer.shouldNotBeNull()
        val supplierId1 = UUID.randomUUID().toString()
        val doc1 = SupplierDoc(
            id = supplierId1,
            identifier = supplierId1,
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
        val supplierId2 = UUID.randomUUID().toString()
        val doc2 = SupplierDoc(
            id = supplierId2,
            identifier = supplierId2,
            status = SupplierStatus.ACTIVE,
            name = "Supplier 2",
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
        val docs = listOf(doc1, doc2)
        val response = supplierIndexer.index(docs)
        LOG.info("OS running: ${osContainer.container.httpHostAddress}")
        response.shouldNotBeNull()
        response.errors() shouldBe false
        supplierIndexer.getAlias() shouldBe true
    }

    @Test
    fun testProductIndexer() {
        osContainer.shouldNotBeNull()
        productIndexer.shouldNotBeNull()
        val productDoc = ProductDoc (
            id = UUID.randomUUID().toString(),
            supplier = ProductSupplier(
                id = UUID.randomUUID().toString(),
                identifier = "identifier",
                name = "supplier 1"
            ),
            title = "title",
            articleName = "articleName",
            attributes = AttributesDoc(),
            hmsArtNr = "123456",
            supplierRef = "supplierRef",
            isoCategory = "12345678",
            isoCategoryTitle = "isoCategoryTitle",
            isoCategoryTitleShort = "isoCategoryTitleShort",
            isoCategoryText = "isoCategoryText",
            isoCategoryTextShort = "isoCategoryTextShort",
            isoSearchTag = "isoSearchTag".split(","),
            accessory = false,
            sparePart = false,
            main = true,
            seriesId = UUID.randomUUID().toString(),
            data = listOf(TechData("key", "value", "unit")),
            media =  emptyList(),
            expired = LocalDateTime.now().plusYears(2),
            filters = TechDataFilters(),
            agreements = emptyList(),
            previousAgreements = emptyList(),
            hasAgreement = true,
            hasPreviousAgreement = false,
            status = ProductStatus.ACTIVE,
            identifier = "identifier",
            created = LocalDateTime.now(),
            updated = LocalDateTime.now(),
            createdBy = "REGISTER",
            updatedBy = "REGISTER",
        )
        val response = productIndexer.index(productDoc)
        response.errors() shouldBe false
        productIndexer.getAlias() shouldBe true
    }
}
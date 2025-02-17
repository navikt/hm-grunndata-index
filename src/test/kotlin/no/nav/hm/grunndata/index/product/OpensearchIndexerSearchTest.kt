package no.nav.hm.grunndata.index.product

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.toUUID
import no.nav.hm.grunndata.index.agreement.AgreementDoc
import no.nav.hm.grunndata.index.agreement.AgreementIndexer
import no.nav.hm.grunndata.index.supplier.SupplierDoc
import no.nav.hm.grunndata.index.supplier.SupplierIndexer
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.SupplierStatus
import no.nav.hm.grunndata.rapid.dto.TechData
import org.junit.jupiter.api.Test
import org.opensearch.client.opensearch._types.Result
import org.slf4j.LoggerFactory

@MicronautTest
class OpensearchIndexerSearchTest(
    private val osContainer: OSContainer,
    private val supplierIndexer: SupplierIndexer,
    private val productIndexer: ProductIndexer,
    private val agreementIndexer: AgreementIndexer
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
        response.shouldNotBeNull()
        response.errors() shouldBe false
        supplierIndexer.getAlias().result().keys.size shouldBe 1
        supplierIndexer.docCount() shouldBe 2
        supplierIndexer.delete(doc2.id.toUUID()).result() shouldBe Result.Deleted

    }

    @Test
    fun testProductIndexer() {
        osContainer.shouldNotBeNull()
        productIndexer.shouldNotBeNull()
        val productDoc = ProductDoc(
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
            isoCategoryTitleInternational = "isoCategoryTitleInternational",
            accessory = false,
            sparePart = false,
            main = true,
            seriesId = UUID.randomUUID().toString(),
            data = listOf(TechData("key", "value", "unit")),
            media = emptyList(),
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
        productIndexer.getAlias().result().keys.size shouldBe 1

    }

    @Test
    fun testAgreementIndexer() {
        osContainer.shouldNotBeNull()
        agreementIndexer.shouldNotBeNull()
        val agreementDoc = AgreementDoc(
            id = UUID.randomUUID().toString(),
            identifier = "identifier",
            title = "title",
            label = "label",
            status = AgreementStatus.ACTIVE,
            resume = "resume",
            text = "text",
            reference = "reference",
            published = LocalDateTime.now(),
            expired = LocalDateTime.now(),
            attachments =  emptyList(),
            posts = emptyList(),
            isoCategory = emptyList(),
            createdBy = "REGISTER",
            updatedBy = "REGISTER",
            created = LocalDateTime.now(),
            updated = LocalDateTime.now(),
        )
        val response = agreementIndexer.index(agreementDoc)
        response.errors() shouldBe false
    }
}
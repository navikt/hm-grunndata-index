package no.nav.hm.grunndata.index.product

import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

@MicronautTest
class OpensearchIndexerSearchTest(val osContainer: OSContainer, val productIndexer: ProductIndexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(OpensearchIndexerSearchTest::class.java)
    }

    @Test
    fun integrationTestIndexerAndSearch() {
        osContainer.shouldNotBeNull()
        productIndexer.shouldNotBeNull()
    }


}
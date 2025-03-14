package no.nav.hm.grunndata.index.agreement

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.createIndexName
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.rapid.dto.AgreementStatus
import org.opensearch.client.opensearch.OpenSearchClient

@Singleton
class AgreementIndexer(private val agreementGdbApiClient: AgreementGdbApiClient,
                       @Value("\${agreements.aliasName}") private val aliasName: String,
                       private val client: OpenSearchClient): Indexer(client, settings, mapping, aliasName) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexer::class.java)
        val settings = AgreementIndexer::class.java
        .getResource("/opensearch/agreements_settings.json")!!.readText()
        val mapping = AgreementIndexer::class.java
        .getResource("/opensearch/agreements_mapping.json")!!.readText()
    }

    fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexType.agreements)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, settings, mapping)
        }
        var updated =  LocalDateTime.now().minusYears(30)
        var page = agreementGdbApiClient.findAgreements(params = mapOf("updatedAfter" to updated.toString()),
            size=1000, page = 0, sort="updated,asc")
        var lastId: UUID? = null
        while(page.numberOfElements>0) {
            val agreements = page.content.map { it.toDoc() }.filter {  it.status != AgreementStatus.DELETED }
            LOG.info("indexing ${agreements.size} agreements to $indexName")
            if (agreements.isNotEmpty()) index(agreements, indexName)
            val last = page.last()
            if (updated.equals(last.updated) && last.id == lastId) {
                LOG.info("Last updated ${last.updated} ${last.id} is the same, increasing last updated")
                updated = updated.plusNanos(1000000)
            }
            else {
                lastId = last.id
                updated = last.updated
            }
            page = agreementGdbApiClient.findAgreements(params = mapOf("updatedAfter" to updated.toString()),
                size=1000, page = 0, sort="updated,asc")
        }
        if (alias) {
           updateAlias(indexName)
        }
    }
}

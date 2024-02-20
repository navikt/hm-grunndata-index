package no.nav.hm.grunndata.index.news

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.createIndexName
import org.opensearch.action.bulk.BulkResponse
import org.opensearch.action.delete.DeleteResponse
import java.util.*

@Singleton
class NewsIndexer(private val indexer: Indexer,
                  @Value("\${news.aliasName}") private val aliasName: String,
                  private val newsGdbApiClient: NewsGDBApiClient) {

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(NewsIndexer::class.java)
        private val settings = NewsIndexer::class.java
            .getResource("/opensearch/news_settings.json")?.readText()
        private val mapping = NewsIndexer::class.java
            .getResource("/opensearch/news_mapping.json")?.readText()
    }

    init {
        try {
            initAlias()
        } catch (e: Exception) {
            LOG.error("OpenSearch might not be ready ${e.message}, will wait 10s and retry")
            Thread.sleep(10000)
            initAlias()
        }
    }

    fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexType.news)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName)
        }
        val page = newsGdbApiClient.findNews(size=5000, page = 0, sort="updated,asc")
        val news = page.content.map { it.toDoc() }
        LOG.info("indexing ${news.size} news to $indexName")
        index(news, indexName)
        if (alias) {
            updateAlias(indexName)
        }

    }



    fun index(docs: List<NewsDoc>): BulkResponse = indexer.index(docs, aliasName)

    fun index(doc: NewsDoc): BulkResponse = indexer.index(listOf(doc), aliasName)


    fun index(doc: NewsDoc, indexName: String): BulkResponse =
        indexer.index(listOf(doc), indexName)


    fun index(docs: List<NewsDoc>, indexName: String): BulkResponse =
        indexer.index(docs,indexName)

    fun delete(uuid: UUID): DeleteResponse = indexer.delete(uuid.toString(), aliasName)

    fun createIndex(indexName: String): Boolean = indexer.createIndex(indexName, settings, mapping)

    fun updateAlias(indexName: String): Boolean = indexer.updateAlias(indexName,aliasName)

    fun getAlias() = indexer.getAlias(aliasName)

    fun indexExists(indexName: String): Boolean = indexer.indexExists(indexName)

    fun initAlias() = indexer.initAlias(aliasName, settings, mapping)


}
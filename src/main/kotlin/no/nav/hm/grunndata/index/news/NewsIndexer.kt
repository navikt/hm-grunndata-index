package no.nav.hm.grunndata.index.news

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.IndexType
import no.nav.hm.grunndata.index.Indexer
import no.nav.hm.grunndata.index.createIndexName
import org.opensearch.client.opensearch.OpenSearchClient

@Singleton
class NewsIndexer(@Value("\${news.aliasName}") private val aliasName: String,
                  private val newsGdbApiClient: NewsGDBApiClient,
                  private val client: OpenSearchClient): Indexer(client, settings, mapping, aliasName) {

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(NewsIndexer::class.java)
        private val settings = NewsIndexer::class.java
            .getResource("/opensearch/news_settings.json")!!.readText()
        private val mapping = NewsIndexer::class.java
            .getResource("/opensearch/news_mapping.json")!!.readText()
    }


    fun reIndex(alias: Boolean) {
        val indexName = createIndexName(IndexType.news)
        if (!indexExists(indexName)) {
            LOG.info("creating index $indexName")
            createIndex(indexName, settings, mapping)
        }
        val page = newsGdbApiClient.findNews(size=5000, page = 0, sort="updated,asc")
        val news = page.content.map { it.toDoc() }
        LOG.info("indexing ${news.size} news to $indexName")
        index(news, indexName)
        if (alias) {
            updateAlias(indexName)
        }

    }

}
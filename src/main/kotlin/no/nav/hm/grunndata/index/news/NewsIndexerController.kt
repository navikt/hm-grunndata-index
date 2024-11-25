package no.nav.hm.grunndata.index.news

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import org.slf4j.LoggerFactory

@Controller("/internal/index/news")
@ExecuteOn(TaskExecutors.BLOCKING)
class NewsIndexerController(private val newsIndexer: NewsIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(NewsIndexerController::class.java)
    }

    @Post("/")
    fun indexNews(@QueryValue(value = "alias", defaultValue = "false") alias: Boolean) {
        newsIndexer.reIndex(alias)
    }

    @Put("/alias/{indexName}")
    fun aliasNews(indexName: String) {
        newsIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = newsIndexer.getAlias().toJsonString()

    @Get("/count")
    fun count() = newsIndexer.docCount()
}
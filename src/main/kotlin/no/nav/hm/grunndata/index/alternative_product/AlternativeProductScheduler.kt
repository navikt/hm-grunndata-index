package no.nav.hm.grunndata.index.alternative_product

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.nav.hm.grunndata.register.leaderelection.LeaderOnly
import org.slf4j.LoggerFactory

@Singleton
open class AlternativeProductScheduler(private val alternativeProductIndexer: AlternativeProductIndexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductScheduler::class.java)
    }

    @LeaderOnly
    @Scheduled(cron = "0 0 1 * * *")
    open fun runReIndexAlternativeProducts() {
        // temporary implementation for løfteplattform agreement
        LOG.info("Reindexing alternative products, to get updated warehouse stock")
        alternativeProductIndexer.reIndexByIsoCategory("12312101")
        alternativeProductIndexer.reIndexByIsoCategory("12360401")
        alternativeProductIndexer.reIndexByIsoCategory("12360301")
        alternativeProductIndexer.reIndexByIsoCategory("12361202")
        alternativeProductIndexer.reIndexByIsoCategory("12361501")
    }
}
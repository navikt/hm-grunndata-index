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

    val isos = setOf(
        // løfteplattform
        "12360401",
        "12360301",
        "12361202",
        "12361501",
        // stoler med oppreisningsfunksjon
        "18091501",
        "18091502",
        // Kalendere, dagsplanleggere og tidtakere
        "22271501",
        "22271201",
    )

    @LeaderOnly
    @Scheduled(cron = "0 0 1 * * *")
    open fun runReIndexAlternativeProducts() {
        // temporary implementation for løfteplattform agreement
        LOG.info("Reindexing alternative products, to get updated warehouse stock")
        isos.forEach {
            LOG.info("reindexing alternative products for iso: $it")
            alternativeProductIndexer.reIndexByIsoCategory(it)
        }
    }
}
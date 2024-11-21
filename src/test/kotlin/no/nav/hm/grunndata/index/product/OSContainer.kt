package no.nav.hm.grunndata.index.product

import jakarta.inject.Singleton

import org.opensearch.testcontainers.OpensearchContainer
import org.slf4j.LoggerFactory
import org.testcontainers.utility.DockerImageName

@Singleton
class OSContainer {

    val container = OpensearchContainer(OPENSEARCH_IMAGE).withSecurityEnabled()

    companion object {
        private val OPENSEARCH_IMAGE = DockerImageName.parse("opensearchproject/opensearch:2.11.0")
        private val LOG = LoggerFactory.getLogger(OSContainer::class.java)
    }
    init {
        container.start()
        LOG.info("Opensearch container started")

    }

}

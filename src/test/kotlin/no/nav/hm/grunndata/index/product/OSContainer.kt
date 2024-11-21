package no.nav.hm.grunndata.index.product

import jakarta.inject.Singleton
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.conn.ssl.TrustAllStrategy
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.ssl.SSLContextBuilder
import org.opensearch.client.Request
import org.opensearch.client.Response
import org.opensearch.client.RestClient
import org.opensearch.testcontainers.OpensearchContainer
import org.slf4j.LoggerFactory
import org.testcontainers.utility.DockerImageName

@Singleton
class OSContainer {

    val container = OpensearchContainer(OPENSEARCH_IMAGE).withExposedPorts(3333).withSecurityEnabled()

    companion object {
        private val OPENSEARCH_IMAGE = DockerImageName.parse("opensearchproject/opensearch:2.11.0")
        private val LOG = LoggerFactory.getLogger(OSContainer::class.java)
    }
    init {
        container.start()
        LOG.info("Opensearch container started")

    }

}

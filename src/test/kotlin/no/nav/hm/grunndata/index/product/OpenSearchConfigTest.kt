package no.nav.hm.grunndata.index.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import jakarta.inject.Singleton
import java.security.cert.X509Certificate
import no.nav.hm.grunndata.index.OpenSearchConfig
import org.apache.hc.client5.http.auth.AuthScope
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.ssl.SSLContextBuilder
import org.opensearch.client.json.jackson.JacksonJsonpMapper
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.transport.OpenSearchTransport
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder
import org.slf4j.LoggerFactory


@Factory
@Replaces(factory = OpenSearchConfig::class)
class OpenSearchConfigTest(private val osContainer: OSContainer,
                       private val objectMapper: ObjectMapper) {

    companion object {
        private val LOG = LoggerFactory.getLogger(OpenSearchConfigTest::class.java)
    }

    @Singleton
    fun buildOpenSearchClient(): OpenSearchClient {
        val host = HttpHost.create(osContainer.container.httpHostAddress)
        val credentialsProvider: BasicCredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope(host),
            UsernamePasswordCredentials("admin", "admin".toCharArray())
        )
        val sslcontext = SSLContextBuilder
            .create()
            .loadTrustMaterial(
                null
            ) { chains: Array<X509Certificate?>?, authType: String? -> true }
            .build()

        val builder = ApacheHttpClient5TransportBuilder.builder(host)
            .setMapper(JacksonJsonpMapper(objectMapper))
            .setHttpClientConfigCallback { httpClientBuilder: HttpAsyncClientBuilder ->
                val tlsStrategy = ClientTlsStrategyBuilder.create()
                    .setSslContext(sslcontext)
                    .build()
                val connectionManager = PoolingAsyncClientConnectionManagerBuilder
                    .create()
                    .setTlsStrategy(tlsStrategy)
                    .build()
                httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setConnectionManager(connectionManager)
            }

        val transport: OpenSearchTransport = builder.build()
        val client = OpenSearchClient(transport)
        LOG.info("Opensearch client using admin and url ${osContainer.container.httpHostAddress}")
        return client
    }

}


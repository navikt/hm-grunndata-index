package no.nav.hm.grunndata.index.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import jakarta.inject.Singleton
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import no.nav.hm.grunndata.index.OpenSearchConfig
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.opensearch.client.RestClient
import org.opensearch.client.json.jackson.JacksonJsonpMapper
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.transport.rest_client.RestClientTransport
import org.slf4j.LoggerFactory

@Factory
@Replaces(factory = OpenSearchConfig::class)
class OpensearchConfigTest(private val osContainer: OSContainer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(OpensearchConfigTest::class.java)
    }

    @Singleton
    fun buildOpenSearchRestClient(): RestClient {
        val credentialsProvider: CredentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope.ANY,
            UsernamePasswordCredentials("admin", "admin")
        )
        val client = RestClient.builder(HttpHost.create(osContainer.container.getHttpHostAddress()))
            .setHttpClientConfigCallback {
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                    devAndTestSettings(httpClientBuilder)
                httpClientBuilder
            }.build()

        LOG.info("Opensearch client using url: ${osContainer.container.getHttpHostAddress()}")
        return client
    }

    @Singleton
    fun buildOpenSearchClient(restClient: RestClient): OpenSearchClient =
        OpenSearchClient(RestClientTransport(restClient, JacksonJsonpMapper(ObjectMapper().registerModule(JavaTimeModule()))))



    private fun devAndTestSettings(httpClientBuilder: HttpAsyncClientBuilder) {
        httpClientBuilder.setSSLHostnameVerifier { _, _ -> true }
        val context = SSLContext.getInstance("SSL")
        context.init(null, arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate>? {
                return null
            }

        }), SecureRandom())
        httpClientBuilder.setSSLContext(context)
    }

}
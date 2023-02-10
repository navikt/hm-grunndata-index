package no.nav.hm.grunndata.index.product

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.River
import no.nav.hm.grunndata.dto.ProductDTO
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class ProductIndexerRiver(river: RiverHead, private val objectMapper: ObjectMapper,
                          private val productIndexer: ProductIndexer): River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(ProductIndexerRiver::class.java)
    }

    init {
        river
            .validate { it.demandValue("payloadType", ProductDTO::class.java.simpleName)}
            .validate { it.demandKey("payload")}
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val dto = objectMapper.treeToValue(packet["payload"], ProductDTO::class.java)

        LOG.info("indexing product id: ${dto.id} hmsnr: ${dto.hmsArtNr}")
        runBlocking {
            productIndexer.index(dto.toDoc())
        }
    }

}
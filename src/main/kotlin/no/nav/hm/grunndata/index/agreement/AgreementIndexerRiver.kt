package no.nav.hm.grunndata.index.agreement

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.River
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class AgreementIndexerRiver(river: RiverHead, private val objectMapper: ObjectMapper,
                            private val agreementIndexer: AgreementIndexer
): River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(AgreementIndexerRiver::class.java)
    }

    init {
        river
            .validate { it.demandValue("payloadType", AgreementDTO::class.java.simpleName)}
            .validate { it.demandKey("payload")}
            .validate { it.requireKey("dtoVersion")}
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val dto = objectMapper.treeToValue(packet["payload"], AgreementDTO::class.java)
        val dtoVersion = packet["dtoVersion"]
        LOG.info("indexing agreement id: ${dto.id} reference: ${dto.reference} EventDTOVersion: $dtoVersion " +
                "rapidDTOVersion: $rapidDTOVersion")
        agreementIndexer.index(dto.toDoc())
    }

}
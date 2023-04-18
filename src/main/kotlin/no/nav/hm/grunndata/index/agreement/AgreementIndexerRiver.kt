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
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.grunndata.rapid.event.RapidApp
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
            .validate { it.demandValue("createdBy", RapidApp.grunndata_db)}
            .validate { it.demandValue("eventName", EventName.hmdbagreementsyncV1)}
            .validate { it.demandKey("payload")}
            .validate { it.requireKey("dtoVersion")}
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {

        val dtoVersion = packet["dtoVersion"].asLong()
        if (dtoVersion > rapidDTOVersion) LOG.warn("this event dto version $dtoVersion is newer than our version: $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], AgreementDTO::class.java)
        LOG.info("indexing agreement id: ${dto.id} reference: ${dto.reference}")
        agreementIndexer.index(dto.toDoc())
    }

}

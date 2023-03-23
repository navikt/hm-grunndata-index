package no.nav.hm.grunndata.index.supplier

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.River
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class SupplierIndexerRiver(river: RiverHead, private val objectMapper: ObjectMapper,
                           private val supplierIndexer: SupplierIndexer
): River.PacketListener {

    companion object {
        private val LOG = LoggerFactory.getLogger(SupplierIndexerRiver::class.java)
    }

    init {
        river
            .validate { it.demandValue("payloadType", SupplierDTO::class.java.simpleName)}
            .validate { it.demandKey("payload")}
            .validate { it.demandKey("dtoVersion")}
            .register(this)

    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val dtoVersion = packet["dtoVersion"].asLong()
        if (dtoVersion > rapidDTOVersion) LOG.warn("this event dto version $dtoVersion is newer than our version: $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], SupplierDTO::class.java)
        LOG.info("indexing supplier id: ${dto.id} name: ${dto.name}")
        supplierIndexer.index(dto.toDoc())
    }
}

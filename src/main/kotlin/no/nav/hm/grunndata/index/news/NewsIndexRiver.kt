package no.nav.hm.grunndata.index.news

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.River
import no.nav.hm.grunndata.rapid.dto.NewsDTO
import no.nav.hm.grunndata.rapid.dto.NewsStatus
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.grunndata.rapid.event.RapidApp
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class NewsIndexRiver(river: RiverHead,
                     private val objectMapper: ObjectMapper,
                     private val newsIndexer: NewsIndexer): River.PacketListener{

    companion object {
        private val LOG = LoggerFactory.getLogger(NewsIndexRiver::class.java)
    }

    init {
        LOG.info("Using Rapid DTO version $rapidDTOVersion")
        river
            .validate{ it.demandValue("createdBy", RapidApp.grunndata_db ) }
            .validate{ it.demandAny("eventName", listOf(EventName.hmdbnewsyncV1)) }
            .validate{ it.demandKey("payload") }
            .validate{ it.demandKey("dtoVersion") }
        river.register(this)
    }
    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val dtoVersion = packet["dtoVersion"].asLong()
        if (dtoVersion > rapidDTOVersion) LOG.warn("this event dto version $dtoVersion is newer than our version $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], NewsDTO::class.java)
        if (dto.status == NewsStatus.DELETED) {
            LOG.info("deleting news id: ${dto.id} title: ${dto.title}")
            newsIndexer.delete(dto.id)
        }
        else {
            LOG.info("indexing news id: ${dto.id} title: ${dto.title}")
            newsIndexer.index(dto.toDoc())
        }
    }

}
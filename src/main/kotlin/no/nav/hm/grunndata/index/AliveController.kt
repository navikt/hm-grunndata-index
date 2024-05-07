package no.nav.hm.grunndata.index

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.helse.rapids_rivers.KafkaRapid

@Controller("/internal")
class AliveController(private val kafkaRapid: KafkaRapid) {


            @Get("/isAlive")
    fun alive(): String {
        if (kafkaRapid.isConsumerClosed()) {
            throw Exception("Error: consumer is closed")
        }
        return "ALIVE"
    }

    @Get("/isReady")
    fun ready() = "OK"

}
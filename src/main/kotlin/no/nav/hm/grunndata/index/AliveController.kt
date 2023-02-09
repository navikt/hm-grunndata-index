package no.nav.hm.grunndata.index

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/internal")
class AliveController {

    @Get("/isAlive")
    fun alive() = "ALIVE"

    @Get("/isReady")
    fun ready() = "OK"

}
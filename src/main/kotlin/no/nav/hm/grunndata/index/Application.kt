package no.nav.hm.grunndata.index

import io.micronaut.runtime.Micronaut

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.nav.hm.grunndata.index")
            .mainClass(Application.javaClass)
            .start()
    }
}
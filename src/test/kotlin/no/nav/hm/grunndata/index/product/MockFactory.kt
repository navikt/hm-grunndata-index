package no.nav.hm.grunndata.index.product

import io.micronaut.context.annotation.Factory
import io.mockk.mockk
import jakarta.inject.Singleton
import no.nav.hm.grunndata.index.GdbApiClient


@Factory
class MockFactory {

    @Singleton
    fun mockGdbClient(): GdbApiClient = mockk(relaxed = true)

}
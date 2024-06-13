package fi.metatavu.vp.vehiclemanagement.test.functional.resources

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import fi.metatavu.vp.usermanagement.model.Driver
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import java.util.*

/**
 * Wiremock for vehicle management service
 */
class UserManagementMockTestResource : QuarkusTestResourceLifecycleManager {
    private lateinit var wireMockServer: WireMockServer

    override fun start(): Map<String, String> {
        wireMockServer = WireMockServer(8082)
        wireMockServer.start()
        userManagementStubs()

        return mapOf(
            "quarkus.rest-client.\"fi.metatavu.vp.usermanagement.spec.DriversApi\".url" to wireMockServer.baseUrl()
        )
    }

    private fun getDriversMock(): List<Driver> = listOf(
        Driver(
            id = UUID.fromString("67F42F21-CAF4-4D52-9220-C979A7B072DC")
        )
    )

    private fun userManagementStubs() {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/drivers"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-API-Key", DefaultTestProfile.VEHICLE_MANAGEMENT_TELEMATICS_API_KEY)
                        .withBody(jacksonObjectMapper().writeValueAsString(getDriversMock()))
                )
        )
    }

    override fun stop() {
        wireMockServer.stop()
    }
}
package fi.metatavu.vp.vehiclemanagement.test.functional.resources.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

/**
 * Wiremock for vehicle management service
 */
class UserManagementMockTestResource : QuarkusTestResourceLifecycleManager {
    private lateinit var wireMockServer: WireMockServer

    override fun start(): Map<String, String> {
        wireMockServer = WireMockServer(WireMockConfiguration().port(8082).extensions(ListDriversResponseTransformer()))
        wireMockServer.start()

        wireMockServer.stubFor(
            get(urlPathEqualTo("/v1/drivers"))
                .withHeader("Authorization", matching("Bearer\\s.+"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withTransformers(ListDriversResponseTransformer.NAME)
                )
        )

        return mapOf(
            "quarkus.rest-client.\"fi.metatavu.vp.usermanagement.spec.DriversApi\".url" to wireMockServer.baseUrl()
        )
    }
    override fun stop() {
        wireMockServer.stop()
    }
}
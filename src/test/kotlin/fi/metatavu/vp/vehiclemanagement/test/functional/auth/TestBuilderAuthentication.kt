package fi.metatavu.vp.vehiclemanagement.test.functional.auth

import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenTestBuilderAuthentication
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.vehiclemanagement.test.functional.impl.*


/**
 * Test builder authentication
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 * @param apiKey api key
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    val accessTokenProvider: AccessTokenProvider,
    private val apiKey: String?
    ): AccessTokenTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

    val trucks = TrucksTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val publicTrucks = PublicTrucksTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val towables = TowablesTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val vehicles = VehiclesTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val telematics = TelematicsTestBuilderResource(testBuilder, this.apiKey, createClient())
    val driverCards = DriverCardTestBuilderResource(testBuilder, this.apiKey, createClient())

    override fun createClient(authProvider: AccessTokenProvider?): ApiClient {
        val result = ApiClient(ApiTestSettings.apiBasePath)
        ApiClient.accessToken = authProvider?.accessToken
        return result
    }

}
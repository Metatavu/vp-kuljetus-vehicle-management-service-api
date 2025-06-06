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
 * @param dataReceiverApiKey api key
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    val accessTokenProvider: AccessTokenProvider,
    private val dataReceiverApiKey: String?,
    private val keycloakApiKey: String?
    ): AccessTokenTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

    val trucks = TrucksTestBuilderResource(testBuilder, accessTokenProvider, this.dataReceiverApiKey, this.keycloakApiKey, createClient(accessTokenProvider))
    val publicTrucks = PublicTrucksTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val towables = TowablesTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val vehicles = VehiclesTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val temperatureReadings = TemperatureReadingTestBuilderResource(testBuilder, this.dataReceiverApiKey, createClient(accessTokenProvider))
    val trackables = TrackablesTestBuilderResource(testBuilder, this.dataReceiverApiKey, createClient(accessTokenProvider))
    val thermometers = ThermometersTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))

    override fun createClient(authProvider: AccessTokenProvider?): ApiClient {
        val result = ApiClient(ApiTestSettings.apiBasePath)
        ApiClient.accessToken = authProvider?.accessToken
        return result
    }

}
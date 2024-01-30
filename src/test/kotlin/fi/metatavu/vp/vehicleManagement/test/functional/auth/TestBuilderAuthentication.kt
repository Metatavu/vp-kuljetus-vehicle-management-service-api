package fi.metatavu.vp.vehicleManagement.test.functional.auth

import fi.metatavu.vp.vehicleManagement.test.functional.TestBuilder
import fi.metatavu.vp.vehicleManagement.test.functional.settings.ApiTestSettings
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenTestBuilderAuthentication
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.vehicleManagement.test.functional.impl.TrailersTestBuilderResource
import fi.metatavu.vp.vehicleManagement.test.functional.impl.TrucksTestBuilderResource
import fi.metatavu.vp.vehicleManagement.test.functional.impl.VehiclesTestBuilderResource


/**
 * Test builder authentication
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 *
 * @param testBuilder test builder instance
 * @param accessTokenProvider access token provider
 */
class TestBuilderAuthentication(
    private val testBuilder: TestBuilder,
    val accessTokenProvider: AccessTokenProvider
): AccessTokenTestBuilderAuthentication<ApiClient>(testBuilder, accessTokenProvider) {

    val trucks = TrucksTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val trailers = TrailersTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))
    val vehicles = VehiclesTestBuilderResource(testBuilder, accessTokenProvider, createClient(accessTokenProvider))

    override fun createClient(authProvider: AccessTokenProvider): ApiClient {
        val result = ApiClient(ApiTestSettings.apiBasePath)
        ApiClient.accessToken = authProvider.accessToken
        return result
    }

}
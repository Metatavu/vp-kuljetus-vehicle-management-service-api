package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.apis.PublicTrucksApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.models.PublicTruck
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings

/**
 * Public Trucks API test resource
 */
class PublicTrucksTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<Truck, ApiClient>(testBuilder, apiClient) {


    override fun clean(p0: Truck?) {
        // Trucks are deleted and managed in TrucksTsetBuilderResource
    }

    override fun getApi(): PublicTrucksApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return PublicTrucksApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Lists trucks
     *
     * @param first first
     * @param max max
     * @param vin vin
     * @return public trucks
     */
    fun list(
        first: Int? = null,
        max: Int? = null,
        vin: String? = null
    ): Array<PublicTruck> {
        return api.listPublicTrucks(vin = vin, first = first, max = max)
    }
}
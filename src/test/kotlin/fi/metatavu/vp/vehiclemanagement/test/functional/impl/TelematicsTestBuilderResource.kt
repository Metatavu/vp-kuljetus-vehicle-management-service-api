package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.apis.TelematicsApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.models.TelematicData
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings

/**
 * Test builder resource for Telematics API
 */
class TelematicsTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<TelematicData, ApiClient>(testBuilder, apiClient) {

    /**
     * Sends telematic data to the API
     *
     * @param vin VIN
     * @param telematicData telematic data
     * @return created telematics
     */
    fun receiveTelematicData(vin:String, telematicData: TelematicData) {
        return api.receiveTelematicData(vin, telematicData)
    }

    override fun clean(p0: TelematicData?) {
        // No cleanup functionality implemented yet
    }

    override fun getApi(): TelematicsApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return TelematicsApi(ApiTestSettings.apiBasePath)
    }
}
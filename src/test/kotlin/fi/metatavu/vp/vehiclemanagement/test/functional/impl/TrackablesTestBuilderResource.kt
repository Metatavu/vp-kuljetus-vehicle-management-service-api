package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.vp.test.client.apis.TrackablesApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.models.Trackable
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings

/**
 * Test builder resource for Trackables API
 */
class TrackablesTestBuilderResource(
    testBuilder: TestBuilder,
    private val dataReceiverApiKey: String?,
    apiClient: ApiClient
): ApiTestBuilderResource<Trackable, ApiClient>(testBuilder, apiClient) {
    override fun clean(p0: Trackable?) {
        // This API doesn't create any resources and therefore doesn't need to clean anything
    }

    override fun getApi(): TrackablesApi {
        if (dataReceiverApiKey != null) {
            ApiClient.apiKey["X-DataReceiver-API-Key"] = dataReceiverApiKey
        }

        return TrackablesApi(ApiTestSettings.apiBasePath)
    }

    fun findTrackable(imei: String): Trackable {
        return api.getTrackableByImei(imei)
    }
}
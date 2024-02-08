package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.vp.test.client.apis.TelematicsApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.TelematicData
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import org.junit.Assert

/**
 * Test builder resource for Telematics API
 */
class TelematicsTestBuilderResource(
    testBuilder: TestBuilder,
    apiClient: ApiClient
) : ApiTestBuilderResource<TelematicData, ApiClient>(testBuilder, apiClient) {

    override fun clean(p0: TelematicData?) {
        // No cleanup functionality implemented yet
    }

    override fun getApi(): TelematicsApi {
        return TelematicsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Sets api key
     *
     * @param key key
     */
    fun setKey(key: String) {
        ApiClient.apiKey["X-API-Key"] = key
    }

    /**
     * Removes api key
     */
    fun removeKey() {
        ApiClient.apiKey.remove("X-API-Key")
    }

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

    /**
     * Asserts that the data could not be received
     *
     * @param vin VIN
     * @param telematicData telematic data
     * @param expectedStatus expected status
     */
    fun assertReceiveDataFail(vin: String, telematicData: TelematicData, expectedStatus: Int) {
        try {
            api.receiveTelematicData(vin, telematicData)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
}
package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.vp.test.client.apis.TemperatureReadingsApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.TruckOrTowableTemperatureReading
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import org.junit.Assert

/**
 * Test builder resource for TemperatureReadings API
 */
class TemperatureReadingTestBuilderResource(
    testBuilder: TestBuilder,
    private val dataReceiverApiKey: String?,
    apiClient: ApiClient
) : ApiTestBuilderResource<TruckOrTowableTemperatureReading, ApiClient>(testBuilder, apiClient) {

    override fun clean(p0: TruckOrTowableTemperatureReading?) {
        // no delete endpoint available, will be cleaned by deleting truck/towable
    }

    override fun getApi(): TemperatureReadingsApi {
        if (dataReceiverApiKey != null) {
            ApiClient.apiKey["X-DataReceiver-API-Key"] = dataReceiverApiKey
        }
        return TemperatureReadingsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Creates new temperature reading
     *
     * @param truckTemperatureReading temperature reading
     */
    fun createTemperatureReading(truckTemperatureReading: TruckOrTowableTemperatureReading) {
        api.createTemperatureReading(truckTemperatureReading)
    }

    /**
     * Asserts that creating temperature reading fails with expected status
     *
     * @param expectedStatus expected status
     * @param truckTemperatureReading temperature reading
     */
    fun assertCreateTemperatureReadingFail( expectedStatus: Int, truckTemperatureReading: TruckOrTowableTemperatureReading) {
        try {
            api.createTemperatureReading(truckTemperatureReading)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

}
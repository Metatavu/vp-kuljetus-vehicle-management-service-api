package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.vp.test.client.apis.DriverCardsApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import org.junit.Assert

/**
 * Test builder resource for DriverCard API
 */
class DriverCardTestBuilderResource(
    testBuilder: TestBuilder,
    private val apiKey: String?,
    apiClient: ApiClient
) : ApiTestBuilderResource<fi.metatavu.vp.test.client.models.DriverCard, ApiClient>(testBuilder, apiClient) {

    override fun clean(p0: fi.metatavu.vp.test.client.models.DriverCard?) {
        // Delete endpoint does not exist
    }

    override fun getApi(): DriverCardsApi {
        if (apiKey != null) {
            ApiClient.apiKey["X-API-Key"] = apiKey
        }
        return DriverCardsApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Updates a driver card
     *
     * @param driverCardId driver card id
     * @param driverCard driver card
     * @return created driver card
     */
    fun updateDriverCard(driverCardId: String, driverCard: fi.metatavu.vp.test.client.models.DriverCard): fi.metatavu.vp.test.client.models.DriverCard {
        return api.updateDriverCard(driverCardId, driverCard)
    }

    /**
     * Asserts that the data could not be updated
     *
     * @param driverCardId driver card id
     * @param driverCard driver card
     * @param expectedStatus expected status
     */
    fun assertReceiveDataFail(driverCardId: String, driverCard: fi.metatavu.vp.test.client.models.DriverCard, expectedStatus: Int) {
        try {
            api.updateDriverCard(driverCardId, driverCard)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
}
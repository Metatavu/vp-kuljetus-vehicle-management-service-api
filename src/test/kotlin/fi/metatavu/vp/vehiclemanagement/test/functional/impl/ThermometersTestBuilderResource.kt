package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.apis.ThermometersApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.TruckOrTowableThermometer
import fi.metatavu.vp.test.client.models.UpdateTruckOrTowableThermometerRequest
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Thermometers API
 */
class ThermometersTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider,
    apiClient: ApiClient
): ApiTestBuilderResource<TruckOrTowableThermometer, ApiClient>(testBuilder, apiClient) {
    override fun clean(p0: TruckOrTowableThermometer?) {
        // This API doesn't create any resources and therefore doesn't need to clean anything
    }

    override fun getApi(): ThermometersApi {
        ApiClient.accessToken = accessTokenProvider.accessToken
        return ThermometersApi(ApiTestSettings.apiBasePath)
    }

    /**
     * Lists thermometers
     *
     * @param entityId entity id
     * @param entityType entity type
     * @param includeArchived include archived
     * @param first first
     * @param max max
     */
    fun listThermometers(
        entityId: UUID? = null,
        entityType: ThermometersApi.EntityTypeListTruckOrTowableThermometers? = null,
        includeArchived: Boolean = false,
        first: Int? = null,
        max: Int? = null
    ): Array<TruckOrTowableThermometer> {
        return api.listTruckOrTowableThermometers(
            entityId = entityId,
            entityType = entityType,
            includeArchived = includeArchived,
            first = first,
            max = max
        )
    }

    /**
     * Updates thermometers
     *
     * @param thermometerId thermometer id
     * @param name name
     */
    fun updateThermometer(
        thermometerId: UUID,
        name: String,
    ): TruckOrTowableThermometer {
        return api.updateTruckOrTowableThermometer(
            thermometerId = thermometerId,
            updateTruckOrTowableThermometerRequest = UpdateTruckOrTowableThermometerRequest(
                name = name,
            ),
        )
    }

    /**
     * Asserts that updating thermometer fails with expected status
     *
     * @param expectedStatus expected status
     * @param thermometerId thermometer id
     * @param name name
     */
    fun assertUpdateThermometerFail( expectedStatus: Int, thermometerId: UUID, name: String) {
        try {
            api.updateTruckOrTowableThermometer(
                thermometerId = thermometerId,
                updateTruckOrTowableThermometerRequest = UpdateTruckOrTowableThermometerRequest(name = name)
            )
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }
}
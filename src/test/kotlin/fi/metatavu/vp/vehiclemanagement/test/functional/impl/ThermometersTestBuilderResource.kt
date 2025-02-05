package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.apis.ThermometersApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.models.TruckOrTowableThermometer
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
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
}
package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.apis.TrucksApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Trucks API
 */
class TrucksTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<Truck, ApiClient>(testBuilder, apiClient) {

    /**
     * Creates new truck
     *
     * @return created truck
     */
    fun create(truck: Truck): Truck? {
        return addClosable(api.createTruck(truck))
    }

    /**
     * Finds truck
     *
     * @param truckId truck id
     * @return found truck
     */
    fun find(truckId: UUID): Truck {
        return api.findTruck(truckId)
    }

    /**
     * Lists trucks
     *
     * @param plateNumber plate number
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trucks
     */
    fun list(
        plateNumber: String? = null,
        firstResult: Int? = null,
        maxResults: Int? = null
    ): Array<Truck> {
        return api.listTrucks(
            plateNumber,
            firstResult,
            maxResults
        )
    }

    /**
     * Updates truck
     *
     * @param truckId truck id
     * @param updateData update data
     * @return updated truck
     */
    fun update(truckId: UUID, updateData: Truck): Truck {
        return api.updateTruck(truckId, updateData)
    }

    /**
     * Deletes truck
     *
     * @param truckId truck id
     */
    fun delete(truckId: UUID) {
        api.deleteTruck(truckId)
        removeCloseable { closable: Any ->
            if (closable !is Truck) {
                return@removeCloseable false
            }

            closable.id == truckId
        }
    }

    fun assertFindFail(expectedStatus: Int, truckId: UUID) {
        try {
            api.findTruck(truckId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertListFail(
        expectedStatus: Int,
        plateNumber: String? = null,
        firstResult: Int? = null,
        maxResults: Int? = null
    ) {
        try {
            api.listTrucks(plateNumber, firstResult, maxResults)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertUpdateFail(expectedStatus: Int, truckId: UUID, updateData: Truck) {
        try {
            api.updateTruck(truckId, updateData)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertDeleteFail(expectedStatus: Int, truckId: UUID) {
        try {
            api.deleteTruck(truckId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertCreateFail(expectedStatus: Int, truck: Truck) {
        try {
            api.createTruck(truck)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    override fun clean(p0: Truck?) {
        api.deleteTruck(p0?.id!!)
    }

    override fun getApi(): TrucksApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return TrucksApi(ApiTestSettings.apiBasePath)
    }


}
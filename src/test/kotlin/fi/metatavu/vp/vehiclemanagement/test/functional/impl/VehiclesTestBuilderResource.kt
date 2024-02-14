package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.apis.VehiclesApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.Vehicle
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Vehicles API
 */
class VehiclesTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<Vehicle, ApiClient>(testBuilder, apiClient) {

    /**
     * Creates new vehicle
     *
     * @param vehicle vehicle to be created
     * @return created vehicle
     */
    fun create(vehicle: Vehicle): Vehicle {
        return addClosable(api.createVehicle(vehicle))
    }

    /**
     * Creates new vehicle with default values
     *
     * @param truckId truck id
     * @param towableIds towable id list
     * @return created vehicle
     */
    fun create(truckId: UUID, towableIds: Array<UUID>): Vehicle {
        return create(Vehicle(truckId = truckId, towableIds = towableIds))
    }

    /**
     * Finds vehicle
     *
     * @param vehicleId vehicle id
     * @return found vehicle
     */
    fun find(vehicleId: UUID): Vehicle {
        return api.findVehicle(vehicleId)
    }

    /**
     * Lists vehicles
     *
     * @param truckId truck id
     * @param archived archived
     * @param firstResult first result
     * @param maxResults max results
     * @return list of vehicles
     */
    fun list(
        truckId: UUID? = null,
        archived: Boolean? = null,
        firstResult: Int? = null,
        maxResults: Int? = null
    ): Array<Vehicle> {
        return api.listVehicles(truckId, archived, firstResult, maxResults)
    }

    /**
     * Updates vehicle
     *
     * @param existingVehicle existing vehicle
     * @param newVehicleData new vehicle data
     * @return updated vehicle
     */
    fun update(
        existingVehicle: Vehicle,
        newVehicleData: Vehicle
    ): Vehicle {
        return api.updateVehicle(existingVehicle.id!!, newVehicleData)
    }

    /**
     * Deletes a vehicle from the API
     *
     * @param vehicleId vehicle id
     */
    fun delete(vehicleId: UUID) {
        removeCloseable { closable: Any ->
            if (closable !is Vehicle) {
                return@removeCloseable false
            }

            closable.id == vehicleId
        }
        api.deleteVehicle(vehicleId)
    }

    /**
     * Asserts that find operation fails with expected status
     *
     * @param expectedStatus expected status
     * @param vehicleId vehicle id
     */
    fun assertFindFail(expectedStatus: Int, vehicleId: UUID) {
        try {
            api.findVehicle(vehicleId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Asserts that list operation fails with expected status
     *
     * @param expectedStatus expected status
     * @param vehicleId vehicle id
     * @param archived archived
     * @param firstResult first result
     * @param maxResults max results
     */
    fun assertListFail(
        expectedStatus: Int,
        vehicleId: UUID? = null,
        archived: Boolean? = null,
        firstResult: Int? = null,
        maxResults: Int? = null
    ) {
        try {
            api.listVehicles(vehicleId, archived, firstResult, maxResults)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Asserts that update operation fails with expected status
     *
     * @param expectedStatus expected status
     * @param vehicleId vehicle id
     * @param updateData update data
     */
    fun assertUpdateFail(expectedStatus: Int, vehicleId: UUID, updateData: Vehicle) {
        try {
            api.updateVehicle(vehicleId, updateData)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Asserts that delete operation fails with expected status
     *
     * @param expectedStatus expected status
     * @param vehicleId vehicle id
     */
    fun assertDeleteFail(expectedStatus: Int, vehicleId: UUID) {
        try {
            api.deleteVehicle(vehicleId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Asserts that create operation fails with expected status
     *
     * @param expectedStatus expected status
     * @param truck vehicle
     */
    fun assertCreateFail(expectedStatus: Int, truck: Vehicle) {
        try {
            api.createVehicle(truck)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    override fun clean(p0: Vehicle?) {
        delete(p0!!.id!!)
    }

    override fun getApi(): VehiclesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return VehiclesApi(ApiTestSettings.apiBasePath)
    }

}
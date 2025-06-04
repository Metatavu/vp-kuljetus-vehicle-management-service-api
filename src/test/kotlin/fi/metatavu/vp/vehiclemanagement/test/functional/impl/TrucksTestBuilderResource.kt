package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.apis.TrucksApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.*
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import org.junit.Assert
import java.time.OffsetDateTime
import java.util.*

/**
 * Test builder resource for Trucks API
 */
class TrucksTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    private val dataReceiverApiKey: String?,
    private val keycloakApiKey: String?,
    apiClient: ApiClient
) : ApiTestBuilderResource<Truck, ApiClient>(testBuilder, apiClient) {

    /**
     * Creates new truck and adds the truck's vehicle to the closable resources
     *
     * @param truck truck data
     * @param vehiclesTestBuilderResource test resource for vehicles
     * @return created truck
     */
    fun create(
        truck: Truck,
        vehiclesTestBuilderResource: VehiclesTestBuilderResource
    ): Truck {
        val createdTruck = api.createTruck(truck)
        // Create "fake" vehicle with the id of the one that got
        // auto-created by the truck and mark it as closable resource
        // (since vehicle is automatically created for a new truck in the API)
        val createdVehicle = Vehicle(
            id = createdTruck.activeVehicleId,
            truckId = createdTruck.id!!,
            towableIds = emptyArray()
        )
        addClosable(createdTruck)
        vehiclesTestBuilderResource.addClosable(createdVehicle)
        return createdTruck
    }


    fun create(
        plateNumber: String,
        vin: String,
        name: String? = null,
        imei: String? = null,
        vehiclesTestBuilderResource: VehiclesTestBuilderResource
    ): Truck {
        return create(
            Truck(
                plateNumber = plateNumber,
                vin = vin,
                name = name,
                type = Truck.Type.TRUCK,
                imei = imei
            ), vehiclesTestBuilderResource
        )
    }

    /**
     * Creates new truck with default values
     *
     * @return created truck
     */
    fun create(vehiclesTestBuilderResource: VehiclesTestBuilderResource): Truck {
        return create("ABC-123", "001", null, null, vehiclesTestBuilderResource)
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
     * @param archived archived
     * @param sortBy sort by field
     * @param sortDirection sort direction
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trucks
     */
    fun list(
        plateNumber: String? = null,
        archived: Boolean? = null,
        sortBy: TruckSortByField? = null,
        sortDirection: SortOrder? = null,
        firstResult: Int? = null,
        thermometerId: UUID? = null,
        maxResults: Int? = null
    ): Array<Truck> {
        return api.listTrucks(
            plateNumber,
            archived,
            sortBy = sortBy,
            sortDirection = sortDirection,
            thermometerId = thermometerId,
            first = firstResult,
            max = maxResults
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
        archived: Boolean? = null,
        firstResult: Int? = null,
        maxResults: Int? = null
    ) {
        try {
            api.listTrucks(plateNumber, archived, null, null, null, firstResult, maxResults)
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

    /**
     * Creates driver card
     *
     * @param truckId truck id
     * @param truckDriverCard driver card data
     * @return created driver card
     */
    fun createDriverCard(truckId: UUID, truckDriverCard: TruckDriverCard): TruckDriverCard {
        return api.createTruckDriverCard(truckId, truckDriverCard)
    }

    /**
     * Asserts that the driver card could not be created
     *
     * @param truckId truck id
     * @param truckDriverCard driver card data
     * @param expectedStatus expected status
     */
    fun assertCreateDriverCardFail(truckId: UUID, truckDriverCard: TruckDriverCard, expectedStatus: Int) {
        try {
            api.createTruckDriverCard(truckId, truckDriverCard)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Deletes driver card
     *
     * @param truckId truck id
     * @param driverCardId driver card id
     * @param removedAt removed at, defaults to now
     */
    fun deleteTruckDriverCard(truckId: UUID, driverCardId: String, removedAt: OffsetDateTime = OffsetDateTime.now()) {
        api.deleteTruckDriverCard(truckId, driverCardId, removedAt.toString())
    }

    /**
     * Asserts that the driver card could not be deleted
     *
     * @param truckId truck id
     * @param driverCardId driver card id
     * @param removedAt removed at, defaults to now
     * @param expectedStatus expected status
     */
    fun assertDeleteDriverCardFail(truckId: UUID, driverCardId: String, expectedStatus: Int, removedAt: OffsetDateTime = OffsetDateTime.now()) {
        try {
            api.deleteTruckDriverCard(truckId, driverCardId, removedAt.toString())
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Lists driver cards
     *
     * @param truckId truck id
     * @return list of driver cards
     */
    fun listDriverCards(truckId: UUID): Array<TruckDriverCard> {
        return api.listTruckDriverCards(truckId)
    }

    /**
     * Asserts that the list of driver cards could not be retrieved
     *
     * @param truckId truck id
     * @param expectedStatus expected status
     */
    fun assertListDriverCardsFail(truckId: UUID, expectedStatus: Int) {
        try {
            api.listTruckDriverCards(truckId)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Creates truck speed
     *
     * @param truckId truck id
     * @param truckSpeed truck speed data
     */
    fun createTruckSpeed(truckId: UUID, truckSpeed: TruckSpeed) {
        return api.createTruckSpeed(truckId, truckSpeed)
    }

    /**
     * Lists truck speeds
     *
     * @param truckId truck id
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return list of truck speeds
     */
    fun listTruckSpeed(
        truckId: UUID,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Array<TruckSpeed> {
        return api.listTruckSpeeds(truckId, after?.toString(), before?.toString(), first, max)
    }

    /**
     * Asserts that the list of truck speeds could not be retrieved
     *
     * @param truckId truck id
     * @param expectedStatus expected status
     */
    fun assertListTruckSpeedFail(
        truckId: UUID,
        expectedStatus: Int
    ) {
        try {
            api.listTruckSpeeds(truckId, null, null, null)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Lists truck locations
     *
     * @param truckId truck id
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return list of truck locations
     */
    fun listTruckLocations(
        truckId: UUID,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Array<TruckLocation> {
        return api.listTruckLocations(truckId, after?.toString(), before?.toString(), first, max)
    }

    /**
     * Asserts that the list of truck locations could not be retrieved
     *
     * @param truckId truck id
     * @param expectedStatus expected status
     */
    fun assertListTruckLocationsFail(truckId: UUID, expectedStatus: Int) {
        try {
            api.listTruckLocations(truckId)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Creates truck location
     *
     * @param truckId truck id
     * @param truckLocation truck location id
     */
    fun createTruckLocation(truckId: UUID, truckLocation: TruckLocation) {
        api.createTruckLocation(truckId, truckLocation)
    }

    /**
     * Asserts that the truck location could not be created
     *
     * @param truckId truck id
     * @param truckLocation truck location data
     * @param expectedStatus expected status
     */
    fun assertCreateTruckLocationFail(truckId: UUID, truckLocation: TruckLocation, expectedStatus: Int) {
        try {
            api.createTruckLocation(truckId, truckLocation)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Lists drive states
     *
     * @param truckId truck id
     * @param driverId driver id
     * @param state state
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return list of drive states
     */
    fun listDriveStates(
        truckId: UUID,
        driverId: UUID? = null,
        state: Array<TruckDriveStateEnum>? = null,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Array<TruckDriveState> {
        return api.listDriveStates(
            truckId = truckId,
            driverId = driverId,
            state = state,
            after = after?.toString(),
            before = before?.toString(),
            first = first,
            max = max
        )
    }

    /**
     * Asserts that the list of drive states could not be retrieved
     *
     * @param truckId truck id
     * @param expectedStatus expected status
     */
    fun assertListDriveStatesFail(
        truckId: UUID,
        expectedStatus: Int
    ) {
        try {
            api.listDriveStates(
                truckId = truckId,
                driverId = null,
                state = null,
                after = null,
                before = null,
                first = null,
                max = null
            )
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Creates drive state
     *
     * @param truckId truck id
     * @param truckDriveState drive state data
     */
    fun createDriveState(
        truckId: UUID,
        truckDriveState: TruckDriveState
    ) {
        return api.createDriveState(truckId, truckDriveState)
    }

    /**
     * Asserts that the drive state could not be created
     *
     * @param truckId truck id
     * @param truckDriveState drive state data
     * @param expectedStatus expected status
     */
    fun assertCreateDriveStateFail(
        truckId: UUID,
        truckDriveState: TruckDriveState,
        expectedStatus: Int
    ) {
        try {
            api.createDriveState(truckId, truckDriveState)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Creates truck odometer reading
     *
     * @param truckId truck id
     * @param truckOdometerReading truck odometer reading data
     */
    fun createTruckOdometerReading(truckId: UUID, truckOdometerReading: TruckOdometerReading) {
        api.createTruckOdometerReading(truckId, truckOdometerReading)
    }

    /**
     * Lists truck odometer readings
     *
     * @param truckId truck id
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return list of truck odometer readings
     */
    fun listTruckOdometerReading(
        truckId: UUID,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Array<TruckOdometerReading> {
        return api.listTruckOdometerReadings(truckId, after?.toString(), before?.toString(), first, max)
    }

    /**
     * Asserts that the list of odometer readings could not be retrieved
     *
     * @param truckId truck id
     * @param expectedStatus expected status
     */
    fun assertListTruckOdometerReadingFail(
        truckId: UUID,
        expectedStatus: Int
    ) {
        try {
            api.listTruckOdometerReadings(
                truckId = truckId,
                after = null,
                before = null,
                first = null,
                max = null
            )
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    /**
     * Lists truck or towable temperature readings
     *
     * @param truckId truck id
     * @param includeArchived include archived thermometers data
     * @param first first result
     * @param max max results
     */
    fun listTemperatureReadings(
        truckId: UUID,
        includeArchived: Boolean,
        first: Int? = null,
        max: Int? = null
    ): Array<TruckOrTowableTemperature> {
        return api.listTruckTemperatures(truckId, includeArchived, first, max)
    }

    override fun clean(p0: Truck?) {
        api.deleteTruck(p0?.id!!)
    }

    override fun getApi(): TrucksApi {
        if (dataReceiverApiKey != null) {
            ApiClient.apiKey["X-DataReceiver-API-Key"] = dataReceiverApiKey
        }

        if (keycloakApiKey != null) {
            ApiClient.apiKey["X-Keycloak-API-Key"] = keycloakApiKey
        }
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return TrucksApi(ApiTestSettings.apiBasePath)
    }
}
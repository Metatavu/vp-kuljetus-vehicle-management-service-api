package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.test.client.models.Towable
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.vehiclemanagement.test.functional.impl.InvalidTestValues
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Test class for testing Trucks API
 */
@QuarkusTest
class TruckTestIT : AbstractFunctionalTest() {

    @Test
    fun testList() = createTestBuilder().use { builder ->
        builder.user.trucks.create(plateNumber = plateNumber, vin = "001")
        builder.user.trucks.create(plateNumber = "DEF-456", vin = "002")
        builder.user.trucks.create(plateNumber = "GHI-789", vin = "003")
        val totalList = builder.user.trucks.list()
        assertEquals(3, totalList.size)

        val pagedList = builder.user.trucks.list(firstResult = 1, maxResults = 1)
        assertEquals(1, pagedList.size)

        val pagedList2 = builder.user.trucks.list(firstResult = 0, maxResults = 3)
        assertEquals(3, pagedList2.size)

        val pagedList3 = builder.user.trucks.list(firstResult = 0, maxResults = 2)
        assertEquals(2, pagedList3.size)

        val pagedList4 = builder.user.trucks.list(firstResult = 0, maxResults = 0)
        assertEquals(0, pagedList4.size)

        val filteredList = builder.user.trucks.list(plateNumber = plateNumber)
        assertEquals(1, filteredList.size)
    }


    @Test
    fun testCreate() = createTestBuilder().use { builder ->
        val truckData = Truck(plateNumber = plateNumber, type = Truck.Type.TRUCK, vin = "someVinNumber")
        val createdTruck = builder.user.trucks.create(truckData)
        assertNotNull(createdTruck)
        assertNotNull(createdTruck.id)
        assertNotNull(createdTruck.createdAt)
        assertEquals(truckData.plateNumber, createdTruck.plateNumber)
        assertEquals(truckData.type, createdTruck.type)
        assertEquals(truckData.vin, createdTruck.vin)

        // We cannot create trucks or towables with already existing plate number
        builder.user.trucks.assertCreateFail(400, createdTruck)
        builder.user.towables.assertCreateFail(400, Towable(plateNumber = plateNumber, vin = "003", type = Towable.Type.TRAILER))
        // Same check for vin
        builder.user.trucks.assertCreateFail(
            400,
            Truck(plateNumber = "DEF-456", type = Truck.Type.TRUCK, vin = createdTruck.vin)
        )
        builder.user.towables.assertCreateFail(
            400,
            Towable(plateNumber = "DEF-456", type = Towable.Type.TRAILER, vin = createdTruck.vin)
        )
    }

    @Test
    fun testCreateFail() = createTestBuilder().use { builder ->
        InvalidValueTestScenarioBuilder(
            path = "v1/trucks",
            method = Method.POST,
            token = builder.user.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.Trucks.INVALID_TRUCKS,
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testFind() = createTestBuilder().use { builder ->
        val truckData = Truck(plateNumber = plateNumber, vin = "001", type = Truck.Type.TRUCK)
        val createdTruck = builder.user.trucks.create(truckData)
        assertNotNull(createdTruck)
        assertEquals(truckData.plateNumber, createdTruck.plateNumber)
    }

    @Test
    fun testFindFail() = createTestBuilder().use { builder ->
        val createdTruck = builder.user.trucks.create()

        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}",
            method = Method.GET,
            token = builder.user.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "truckId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdTruck.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

    @Test
    fun testUpdate() = createTestBuilder().use { builder ->
        val truck1 = builder.user.trucks.create(Truck(plateNumber = "0111", type = Truck.Type.SEMI_TRUCK, vin = "vin1"))
        val truck2 = builder.user.trucks.create(Truck(plateNumber = "0222", type = Truck.Type.SEMI_TRUCK, vin = "vin2"))

        val updateData = Truck(plateNumber = "DEF-456", type = Truck.Type.SEMI_TRUCK, vin = "someVinNumber")
        val updatedTruck = builder.user.trucks.update(truck1.id!!, updateData)
        assertNotNull(updatedTruck)
        assertEquals(updateData.plateNumber, updatedTruck.plateNumber)
        assertEquals(updateData.type, updatedTruck.type)
        assertEquals(updateData.vin, updatedTruck.vin)

        // Truck updates check for the plate number duplicates (ignoring own number)
        builder.user.trucks.update(truck1.id, updatedTruck)
        builder.user.trucks.assertUpdateFail(400, truck1.id, truck2)

        // Trailer updates check for plate number duplicates too
        val towable = builder.user.towables.create(Towable("trailerNumber", vin = "001", type =   Towable.Type.TRAILER))
        builder.user.towables.assertUpdateFail(
            400,
            towable.id!!,
            Towable(plateNumber = updatedTruck.plateNumber, vin = "001", type = Towable.Type.TRAILER)
        )

        // Same checks for vin
        val someNewNumber = "someNewNumber"
        builder.user.trucks.assertUpdateFail(
            400,
            truck1.id,
            Truck(plateNumber = someNewNumber, type = Truck.Type.SEMI_TRUCK, vin = truck2.vin)
        )
        builder.user.towables.assertUpdateFail(
            400,
            towable.id,
            Towable(plateNumber = someNewNumber, type = Towable.Type.TRAILER, vin = truck2.vin)
        )
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use { builder ->
        val createdTruck = builder.user.trucks.create()
        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}",
            method = Method.PUT,
            token = builder.user.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "truckId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdTruck.id,
                    expectedStatus = 404
                )
            )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.Trucks.INVALID_TRUCKS,//todo vin must be checked too
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testDelete() = createTestBuilder().use { builder ->
        val createdTruck = builder.user.trucks.create()
        builder.user.trucks.delete(createdTruck.id!!)
        builder.user.trucks.assertFindFail(404, createdTruck.id)
    }

    @Test
    fun testDeleteFail() = createTestBuilder().use { builder ->
        val createdTruck = builder.user.trucks.create()
        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}",
            method = Method.DELETE,
            token = builder.user.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "truckId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdTruck.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

}
package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.test.client.models.Towable
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.vehiclemanagement.test.functional.impl.InvalidTestValues
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Test class for testing Trucks API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class TruckTestIT : AbstractFunctionalTest() {

    @Test
    fun testList() = createTestBuilder().use { builder ->
        builder.manager.trucks.create(plateNumber = plateNumber, vin = "001")
        builder.manager.trucks.create(plateNumber = "DEF-456", vin = "002")
        builder.manager.trucks.create(plateNumber = "GHI-789", vin = "003")
        val totalList = builder.manager.trucks.list()
        assertEquals(3, totalList.size)

        val pagedList = builder.manager.trucks.list(firstResult = 1, maxResults = 1)
        assertEquals(1, pagedList.size)

        val pagedList2 = builder.manager.trucks.list(firstResult = 0, maxResults = 3)
        assertEquals(3, pagedList2.size)

        val pagedList3 = builder.manager.trucks.list(firstResult = 0, maxResults = 2)
        assertEquals(2, pagedList3.size)

        val pagedList4 = builder.manager.trucks.list(firstResult = 0, maxResults = 0)
        assertEquals(0, pagedList4.size)

        val filteredList = builder.manager.trucks.list(plateNumber = plateNumber)
        assertEquals(1, filteredList.size)
    }

    @Test
    fun testListFail(): Unit = createTestBuilder().use {
        it.user.trucks.assertListFail(403)
        assertNotNull(it.driver.trucks.list())
        assertNotNull(it.manager.trucks.list())
    }

    @Test
    fun testCreate() = createTestBuilder().use { builder ->
        val truckData = Truck(plateNumber = plateNumber, type = Truck.Type.TRUCK, vin = "someVinNumber")
        val createdTruck = builder.manager.trucks.create(truckData)
        assertNotNull(createdTruck)
        assertNotNull(createdTruck.id)
        assertNotNull(createdTruck.createdAt)
        assertEquals(truckData.plateNumber, createdTruck.plateNumber)
        assertEquals(truckData.type, createdTruck.type)
        assertEquals(truckData.vin, createdTruck.vin)

        // We cannot create trucks or towables with already existing plate number
        builder.manager.trucks.assertCreateFail(400, createdTruck)
        builder.manager.towables.assertCreateFail(400, Towable(plateNumber = plateNumber, vin = "003", type = Towable.Type.TRAILER))
        // Same check for vin
        builder.manager.trucks.assertCreateFail(
            400,
            Truck(plateNumber = "DEF-456", type = Truck.Type.TRUCK, vin = createdTruck.vin)
        )
        builder.manager.towables.assertCreateFail(
            400,
            Towable(plateNumber = "DEF-456", type = Towable.Type.TRAILER, vin = createdTruck.vin)
        )
    }

    @Test
    fun testCreateFail() = createTestBuilder().use { builder ->
        val truckData = Truck(plateNumber = plateNumber, type = Truck.Type.TRUCK, vin = "someVinNumber")

        builder.user.trucks.assertCreateFail(403, truckData)
        builder.driver.trucks.assertCreateFail(403, truckData)

        InvalidValueTestScenarioBuilder(
            path = "v1/trucks",
            method = Method.POST,
            token = builder.manager.accessTokenProvider.accessToken,
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
        val createdTruck = builder.manager.trucks.create(truckData)
        assertNotNull(createdTruck)
        assertEquals(truckData.plateNumber, createdTruck.plateNumber)
    }

    @Test
    fun testFindFail() = createTestBuilder().use { builder ->
        val createdTruck = builder.manager.trucks.create()

        builder.user.trucks.assertFindFail(403, createdTruck.id!!)
        assertNotNull(builder.driver.trucks.find(createdTruck.id))

        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}",
            method = Method.GET,
            token = builder.manager.accessTokenProvider.accessToken,
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
        val truck1 = builder.manager.trucks.create(Truck(plateNumber = "0111", type = Truck.Type.SEMI_TRUCK, vin = "vin1"))
        val truck2 = builder.manager.trucks.create(Truck(plateNumber = "0222", type = Truck.Type.SEMI_TRUCK, vin = "vin2"))

        val updateData = Truck(plateNumber = "DEF-456", type = Truck.Type.SEMI_TRUCK, vin = "someVinNumber")
        val updatedTruck = builder.manager.trucks.update(truck1.id!!, updateData)
        assertNotNull(updatedTruck)
        assertEquals(updateData.plateNumber, updatedTruck.plateNumber)
        assertEquals(updateData.type, updatedTruck.type)
        assertEquals(updateData.vin, updatedTruck.vin)

        // Truck updates check for the plate number duplicates (ignoring own number)
        builder.manager.trucks.update(truck1.id, updatedTruck)
        builder.manager.trucks.assertUpdateFail(400, truck1.id, truck2)

        // Trailer updates check for plate number duplicates too
        val towable = builder.manager.towables.create(Towable("trailerNumber", vin = "001", type =   Towable.Type.TRAILER))
        builder.manager.towables.assertUpdateFail(
            400,
            towable.id!!,
            Towable(plateNumber = updatedTruck.plateNumber, vin = "001", type = Towable.Type.TRAILER)
        )

        // Same checks for vin
        val someNewNumber = "someNewNumber"
        builder.manager.trucks.assertUpdateFail(
            400,
            truck1.id,
            Truck(plateNumber = someNewNumber, type = Truck.Type.SEMI_TRUCK, vin = truck2.vin)
        )
        builder.manager.towables.assertUpdateFail(
            400,
            towable.id,
            Towable(plateNumber = someNewNumber, type = Towable.Type.TRAILER, vin = truck2.vin)
        )
    }

    @Test
    fun testArchiving() = createTestBuilder().use { builder ->
        val createdTruck = builder.manager.trucks.create()
        var total = builder.manager.trucks.list()
        assertEquals(1, total.size)

        //archiving
        val archived = builder.manager.trucks.update(
            truckId = createdTruck.id!!,
            updateData = createdTruck.copy(archivedAt = createdTruck.createdAt)
        )
        assertNotNull(archived.archivedAt)
        total = builder.manager.trucks.list()
        assertEquals(0, total.size)
        val totalUnarchived = builder.manager.trucks.list(archived = false)
        assertEquals(0, totalUnarchived.size)
        val totalArchived = builder.manager.trucks.list(archived = true)
        assertEquals(1, totalArchived.size)

        //cannot update archived data
        builder.manager.trucks.assertUpdateFail(
            409,
            createdTruck.id,
            archived
        )

        //can un-archive truck
        val unarchived = builder.manager.trucks.update(
            truckId = createdTruck.id,
            updateData = archived.copy(archivedAt = null)
        )
        Assertions.assertNull(unarchived.archivedAt)
        total = builder.manager.trucks.list()
        assertEquals(1, total.size)
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use { builder ->
        val createdTruck = builder.manager.trucks.create()

        builder.user.trucks.assertUpdateFail(403, createdTruck.id!!, createdTruck)
        builder.driver.trucks.assertUpdateFail(403, createdTruck.id, createdTruck)

        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}",
            method = Method.PUT,
            token = builder.manager.accessTokenProvider.accessToken,
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
                    values = InvalidTestValues.Trucks.INVALID_TRUCKS,
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testDelete() = createTestBuilder().use { builder ->
        val createdTruck = builder.manager.trucks.create()
        builder.manager.trucks.delete(createdTruck.id!!)
        builder.manager.trucks.assertFindFail(404, createdTruck.id)
    }

    @Test
    fun testDeleteFail() = createTestBuilder().use { builder ->
        val createdTruck = builder.manager.trucks.create()

        builder.user.trucks.assertDeleteFail(403, createdTruck.id!!)
        builder.driver.trucks.assertDeleteFail(403, createdTruck.id)

        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}",
            method = Method.DELETE,
            token = builder.manager.accessTokenProvider.accessToken,
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
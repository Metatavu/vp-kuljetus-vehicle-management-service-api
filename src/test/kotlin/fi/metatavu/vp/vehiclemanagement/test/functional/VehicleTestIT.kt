package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.test.client.models.Vehicle
import fi.metatavu.vp.vehiclemanagement.test.functional.impl.InvalidTestValues
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class VehicleTestIT : AbstractFunctionalTest() {

    @Test
    fun testList() = createTestBuilder().use { builder ->
        val truck1 = builder.manager.trucks.create(builder.manager.vehicles)
        val truck2 = builder.manager.trucks.create("DEF-457", "002", null, null, builder.manager.vehicles)

        val towable1 = builder.manager.towables.create(plateNumber = "DEF-456", vin = "003")
        val towable2 = builder.manager.towables.create(plateNumber = "GHI-789", vin = "004")
        val towable3 = builder.manager.towables.create(plateNumber = "JKL-012", vin = "005")

        builder.manager.vehicles.create(
            truckId = truck1.id!!,
            towableIds = arrayOf(towable1.id!!, towable2.id!!)
        )
        builder.manager.vehicles.create(
            truckId = truck2.id!!,
            towableIds = arrayOf(towable3.id!!)
        )

        val totalList = builder.manager.vehicles.list()
        assertEquals(2, totalList.size)

        val pagedList = builder.manager.vehicles.list(firstResult = 1, maxResults = 1)
        assertEquals(1, pagedList.size)

        val pagedList2 = builder.manager.vehicles.list(firstResult = 0, maxResults = 3)
        assertEquals(2, pagedList2.size)

        val pagedList3 = builder.manager.vehicles.list(firstResult = 0, maxResults = 2)
        assertEquals(2, pagedList3.size)

        val pagedList4 = builder.manager.vehicles.list(firstResult = 0, maxResults = 0)
        assertEquals(0, pagedList4.size)

        val filteredList = builder.manager.vehicles.list(truckId = truck1.id)
        assertEquals(1, filteredList.size)
    }
    @Test
    fun testListFail(): Unit = createTestBuilder().use {
        it.user.vehicles.assertListFail(403)
        assertNotNull(it.driver.vehicles.list())
        assertNotNull(it.manager.vehicles.list())
    }

    @Test
    fun testCreate() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val towable1 = it.manager.towables.create(plateNumber = "DEF-456", vin = "002")
        val towable2 = it.manager.towables.create(plateNumber = "GHI-789", vin = "003")

        val createdVehicle = it.manager.vehicles.create(
            towableIds = arrayOf(towable1.id!!, towable2.id!!),
            truckId = truck.id!!
        )

        assertNotNull(createdVehicle)
        assertNotNull(createdVehicle.createdAt)
        assertEquals(truck.id, createdVehicle.truckId)
        assertEquals(2, createdVehicle.towableIds.size)
        assertEquals(towable1.id, createdVehicle.towableIds[0])
        assertEquals(towable2.id, createdVehicle.towableIds[1])

        // cannot delete towables or trucks connected to the vehicle
        it.manager.trucks.assertDeleteFail(400, truck.id)
        it.manager.towables.assertDeleteFail(400, towable1.id)
        it.manager.towables.assertDeleteFail(400, towable2.id)

        // Check that drivers can create vehicles but not delete them
        val created2 = it.driver.vehicles.create(
            truckId = truck.id,
            towableIds = emptyArray()
        )
        it.manager.vehicles.delete(created2.id!!)
    }

    /**
     * Test invalid cases for creating vehicles
     */
    @Test
    fun testCreateFail() = createTestBuilder().use {
        val truck1 = it.manager.trucks.create(it.manager.vehicles)
        val towable1 = it.manager.towables.create(plateNumber = "DEF-456", vin = "002")
        val towable2 = it.manager.towables.create(plateNumber = "GHI-789", vin = "003")

        val vehicleData = Vehicle(
            truckId = truck1.id!!,
            towableIds = arrayOf(towable1.id!!, towable2.id!!)
        )
        it.user.vehicles.assertCreateFail(403, vehicleData)

        InvalidValueTestScenarioBuilder(
            path = "v1/vehicles",
            method = Method.POST,
            token = it.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .body(
                InvalidValueTestScenarioBody(
                    values = listOf(
                        // invalid truck id
                        InvalidTestValues.Vehicles.createVehicle(
                            truckId = UUID.randomUUID(),
                            towableIds = arrayOf(towable1.id, towable2.id)
                        ),
                        // invalid towable id
                        InvalidTestValues.Vehicles.createVehicle(
                            truckId = truck1.id,
                            towableIds = arrayOf(UUID.randomUUID(), towable2.id)
                        ),
                        // duplicate towable id
                        InvalidTestValues.Vehicles.createVehicle(
                            truckId = truck1.id,
                            towableIds = arrayOf(towable1.id, towable1.id)
                        ),
                    ),
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testFind() = createTestBuilder().use { builder ->
        val truck = builder.manager.trucks.create(builder.manager.vehicles)
        val towable1 = builder.manager.towables.create(plateNumber = "DEF-456", vin = "002")
        val towable2 = builder.manager.towables.create(plateNumber = "GHI-789", vin = "003")
        val createdVehicle = builder.manager.vehicles.create(
            towableIds = arrayOf(towable1.id!!, towable2.id!!),
            truckId = truck.id!!
        )

        val foundVehicle = builder.manager.vehicles.find(createdVehicle.id!!)
        assertNotNull(foundVehicle)
        assertEquals(truck.id, foundVehicle.truckId)
        assertEquals(2, foundVehicle.towableIds.size)
        assertTrue(foundVehicle.towableIds.contains(towable1.id))
        assertTrue(foundVehicle.towableIds.contains(towable2.id))
    }

    @Test
    fun testFindFail() = createTestBuilder().use { builder ->
        val truck = builder.manager.trucks.create(builder.manager.vehicles)
        val towable1 = builder.manager.towables.create(plateNumber = "DEF-456", vin = "002")
        val createdVehicle = builder.manager.vehicles.create(
            towableIds = arrayOf(towable1.id!!),
            truckId = truck.id!!
        )

        builder.user.vehicles.assertFindFail(403, createdVehicle.id!!)
        assertNotNull(builder.driver.vehicles.find(createdVehicle.id))
        assertNotNull(builder.manager.vehicles.find(createdVehicle.id))
        InvalidValueTestScenarioBuilder(
            path = "v1/vehicles/{vehicleId}",
            method = Method.GET,
            token = builder.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "vehicleId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdVehicle.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

    @Test
    fun testArchiving() = createTestBuilder().use { builder ->
        val createdTruck = builder.manager.trucks.create(builder.manager.vehicles)
        val total = builder.manager.vehicles.list()
        assertEquals(1, total.size)
        val totalUnarchived = builder.manager.vehicles.list(archived = false)
        assertEquals(1, totalUnarchived.size)

        // Assign the truck to new vehicle
        val newVehicle = builder.manager.vehicles.create(
            towableIds = emptyArray(),
            truckId = createdTruck.id!!
        )
        assertNull(newVehicle.archivedAt)

        // Check that truck is assigned to new vehicle
        val foundReAssignedTruck = builder.manager.trucks.find(createdTruck.id)
        assertEquals(newVehicle.id, foundReAssignedTruck.activeVehicleId)

        // Check that we still have 1 active vehicle
        val totalUnarchived1 = builder.manager.vehicles.list()
        assertEquals(1, totalUnarchived1.size)

        // Check that original vehicle assigned to truck got archived
        val totalArchived = builder.manager.vehicles.list(archived = true)
        assertEquals(1, totalArchived.size)
        assertEquals(createdTruck.activeVehicleId, totalArchived[0].id)
    }

    @Test
    fun testDelete() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)

        val createdVehicle = it.manager.vehicles.create(
            truckId = truck.id!!,
            towableIds = emptyArray()
        )

        it.manager.vehicles.delete(createdVehicle.id!!)
        val totalList = it.manager.vehicles.list()
        assertEquals(0, totalList.size)
    }

    @Test
    fun testDeleteFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)

        val createdVehicle = it.manager.vehicles.create(
            truckId = truck.id!!,
            towableIds = emptyArray()
        )

        it.user.vehicles.assertDeleteFail(403, createdVehicle.id!!)
        it.driver.vehicles.assertDeleteFail(403, createdVehicle.id)

        InvalidValueTestScenarioBuilder(
            path = "v1/vehicles/{vehicleId}",
            method = Method.DELETE,
            token = it.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "vehicleId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdVehicle.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }
}
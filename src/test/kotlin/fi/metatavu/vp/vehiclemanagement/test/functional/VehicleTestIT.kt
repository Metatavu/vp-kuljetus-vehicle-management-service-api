package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.test.client.models.Vehicle
import fi.metatavu.vp.vehiclemanagement.test.functional.impl.InvalidTestValues
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
class VehicleTestIT : AbstractFunctionalTest() {

    @Test
    fun testList() = createTestBuilder().use { builder ->
        val truck1 = builder.user.trucks.create()
        val truck2 = builder.user.trucks.create("DEF-457", "002")

        val towable1 = builder.user.towables.create(plateNumber = "DEF-456", vin = "003")
        val towable2 = builder.user.towables.create(plateNumber = "GHI-789", vin = "004")
        val towable3 = builder.user.towables.create(plateNumber = "JKL-012", vin = "005")

        builder.user.vehicles.create(
            truckId = truck1.id!!,
            towableIds = arrayOf(towable1.id!!, towable2.id!!)
        )
        builder.user.vehicles.create(
            truckId = truck2.id!!,
            towableIds = arrayOf(towable3.id!!)
        )

        val totalList = builder.user.vehicles.list()
        assertEquals(2, totalList.size)

        val pagedList = builder.user.vehicles.list(firstResult = 1, maxResults = 1)
        assertEquals(1, pagedList.size)

        val pagedList2 = builder.user.vehicles.list(firstResult = 0, maxResults = 3)
        assertEquals(2, pagedList2.size)

        val pagedList3 = builder.user.vehicles.list(firstResult = 0, maxResults = 2)
        assertEquals(2, pagedList3.size)

        val pagedList4 = builder.user.vehicles.list(firstResult = 0, maxResults = 0)
        assertEquals(0, pagedList4.size)

        val filteredList = builder.user.vehicles.list(truckId = truck1.id)
        assertEquals(1, filteredList.size)
    }

    @Test
    fun testCreate() = createTestBuilder().use {
        val truck = it.user.trucks.create()
        val towable1 = it.user.towables.create(plateNumber = "DEF-456", vin = "002")
        val towable2 = it.user.towables.create(plateNumber = "GHI-789", vin = "003")

        val createdVehicle = it.user.vehicles.create(
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
        it.user.trucks.assertDeleteFail(400, truck.id)
        it.user.towables.assertDeleteFail(400, towable1.id)
        it.user.towables.assertDeleteFail(400, towable2.id)
    }

    /**
     * Test invalid cases for creating vehicles
     */
    @Test
    fun testCreateFail() = createTestBuilder().use {
        val truck1 = it.user.trucks.create()
        val towable1 = it.user.towables.create(plateNumber = "DEF-456", vin = "002")
        val towable2 = it.user.towables.create(plateNumber = "GHI-789", vin = "003")

        InvalidValueTestScenarioBuilder(
            path = "v1/vehicles",
            method = Method.POST,
            token = it.user.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .body(
                InvalidValueTestScenarioBody(
                    values = listOf(
                        // invalid truck id
                        InvalidTestValues.Vehicles.createVehicle(
                            truckId = UUID.randomUUID(),
                            towableIds = arrayOf(towable1.id!!, towable2.id!!)
                        ),
                        // invalid towable id
                        InvalidTestValues.Vehicles.createVehicle(
                            truckId = truck1.id!!,
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
        val truck = builder.user.trucks.create()
        val towable1 = builder.user.towables.create(plateNumber = "DEF-456", vin = "002")
        val towable2 = builder.user.towables.create(plateNumber = "GHI-789", vin = "003")
        val createdVehicle = builder.user.vehicles.create(
            towableIds = arrayOf(towable1.id!!, towable2.id!!),
            truckId = truck.id!!
        )

        val foundVehicle = builder.user.vehicles.find(createdVehicle.id!!)
        assertNotNull(foundVehicle)
        assertEquals(truck.id, foundVehicle.truckId)
        assertEquals(2, foundVehicle.towableIds.size)
        assertTrue(foundVehicle.towableIds.contains(towable1.id))
        assertTrue(foundVehicle.towableIds.contains(towable2.id))
    }

    @Test
    fun testFindFail() = createTestBuilder().use { builder ->
        val truck = builder.user.trucks.create()
        val towable1 = builder.user.towables.create(plateNumber = "DEF-456", vin = "002")
        val createdVehicle = builder.user.vehicles.create(
            towableIds = arrayOf(towable1.id!!),
            truckId = truck.id!!
        )

        InvalidValueTestScenarioBuilder(
            path = "v1/vehicles/{vehicleId}",
            method = Method.GET,
            token = builder.user.accessTokenProvider.accessToken,
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
    fun testUpdate() = createTestBuilder().use {
        val truck = it.user.trucks.create()
        val towable1 = it.user.towables.create(plateNumber = "DEF-456", vin = "002")
        val towable2 = it.user.towables.create(plateNumber = "GHI-789", vin = "003")
        val towable3 = it.user.towables.create(plateNumber = "JKL-012", vin = "004")

        val createdVehicle = it.user.vehicles.create(
            towableIds = arrayOf(towable1.id!!, towable2.id!!),
            truckId = truck.id!!
        )

        val reorderedTowables = it.user.vehicles.update(
            existingVehicle = createdVehicle,
            newVehicleData = Vehicle(
                towableIds = arrayOf(towable2.id, towable1.id),
                truckId = truck.id
            )
        )
        assertEquals(2, reorderedTowables.towableIds.size)
        assertEquals(towable2.id, reorderedTowables.towableIds[0])
        assertEquals(towable1.id, reorderedTowables.towableIds[1])

        val removedTowable = it.user.vehicles.update(
            existingVehicle = reorderedTowables,
            newVehicleData = Vehicle(
                towableIds = arrayOf(towable2.id),
                truckId = truck.id
            )
        )
        assertEquals(1, removedTowable.towableIds.size)
        assertEquals(towable2.id, removedTowable.towableIds[0])

        val differentTowables = it.user.vehicles.update(
            existingVehicle = removedTowable,
            newVehicleData = Vehicle(
                towableIds = arrayOf(towable3.id!!),
                truckId = truck.id
            )
        )

        assertEquals(1, differentTowables.towableIds.size)
        assertEquals(towable3.id, differentTowables.towableIds[0])
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use {
        val truck = it.user.trucks.create()
        val towable1 = it.user.towables.create(plateNumber = "DEF-456", vin = "002")
        it.user.towables.create(plateNumber = "GHI-789", vin = "003")
        it.user.towables.create(plateNumber = "JKL-012", vin = "004")

        val createdVehicle = it.user.vehicles.create(
            towableIds = arrayOf(towable1.id!!),
            truckId = truck.id!!
        )

        InvalidValueTestScenarioBuilder(
            path = "v1/vehicles/{vehicleId}",
            method = Method.PUT,
            token = it.user.accessTokenProvider.accessToken,
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
            .body(
                InvalidValueTestScenarioBody(
                    values = listOf(
                        // invalid truck id
                        InvalidTestValues.Vehicles.createVehicle(
                            truckId = UUID.randomUUID(),
                            towableIds = arrayOf(towable1.id)
                        ),
                        // invalid towable id
                        InvalidTestValues.Vehicles.createVehicle(
                            truckId = truck.id,
                            towableIds = arrayOf(UUID.randomUUID())
                        ),
                    ),
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testDelete() = createTestBuilder().use {
        val truck = it.user.trucks.create()

        val createdVehicle = it.user.vehicles.create(
            truckId = truck.id!!,
            towableIds = emptyArray()
        )

        it.user.vehicles.delete(createdVehicle.id!!)
        val totalList = it.user.vehicles.list()
        assertEquals(0, totalList.size)
    }

    @Test
    fun testDeleteFail() = createTestBuilder().use {
        val truck = it.user.trucks.create()

        val createdVehicle = it.user.vehicles.create(
            truckId = truck.id!!,
            towableIds = emptyArray()
        )

        InvalidValueTestScenarioBuilder(
            path = "v1/vehicles/{vehicleId}",
            method = Method.DELETE,
            token = it.user.accessTokenProvider.accessToken,
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
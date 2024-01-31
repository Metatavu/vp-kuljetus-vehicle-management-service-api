package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.vp.test.client.models.Vehicle
import fi.metatavu.vp.vehiclemanagement.test.functional.common.InvalidValueTestScenarioBody
import fi.metatavu.vp.vehiclemanagement.test.functional.common.InvalidValueTestScenarioBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.common.InvalidValueTestScenarioPath
import fi.metatavu.vp.vehiclemanagement.test.functional.common.InvalidValues
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
class VehicleTestIT : AbstractFunctionalTest() {

    @Test
    fun list() {
        createTestBuilder().use { builder ->
            val truck1 = builder.user.trucks.create()
            val truck2 = builder.user.trucks.create()

            val towable1 = builder.user.towables.create(plateNumber = "DEF-456")
            val towable2 = builder.user.towables.create(plateNumber = "GHI-789")
            val towable3 = builder.user.towables.create(plateNumber = "JKL-012")

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
    }

    @Test
    fun create() {
        createTestBuilder().use {
            val truck = it.user.trucks.create()
            val towable1 = it.user.towables.create(plateNumber = "DEF-456")
            val towable2 = it.user.towables.create(plateNumber = "GHI-789")

            val createdVehicle = it.user.vehicles.create(
                towableIds = arrayOf(towable1.id!!, towable2.id!!),
                truckId = truck.id!!
            )

            assertNotNull(createdVehicle)
            assertEquals(truck.id, createdVehicle.truckId)
            assertEquals(2, createdVehicle.towableIds.size)
            assertEquals(towable1.id, createdVehicle.towableIds[0])
            assertEquals(towable2.id, createdVehicle.towableIds[1])

            // cannot delete towables or trucks connected to the vehicle
            it.user.trucks.assertDeleteFail(400, truck.id)
            it.user.towables.assertDeleteFail(400, towable1.id)
            it.user.towables.assertDeleteFail(400, towable2.id)
        }
    }

    /**
     * Test invalid cases for creating vehicles
     */
    @Test
    fun createFail() {
        createTestBuilder().use {
            val truck1 = it.user.trucks.create()
            val towable1 = it.user.towables.create(plateNumber = "DEF-456")
            val towable2 = it.user.towables.create(plateNumber = "GHI-789")
            val towable3 = it.user.towables.create(plateNumber = "JKL-012")

            InvalidValueTestScenarioBuilder(
                path = "v1/vehicles",
                method = Method.POST,
                token = it.user.accessTokenProvider.accessToken
            )
                .body(
                    InvalidValueTestScenarioBody(
                        values = listOf(
                            // invalid truck id
                            InvalidValues.Vehicles.createVehicle(
                                truckId = UUID.randomUUID(),
                                towableIds = arrayOf(towable1.id!!, towable2.id!!)
                            ),
                            // invalid towable id
                            InvalidValues.Vehicles.createVehicle(
                                truckId = truck1.id!!,
                                towableIds = arrayOf(UUID.randomUUID(), towable2.id)
                            ),
                            // duplicate towable id
                            InvalidValues.Vehicles.createVehicle(
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
    }

    @Test
    fun find() {
        createTestBuilder().use { builder ->
            val truck = builder.user.trucks.create()
            val towable1 = builder.user.towables.create(plateNumber = "DEF-456")
            val towable2 = builder.user.towables.create(plateNumber = "GHI-789")
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
    }

    @Test
    fun findFail() {
        createTestBuilder().use { builder ->
            val truck = builder.user.trucks.create()
            val towable1 = builder.user.towables.create(plateNumber = "DEF-456")
            val createdVehicle = builder.user.vehicles.create(
                towableIds = arrayOf(towable1.id!!),
                truckId = truck.id!!
            )

            InvalidValueTestScenarioBuilder(
                path = "v1/vehicles/{vehicleId}",
                method = Method.GET,
                token = builder.user.accessTokenProvider.accessToken
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

    @Test
    fun update() {
        createTestBuilder().use {
            val truck = it.user.trucks.create()
            val towable1 = it.user.towables.create(plateNumber = "DEF-456")
            val towable2 = it.user.towables.create(plateNumber = "GHI-789")
            val towable3 = it.user.towables.create(plateNumber = "JKL-012")

            val createdVehicle = it.user.vehicles.create(
                towableIds = arrayOf(towable1.id!!, towable2.id!!),
                truckId = truck.id!!
            )

            val reorderedTrailers = it.user.vehicles.update(
                existingVehicle = createdVehicle,
                newVehicleData = Vehicle(
                    towableIds = arrayOf(towable2.id, towable1.id),
                    truckId = truck.id
                )
            )
            assertEquals(2, reorderedTrailers.towableIds.size)
            assertEquals(towable2.id, reorderedTrailers.towableIds[0])
            assertEquals(towable1.id, reorderedTrailers.towableIds[1])

            val removedTrailer = it.user.vehicles.update(
                existingVehicle = reorderedTrailers,
                newVehicleData = Vehicle(
                    towableIds = arrayOf(towable2.id),
                    truckId = truck.id
                )
            )
            assertEquals(1, removedTrailer.towableIds.size)
            assertEquals(towable2.id, removedTrailer.towableIds[0])

            val differentTrailers = it.user.vehicles.update(
                existingVehicle = removedTrailer,
                newVehicleData = Vehicle(
                    towableIds = arrayOf(towable3.id!!),
                    truckId = truck.id
                )
            )

            assertEquals(1, differentTrailers.towableIds.size)
            assertEquals(towable3.id, differentTrailers.towableIds[0])
        }
    }

    @Test
    fun updateFail() {
        createTestBuilder().use {
            val truck = it.user.trucks.create()
            val towable1 = it.user.towables.create(plateNumber = "DEF-456")
            val towable2 = it.user.towables.create(plateNumber = "GHI-789")
            val towable3 = it.user.towables.create(plateNumber = "JKL-012")

            val createdVehicle = it.user.vehicles.create(
                towableIds = arrayOf(towable1.id!!),
                truckId = truck.id!!
            )

            InvalidValueTestScenarioBuilder(
                path = "v1/vehicles/{vehicleId}",
                method = Method.PUT,
                token = it.user.accessTokenProvider.accessToken
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
                            InvalidValues.Vehicles.createVehicle(
                                truckId = UUID.randomUUID(),
                                towableIds = arrayOf(towable1.id)
                            ),
                            // invalid towable id
                            InvalidValues.Vehicles.createVehicle(
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
    }

    @Test
    fun delete() {
        createTestBuilder().use {
            val truck = it.user.trucks.create()

            val createdVehicle = it.user.vehicles.create(
                truckId = truck.id!!,
                towableIds = emptyArray()
            )

            it.user.vehicles.delete(createdVehicle.id!!)
            val totalList = it.user.vehicles.list()
            assertEquals(0, totalList.size)
        }
    }

    @Test
    fun deleteFail() {
        createTestBuilder().use {
            val truck = it.user.trucks.create()

            val createdVehicle = it.user.vehicles.create(
                truckId = truck.id!!,
                towableIds = emptyArray()
            )

            InvalidValueTestScenarioBuilder(
                path = "v1/vehicles/{vehicleId}",
                method = Method.DELETE,
                token = it.user.accessTokenProvider.accessToken
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
}
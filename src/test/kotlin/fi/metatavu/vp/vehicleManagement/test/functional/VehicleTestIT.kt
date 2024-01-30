package fi.metatavu.vp.vehicleManagement.test.functional

import fi.metatavu.vp.test.client.models.Trailer
import fi.metatavu.vp.test.client.models.Vehicle
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioBody
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioBuilder
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioPath
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValues
import fi.metatavu.vp.vehicleManagement.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class)
)
class VehicleTestIT : AbstractFunctionalTest() {

    @Test
    fun list() {
        createTestBuilder().use { builder ->
            val truck1 = builder.user.trucks.create()
            val truck2 = builder.user.trucks.create()

            val trailer1 = builder.user.trailers.create(Trailer(plateNumber = "DEF-456"))
            val trailer2 = builder.user.trailers.create(Trailer(plateNumber = "GHI-789"))
            val trailer3 = builder.user.trailers.create(Trailer(plateNumber = "JKL-012"))

            builder.user.vehicles.create(
                truckId = truck1.id!!,
                trailerIds = arrayOf(trailer1.id!!, trailer2.id!!)
            )
            builder.user.vehicles.create(
                truckId = truck2.id!!,
                trailerIds = arrayOf(trailer3.id!!)
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
            val trailer1 = it.user.trailers.create(Trailer(plateNumber = "DEF-456"))
            val trailer2 = it.user.trailers.create(Trailer(plateNumber = "GHI-789"))

            val createdVehicle = it.user.vehicles.create(
                trailerIds = arrayOf(trailer1.id!!, trailer2.id!!),
                truckId = truck.id!!
            )

            assertNotNull(createdVehicle)
            assertEquals(truck.id, createdVehicle.truckId)
            assertEquals(2, createdVehicle.trailerIds.size)
            assertEquals(trailer1.id, createdVehicle.trailerIds[0])
            assertEquals(trailer2.id, createdVehicle.trailerIds[1])

            // cannot delete trailers or trucks connected to the vehicle
            it.user.trucks.assertDeleteFail(400, truck.id)
            it.user.trailers.assertDeleteFail(400, trailer1.id)
            it.user.trailers.assertDeleteFail(400, trailer2.id)
        }
    }

    /**
     * Test invalid cases for creating vehicles
     */
    @Test
    fun createFail() {
        createTestBuilder().use {
            val truck1 = it.user.trucks.create()
            val trailer1 = it.user.trailers.create(Trailer(plateNumber = "DEF-456"))
            val trailer2 = it.user.trailers.create(Trailer(plateNumber = "GHI-789"))
            val trailer3 = it.user.trailers.create(Trailer(plateNumber = "JKL-012"))

            InvalidValueTestScenarioBuilder(
                path = "v1/vehicles",
                method = Method.POST,
                token = it.user.accessTokenProvider.accessToken
            )
                .body(
                    InvalidValueTestScenarioBody(
                        values = listOf(
                            // too many trailers
                            InvalidValues.Vehicles.createVehicle(
                                truckId = truck1.id!!,
                                trailerIds = arrayOf(trailer1.id!!, trailer2.id!!, trailer3.id!!)
                            ),
                            // invalid truck id
                            InvalidValues.Vehicles.createVehicle(
                                truckId = UUID.randomUUID(),
                                trailerIds = arrayOf(trailer1.id, trailer2.id)
                            ),
                            // invalid trailer id
                            InvalidValues.Vehicles.createVehicle(
                                truckId = truck1.id,
                                trailerIds = arrayOf(UUID.randomUUID(), trailer2.id)
                            ),
                            // duplicate trailer id
                            InvalidValues.Vehicles.createVehicle(
                                truckId = truck1.id,
                                trailerIds = arrayOf(trailer1.id, trailer1.id)
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
            val trailer1 = builder.user.trailers.create(Trailer(plateNumber = "DEF-456"))
            val trailer2 = builder.user.trailers.create(Trailer(plateNumber = "GHI-789"))
            val createdVehicle = builder.user.vehicles.create(
                trailerIds = arrayOf(trailer1.id!!, trailer2.id!!),
                truckId = truck.id!!
            )

            val foundVehicle = builder.user.vehicles.find(createdVehicle.id!!)
            assertNotNull(foundVehicle)
            assertEquals(truck.id, foundVehicle.truckId)
            assertEquals(2, foundVehicle.trailerIds.size)
            assertTrue(foundVehicle.trailerIds.contains(trailer1.id))
            assertTrue(foundVehicle.trailerIds.contains(trailer2.id))
        }
    }

    @Test
    fun findFail() {
        createTestBuilder().use { builder ->
            val truck = builder.user.trucks.create()
            val trailer1 = builder.user.trailers.create(Trailer(plateNumber = "DEF-456"))
            val createdVehicle = builder.user.vehicles.create(
                trailerIds = arrayOf(trailer1.id!!),
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
            val trailer1 = it.user.trailers.create(Trailer(plateNumber = "DEF-456"))
            val trailer2 = it.user.trailers.create(Trailer(plateNumber = "GHI-789"))
            val trailer3 = it.user.trailers.create(Trailer(plateNumber = "JKL-012"))

            val createdVehicle = it.user.vehicles.create(
                trailerIds = arrayOf(trailer1.id!!, trailer2.id!!),
                truckId = truck.id!!
            )

            val reorderedTrailers = it.user.vehicles.update(
                existingVehicle = createdVehicle,
                newVehicleData = Vehicle(
                    trailerIds = arrayOf(trailer2.id, trailer1.id),
                    truckId = truck.id
                )
            )
            assertEquals(2, reorderedTrailers.trailerIds.size)
            assertEquals(trailer2.id, reorderedTrailers.trailerIds[0])
            assertEquals(trailer1.id, reorderedTrailers.trailerIds[1])

            val removedTrailer = it.user.vehicles.update(
                existingVehicle = reorderedTrailers,
                newVehicleData = Vehicle(
                    trailerIds = arrayOf(trailer2.id),
                    truckId = truck.id
                )
            )
            assertEquals(1, removedTrailer.trailerIds.size)
            assertEquals(trailer2.id, removedTrailer.trailerIds[0])

            val differentTrailers = it.user.vehicles.update(
                existingVehicle = removedTrailer,
                newVehicleData = Vehicle(
                    trailerIds = arrayOf(trailer3.id!!),
                    truckId = truck.id
                )
            )

            assertEquals(1, differentTrailers.trailerIds.size)
            assertEquals(trailer3.id, differentTrailers.trailerIds[0])
        }
    }

    @Test
    fun updateFail() {
        createTestBuilder().use {
            val truck = it.user.trucks.create()
            val trailer1 = it.user.trailers.create(Trailer(plateNumber = "DEF-456"))
            val trailer2 = it.user.trailers.create(Trailer(plateNumber = "GHI-789"))
            val trailer3 = it.user.trailers.create(Trailer(plateNumber = "JKL-012"))

            val createdVehicle = it.user.vehicles.create(
                trailerIds = arrayOf(trailer1.id!!),
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
                            // too many trailers
                            InvalidValues.Vehicles.createVehicle(
                                truckId = truck.id,
                                trailerIds = arrayOf(trailer1.id, trailer2.id!!, trailer3.id!!)
                            ),
                            // invalid truck id
                            InvalidValues.Vehicles.createVehicle(
                                truckId = UUID.randomUUID(),
                                trailerIds = arrayOf(trailer1.id)
                            ),
                            // invalid trailer id
                            InvalidValues.Vehicles.createVehicle(
                                truckId = truck.id,
                                trailerIds = arrayOf(UUID.randomUUID())
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
                trailerIds = emptyArray()
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
                trailerIds = emptyArray()
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
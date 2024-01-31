package fi.metatavu.vp.vehicleManagement.test.functional

import fi.metatavu.vp.test.client.models.Trailer
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioBody
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioBuilder
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioPath
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValues
import fi.metatavu.vp.vehicleManagement.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Test class for testing Trucks API
 */
@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class)
)
class TruckTestIT: AbstractFunctionalTest() {

    @Test
    fun testList() {
        createTestBuilder().use { builder ->
            builder.user.trucks.create(Truck(plateNumber = plateNumber))
            builder.user.trucks.create(Truck(plateNumber = "DEF-456"))
            builder.user.trucks.create(Truck(plateNumber = "GHI-789"))
            val totalList = builder.user.trucks.list()
            Assertions.assertEquals(3, totalList.size)

            val pagedList = builder.user.trucks.list(firstResult = 1, maxResults = 1)
            Assertions.assertEquals(1, pagedList.size)

            val pagedList2 = builder.user.trucks.list(firstResult = 0, maxResults = 3)
            Assertions.assertEquals(3, pagedList2.size)

            val pagedList3 = builder.user.trucks.list(firstResult = 0, maxResults = 2)
            Assertions.assertEquals(2, pagedList3.size)

            val pagedList4 = builder.user.trucks.list(firstResult = 0, maxResults = 0)
            Assertions.assertEquals(0, pagedList4.size)

            val filteredList = builder.user.trucks.list(plateNumber = plateNumber)
            Assertions.assertEquals(1, filteredList.size)
        }
    }

    @Test
    fun testFind() {
        createTestBuilder().use { builder ->
            val truckData = Truck(plateNumber = plateNumber)
            val createdTruck = builder.user.trucks.create(truckData)
            Assertions.assertNotNull(createdTruck)
            Assertions.assertEquals(truckData.plateNumber, createdTruck.plateNumber)
        }
    }

    @Test
    fun testFindFail() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create()

            InvalidValueTestScenarioBuilder(
                path = "v1/trucks/{truckId}",
                method = Method.GET,
                token = builder.user.accessTokenProvider.accessToken
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

    @Test
    fun testCreate() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create()
            val foundTruck = builder.user.trucks.find(createdTruck.id!!)
            Assertions.assertNotNull(foundTruck)
            Assertions.assertEquals(plateNumber, foundTruck.plateNumber)

            // We cannot create trucks or trailers with already existing plate number
            builder.user.trucks.assertCreateFail(400, createdTruck)
            builder.user.trailers.assertCreateFail(400, Trailer(plateNumber = plateNumber))
        }
    }

    @Test
    fun testCreateFail() {
        createTestBuilder().use { builder ->
            InvalidValueTestScenarioBuilder(
                path = "v1/trucks",
                method = Method.POST,
                token = builder.user.accessTokenProvider.accessToken
            )
                .body(
                    InvalidValueTestScenarioBody(
                        values = InvalidValues.Trucks.INVALID_TRUCKS,
                        expectedStatus = 400
                    )
                )
                .build()
                .test()
        }
    }

    @Test
    fun testUpdate() {
        createTestBuilder().use { builder ->
            val truck1 = builder.user.trucks.create()
            val truck2 = builder.user.trucks.create(Truck(plateNumber = "DEF-456"))

            val updateData = Truck(plateNumber = "somenewnumber")
            val updatedTruck = builder.user.trucks.update(truck1.id!!, updateData)
            Assertions.assertEquals(updateData.plateNumber, updatedTruck.plateNumber)

            // Truck updates check for the plate number duplicates (ignoring own number)
            builder.user.trucks.update(truck1.id, truck1)
            builder.user.trucks.assertUpdateFail(400, truck1.id, truck2)
            // Trailer updates check for plate number duplicates too
            val trailer = builder.user.trailers.create(Trailer("trailerNumber"))
            builder.user.trailers.assertUpdateFail(400, trailer.id!!, Trailer(plateNumber = truck1.plateNumber))
        }
    }

    @Test
    fun testUpdateFail() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create()
            InvalidValueTestScenarioBuilder(
                path = "v1/trucks/{truckId}",
                method = Method.PUT,
                token = builder.user.accessTokenProvider.accessToken
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
                        values = InvalidValues.Trucks.INVALID_TRUCKS,
                        expectedStatus = 400
                    )
                )
                .build()
                .test()
        }
    }

    @Test
    fun testDelete() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create()
            builder.user.trucks.delete(createdTruck.id!!)
            builder.user.trucks.assertFindFail(404, createdTruck.id)
        }
    }

    @Test
    fun testDeleteFail() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create()
            InvalidValueTestScenarioBuilder(
                path = "v1/trucks/{truckId}",
                method = Method.DELETE,
                token = builder.user.accessTokenProvider.accessToken
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

}
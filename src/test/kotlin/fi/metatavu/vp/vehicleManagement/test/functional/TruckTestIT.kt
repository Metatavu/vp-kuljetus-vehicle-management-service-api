package fi.metatavu.vp.vehicleManagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.vp.vehicleManagement.test.functional.resources.MysqlResource
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioBuilder
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValues
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioPath
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class)
)
class TruckTestIT: AbstractFunctionalTest() {

    val plateNumber = "ABC-123"

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
            val createdTruck = builder.user.trucks.create(Truck(plateNumber = plateNumber))
            val foundTruck = builder.user.trucks.find(createdTruck!!.id!!)
            Assertions.assertNotNull(foundTruck)
            Assertions.assertEquals(plateNumber, foundTruck.plateNumber)
        }
    }

    @Test
    fun testFindFail() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create(Truck(plateNumber = plateNumber))

            InvalidValueTestScenarioBuilder(path = "v1/trucks/{truckId}", method = Method.GET, token = builder.user.accessTokenProvider.accessToken)
                .path(InvalidValueTestScenarioPath(name = "truckId", values = InvalidValues.STRING_NOT_NULL, default = createdTruck!!.id, expectedStatus = 404))
                .build()
                .test()
        }
    }

    @Test
    fun testCreate() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create(Truck(plateNumber = plateNumber))
            val foundTruck = builder.user.trucks.find(createdTruck!!.id!!)
            Assertions.assertNotNull(foundTruck)
            Assertions.assertEquals(plateNumber, foundTruck.plateNumber)
        }
    }

    @Test
    fun testUpdate() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create(Truck(plateNumber = plateNumber))
            val updatedTruck = builder.user.trucks.update(createdTruck!!.id!!, Truck(plateNumber = "DEF-456"))
            Assertions.assertEquals("DEF-456", updatedTruck.plateNumber)
        }
    }

    @Test
    fun testUpdateFail() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create(Truck(plateNumber = plateNumber))
            InvalidValueTestScenarioBuilder(path = "v1/trucks/{truckId}", method = Method.PUT, body = jacksonObjectMapper().writeValueAsString(Truck(plateNumber = "qqq")), token = builder.user.accessTokenProvider.accessToken)
                .path(InvalidValueTestScenarioPath(name = "truckId", values = InvalidValues.STRING_NOT_NULL, default = createdTruck!!.id, expectedStatus = 404))
                .build()
                .test()
        }
    }


    @Test
    fun testDelete() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create(Truck(plateNumber = plateNumber))
            builder.user.trucks.delete(createdTruck!!.id!!)
            builder.user.trucks.assertFindFail(404, createdTruck.id!!)
        }
    }

    @Test
    fun testDeleteFail() {
        createTestBuilder().use { builder ->
            val createdTruck = builder.user.trucks.create(Truck(plateNumber = plateNumber))
            InvalidValueTestScenarioBuilder(path = "v1/trucks/{truckId}", method = Method.DELETE, token = builder.user.accessTokenProvider.accessToken)
                .path(InvalidValueTestScenarioPath(name = "truckId", values = InvalidValues.STRING_NOT_NULL, default = createdTruck!!.id, expectedStatus = 404))
                .build()
                .test()
        }
    }

}
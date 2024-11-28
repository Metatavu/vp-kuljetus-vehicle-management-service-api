package fi.metatavu.vp.vehiclemanagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.test.client.models.TruckSpeed
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

/**
 * Tests for TruckSpeed part of Trucks API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class TruckSpeedTestIT : AbstractFunctionalTest() {

    @Test
    fun createTruckSpeed() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val now = OffsetDateTime.now()
        val time1 = now.toEpochSecond()
        val time2 = now.plusSeconds(1).toEpochSecond()
        val truckSpeedData = TruckSpeed(
            timestamp = time1,
            speed = 100.0f
        )
        it.setApiKey().trucks.createTruckSpeed(
            truckId = truck.id!!,
            truckSpeed = truckSpeedData
        )
        // should be ignored because timestamp is same
        it.setApiKey().trucks.createTruckSpeed(
            truckId = truck.id,
            truckSpeed = truckSpeedData.copy(speed = 101.0f)
        )
        // should be ignored because the latest speed record is the same
        it.setApiKey().trucks.createTruckSpeed(
            truckId = truck.id,
            truckSpeed = truckSpeedData.copy(timestamp = time2)
        )
        // should be created successfully
        it.setApiKey().trucks.createTruckSpeed(
            truckId = truck.id,
            truckSpeed = truckSpeedData.copy(speed = 101.0f, timestamp = time2)
        )

        val createdTruckSpeed = it.manager.trucks.listTruckSpeed(truck.id)
        assertEquals(2, createdTruckSpeed.size)

        assertNotNull(createdTruckSpeed[0].id)
        assertEquals(101.0f, createdTruckSpeed[0].speed)
        assertEquals(time2, createdTruckSpeed[0].timestamp)

        assertNotNull(createdTruckSpeed[1].id)
        assertEquals(time1, createdTruckSpeed[1].timestamp)
    }

    @Test
    fun createTruckSpeedFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val truckSpeedData = TruckSpeed(
            timestamp = OffsetDateTime.now().toEpochSecond(),
            speed = 100.0f
        )
        InvalidValueTestScenarioBuilder(
            basePath = ApiTestSettings.apiBasePath,
            path = "/v1/trucks/{truckId}/speeds",
            method = Method.POST,
            header = "X-DataReceiver-API-Key" to "test-api-key",
            body = jacksonObjectMapper().writeValueAsString(truckSpeedData) // nothing to verify in truck body
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "truckId",
                    values = InvalidValues.STRING_NOT_NULL,
                    expectedStatus = 404,
                    default = truck.id
                )
            )
            .build()
            .test()
    }

    @Test
    fun listTruckSpeeds() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val truck2 = it.manager.trucks.create(
            Truck(plateNumber = "0002", type = Truck.Type.TRUCK, vin = "0002"),
            it.manager.vehicles
        )

        val now = OffsetDateTime.now()
        it.setApiKey().trucks.createTruckSpeed(
            truckId = truck.id!!,
            truckSpeed = TruckSpeed(
                timestamp = now.toEpochSecond(),
                speed = 1.0f
            )
        )

        it.setApiKey().trucks.createTruckSpeed(
            truckId = truck.id,
            truckSpeed = TruckSpeed(
                timestamp = now.plusMinutes(1).toEpochSecond(),
                speed = 2.0f
            )
        )

        it.setApiKey().trucks.createTruckSpeed(
            truckId = truck.id,
            truckSpeed = TruckSpeed(
                timestamp = now.plusMinutes(2).toEpochSecond(),
                speed = 3.0f
            )
        )

        it.setApiKey().trucks.createTruckSpeed(
            truckId = truck2.id!!,
            truckSpeed = TruckSpeed(
                timestamp = now.toEpochSecond(),
                speed = 4.0f
            )
        )

        it.setApiKey().trucks.createTruckSpeed(
            truckId = truck2.id,
            truckSpeed = TruckSpeed(
                timestamp = now.minusMinutes(1).toEpochSecond(),
                speed = 4.0f
            )
        )

        val truck1List = it.manager.trucks.listTruckSpeed(truck.id)
        val truck2List = it.manager.trucks.listTruckSpeed(truck2.id)
        assertEquals(3, truck1List.size)
        assertEquals(3.0f, truck1List[0].speed)
        assertEquals(2, truck2List.size)

        val filtered = it.manager.trucks.listTruckSpeed(truck.id, after = now.minusMinutes(1), before = now.plusMinutes(1))
        assertEquals(2, filtered.size)

        val paged = it.manager.trucks.listTruckSpeed(truck.id, first = 3, max = 4)
        assertEquals(0, paged.size)

        val paged2 = it.manager.trucks.listTruckSpeed(truck.id, first = 2, max = 4)
        assertEquals(1, paged2.size)

    }

    @Test
    fun listTruckSpeedsFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)

        //access rights
        it.user.trucks.assertListTruckSpeedFail(truck.id!!, 403)
        it.driver.trucks.assertListTruckSpeedFail(truck.id, 403)

        InvalidValueTestScenarioBuilder(
            basePath = ApiTestSettings.apiBasePath,
            path = "/v1/trucks/{truckId}/speeds",
            method = Method.GET,
            token = it.manager.accessTokenProvider.accessToken
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "truckId",
                    values = InvalidValues.STRING_NOT_NULL,
                    expectedStatus = 404,
                    default = truck.id
                )
            )
            .build()
            .test()
    }


}
package fi.metatavu.vp.vehiclemanagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.test.client.models.TruckOdometerReading
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
 * Tests for TruckOdometerReading part of Trucks API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class TruckOdometerReadingTestIT : AbstractFunctionalTest() {

    @Test
    fun createTruckOdometerReading() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val now = OffsetDateTime.now()
        val time1 = now.toEpochSecond()
        val time2 = now.plusSeconds(1).toEpochSecond()
        val truckOdometerReadingData = TruckOdometerReading(
            timestamp = time1,
            odometerReading = 1000
        )
        it.setApiKey().trucks.createTruckOdometerReading(
            truckId = truck.id!!,
            truckOdometerReading = truckOdometerReadingData
        )
        // should be ignored because timestamp is same
        it.setApiKey().trucks.createTruckOdometerReading(
            truckId = truck.id,
            truckOdometerReading = truckOdometerReadingData.copy(odometerReading = 1001)
        )
        // should be ignored because the latest odometer reading record is the same
        it.setApiKey().trucks.createTruckOdometerReading(
            truckId = truck.id,
            truckOdometerReading = truckOdometerReadingData.copy(timestamp = time2)
        )
        // should be created successfully
        it.setApiKey().trucks.createTruckOdometerReading(
            truckId = truck.id,
            truckOdometerReading = truckOdometerReadingData.copy(odometerReading = 1001, timestamp = time2)
        )

        val createdTruckOdometerReading = it.manager.trucks.listTruckOdometerReading(truck.id)
        assertEquals(2, createdTruckOdometerReading.size)

        assertNotNull(createdTruckOdometerReading[0].id)
        assertEquals(1001, createdTruckOdometerReading[0].odometerReading)
        assertEquals(time2, createdTruckOdometerReading[0].timestamp)

        assertNotNull(createdTruckOdometerReading[1].id)
        assertEquals(time1, createdTruckOdometerReading[1].timestamp)
    }

    @Test
    fun createTruckOdometerReadingFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val truckOdometerReadingData = TruckOdometerReading(
            timestamp = OffsetDateTime.now().toEpochSecond(),
            odometerReading = 1000
        )
        InvalidValueTestScenarioBuilder(
            basePath = ApiTestSettings.apiBasePath,
            path = "/v1/trucks/{truckId}/odometer-readings",
            method = Method.POST,
            header = "X-API-Key" to "test-api-key",
            body = jacksonObjectMapper().writeValueAsString(truckOdometerReadingData) // nothing to verify in truck body
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
    fun listTruckOdometerReadings() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val truck2 = it.manager.trucks.create(
            Truck(plateNumber = "0002", type = Truck.Type.TRUCK, vin = "0002"),
            it.manager.vehicles
        )

        val now = OffsetDateTime.now()
        it.setApiKey().trucks.createTruckOdometerReading(
            truckId = truck.id!!,
            truckOdometerReading = TruckOdometerReading(
                timestamp = now.toEpochSecond(),
                odometerReading = 1000
            )
        )

        it.setApiKey().trucks.createTruckOdometerReading(
            truckId = truck.id,
            truckOdometerReading = TruckOdometerReading(
                timestamp = now.plusMinutes(1).toEpochSecond(),
                odometerReading = 2000
            )
        )

        it.setApiKey().trucks.createTruckOdometerReading(
            truckId = truck.id,
            truckOdometerReading = TruckOdometerReading(
                timestamp = now.plusMinutes(2).toEpochSecond(),
                odometerReading = 3000
            )
        )

        it.setApiKey().trucks.createTruckOdometerReading(
            truckId = truck2.id!!,
            truckOdometerReading = TruckOdometerReading(
                timestamp = now.toEpochSecond(),
                odometerReading = 4000
            )
        )

        it.setApiKey().trucks.createTruckOdometerReading(
            truckId = truck2.id,
            truckOdometerReading = TruckOdometerReading(
                timestamp = now.minusMinutes(1).toEpochSecond(),
                odometerReading = 4000
            )
        )

        val truck1List = it.manager.trucks.listTruckOdometerReading(truck.id)
        val truck2List = it.manager.trucks.listTruckOdometerReading(truck2.id)
        assertEquals(3, truck1List.size)
        assertEquals(3000, truck1List[0].odometerReading)
        assertEquals(2, truck2List.size)

        val filtered = it.manager.trucks.listTruckOdometerReading(truck.id, after = now.minusMinutes(1), before = now.plusMinutes(1))
        assertEquals(2, filtered.size)

        val paged = it.manager.trucks.listTruckOdometerReading(truck.id, first = 3, max = 4)
        assertEquals(0, paged.size)

        val paged2 = it.manager.trucks.listTruckOdometerReading(truck.id, first = 2, max = 4)
        assertEquals(1, paged2.size)

    }

    @Test
    fun listTruckOdometerReadingsFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)

        //access rights
        it.user.trucks.assertListTruckOdometerReadingFail(truck.id!!, 403)
        it.driver.trucks.assertListTruckOdometerReadingFail(truck.id, 403)

        InvalidValueTestScenarioBuilder(
            basePath = ApiTestSettings.apiBasePath,
            path = "/v1/trucks/{truckId}/odometerReadings",
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
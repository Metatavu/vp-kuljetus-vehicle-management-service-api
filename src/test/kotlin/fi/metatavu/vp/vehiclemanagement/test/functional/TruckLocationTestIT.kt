package fi.metatavu.vp.vehiclemanagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.test.client.models.TruckLocation
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
 * Tests for TruckLocation part of Trucks API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class TruckLocationTestIT : AbstractFunctionalTest() {

    @Test
    fun testCreateTruckLocation() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val now = System.currentTimeMillis()
        val truckLocationData = TruckLocation(
            latitude = 1.0,
            longitude = 1.0,
            heading = 1.0,
            timestamp = now
        )
        it.setApiKey().trucks.createTruckLocation(truck.id!!, truckLocationData)
        val createdTruckLocation = it.manager.trucks.listTruckLocations(truck.id)[0]
        assertNotNull(createdTruckLocation.id)
        assertEquals(truckLocationData.latitude, createdTruckLocation.latitude)
        assertEquals(truckLocationData.longitude, createdTruckLocation.longitude)
        assertEquals(truckLocationData.heading, createdTruckLocation.heading)
    }

    @Test
    fun testCreateTruckLocationFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val now = System.currentTimeMillis()
        val truckLocationData = TruckLocation(
            latitude = 1.0,
            longitude = 1.0,
            heading = 1.0,
            timestamp = now
        )
        it.setApiKey("fake key").trucks.assertCreateTruckLocationFail(truck.id!!, truckLocationData, 403)

        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}/locations",
            method = Method.POST,
            header = "X-API-Key" to "test-api-key",
            basePath = ApiTestSettings.apiBasePath,
            body = jacksonObjectMapper().writeValueAsString(truckLocationData)  // nothing to verify
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "truckId",
                    values = InvalidValues.STRING_NOT_NULL,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

    @Test
    fun testListTruckLocations() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val truck2 = it.manager.trucks.create("002", "002", it.manager.vehicles)
        val now = OffsetDateTime.now()
        it.setApiKey().trucks.createTruckLocation(
            truck.id!!, TruckLocation(
                latitude = 1.0,
                longitude = 1.0,
                heading = 1.0,
                timestamp = now.toEpochSecond() * 1000
            )
        )
        it.setApiKey().trucks.createTruckLocation(
            truck.id, TruckLocation(
                latitude = 2.0,
                longitude = 2.0,
                heading = 2.0,
                timestamp = now.minusMinutes(1).toEpochSecond() * 1000
            )
        )
        it.setApiKey().trucks.createTruckLocation(
            truck2.id!!, TruckLocation(
                latitude = 1.0,
                longitude = 1.0,
                heading = 1.0,
                timestamp = now.toEpochSecond() * 1000
            )
        )
        val truckLocations = it.manager.trucks.listTruckLocations(truck.id)
        assertEquals(2, truckLocations.size)
        assertEquals(1.0, truckLocations[0].latitude)

        val truck2Locations = it.manager.trucks.listTruckLocations(truck2.id)
        assertEquals(1, truck2Locations.size)

        val pagedList = it.manager.trucks.listTruckLocations(truck.id, first = 1, max = 1)
        assertEquals(1, pagedList.size)

        val pagedList2 = it.manager.trucks.listTruckLocations(truck.id, first = 2, max = 1)
        assertEquals(0, pagedList2.size)

        val filteredList = it.manager.trucks.listTruckLocations(truck.id, after = now.minusMinutes(5), before = now.minusSeconds(10))
        assertEquals(1, filteredList.size)
    }

    @Test
    fun testListTruckLocationsFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        it.user.trucks.assertListTruckLocationsFail(truck.id!!, 403)
        it.driver.trucks.assertListTruckLocationsFail(truck.id, 403)

        InvalidValueTestScenarioBuilder(
            basePath = ApiTestSettings.apiBasePath,
            path = "/v1/trucks/{truckId}/locations",
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
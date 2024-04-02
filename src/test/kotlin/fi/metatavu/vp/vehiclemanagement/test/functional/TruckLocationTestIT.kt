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
            id = now
        )
        it.setApiKey().trucks.createTruckLocation(truck.id!!, truckLocationData)
        val createdTruckLocation = it.manager.trucks.listTruckLocations(truck.id)[0]
        assertNotNull(createdTruckLocation)
        assertEquals(truckLocationData.latitude, createdTruckLocation.latitude)
        assertEquals(truckLocationData.longitude, createdTruckLocation.longitude)
        assertEquals(truckLocationData.heading, createdTruckLocation.heading)
        assertEquals(truckLocationData.id, createdTruckLocation.id)
    }

    @Test
    fun testCreateTruckLocationFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val now = System.currentTimeMillis()
        val truckLocationData = TruckLocation(
            latitude = 1.0,
            longitude = 1.0,
            heading = 1.0,
            id = now
        )
        it.user.trucks.assertCreateTruckLocationFail(truck.id!!, truckLocationData, 403)
        it.driver.trucks.assertCreateTruckLocationFail(truck.id, truckLocationData, 403)

        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}/truckLocations",
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
        it.setApiKey().trucks.createTruckLocation(
            truck.id!!, TruckLocation(
                latitude = 1.0,
                longitude = 1.0,
                heading = 1.0,
                id = System.currentTimeMillis()
            )
        )
        it.setApiKey().trucks.createTruckLocation(
            truck.id!!, TruckLocation(
                latitude = 1.0,
                longitude = 1.0,
                heading = 1.0,
                id = System.currentTimeMillis()
            )
        )
        it.setApiKey().trucks.createTruckLocation(
            truck.id!!, TruckLocation(
                latitude = 1.0,
                longitude = 1.0,
                heading = 1.0,
                id = System.currentTimeMillis()
            )
        )
        val truckLocations = it.manager.trucks.listTruckLocations(truck.id!!)
        assertEquals(1, truckLocations.size)
        assertEquals(truckLocation.id, truckLocations[0].id)
    }

    @Test
    fun testListTruckLocationsFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        it.user.trucks.assertListTruckLocationsFail(truck.id!!, 403)
        it.driver.trucks.assertListTruckLocationsFail(truck.id!!, 403)
    }
}
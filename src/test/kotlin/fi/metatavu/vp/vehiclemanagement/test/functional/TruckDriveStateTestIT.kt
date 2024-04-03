package fi.metatavu.vp.vehiclemanagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.invalid.providers.SimpleInvalidValueProvider
import fi.metatavu.vp.test.client.models.TruckDriveState
import fi.metatavu.vp.test.client.models.TruckDriveStateEnum
import fi.metatavu.vp.test.client.models.TruckDriverCard
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

/**
 * Tests for TruckDriveState part of Trucks API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class TruckDriveStateTestIT : AbstractFunctionalTest() {

    @Test
    fun testCreateTruckDriveStates() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val driverCard = it.setApiKey().trucks.createDriverCard(truck.id!!, TruckDriverCard("driverCardId"))
        val now = System.currentTimeMillis()
        val truckDriveStateData = TruckDriveState(
            state = TruckDriveStateEnum.DRIVE,
            timestamp = now,
            driverCardId = driverCard.id,
            driverId = UUID.randomUUID()
        )
        it.setApiKey().trucks.createDriveState(truck.id, truckDriveStateData)
        val createdTruckLocation = it.manager.trucks.listDriveStates(truck.id)[0]
        assertNotNull(createdTruckLocation.id)
        assertEquals(truckDriveStateData.state, createdTruckLocation.state)
        assertEquals(truckDriveStateData.driverCardId, createdTruckLocation.driverCardId)
        assertEquals(truckDriveStateData.driverId, createdTruckLocation.driverId)
        assertEquals(truckDriveStateData.timestamp, createdTruckLocation.timestamp)
    }

    @Test
    fun testCreateTruckDriveStatesFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val truck2 = it.manager.trucks.create("002", "002", it.manager.vehicles)
        val driverCard = it.setApiKey().trucks.createDriverCard(truck.id!!, TruckDriverCard("driverCardId"))
        val driverCardTruck2 = it.setApiKey().trucks.createDriverCard(truck2.id!!, TruckDriverCard("driverCardId2"))

        val now = System.currentTimeMillis()
        val truckDriveStateData = TruckDriveState(
            state = TruckDriveStateEnum.DRIVE,
            timestamp = now,
            driverCardId = driverCard.id,
            driverId = UUID.randomUUID()
        )

        // access rights
        it.setApiKey("fake key").trucks.assertCreateDriveStateFail(truck.id, truckDriveStateData, 403)

        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}/driveStates",
            method = Method.POST,
            header = "X-API-Key" to "test-api-key",
            basePath = ApiTestSettings.apiBasePath,
            body = jacksonObjectMapper().writeValueAsString(truckDriveStateData)
        )
            .body(
                InvalidValueTestScenarioBody(
                    default = truckDriveStateData,
                    expectedStatus = 400,
                    values = listOf(
                        truckDriveStateData.copy(driverCardId = driverCardTruck2.id),
                        truckDriveStateData.copy(driverCardId = "invalid")
                    )
                        .map { stateData ->
                            SimpleInvalidValueProvider(
                                jacksonObjectMapper().writeValueAsString(stateData)
                            )
                        }
                )
            )
            .path(
                InvalidValueTestScenarioPath(
                    name = "truckId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = truck.id,
                    expectedStatus = 404
                )
            )
            .build()
            .test()
    }

    @Test
    fun testListTruckDriveStates() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val driverCard = it.setApiKey().trucks.createDriverCard(truck.id!!, TruckDriverCard("driverCardId"))

        val truck2 = it.manager.trucks.create("002", "002", it.manager.vehicles)
        val driverCard2 = it.setApiKey().trucks.createDriverCard(truck2.id!!, TruckDriverCard("driverCardId2"))

        val now = OffsetDateTime.now()
        val driver1 = UUID.randomUUID()
        val driver2 = UUID.randomUUID()

        it.setApiKey().trucks.createDriveState(
            truck.id, TruckDriveState(
                state = TruckDriveStateEnum.DRIVE,
                timestamp = now.toEpochSecond() * 1000,
                driverId = driver1,
                driverCardId = driverCard.id
            )
        )
        it.setApiKey().trucks.createDriveState(
            truck.id, TruckDriveState(
                state = TruckDriveStateEnum.REST,
                timestamp = now.minusMinutes(1).toEpochSecond() * 1000,
                driverId = driver2,
                driverCardId = driverCard.id
            )
        )
        it.setApiKey().trucks.createDriveState(
            truck2.id, TruckDriveState(
                state = TruckDriveStateEnum.REST,
                timestamp = now.toEpochSecond() * 1000,
                driverCardId = driverCard2.id
            )
        )

        val truckDriveStates = it.manager.trucks.listDriveStates(truckId = truck.id)
        assertEquals(2, truckDriveStates.size)
        assertEquals(TruckDriveStateEnum.DRIVE, truckDriveStates[0].state)

        val truck2DriveStates = it.manager.trucks.listDriveStates(truck2.id)
        assertEquals(1, truck2DriveStates.size)

        val driver1States = it.manager.trucks.listDriveStates(truckId = truck.id, driverId = driver1)
        assertEquals(1, driver1States.size)

        val driverCardStates = it.manager.trucks.listDriveStates(
            truckId = truck.id,
            state = listOf(TruckDriveStateEnum.REST).toTypedArray()
        )
        assertEquals(1, driverCardStates.size)

        val pagedList = it.manager.trucks.listDriveStates(truck.id, first = 1, max = 1)
        assertEquals(1, pagedList.size)

        val pagedList2 = it.manager.trucks.listDriveStates(truck.id, first = 2, max = 1)
        assertEquals(0, pagedList2.size)

        val filteredList =
            it.manager.trucks.listDriveStates(truck.id, after = now.minusMinutes(5), before = now.minusSeconds(10))
        assertEquals(1, filteredList.size)

        assertNotNull(it.driver.trucks.listDriveStates(truckId = truck.id))
    }

    @Test
    fun testListTruckDriveStatesFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        it.user.trucks.assertListDriveStatesFail(truck.id!!, 403)

        InvalidValueTestScenarioBuilder(
            basePath = ApiTestSettings.apiBasePath,
            path = "/v1/trucks/{truckId}/driveStates",
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
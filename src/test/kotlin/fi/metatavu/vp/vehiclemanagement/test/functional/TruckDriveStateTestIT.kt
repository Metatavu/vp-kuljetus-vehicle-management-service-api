package fi.metatavu.vp.vehiclemanagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
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

/**
 * Tests for TruckDriveState part of Trucks API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class TruckDriveStateTestIT : AbstractFunctionalTest() {

    @Test
    fun testCreateTruckDriveStates() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val now = System.currentTimeMillis()
        val truckDriveStateData = TruckDriveState(
            state = TruckDriveStateEnum.DRIVE,
            timestamp = now,
            driverCardId = driver1CardId,
        )
        it.setApiKey().trucks.createDriveState(truck.id!!, truckDriveStateData)
        // should be ignored because timestamp is same
        it.setApiKey().trucks.createDriveState(truck.id, truckDriveStateData.copy(state = TruckDriveStateEnum.REST))
        // should be ignored because the latest drive state record is the same
        it.setApiKey().trucks.createDriveState(
            truck.id,
            truckDriveStateData.copy(timestamp = now + 1)
        )

        val createdTruckDriveStates = it.manager.trucks.listDriveStates(truck.id)
        assertEquals(1, createdTruckDriveStates.size)
        val createdTruckDriveState = createdTruckDriveStates[0]
        assertNotNull(createdTruckDriveState.id)
        assertEquals(truckDriveStateData.state, createdTruckDriveState.state)
        assertEquals(truckDriveStateData.driverCardId, createdTruckDriveState.driverCardId)
        assertEquals(driver1Id, createdTruckDriveState.driverId)
        assertEquals(truckDriveStateData.timestamp, createdTruckDriveState.timestamp)
    }

    @Test
    fun testCreateTruckDriveStatesFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val driverCard = it.setApiKey().trucks.createDriverCard(truck.id!!, TruckDriverCard("driverCardId"))

        val now = System.currentTimeMillis()
        val truckDriveStateData = TruckDriveState(
            state = TruckDriveStateEnum.DRIVE,
            timestamp = now,
            driverCardId = driverCard.id
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
        val truck2 = it.manager.trucks.create("002", "002", null, it.manager.vehicles)

        val now = OffsetDateTime.now()

        it.setApiKey().trucks.createDriveState(
            truck.id!!, TruckDriveState(
                state = TruckDriveStateEnum.DRIVE,
                timestamp = now.toEpochSecond(),
                driverCardId = driver1CardId
            )
        )
        it.setApiKey().trucks.createDriveState(
            truck.id, TruckDriveState(
                state = TruckDriveStateEnum.REST,
                timestamp = now.plusMinutes(1).toEpochSecond(),
                driverCardId = driver1CardId
            )
        )
        it.setApiKey().trucks.createDriveState(
            truck2.id!!, TruckDriveState(
                state = TruckDriveStateEnum.REST,
                timestamp = now.toEpochSecond() * 1000,
                driverCardId = driver2CardId
            )
        )

        val truckDriveStates = it.manager.trucks.listDriveStates(truckId = truck.id)
        assertEquals(2, truckDriveStates.size)
        assertEquals(TruckDriveStateEnum.DRIVE, truckDriveStates[1].state)

        val truck2DriveStates = it.manager.trucks.listDriveStates(truck2.id)
        assertEquals(1, truck2DriveStates.size)

        val driver1States = it.manager.trucks.listDriveStates(truckId = truck.id, driverId = driver1Id)
        assertEquals(2, driver1States.size)

        val driver2States = it.manager.trucks.listDriveStates(truckId = truck2.id, driverId = driver2Id)
        assertEquals(1, driver2States.size)

        val restStates = it.manager.trucks.listDriveStates(
            truckId = truck.id,
            state = listOf(TruckDriveStateEnum.REST).toTypedArray()
        )
        assertEquals(1, restStates.size)

        val pagedList = it.manager.trucks.listDriveStates(truck.id, first = 1, max = 1)
        assertEquals(1, pagedList.size)

        val pagedList2 = it.manager.trucks.listDriveStates(truck.id, first = 2, max = 1)
        assertEquals(0, pagedList2.size)

        val filteredList =
            it.manager.trucks.listDriveStates(truck.id, after = now.minusMinutes(5), before = now.plusSeconds(1))
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
package fi.metatavu.vp.vehiclemanagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.providers.SimpleInvalidValueProvider
import fi.metatavu.vp.test.client.models.DriverCard
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


/**
 * A test class for testing driver cards API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class DriverCardTestIT : AbstractFunctionalTest() {

    @Test
    fun updateDriverCard() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val truck2 = it.manager.trucks.create(
            Truck(
                plateNumber = "ABC-124",
                type = Truck.Type.SEMI_TRUCK,
                vin = "0001"
            ),
            it.manager.vehicles
        )

        val driverCardData = DriverCard(
            driverCardId = "driverCardId",
            truckVin = truck.vin
        )
        val created = it.setApiKey().driverCards.updateDriverCard(
            driverCardId = driverCardData.driverCardId,
            driverCard = driverCardData
        )
        assertEquals(driverCardData.driverCardId, created.driverCardId)
        assertEquals(driverCardData.truckVin, created.truckVin)

        val updated = it.setApiKey().driverCards.updateDriverCard(
            driverCardId = driverCardData.driverCardId,
            driverCard = driverCardData.copy(truckVin = truck2.vin)
        )
        assertEquals(driverCardData.driverCardId, updated.driverCardId)
        assertEquals(truck2.vin, updated.truckVin)

        // Access rights
        it.setApiKey("invalid").driverCards.assertReceiveDataFail(
            driverCardId = driverCardData.driverCardId,
            driverCard = driverCardData,
            expectedStatus = 403
        )
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use { _ ->
        InvalidValueTestScenarioBuilder(
            path = "v1/driverCards/cardId",  //hardcoded since in case it's missing the new driver card is created
            method = Method.PUT,
            header = "X-API-Key" to "test-api-key",
            basePath = ApiTestSettings.apiBasePath,
        )
            .body(
                InvalidValueTestScenarioBody(
                    expectedStatus = 404,
                    values = arrayOf(
                        DriverCard(
                            driverCardId = "cardId", // not important field, it is not updatable
                            truckVin = "invalid"
                        )
                    ).map { jacksonObjectMapper().writeValueAsString(it) }
                        .map { SimpleInvalidValueProvider(it) }
                )
            )
            .build()
            .test()
    }

    @Test
    fun listDriverCards() = createTestBuilder().use {
        val truck = it.manager.trucks.create(
            Truck(
                plateNumber = "ABC-124",
                type = Truck.Type.SEMI_TRUCK,
                vin = "vin1"
            ),
            it.manager.vehicles)
        val truck2 = it.manager.trucks.create(
            Truck(
                plateNumber = "ABC-125",
                type = Truck.Type.SEMI_TRUCK,
                vin = "vin2"
            ),
            it.manager.vehicles
        )

        val driverCardData = DriverCard(
            driverCardId = "driverCardId",
            truckVin = truck.vin
        )
        it.setApiKey().driverCards.updateDriverCard(
            driverCardId = driverCardData.driverCardId,
            driverCard = driverCardData
        )

        val driverCardData2 = DriverCard(
            driverCardId = "driverCardId2",
            truckVin = truck2.vin
        )
        it.setApiKey().driverCards.updateDriverCard(
            driverCardId = driverCardData2.driverCardId,
            driverCard = driverCardData2
        )

        val list = it.setApiKey().driverCards.listDriverCards(truck.vin)
        assertEquals(1, list.size)
        assertEquals(driverCardData.driverCardId, list[0].driverCardId)
        assertEquals(driverCardData.truckVin, list[0].truckVin)

        val list2 = it.setApiKey().driverCards.listDriverCards(truck2.vin)
        assertEquals(1, list2.size)
        assertEquals(driverCardData2.driverCardId, list2[0].driverCardId)
        assertEquals(driverCardData2.truckVin, list2[0].truckVin)

        val emptyList = it.setApiKey().driverCards.listDriverCards("invalid")
        assertEquals(0, emptyList.size)

        // Access rights
        it.setApiKey("invalid").driverCards.assertListDriverCardsFail(
            vin = truck.vin,
            expectedStatus = 403
        )
    }
}
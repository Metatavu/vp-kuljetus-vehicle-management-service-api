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
import org.junit.jupiter.api.Assertions
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
        Assertions.assertEquals(driverCardData.driverCardId, created.driverCardId)
        Assertions.assertEquals(driverCardData.truckVin, created.truckVin)

        val updated = it.setApiKey().driverCards.updateDriverCard(
            driverCardId = driverCardData.driverCardId,
            driverCard = driverCardData.copy(truckVin = truck2.vin)
        )
        Assertions.assertEquals(driverCardData.driverCardId, updated.driverCardId)
        Assertions.assertEquals(truck2.vin, updated.truckVin)

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
}
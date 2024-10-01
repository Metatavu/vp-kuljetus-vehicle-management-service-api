package fi.metatavu.vp.vehiclemanagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.messaging.RoutingKey
import fi.metatavu.vp.messaging.client.MessagingClient
import fi.metatavu.vp.messaging.events.DriverWorkEventGlobalEvent
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.test.client.models.TruckDriverCard
import fi.metatavu.vp.usermanagement.model.WorkEventType
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*


/**
 * A test class for testing driver cards API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class DriverCardTestIT : AbstractFunctionalTest() {

    @Test
    fun testDriverCardWorkEvents() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val now = OffsetDateTime.now()
        val driverWorkEventConsumer = MessagingClient.setConsumer<DriverWorkEventGlobalEvent>(RoutingKey.DRIVER_WORKING_STATE_CHANGE.name)
        it.setApiKey().trucks.createDriverCard(
            truckId = truck.id!!,
            truckDriverCard = TruckDriverCard(
                id = driver1CardId,
                timestamp = now.toEpochSecond()
            )
        )

        val messages1 = driverWorkEventConsumer.consumeMessages(1)

        assertEquals(1, messages1.size)
        val message = messages1.first()
        assertEquals(message.driverId, driver1Id)
        assertEquals(message.workEventType, WorkEventType.DRIVER_CARD_INSERTED)
        assertEquals(message.time.toEpochSecond(), now.toEpochSecond())

        it.setApiKey().trucks.deleteTruckDriverCard(truck.id, driver1CardId, OffsetDateTime.now().minusDays(1))

        val messages2 = driverWorkEventConsumer.consumeMessages(1)

        assertEquals(1, messages2.size)
        val message2 = messages2.first()
        assertEquals(message2.driverId, driver1Id)
        assertEquals(message2.workEventType, WorkEventType.DRIVER_CARD_REMOVED)
        assertEquals(message2.time.dayOfMonth, OffsetDateTime.now().minusDays(1).dayOfMonth)
    }

    @Test
    fun createDriverCard() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val truck2 = it.manager.trucks.create(Truck(plateNumber="0002", type = Truck.Type.TRUCK, vin = "0002"), it.manager.vehicles)
        val driverCardData = TruckDriverCard(
            id = "driverCardId",
            timestamp = OffsetDateTime.now().toEpochSecond()
        )
        val created = it.setApiKey().trucks.createDriverCard(
            truckId = truck.id!!,
            truckDriverCard = driverCardData
        )
        assertEquals(driverCardData.id, created.id)

        // Access rights
        it.setApiKey("invalid").trucks.assertCreateDriverCardFail(
            truckId = truck.id,
            truckDriverCard = driverCardData,
            expectedStatus = 403
        )

        // Cannot insert second card
        it.setApiKey().trucks.assertCreateDriverCardFail(
            truckId = truck.id,
            truckDriverCard = driverCardData,
            expectedStatus = 409
        )

        // Cannot use same card id
        it.setApiKey().trucks.assertCreateDriverCardFail(
            truckId = truck2.id!!,
            truckDriverCard = driverCardData,
            expectedStatus = 409
        )
    }

    @Test
    fun createDriverCardFail() = createTestBuilder().use { _ ->
        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}/driverCards",
            method = Method.POST,
            header = "X-API-Key" to "test-api-key",
            basePath = ApiTestSettings.apiBasePath,
            body = jacksonObjectMapper().writeValueAsString(
                TruckDriverCard(
                    id = "cardId",
                    timestamp = OffsetDateTime.now().toEpochSecond()
                )
            )
        )
            .path(InvalidValueTestScenarioPath(
                name = "truckId",
                values = InvalidValues.STRING_NOT_NULL,
                expectedStatus = 404
            ))
            .build()
            .test()
    }

    @Test
    fun deleteDriverCard() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val truck2 = it.manager.trucks.create(Truck(plateNumber="0002", type = Truck.Type.TRUCK, vin = "0002"), it.manager.vehicles)

        val driverCard1 = it.setApiKey().trucks.createDriverCard(
            truckId = truck.id!!,
            truckDriverCard = TruckDriverCard(
                id = "driverCardId",
                timestamp = OffsetDateTime.now().toEpochSecond()
            )
        )

        val driverCard2 = it.setApiKey().trucks.createDriverCard(
            truckId = truck2.id!!,
            truckDriverCard = TruckDriverCard(
                id = "driverCardId2",
                timestamp = OffsetDateTime.now().toEpochSecond()
            )
        )

        // Access rights
        it.setApiKey("invalid").trucks.assertDeleteDriverCardFail(
            truckId = truck.id,
            driverCardId = driverCard1.id,
            expectedStatus = 403
        )
        // wrong truck/driver card combination
        it.setApiKey().trucks.assertDeleteDriverCardFail(
            truckId = truck.id,
            driverCardId = driverCard2.id,
            expectedStatus = 404
        )

        it.setApiKey().trucks.deleteTruckDriverCard(truck.id, driverCard1.id)
        assertEquals(0, it.setApiKey().trucks.listDriverCards(truck.id).size)
    }

    @Test
    fun deleteDriverCardFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)

        val driverCardData = TruckDriverCard(
            id = "driverCardId",
            timestamp = OffsetDateTime.now().toEpochSecond()
        )
        it.setApiKey().trucks.createDriverCard(
            truckId = truck.id!!,
            truckDriverCard = driverCardData
        )

        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}/driverCards/{driverCardId}",
            method = Method.DELETE,
            header = "X-API-Key" to "test-api-key",
            basePath = ApiTestSettings.apiBasePath
        )
            .path(InvalidValueTestScenarioPath(
                name = "truckId",
                values = InvalidValues.STRING_NOT_NULL,
                expectedStatus = 404
            ))
            .path(InvalidValueTestScenarioPath(
                name = "driverCardId",
                values = InvalidValues.STRING_NOT_NULL,
                expectedStatus = 404
            ))
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

        val driverCardData = TruckDriverCard(
            id = "driverCardId",
            timestamp = OffsetDateTime.now().toEpochSecond()
        )
        it.setApiKey().trucks.createDriverCard(
            truckId = truck.id!!,
            truckDriverCard = driverCardData
        )

        val driverCardData2 = TruckDriverCard(
            id = "driverCardId2",
            timestamp = OffsetDateTime.now().toEpochSecond()
        )
        it.setApiKey().trucks.createDriverCard(
            truckId = truck2.id!!,
            truckDriverCard = driverCardData2
        )

        val list = it.setApiKey().trucks.listDriverCards(truck.id)
        assertEquals(1, list.size)
        assertEquals(driverCardData.id, list[0].id)

        val list2 = it.setApiKey().trucks.listDriverCards(truck2.id)
        assertEquals(1, list2.size)
        assertEquals(driverCardData2.id, list2[0].id)

        it.setApiKey().trucks.assertListDriverCardsFail(UUID.randomUUID(), 404)

        // Access rights
        it.setApiKey("invalid").trucks.assertListDriverCardsFail(
            truckId = truck.id,
            expectedStatus = 403
        )
    }

    @Test
    fun listDriverCardsFail() = createTestBuilder().use { _ ->
        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}/driverCards",
            method = Method.GET,
            header = "X-API-Key" to "test-api-key",
            basePath = ApiTestSettings.apiBasePath
        )
            .path(InvalidValueTestScenarioPath(
                name = "truckId",
                values = InvalidValues.STRING_NOT_NULL,
                expectedStatus = 404
            ))
            .build()
            .test()
    }
}
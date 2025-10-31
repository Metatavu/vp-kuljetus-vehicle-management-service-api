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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility
import java.time.Duration
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
        val driverWorkEventConsumer = MessagingClient.setConsumer<DriverWorkEventGlobalEvent>(RoutingKey.DRIVER_WORKING_STATE_CHANGE)
        it.setDataReceiverApiKey().trucks.createDriverCard(
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

        it.setDataReceiverApiKey().trucks.deleteTruckDriverCard(truck.id, driver1CardId, OffsetDateTime.now().minusDays(1))

        val messages2 = driverWorkEventConsumer.consumeMessages(1)

        assertEquals(1, messages2.size)
        val message2 = messages2.first()
        assertEquals(message2.driverId, driver1Id)
        assertEquals(message2.workEventType, WorkEventType.DRIVER_CARD_REMOVED)
        assertEquals(message2.time.dayOfMonth, OffsetDateTime.now().minusDays(1).dayOfMonth)
    }

    @Test
    fun createDriverCard() = createTestBuilder().use {
        val now = OffsetDateTime.now()
        val truck = it.manager.trucks.create(it.manager.vehicles)
        val truck2 = it.manager.trucks.create(Truck(plateNumber="0002", type = Truck.Type.TRUCK, vin = "0002"), it.manager.vehicles)
        val testCard = TruckDriverCard(
            id = "driverCardId",
            timestamp = OffsetDateTime.now().toEpochSecond()
        )
        val cardId = testCard.id
        val created = it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck.id!!,
            truckDriverCard = testCard
        )
        assertEquals(testCard.id, created.id)

        val testCard2 = TruckDriverCard(
            id = "driverCardId2",
            timestamp = OffsetDateTime.now().toEpochSecond()
        )
        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck2.id!!,
            truckDriverCard = testCard2
        )

        // Invalid access rights
        it.setDataReceiverApiKey("invalid").trucks.assertCreateDriverCardFail(
            truckId = truck.id,
            truckDriverCard = testCard,
            expectedStatus = 403
        )

        // Can reinsert the card
        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck.id,
            truckDriverCard = testCard
        )

        // Remove and re-insert the card, checking that removedAt status was updated correctly
        val removedAt = OffsetDateTime.now().minusMinutes(1)
        it.setDataReceiverApiKey().trucks.deleteTruckDriverCard(
            truckId = truck.id,
            driverCardId = cardId,
            removedAt = removedAt
        )
        val truck1Cards = it.driver.trucks.listDriverCards(truckId = truck.id)
        assertEquals(1, truck1Cards.size)
        val removedCard1 = truck1Cards.first()
        assertEquals(removedAt.toEpochSecond(), OffsetDateTime.parse(removedCard1.removedAt.toString()).toEpochSecond())
        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck.id,
            truckDriverCard = TruckDriverCard(
                id = cardId,
                timestamp = now.toEpochSecond()
            )
        )
        val truck1CardsAfterReInsert = it.driver.trucks.listDriverCards(truckId = truck.id)
        assertEquals(1, truck1CardsAfterReInsert.size)
        assertNull(truck1CardsAfterReInsert.first().removedAt)

        // Remove the card from a truck, insert it to another one, test that it is completely removed from the first truck
        it.setDataReceiverApiKey().trucks.deleteTruckDriverCard(
            truckId = truck.id,
            driverCardId = cardId
        )
        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck2.id!!,
            truckDriverCard = TruckDriverCard(
                id = cardId,
                timestamp = now.toEpochSecond()
            )
        )
        // Check that cards at truck 1 are deleted
        val truck1CardsAfterDeletion = it.driver.trucks.listDriverCards(truckId = truck.id)
        assertEquals(0, truck1CardsAfterDeletion.size)
        val truck2Cards = it.driver.trucks.listDriverCards(truckId = truck2.id)
        assertEquals(2, truck2Cards.size)
        assertNotNull(truck2Cards.firstOrNull { it.id == testCard2.id })
        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck2.id,
            truckDriverCard = testCard2
        )
        val truck2CardsAfterReinsert = it.driver.trucks.listDriverCards(truckId = truck2.id)
        assertEquals(2, truck2CardsAfterReinsert.size)
        assertNotNull(truck2CardsAfterReinsert.find { card -> card.id == testCard2.id && card.removedAt == null } )
        assertNotNull(truck2CardsAfterReinsert.find { card -> card.id == cardId && card.removedAt != null } )
        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck.id,
            truckDriverCard = testCard2
        )

        val truck1CardsAfterReinsert2 = it.driver.trucks.listDriverCards(truckId = truck.id)
        assertEquals(1, truck1CardsAfterReinsert2.size)
        assertNotNull(truck1CardsAfterReinsert2.find { card -> card.id == testCard2.id && card.removedAt == null } )

        val truck2CardsAfterReinsert2 = it.driver.trucks.listDriverCards(truckId =  truck2.id)
        assertEquals(1, truck2CardsAfterReinsert2.size)
        assertNotNull(truck2CardsAfterReinsert2.find { card -> card.id == cardId && card.removedAt != null } )
    }

    @Test
    fun createDriverCardFail() = createTestBuilder().use { _ ->
        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}/driverCards",
            method = Method.POST,
            header = "X-DataReceiver-API-Key" to "test-api-key",
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

        val driverCard1 = it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck.id!!,
            truckDriverCard = TruckDriverCard(
                id = "driverCardId",
                timestamp = OffsetDateTime.now().toEpochSecond()
            )
        )

        val driverCard2 = it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck2.id!!,
            truckDriverCard = TruckDriverCard(
                id = "driverCardId2",
                timestamp = OffsetDateTime.now().toEpochSecond()
            )
        )

        // Access rights
        it.setDataReceiverApiKey("invalid").trucks.assertDeleteDriverCardFail(
            truckId = truck.id,
            driverCardId = driverCard1.id,
            expectedStatus = 403
        )
        // wrong truck/driver card combination
        it.setDataReceiverApiKey().trucks.assertDeleteDriverCardFail(
            truckId = truck.id,
            driverCardId = driverCard2.id,
            expectedStatus = 404
        )

        it.setDataReceiverApiKey().trucks.deleteTruckDriverCard(truck.id, driverCard1.id)
        val truckCards = it.driver.trucks.listDriverCards(truck.id)
        assertEquals(1, truckCards.size)
        assertNotNull(truckCards.first().removedAt)
    }

    @Test
    fun deleteDriverCardFail() = createTestBuilder().use {
        val truck = it.manager.trucks.create(it.manager.vehicles)

        val driverCardData = TruckDriverCard(
            id = "driverCardId",
            timestamp = OffsetDateTime.now().toEpochSecond()
        )
        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck.id!!,
            truckDriverCard = driverCardData
        )

        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}/driverCards/{driverCardId}",
            method = Method.DELETE,
            header = "X-DataReceiver-API-Key" to "test-api-key",
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
        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck.id!!,
            truckDriverCard = driverCardData
        )

        val driverCardData2 = TruckDriverCard(
            id = "driverCardId2",
            timestamp = OffsetDateTime.now().toEpochSecond()
        )
        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck2.id!!,
            truckDriverCard = driverCardData2
        )

        val list = it.driver.trucks.listDriverCards(truck.id)
        assertEquals(1, list.size)
        assertEquals(driverCardData.id, list[0].id)

        val list2 = it.driver.trucks.listDriverCards(truck2.id)
        assertEquals(1, list2.size)
        assertEquals(driverCardData2.id, list2[0].id)

        val truck1CardsBYKeycloak = it.setKeycloakApiKey().trucks.listDriverCards(truckId = truck.id)
        assertEquals(1, truck1CardsBYKeycloak.size)

        it.driver.trucks.assertListDriverCardsFail(UUID.randomUUID(), 404)
    }

    @Test
    fun listDriverCardsFail() = createTestBuilder().use { _ ->
        InvalidValueTestScenarioBuilder(
            path = "v1/trucks/{truckId}/driverCards",
            method = Method.GET,
            header = "X-DataReceiver-API-Key" to "test-api-key",
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

    @Test
    fun cleanTruckDriverCards() = createTestBuilder().use {
        val truck1 = it.manager.trucks.create(plateNumber = "01", vin = "01", vehiclesTestBuilderResource = it.manager.vehicles)
        val truck2 = it.manager.trucks.create(plateNumber = "02", vin = "02", vehiclesTestBuilderResource = it.manager.vehicles)

        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck1.id!!,
            truckDriverCard = TruckDriverCard(
                id = driver1CardId,
                timestamp = OffsetDateTime.now().toEpochSecond()
            )
        )

        it.setDataReceiverApiKey().trucks.createDriverCard(
            truckId = truck2.id!!,
            truckDriverCard = TruckDriverCard(
                id = driver2CardId,
                timestamp = OffsetDateTime.now().toEpochSecond()
            )
        )

        // remove both cards
        it.setDataReceiverApiKey().trucks.deleteTruckDriverCard(
            truckId = truck1.id!!,
            driverCardId = driver1CardId,
            removedAt = OffsetDateTime.now().minusDays(1)
        )

        it.setDataReceiverApiKey().trucks.deleteTruckDriverCard(
            truckId = truck2.id!!,
            driverCardId = driver2CardId,
            removedAt = OffsetDateTime.now().minusDays(1)
        )

        Awaitility.await().atMost(Duration.ofMinutes(1)).until {
            val truck1Cards = it.driver.trucks.listDriverCards(truckId = truck1.id)
            val truck2Cards = it.driver.trucks.listDriverCards(truckId = truck2.id)
            (truck2Cards + truck1Cards).isEmpty()
        }
    }
}
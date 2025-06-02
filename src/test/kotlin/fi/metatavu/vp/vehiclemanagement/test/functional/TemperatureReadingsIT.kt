package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.vp.messaging.RoutingKey
import fi.metatavu.vp.messaging.client.MessagingClient
import fi.metatavu.vp.messaging.events.TemperatureGlobalEvent
import fi.metatavu.vp.test.client.models.TruckOrTowableTemperatureReading
import fi.metatavu.vp.test.client.models.TemperatureReadingSourceType
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

/**
 * Tests for TemperatureReadings API and Thermometers
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class TemperatureReadingsIT : AbstractFunctionalTest() {

    /**
     * Tests creating truck temperature reading
     */
    @Test
    fun createTruckTemperatureReading() = createTestBuilder().use {
        val truck = it.manager.trucks.create(
            plateNumber = "plate",
            vin = "1",
            imei = "000",
            vehiclesTestBuilderResource = it.manager.vehicles
        )
        val timestamp = Instant.now().toEpochMilli()
        val temperatureReading = TruckOrTowableTemperatureReading(
            deviceIdentifier = truck.imei!!,
            timestamp = timestamp,
            hardwareSensorId = "000",
            value = -12.0f,
            sourceType = TemperatureReadingSourceType.TRUCK
        )
        val messageConsumer = MessagingClient.setConsumer<TemperatureGlobalEvent>(RoutingKey.TEMPERATURE)
        it.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(temperatureReading)
        it.setDataReceiverApiKey().temperatureReadings.assertCreateTemperatureReadingFail(
            400,
            temperatureReading
        ) // this will be ignored because it is same

        val messages = messageConsumer.consumeMessages(1)
        val thermometer = it.manager.thermometers.listThermometers().first()
        assertEquals(1, messages.size)
        assertEquals(-12.0f, messages.first().temperature)
        assertEquals(thermometer.id, messages.first().thermometerId)

        val truck1TemperatureReadings = it.manager.trucks.listTemperatureReadings(truck.id!!, false)
        assertEquals(1, truck1TemperatureReadings.size)
        assertEquals(temperatureReading.value, truck1TemperatureReadings.first().value)
        assertEquals(temperatureReading.timestamp, truck1TemperatureReadings.first().timestamp)

        val thermometers1 = it.manager.thermometers.listThermometers()
        assertEquals(1, thermometers1.size)
        assertEquals(temperatureReading.hardwareSensorId, thermometers1.first().macAddress)
        assertEquals(thermometers1[0].id, truck1TemperatureReadings.first().thermometerId)

        val trucks = it.manager.trucks.list(thermometerId = thermometer.id)
        assertEquals(1, trucks.size, "Truck should be found by thermometer id")
        assertEquals(truck.id, trucks.first().id, "Truck id should match when listed by thermometer")
        assertEquals(0, it.manager.trucks.list(thermometerId = UUID.randomUUID()).size, "Listing trucks by non-existing thermometer id should return empty list")
        assertEquals(1, it.manager.trucks.list(plateNumber = "plate", thermometerId = thermometer.id).size, "Truck should be found by plate number and thermometer")
        assertEquals(0, it.manager.trucks.list(plateNumber = "2133", thermometerId = thermometer.id).size, "Truck should not be found by non-existing plate number and thermometer")
        assertEquals(0, it.manager.trucks.list(archived = true, thermometerId = thermometer.id).size, "There should be no archived trucks")
        assertEquals(0, it.manager.trucks.list(maxResults = 0, thermometerId = thermometer.id).size, "Max 0 should return no trucks")
        assertEquals(0, it.manager.trucks.list(firstResult = 1, thermometerId = thermometer.id).size, "First 1 should return no trucks")

        // New reading from the different mac of thermometer but same truck
        it.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            temperatureReading.copy(
                hardwareSensorId = "001",
                deviceIdentifier = truck.imei,
                value = -13.0f,
                timestamp = Instant.now().toEpochMilli(),
                sourceType = TemperatureReadingSourceType.TRUCK
            )
        )
        val thermometers2 = it.manager.thermometers.listThermometers()
        assertEquals(1, thermometers2.size)

        val truckTemperatureReadings = it.manager.trucks.listTemperatureReadings(truck.id, false)
        assertEquals(1, truckTemperatureReadings.size)
        assertEquals(-13.0f, truckTemperatureReadings[0].value)
        val allTruckTemperatureReadings = it.manager.trucks.listTemperatureReadings(truck.id, true)
        assertEquals(2, allTruckTemperatureReadings.size)

        // New reading from new thermometer mac from a new towable
        val towable = it.manager.towables.create(plateNumber = "plate2", vin = "2", imei = "002")
        val temperatureReadingTowable = TruckOrTowableTemperatureReading(
            deviceIdentifier = towable.imei!!,
            timestamp = Instant.now().toEpochMilli(),
            hardwareSensorId = "002",
            value = -12.0f,
            sourceType = TemperatureReadingSourceType.TOWABLE
        )
        it.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(temperatureReadingTowable)
        val thermometers3 = it.manager.thermometers.listThermometers()
        assertEquals(2, thermometers3.size)
        val createdTemperatureReadingTowable = it.manager.towables.listTemperatureReadings(towable.id!!, false)
        assertEquals(1, createdTemperatureReadingTowable.size)
        val thermometer2 = it.manager.thermometers.listThermometers().first()
        val towables = it.manager.towables.list(thermometerId = thermometer2.id)
        assertEquals(1, towables.size, "Towable should be found by thermometer id")
        assertEquals(towable.id, towables.first().id, "Towable id should match when listed by thermometer")
        assertEquals(0, it.manager.towables.list(thermometerId = UUID.randomUUID()).size, "Listing trucks by non-existing thermometer id should return empty list")
        assertEquals(1, it.manager.towables.list(plateNumber = "plate2", thermometerId = thermometer2.id).size, "Towable should be found by plate number and thermometer")
        assertEquals(0, it.manager.towables.list(plateNumber = "2133", thermometerId = thermometer2.id).size, "Towable should not be found by non-existing plate number and thermometer")
        assertEquals(0, it.manager.towables.list(archived = true, thermometerId = thermometer2.id).size, "There should be no archived towables")
        assertEquals(0, it.manager.towables.list(maxResults = 0, thermometerId = thermometer2.id).size, "Max 0 should return no towables")
        assertEquals(0, it.manager.towables.list(firstResult = 1, thermometerId = thermometer2.id).size, "First 1 should return no towables")

    }

    /**
     * Tests creating towable temperature readings that fail
     */
    @Test
    fun createTemperatureReadingFail() = createTestBuilder().use { tb ->
        tb.setDataReceiverApiKey().temperatureReadings.assertCreateTemperatureReadingFail(
            expectedStatus = 400,
            truckTemperatureReading = TruckOrTowableTemperatureReading(
                deviceIdentifier = "000",
                timestamp = Instant.now().toEpochMilli(),
                hardwareSensorId = "001",
                value = -12.0f,
                sourceType = TemperatureReadingSourceType.TRUCK
            )
        )

        tb.setDataReceiverApiKey("wrong").temperatureReadings.assertCreateTemperatureReadingFail(
            expectedStatus = 403,
            truckTemperatureReading = TruckOrTowableTemperatureReading(
                deviceIdentifier = "000",
                timestamp = Instant.now().toEpochMilli(),
                hardwareSensorId = "001",
                value = -12.0f,
                sourceType = TemperatureReadingSourceType.TRUCK
            )
        )
    }

    /**
     * Tests listing truck temperature readings and various archiving scenarios
     */
    @Test
    fun listThermometersTests() = createTestBuilder().use { tb ->
        val truck1 = tb.manager.trucks.create(
            plateNumber = "plate1",
            vin = "1",
            imei = "000",
            vehiclesTestBuilderResource = tb.manager.vehicles
        )
        val truck2 = tb.manager.trucks.create(
            plateNumber = "plate2",
            vin = "2",
            imei = "001",
            vehiclesTestBuilderResource = tb.manager.vehicles
        )
        val towable1 = tb.manager.towables.create(
            plateNumber = "plate3",
            vin = "3",
            imei = "002"
        )
        val thermometer1Mac = "000"
        val thermometer2Mac = "001"
        val thermometer3Mac = "002"
        val thermometer4Mac = "003"

        val thermometers = tb.manager.thermometers.listThermometers()
        assertEquals(0, thermometers.size)

        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TruckOrTowableTemperatureReading(
                deviceIdentifier = truck1.imei!!,
                timestamp = Instant.now().toEpochMilli(),
                hardwareSensorId = thermometer1Mac,
                value = 0f,
                sourceType = TemperatureReadingSourceType.TRUCK
            )
        )

        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TruckOrTowableTemperatureReading(
                deviceIdentifier = truck2.imei!!,
                timestamp = Instant.now().toEpochMilli(),
                hardwareSensorId = thermometer2Mac,
                value = -12.0f,
                sourceType = TemperatureReadingSourceType.TRUCK
            )
        )

        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TruckOrTowableTemperatureReading(
                deviceIdentifier = towable1.imei!!,
                timestamp = Instant.now().toEpochMilli(),
                hardwareSensorId = thermometer3Mac,
                value = 0f,
                sourceType = TemperatureReadingSourceType.TOWABLE
            )
        )

        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TruckOrTowableTemperatureReading(
                deviceIdentifier = towable1.imei,
                timestamp = Instant.now().toEpochMilli(),
                hardwareSensorId = thermometer3Mac,
                value = 1f,
                sourceType = TemperatureReadingSourceType.TOWABLE
            )
        )

        val thermometers2 = tb.manager.thermometers.listThermometers()
        assertEquals(3, thermometers2.size)
        val archivedThermometers = tb.manager.thermometers.listThermometers(includeArchived = true)
        assertEquals(3, archivedThermometers.size)
        // New reading from mac 000 which comes from towable 1 -> old thermometer (000 at truck 1) gets archived,
        // current thermometer at towable 1 gets archived.
        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TruckOrTowableTemperatureReading(
                deviceIdentifier = towable1.imei,
                timestamp = Instant.now().toEpochMilli(),
                hardwareSensorId = thermometer1Mac,
                value = 0f,
                sourceType = TemperatureReadingSourceType.TOWABLE
            )
        )

        val thermometers3 = tb.manager.thermometers.listThermometers()
        assertEquals(2, thermometers3.size)

        val archivedThermometers2 = tb.manager.thermometers.listThermometers(includeArchived = true)
        assertEquals(4, archivedThermometers2.size)

        // New reading from mac 003 which comes from truck 1 -> current thermometer at truck 1 is already archived
        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TruckOrTowableTemperatureReading(
                deviceIdentifier = truck1.imei,
                timestamp = Instant.now().toEpochMilli(),
                hardwareSensorId = thermometer4Mac,
                value = 0f,
                sourceType = TemperatureReadingSourceType.TRUCK
            )
        )
        val thermometers4 = tb.manager.thermometers.listThermometers()
        assertEquals(3, thermometers4.size)

        val archivedThermometers3 = tb.manager.thermometers.listThermometers(includeArchived = true)
        assertEquals(5, archivedThermometers3.size)
    }

    /**
     * Tests updating truck or towable temperatures
     */
    @Test
    fun updateThermometersTests() = createTestBuilder().use { tb ->
        val truck1 = tb.manager.trucks.create(
            plateNumber = "plate1",
            vin = "1",
            imei = "000",
            vehiclesTestBuilderResource = tb.manager.vehicles
        )

        val thermometer1Mac = "000"

        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TruckOrTowableTemperatureReading(
                deviceIdentifier = truck1.imei!!,
                timestamp = Instant.now().toEpochMilli(),
                hardwareSensorId = thermometer1Mac,
                value = 0f,
                sourceType = TemperatureReadingSourceType.TRUCK
            )
        )

        val thermometers = tb.manager.thermometers.listThermometers()
        assertEquals(1, thermometers.size)

        val updatedName = "Test thermometer"
        val updatedThermometer = tb.manager.thermometers.updateThermometer(
            thermometers[0].id!!,
            updatedName
        )

        assertEquals(
            updatedThermometer.name,
            updatedName,
            "Thermometer name should be updated"
        )

        val thermometersAfterUpdate = tb.manager.thermometers.listThermometers()
        assertEquals(1, thermometersAfterUpdate.size)
        assertEquals(
            updatedThermometer.name,
            thermometersAfterUpdate[0].name,
            "Thermometer name should be updated"
        )

        // Anonymous user should not be able to update thermometer
        tb.anon.thermometers.assertUpdateThermometerFail(
            expectedStatus = 401,
            thermometerId = thermometers[0].id!!,
            name = "test"
        )
        // User without role should not be able to update thermometer
        tb.user.thermometers.assertUpdateThermometerFail(
            expectedStatus = 403,
            thermometerId = thermometers[0].id!!,
            name = "test"
        )
        // Driver should not be able to update thermometer
        tb.driver.thermometers.assertUpdateThermometerFail(
            expectedStatus = 403,
            thermometerId = thermometers[0].id!!,
            name = "test"
        )
    }
}
package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.vp.test.client.models.TemperatureReading
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

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
        val temperatureReading = TemperatureReading(
            deviceIdentifier = truck.imei!!,
            timestamp = OffsetDateTime.now().toString(),
            macAddress = "000",
            value = -12.0f
        )

        it.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(temperatureReading)
        it.setDataReceiverApiKey().temperatureReadings.assertCreateTemperatureReadingFail(
            400,
            temperatureReading
        ) // this will be ignored because it is same

        val truck1TemperatureReadings = it.manager.trucks.listTemperatureReadings(truck.id!!, false)
        assertEquals(1, truck1TemperatureReadings.size)
        assertEquals(temperatureReading.value, truck1TemperatureReadings.first().value)
        assertOffsetDatetimeEquals(temperatureReading.timestamp, truck1TemperatureReadings.first().timestamp)

        var thermometers = it.manager.trucks.listThermometers()
        assertEquals(1, thermometers.size)
        assertEquals(temperatureReading.macAddress, thermometers.first().macAddress)
        assertEquals(thermometers[0].id, truck1TemperatureReadings.first().thermometerId)

        // New reading from the different mac of thermometer but same truck
        it.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            temperatureReading.copy(
                macAddress = "001",
                deviceIdentifier = truck.imei,
                value = -13.0f,
                timestamp = OffsetDateTime.now().toString()
            )
        )
        thermometers = it.manager.trucks.listThermometers()
        assertEquals(1, thermometers.size)
        val truckTemperatureReadings = it.manager.trucks.listTemperatureReadings(truck.id, false)
        assertEquals(1, truckTemperatureReadings.size)
        assertEquals(-13.0f, truckTemperatureReadings[0].value)
        assertEquals(thermometers[0].id, truckTemperatureReadings[0].thermometerId)
        val allTruckTemperatureReadings = it.manager.trucks.listTemperatureReadings(truck.id, true)
        assertEquals(2, allTruckTemperatureReadings.size)

        // New reading from new thermometer mac from a new towable
        val towable = it.manager.towables.create(plateNumber = "plate2", vin = "2", imei = "002")
        val temperatureReadingTowable = TemperatureReading(
            deviceIdentifier = towable.imei!!,
            timestamp = OffsetDateTime.now().toString(),
            macAddress = "002",
            value = -12.0f
        )
        it.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(temperatureReadingTowable)
        thermometers = it.manager.trucks.listThermometers()
        assertEquals(2, thermometers.size)
        val createdTemperatureReadingTowable = it.manager.towables.listTemperatureReadings(towable.id!!, false)
        assertEquals(1, createdTemperatureReadingTowable.size)
    }

    /**
     * Tests creating towable temperature readings that fail
     */
    @Test
    fun createTemperatureReadingFail() = createTestBuilder().use { tb ->
        tb.setDataReceiverApiKey().temperatureReadings.assertCreateTemperatureReadingFail(
            expectedStatus = 400,
            truckTemperatureReading = TemperatureReading(
                deviceIdentifier = "000",
                timestamp = OffsetDateTime.now().toString(),
                macAddress = "001",
                value = -12.0f
            )
        )

        tb.setDataReceiverApiKey("wrong").temperatureReadings.assertCreateTemperatureReadingFail(
            expectedStatus = 403,
            truckTemperatureReading = TemperatureReading(
                deviceIdentifier = "000",
                timestamp = OffsetDateTime.now().toString(),
                macAddress = "001",
                value = -12.0f
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

        val thermometers = tb.manager.trucks.listThermometers()
        assertEquals(0, thermometers.size)

        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TemperatureReading(
                deviceIdentifier = truck1.imei!!,
                timestamp = OffsetDateTime.now().toString(),
                macAddress = thermometer1Mac,
                value = 0f
            )
        )

        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TemperatureReading(
                deviceIdentifier = truck2.imei!!,
                timestamp = OffsetDateTime.now().toString(),
                macAddress = thermometer2Mac,
                value = -12.0f
            )
        )

        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TemperatureReading(
                deviceIdentifier = towable1.imei!!,
                timestamp = OffsetDateTime.now().toString(),
                macAddress = thermometer3Mac,
                value = 0f
            )
        )

        val thermometers2 = tb.manager.trucks.listThermometers()
        assertEquals(3, thermometers2.size)
        val archivedThermometers = tb.manager.trucks.listThermometers(includeArchived = true)
        assertEquals(3, archivedThermometers.size)

        // New reading from mac 000 which comes from towable 1 -> old thermometer (000 at truck 1) gets archived,
        // current thermimeter at towable 1 gets archived.
        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TemperatureReading(
                deviceIdentifier = towable1.imei!!,
                timestamp = OffsetDateTime.now().toString(),
                macAddress = thermometer1Mac,
                value = 0f
            )
        )
        val thermometers3 = tb.manager.trucks.listThermometers()
        assertEquals(2, thermometers3.size)

        val archivedThermometers2 = tb.manager.trucks.listThermometers(includeArchived = true)
        assertEquals(4, archivedThermometers2.size)

        // New reading from mac 003 which comes from truck 1 -> current thermometer at truck 1 is already archived
        tb.setDataReceiverApiKey().temperatureReadings.createTemperatureReading(
            TemperatureReading(
                deviceIdentifier = truck1.imei,
                timestamp = OffsetDateTime.now().toString(),
                macAddress = thermometer4Mac,
                value = 0f
            )
        )
        val thermometers4 = tb.manager.trucks.listThermometers()
        assertEquals(3, thermometers4.size)

        val archivedThermometers3 = tb.manager.trucks.listThermometers(includeArchived = true)
        assertEquals(5, archivedThermometers3.size)
    }
}
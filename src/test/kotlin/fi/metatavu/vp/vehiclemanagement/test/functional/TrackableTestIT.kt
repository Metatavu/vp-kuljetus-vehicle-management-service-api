package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.vp.test.client.models.TrackableType
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test class for testing Trackables API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class TrackableTestIT: AbstractFunctionalTest() {

    @Test
    fun testFind() = createTestBuilder().use { testBuilder ->
        testBuilder.manager.trucks.create(plateNumber = "ABC-1", vin = "001", imei = "truck-imei", vehiclesTestBuilderResource = testBuilder.manager.vehicles)
        testBuilder.manager.trucks.create(plateNumber = "ABC-12", vin = "0011", vehiclesTestBuilderResource = testBuilder.manager.vehicles)
        testBuilder.manager.towables.create(plateNumber = "ABC-123", vin = "00111", imei = "towable-imei")
        testBuilder.manager.towables.create(plateNumber = "ABC-1234", vin = "001111")

        val allTrucks = testBuilder.manager.trucks.list()
        val truckWithImei = allTrucks.find { it.imei == "truck-imei" }
        assertEquals(2, allTrucks.size)

        val allTowables = testBuilder.manager.towables.list()
        val towableWithImei = allTowables.find { it.imei == "towable-imei" }
        assertEquals(2, allTowables.size)

        testBuilder.setDataReceiverApiKey()

        val foundTruckByImei = testBuilder.manager.trackables.findTrackable(imei = "truck-imei")
        assertEquals(foundTruckByImei.imei, "truck-imei")
        assertEquals(foundTruckByImei.trackableType, TrackableType.TRUCK)
        assertEquals(foundTruckByImei.id, truckWithImei?.id)
        
        val foundTowableByImei = testBuilder.manager.trackables.findTrackable(imei = "towable-imei")
        assertEquals(foundTowableByImei.imei, "towable-imei")
        assertEquals(foundTowableByImei.trackableType, TrackableType.TOWABLE)
        assertEquals(foundTowableByImei.id, towableWithImei?.id)
    }
}
package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Test class for testing Public Trucks API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class PublicTruckTestIT : AbstractFunctionalTest() {

    @Test
    fun testList() = createTestBuilder().use { builder ->
        val createdTruck = builder.manager.trucks.create(plateNumber = plateNumber, vin = "001", vehiclesTestBuilderResource = builder.manager.vehicles)
        builder.manager.trucks.create(plateNumber = "DEF-456", vin = "002", vehiclesTestBuilderResource = builder.manager.vehicles)
        builder.manager.trucks.create(plateNumber = "GHI-789", vin = "003", vehiclesTestBuilderResource = builder.manager.vehicles)
        val totalList = builder.anon.publicTrucks.list()
        assertEquals(3, totalList.size)
        val truck1 = totalList.find { it.vin == "001" }
        assertEquals(createdTruck.plateNumber, truck1!!.plateNumber)
        assertEquals(createdTruck.vin, truck1.vin)
        assertEquals(createdTruck.name, truck1.name)

        val pagedList = builder.anon.publicTrucks.list(first = 1, max = 1)
        assertEquals(1, pagedList.size)

        val pagedList2 = builder.anon.publicTrucks.list(first = 0, max = 3)
        assertEquals(3, pagedList2.size)

        val pagedList3 = builder.anon.publicTrucks.list(first = 0, max = 2)
        assertEquals(2, pagedList3.size)

        val pagedList4 = builder.anon.publicTrucks.list(first = 0, max = 0)
        assertEquals(0, pagedList4.size)

        val filteredByVin = builder.anon.publicTrucks.list(vin = "002")
        assertEquals(1, filteredByVin.size)

        val filteredByVin2 = builder.anon.publicTrucks.list(vin = "005")
        assertEquals(0, filteredByVin2.size)
    }

}

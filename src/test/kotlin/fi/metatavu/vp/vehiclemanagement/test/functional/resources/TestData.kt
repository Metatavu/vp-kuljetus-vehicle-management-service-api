package fi.metatavu.vp.vehiclemanagement.test.functional.resources

import fi.metatavu.vp.usermanagement.model.Driver
import fi.metatavu.vp.vehiclemanagement.test.functional.AbstractFunctionalTest

class TestData {
    companion object {
        private val driver1 = Driver(
            id = AbstractFunctionalTest.driver1Id,
            displayName = "Tommi Tommi"
        )

        private val driver2 = Driver(
            id = AbstractFunctionalTest.driver2Id,
            displayName = "Tommi2 Tommi2"
        )

        val driverCardDriverMap = mapOf(
            AbstractFunctionalTest.driver1CardId to driver1,
            AbstractFunctionalTest.driver2CardId to driver2
        )
    }
}
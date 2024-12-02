package fi.metatavu.vp.vehiclemanagement.test.functional.settings

import fi.metatavu.vp.vehiclemanagement.test.functional.resources.wiremock.UserManagementMockTestResource
import io.quarkus.test.junit.QuarkusTestProfile
import io.quarkus.test.junit.QuarkusTestProfile.TestResourceEntry

/**
 * Default test profile
 */
class DefaultTestProfile: QuarkusTestProfile {

    override fun getConfigOverrides(): MutableMap<String, String> {
        val config: MutableMap<String, String> = HashMap()
        config["vp.vehiclemanagement.data-receiver.apiKey"] = VEHICLE_MANAGEMENT_DATA_RECEIVER_API_KEY
        config["vp.vehiclemanagement.keycloak.apiKey"] = VEHICLE_MANAGEMENT_KEYCLOAK_API_KEY
        config["clearOldRemovedDriverCards.gracePeriod.minutes"] = "30"
        config["clearOldRemovedDriverCards.every"] = "1s"
        config["clearOldRemovedDriverCards.delay"] = "1s"
        config["mp.messaging.outgoing.vp-out.exchange.name"] = EXCHANGE_NAME
        config["vp.keycloak.vehicle-management.user"] = "vehicle-management-user"
        config["vp.keycloak.vehicle-management.password"] = "test"
        config["vp.env"] = "TEST"
        return config
    }

    override fun testResources(): MutableList<TestResourceEntry> {
        return mutableListOf(
            TestResourceEntry(UserManagementMockTestResource::class.java)
        )
    }

    companion object {
        const val VEHICLE_MANAGEMENT_DATA_RECEIVER_API_KEY = "test-api-key"
        const val VEHICLE_MANAGEMENT_KEYCLOAK_API_KEY = "test-api-key"
        const val EXCHANGE_NAME = "test-exchange"
    }
}
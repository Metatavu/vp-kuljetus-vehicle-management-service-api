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
        config["vp.vehiclemanagement.telematics.apiKey"] = VEHICLE_MANAGEMENT_TELEMATICS_API_KEY

        config["mp.messaging.outgoing.vp-out.exchange.name"] = EXCHANGE_NAME
        config["vp.keycloak.vehicle-management.client"] = "vehicle-management-api"
        config["vp.keycloak.vehicle-management.secret"] = "JO46GQ1pda5NjjOO0BUMJOzHLjnoVmqf"
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
        const val VEHICLE_MANAGEMENT_TELEMATICS_API_KEY = "test-api-key"
        const val EXCHANGE_NAME = "test-exchange"
    }
}
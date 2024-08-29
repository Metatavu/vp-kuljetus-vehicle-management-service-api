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

        config["mp.messaging.incoming.vp-in.connector"] = "smallrye-rabbitmq"
//        config["mp.messaging.incoming.vp-in.queue.name"] = "incoming_queue"
//        config["mp.messaging.incoming.vp-in.queue.x-queue-type"] = "quorum"
//        config["mp.messaging.incoming.vp-in.exchange.name"] = EXCHANGE_NAME
//        config["mp.messaging.incoming.vp-in.routing-keys"] = "DRIVER_WORKING_STATE_CHANGE"

        config["mp.messaging.outgoing.vp-out.connector"] = "smallrye-rabbitmq"
        config["mp.messaging.outgoing.vp-out.exchange.name"] = EXCHANGE_NAME

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
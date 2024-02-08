package fi.metatavu.vp.vehiclemanagement.test.functional.settings

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Default test profile
 */
class DefaultTestProfile: QuarkusTestProfile {

    override fun getConfigOverrides(): MutableMap<String, String> {
        val config: MutableMap<String, String> = HashMap()
        config["vp.vehiclemanagement.telematics.apiKey"] = "test-api-key"
        return config
    }
}
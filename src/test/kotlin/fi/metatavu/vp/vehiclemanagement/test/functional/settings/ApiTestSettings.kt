package fi.metatavu.vp.vehiclemanagement.test.functional.settings

/**
 * Settings implementation for test builder
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
class ApiTestSettings {

    companion object {

        /**
         * Returns API service base path
         */
        val apiBasePath: String
            get() = "http://localhost:8081"

    }

}
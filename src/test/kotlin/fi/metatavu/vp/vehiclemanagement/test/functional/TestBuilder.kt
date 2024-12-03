package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.jaxrs.test.functional.builder.AbstractAccessTokenTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.NullAccessTokenProvider
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.vehiclemanagement.test.functional.auth.TestBuilderAuthentication
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile.Companion.VEHICLE_MANAGEMENT_DATA_RECEIVER_API_KEY
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile.Companion.VEHICLE_MANAGEMENT_KEYCLOAK_API_KEY
import org.eclipse.microprofile.config.ConfigProvider

/**
 * Abstract test builder class
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
class TestBuilder(private val config: Map<String, String>): AbstractAccessTokenTestBuilder<ApiClient>() {

    var anon = TestBuilderAuthentication(this, NullAccessTokenProvider(), null, null)
    var user = createTestBuilderAuthentication(username = "user", password = "test")
    val driver = createTestBuilderAuthentication(username = "driver1", password = "test")
    val driver2 = createTestBuilderAuthentication(username = "driver2", password = "test")
    val manager = createTestBuilderAuthentication(username = "manager", password = "test")

    override fun createTestBuilderAuthentication(
        abstractTestBuilder: AbstractTestBuilder<ApiClient, AccessTokenProvider>,
        authProvider: AccessTokenProvider
    ): AuthorizedTestBuilderAuthentication<ApiClient, AccessTokenProvider> {
        return TestBuilderAuthentication(this, authProvider, null, null)
    }

    /**
     * Returns authentication with data receiver api key
     *
     * @param apiKey device key
     * @return authorized client
     */
    fun setDataReceiverApiKey(apiKey: String? = null): TestBuilderAuthentication {
        val key = apiKey ?: VEHICLE_MANAGEMENT_DATA_RECEIVER_API_KEY
        return TestBuilderAuthentication(
            this,
            NullAccessTokenProvider(),
            dataReceiverApiKey = key,
            keycloakApiKey = null
        )
    }

    /**
     * Returns authentication with keycloak api key
     *
     * @param apiKey device key
     * @return authorized client
     */
    fun setKeycloakApiKey(apiKey: String? = null): TestBuilderAuthentication {
        val key = apiKey ?: VEHICLE_MANAGEMENT_KEYCLOAK_API_KEY
        return TestBuilderAuthentication(
            this,
            NullAccessTokenProvider(),
            keycloakApiKey = key,
            dataReceiverApiKey = null
        )
    }
    
    /**
     * Creates test builder authenticatior for given user
     *
     * @param username username
     * @param password password
     * @return test builder authenticatior for given user
     */
    private fun createTestBuilderAuthentication(username: String, password: String): TestBuilderAuthentication {
        val serverUrl = ConfigProvider.getConfig().getValue("quarkus.oidc.auth-server-url", String::class.java).substringBeforeLast("/").substringBeforeLast("/")
        val realm = "vp-kuljetus"
        val clientId = "test"
        return TestBuilderAuthentication(this, KeycloakAccessTokenProvider(serverUrl, realm, clientId, username, password, null), null, null)
    }

}
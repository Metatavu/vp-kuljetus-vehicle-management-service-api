package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.vp.vehiclemanagement.test.functional.auth.TestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.AbstractAccessTokenTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.AbstractTestBuilder
import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.AuthorizedTestBuilderAuthentication
import fi.metatavu.jaxrs.test.functional.builder.auth.KeycloakAccessTokenProvider
import fi.metatavu.jaxrs.test.functional.builder.auth.NullAccessTokenProvider
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import org.eclipse.microprofile.config.ConfigProvider

/**
 * Abstract test builder class
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
class TestBuilder(private val config: Map<String, String>): AbstractAccessTokenTestBuilder<ApiClient>() {

    var anon = TestBuilderAuthentication(this, NullAccessTokenProvider(), null)
    var user = createTestBuilderAuthentication(username = "user", password = "userPassword")
    val driver = createTestBuilderAuthentication(username = "driver", password = "driverPassword")
    val manager = createTestBuilderAuthentication(username = "manager", password = "managerPassword")

    override fun createTestBuilderAuthentication(
        abstractTestBuilder: AbstractTestBuilder<ApiClient, AccessTokenProvider>,
        authProvider: AccessTokenProvider
    ): AuthorizedTestBuilderAuthentication<ApiClient, AccessTokenProvider> {
        return TestBuilderAuthentication(this, authProvider, null)
    }

    /**
     * Returns authentication with api key
     *
     * @param apiKey device key
     * @return authorized client
     */
    fun setApiKey(apiKey: String? = null): TestBuilderAuthentication {
        val key = apiKey ?: "test-api-key"
        return TestBuilderAuthentication(this, NullAccessTokenProvider(), key)
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
        val realm: String = ConfigProvider.getConfig().getValue("quarkus.keycloak.devservices.realm-name", String::class.java)
        val clientId = "test"
        val clientSecret = "secret"
        return TestBuilderAuthentication(this, KeycloakAccessTokenProvider(serverUrl, realm, clientId, username, password, clientSecret), null)
    }

}
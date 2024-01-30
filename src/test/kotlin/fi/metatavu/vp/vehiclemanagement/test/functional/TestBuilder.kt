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
import java.io.IOException

/**
 * Abstract test builder class
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
class TestBuilder(private val config: Map<String, String>): AbstractAccessTokenTestBuilder<ApiClient>() {

    private var anon: TestBuilderAuthentication? = null
    var user = createTestBuilderAuthentication(username = "user", password = "test")

    override fun createTestBuilderAuthentication(
        abstractTestBuilder: AbstractTestBuilder<ApiClient, AccessTokenProvider>,
        authProvider: AccessTokenProvider
    ): AuthorizedTestBuilderAuthentication<ApiClient, AccessTokenProvider> {
        return TestBuilderAuthentication(this, authProvider)
    }

    /**
     * Returns authentication resource without token
     *
     * @return authentication resource without token
     */
    @kotlin.jvm.Throws(IOException::class)
    fun anon(): TestBuilderAuthentication {
        if (anon == null) {
            anon = TestBuilderAuthentication(this, NullAccessTokenProvider())
        }

        return anon!!
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
        return TestBuilderAuthentication(this, KeycloakAccessTokenProvider(serverUrl, realm, clientId, username, password, clientSecret))
    }

}
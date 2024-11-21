package fi.metatavu.vp.vehiclemanagement.keycloak

import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

/**
 * Controller for getting access token to be used for accessing other microservices
 */
@ApplicationScoped
class KeycloakIntegrationsClient : KeycloakClient() {
    override var clientType = KeycloakClientType.INTEGRATIONS

    @ConfigProperty(name = "vp.keycloak.vehicle-management.secret")
    lateinit var clientSecret: String

    @ConfigProperty(name = "vp.keycloak.vehicle-management.client")
    lateinit var clientId: String

    @ConfigProperty(name = "vp.keycloak.vehicle-management.password")
    lateinit var password: String

    @ConfigProperty(name = "vp.keycloak.vehicle-management.user")
    lateinit var user: String

    /**
     * Requests a new access token
     *
     * @return new access token
     */
    override fun requestNewToken(): Uni<KeycloakAccessToken> {
        return sendTokenRequest(
            clientId,
            clientSecret,
            user,
            password
        )
    }

    /**
     * Gets base url
     *
     * @return base url
     */
    private fun getBaseUrl(): String {
        return keycloakUrl.substringBefore("/realms")
    }

    /**
     * Gets realm name
     *
     * @return realm name
     */
    fun getRealm(): String {
        return keycloakUrl.substringAfterLast("realms/").substringBefore("/")
    }

}
package fi.metatavu.vp.vehiclemanagement.integrations

import fi.metatavu.vp.vehiclemanagement.keycloak.KeycloakClient
import fi.metatavu.vp.vehiclemanagement.keycloak.KeycloakIntegrationsClient
import io.quarkus.rest.client.reactive.ReactiveClientHeadersFactory
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.MultivaluedHashMap
import jakarta.ws.rs.core.MultivaluedMap

/**
 * Factory for REST client headers that gets the authorization token from Keycloak
 */
@ApplicationScoped
class KeycloakHeaderFactory : ReactiveClientHeadersFactory() {

    @Inject
    lateinit var keycloakClient: KeycloakIntegrationsClient

    override fun getHeaders(
        incomingHeaders: MultivaluedMap<String?, String?>?,
        clientOutgoingHeaders: MultivaluedMap<String?, String?>?
    ): Uni<MultivaluedMap<String, String>> {
        return keycloakClient.getAccessTokenUni().onItem().transform { token ->
            val newHeaders = MultivaluedHashMap<String, String>()
            newHeaders.add("Authorization", "Bearer $token")
            newHeaders
        }
    }
}

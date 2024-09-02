package fi.metatavu.vp.vehiclemanagement.system

import fi.metatavu.vp.vehiclemanagement.spec.SystemApi
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.RequestScoped
import jakarta.ws.rs.core.Response

/**
 * Implements System api
 */
@RequestScoped
class SystemApiImpl: SystemApi {
    override fun ping(): Uni<Response> = Uni.createFrom().item { Response.ok("pong").build() }
}
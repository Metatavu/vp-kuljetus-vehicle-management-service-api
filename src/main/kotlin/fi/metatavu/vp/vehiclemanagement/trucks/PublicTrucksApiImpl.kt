package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.spec.PublicTrucksApi
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response

/**
 * Api implementations for Public Trucks
 */
@RequestScoped
@WithSession
@Suppress("unused")
class PublicTrucksApiImpl: PublicTrucksApi, AbstractApi() {

    @Inject
    lateinit var truckController: TruckController

    @Inject
    lateinit var publicTruckTranslator: PublicTruckTranslator

    override fun listPublicTrucks(
        vin: String?,
        first: Int?,
        max: Int?
    ): Uni<Response> = withCoroutineScope {
        val ( trucks, count ) = truckController.listTrucks(firstResult = first, maxResults = max, archived = null, plateNumber = null, vin = vin)
        createOk(publicTruckTranslator.translate(trucks), count)
    }
}
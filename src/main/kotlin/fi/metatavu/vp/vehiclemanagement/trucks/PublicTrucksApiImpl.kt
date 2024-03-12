package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.api.spec.PublicTrucksApi
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async

/**
 * Api implementations for Public Trucks
 */
@RequestScoped
@OptIn(ExperimentalCoroutinesApi::class)
@WithSession
class PublicTrucksApiImpl: PublicTrucksApi, AbstractApi() {

    @Inject
    lateinit var truckController: TruckController

    @Inject
    lateinit var publicTruckTranslator: PublicTruckTranslator

    @Inject
    lateinit var vertx: Vertx

    override fun listPublicTrucks(first: Int?, max: Int?): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val ( trucks, count ) = truckController.listTrucks(firstResult = first, maxResults = max, archived = null, plateNumber = null)
        createOk(publicTruckTranslator.translate(trucks), count)
    }.asUni()
}
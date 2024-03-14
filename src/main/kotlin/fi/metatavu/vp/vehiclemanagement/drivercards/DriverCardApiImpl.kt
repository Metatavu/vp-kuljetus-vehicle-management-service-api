package fi.metatavu.vp.vehiclemanagement.drivercards

import fi.metatavu.vp.api.model.DriverCard
import fi.metatavu.vp.api.spec.DriverCardsApi
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.trucks.TruckController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.kotlin.coroutines.dispatcher
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

/**
 * Driver card API implementation, implements inserting the cards into the truck device
 */
@RequestScoped
@WithSession
class DriverCardApiImpl: DriverCardsApi, AbstractApi() {

    @Inject
    lateinit var driverCardController: DriverCardController

    @Inject
    lateinit var driverCardTranslator: DriverCardTranslator

    @Inject
    lateinit var truckController: TruckController

    @Inject
    lateinit var vertx: io.vertx.core.Vertx
    override fun listDriverCards(truckVin: String): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        if (requestApiKey != apiKey) {
            return@async createForbidden("Invalid api key")
        }

        val cards = driverCardController.listDriverCards(truckVin)

        createOk(cards.map { driverCardTranslator.translate(it) })
    }.asUni()

    @WithTransaction
    override fun updateDriverCard(driverCardId: String, driverCard: DriverCard): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        if (requestApiKey != apiKey) {
            return@async createForbidden("Invalid api key")
        }

        if (truckController.findTruck(driverCard.truckVin) == null) {
            return@async createNotFound("Truck with vin ${driverCard.truckVin} not found")
        }

        val existingCard = driverCardController.findDriverCard(driverCardId)

        val newCard = if (existingCard == null) {
            driverCardController.createDriverCard(driverCardId, driverCard)
        } else {
            driverCardController.updateDriverCard(existingCard, driverCard)
        }

        createOk(driverCardTranslator.translate(newCard))
    }.asUni()
}
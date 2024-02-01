package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.api.spec.TrucksApi
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.vehicles.VehicleController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
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
import java.util.*

@RequestScoped
@OptIn(ExperimentalCoroutinesApi::class)
@WithSession
class TrucksApiImpl: TrucksApi, AbstractApi() {

    @Inject
    lateinit var truckTranslator: TruckTranslator

    @Inject
    lateinit var truckController: TruckController

    @Inject
    lateinit var vehicleController: VehicleController

    @Inject
    lateinit var vertx: Vertx

    @WithTransaction
    override fun createTruck(truck: fi.metatavu.vp.api.model.Truck): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        if (vehicleController.isPlateNumberValid(truck.plateNumber).not()) {
            return@async createBadRequest(INVALID_PLATE_NUMBER)
        }

        val createdTruck = truckController.createTruck(
            plateNumber = truck.plateNumber,
            type = truck.type,
            userId = userId
        )
        createOk(truckTranslator.translate(createdTruck))
    }.asUni()

    override fun findTruck(truckId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val truck = truckController.findTruck(truckId) ?: return@async createNotFound(createNotFoundMessage(TRUCK, truckId))

        createOk(truckTranslator.translate(truck))
    }.asUni()

    override fun listTrucks(plateNumber: String?, first: Int?, max: Int?): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val ( trucks, count ) = truckController.listTrucks(plateNumber, first, max)
        createOk(trucks.map { truckTranslator.translate(it) }, count)
    }.asUni()

    @WithTransaction
    override fun updateTruck(truckId: UUID, truck: fi.metatavu.vp.api.model.Truck): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        if (vehicleController.isPlateNumberValid(truck.plateNumber).not()) {
            return@async createBadRequest(INVALID_PLATE_NUMBER)
        }

        val existingTruck = truckController.findTruck(truckId) ?: return@async createNotFound(createNotFoundMessage(TRUCK, truckId))

        val updated = truckController.updateTruck(existingTruck, truck, userId)

        createOk(truckTranslator.translate(updated))
    }.asUni()

   @WithTransaction
   override fun deleteTruck(truckId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
       val existingTruck = truckController.findTruck(truckId) ?: return@async createNotFound(createNotFoundMessage(TRUCK, truckId))

       val partOfVehicles = vehicleController.listVehicles(existingTruck).first
       if (partOfVehicles.isNotEmpty()) {
           return@async createBadRequest("Truck is part of a vehicle")
       }
       truckController.deleteTruck(existingTruck)
       createNoContent()
    }.asUni()
}
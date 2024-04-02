package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.api.model.TruckDriverCard
import fi.metatavu.vp.api.model.TruckLocation
import fi.metatavu.vp.api.spec.TrucksApi
import fi.metatavu.vp.vehiclemanagement.trucks.drivercards.DriverCardController
import fi.metatavu.vp.vehiclemanagement.trucks.drivercards.DriverCardTranslator
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.vehicles.VehicleController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import java.time.OffsetDateTime
import java.util.*

/**
 * Trucks API implementation
 */
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
    lateinit var driverCardController: DriverCardController

    @Inject
    lateinit var driverCardTranslator: DriverCardTranslator

    @Inject
    lateinit var vertx: Vertx

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createTruck(truck: fi.metatavu.vp.api.model.Truck): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        if (!vehicleController.isPlateNumberValid(truck.plateNumber) || truck.vin.isEmpty()) {
            return@async createBadRequest(INVALID_PLATE_NUMBER)
        }

        if (!vehicleController.isPlateNumberUnique(truck.plateNumber)) return@async createBadRequest(NOT_UNIQUE_PLATE_NUMBER)
        if (!vehicleController.isVinUnique(truck.vin)) return@async createBadRequest(NOT_UNIQUE_VIN)

        val createdTruck = truckController.createTruck(
            plateNumber = truck.plateNumber,
            type = truck.type,
            vin = truck.vin,
            name = truck.name,
            userId = userId
        )
        createOk(truckTranslator.translate(createdTruck))
    }.asUni()

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findTruck(truckId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val truck = truckController.findTruck(truckId) ?: return@async createNotFound(createNotFoundMessage(TRUCK, truckId))

        createOk(truckTranslator.translate(truck))
    }.asUni()

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listTrucks(plateNumber: String?, archived: Boolean?, first: Int?, max: Int?): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val ( trucks, count ) = truckController.listTrucks(plateNumber, archived, first, max)
        createOk(trucks.map { truckTranslator.translate(it) }, count)
    }.asUni()

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateTruck(truckId: UUID, truck: fi.metatavu.vp.api.model.Truck): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        if (!vehicleController.isPlateNumberValid(truck.plateNumber) || truck.vin.isEmpty()) {
            return@async createBadRequest(INVALID_PLATE_NUMBER)
        }

        val existingTruck = truckController.findTruck(truckId) ?: return@async createNotFound(createNotFoundMessage(TRUCK, truckId))

        if (existingTruck.archivedAt != null && truck.archivedAt != null) {
            return@async createConflict("Archived truck cannot be updated")
        }

        if (!vehicleController.isPlateNumberUnique(truck.plateNumber) && existingTruck.plateNumber != truck.plateNumber) {
            return@async createBadRequest(NOT_UNIQUE_PLATE_NUMBER)
        }
        if (!vehicleController.isVinUnique(truck.vin) && existingTruck.vin != truck.vin) {
            return@async createBadRequest(NOT_UNIQUE_VIN)
        }

        val updated = truckController.updateTruck(existingTruck, truck, userId)

        createOk(truckTranslator.translate(updated))
    }.asUni()

   @RolesAllowed(MANAGER_ROLE)
   @WithTransaction
   override fun deleteTruck(truckId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
       val existingTruck = truckController.findTruck(truckId) ?: return@async createNotFound(createNotFoundMessage(TRUCK, truckId))
       if (isProduction) return@async createForbidden(FORBIDDEN)
       val partOfVehicles = vehicleController.listVehicles(existingTruck)
       if (partOfVehicles.isNotEmpty()) {
           return@async createBadRequest("Truck is part of a vehicle")
       }
       truckController.deleteTruck(existingTruck)
       createNoContent()
   }.asUni()

    // Driver cards

    override fun listTruckDriverCards(truckId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        if (requestApiKey != apiKey) return@async createForbidden(INVALID_API_KEY)
        val truck = truckController.findTruck(truckId) ?: return@async createNotFound(createNotFoundMessage(TRUCK, truckId))
        val (cards, count) = driverCardController.listDriverCards(truck)
        createOk(driverCardTranslator.translate(cards), count)
    }.asUni()

    @WithTransaction
    override fun createTruckDriverCard(truckId: UUID, truckDriverCard: TruckDriverCard): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        if (requestApiKey != apiKey) return@async createForbidden(INVALID_API_KEY)

        val driverCardWithId = driverCardController.findDriverCard(truckDriverCard.id)
        if (driverCardWithId != null) {
            return@async createConflict("Driver card already exists")
        }

        val truck = truckController.findTruck(truckId) ?: return@async createNotFound(createNotFoundMessage(TRUCK, truckId))
        if (driverCardController.listDriverCards(truck).second > 0) {
            return@async createConflict("Truck already contains driver's card")
        }

        val insertedCard = driverCardController.createDriverCard(
            driverCardId = truckDriverCard.id,
            truck = truck
        )
        createOk(driverCardTranslator.translate(insertedCard))
    }.asUni()

    @WithTransaction
    override fun deleteTruckDriverCard(
        truckId: UUID,
        driverCardId: String
    ): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        if (requestApiKey != apiKey) return@async createForbidden(INVALID_API_KEY)
        val truck = truckController.findTruck(truckId) ?: return@async createNotFound(createNotFoundMessage(TRUCK, truckId))
        val insertedDriverCard = driverCardController.findDriverCard(driverCardId) ?: return@async createNotFound(createNotFoundMessage(DRIVER_CARD, driverCardId))
        if (insertedDriverCard.truck.id != truck.id) {
            return@async createNotFound(createNotFoundMessage(DRIVER_CARD, driverCardId))
        }

        driverCardController.deleteDriverCard(insertedDriverCard)
        createNoContent()
    }.asUni()

    // Truck location endpoints
    @RolesAllowed(MANAGER_ROLE)
    override fun listTruckLocations(
        truckId: UUID,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        TODO("Not yet implemented")
    }.asUni()

    @WithTransaction
    override fun createTruckLocation(truckId: UUID, truckLocation: TruckLocation): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        if (requestApiKey != apiKey) return@async createForbidden(INVALID_API_KEY)
        val truck = truckController.findTruck(truckId) ?: return@async createNotFound(createNotFoundMessage(TRUCK, truckId))
        val insertedLocation = truckController.createTruckLocation(truck, truckLocation)
        createAccepted(insertedLocation)
    }.asUni()
}
package fi.metatavu.vp.vehicleManagement.vehicles

import fi.metatavu.vp.api.model.Vehicle
import fi.metatavu.vp.api.spec.VehiclesApi
import fi.metatavu.vp.vehicleManagement.rest.AbstractApi
import fi.metatavu.vp.vehicleManagement.trailers.TrailerController
import fi.metatavu.vp.vehicleManagement.trucks.TruckController
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
import java.util.*

@RequestScoped
@OptIn(ExperimentalCoroutinesApi::class)
@WithSession
class VehiclesApiImpl: VehiclesApi, AbstractApi() {

    @Inject
    lateinit var vehicleController: VehicleController

    @Inject
    lateinit var truckController: TruckController

    @Inject
    lateinit var trailerController: TrailerController

    @Inject
    lateinit var vehicleTranslator: VehicleTranslator

    @Inject
    lateinit var vertx: Vertx

    override fun listVehicles(truckId: UUID?, first: Int?, max: Int?): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val truckFilter = if (truckId != null) {
            truckController.findTruck(truckId) ?: return@async createBadRequest(createNotFoundMessage(TRUCK, truckId))
        } else {
            null
        }

        val ( vehicles, count ) = vehicleController.listVehicles(truckFilter, first, max)
        createOk(vehicleTranslator.translate(vehicles), count)
    }.asUni()

    override fun createVehicle(vehicle: Vehicle): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        isInvalidVehicle(vehicle).let { response ->
            if (response != null) {
                return@async response
            }
        }

        val truck = truckController.findTruck(vehicle.truckId) ?: return@async createBadRequest(createNotFoundMessage(TRUCK, vehicle.truckId))
        val trailers = vehicle.trailerIds.map {
            val trailer = trailerController.findTrailer(it) ?: return@async createBadRequest(createNotFoundMessage(TRAILER, it))
            trailer
        }
        val createdVehicle = vehicleController.create(
            truck = truck,
            trailers = trailers,
            userId = userId
        )

        createOk(vehicleTranslator.translate(createdVehicle))
    }.asUni()

    private fun isInvalidVehicle(vehicle: Vehicle): Response? {
        if (vehicle.trailerIds.size > 2) {
            return createBadRequest("Vehicle can have maximum of 2 trailers")
        }

        val distinctTrailers = vehicle.trailerIds.distinct().size
        if (distinctTrailers != vehicle.trailerIds.size) {
            return createBadRequest("Vehicle cannot have duplicate trailers")
        }

        return null
    }

    override fun findVehicle(vehicleId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val vehicle = vehicleController.find(vehicleId) ?: return@async createNotFound(createNotFoundMessage(VEHICLE, vehicleId))

        createOk(vehicleTranslator.translate(vehicle))
    }.asUni()

    override fun updateVehicle(vehicleId: UUID, vehicle: Vehicle): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        isInvalidVehicle(vehicle).let { response ->
            if (response != null) {
                return@async response
            }
        }

        val foundVehicle = vehicleController.find(vehicleId) ?: return@async createNotFound(createNotFoundMessage(VEHICLE, vehicleId))
        val newTruck = truckController.findTruck(vehicle.truckId) ?: return@async createBadRequest(createNotFoundMessage(TRUCK, vehicle.truckId))
        val newTrailers = vehicle.trailerIds.map {
            val trailer = trailerController.findTrailer(it) ?: return@async createBadRequest(createNotFoundMessage(TRAILER, it))
            trailer
        }

        val updatedVehicle = vehicleController.update(
            existingVehicle = foundVehicle,
            newTruck = newTruck,
            newTrailers = newTrailers,
            userId = userId
        )

        createOk(vehicleTranslator.translate(updatedVehicle))
    }.asUni()

    override fun deleteVehicle(vehicleId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val vehicle = vehicleController.find(vehicleId) ?: return@async createNotFound(createNotFoundMessage(VEHICLE, vehicleId))
        vehicleController.delete(vehicle)
        createNoContent()
    }.asUni()
}
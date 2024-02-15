package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.api.model.Vehicle
import fi.metatavu.vp.api.spec.VehiclesApi
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.towables.TowableController
import fi.metatavu.vp.vehiclemanagement.trucks.TruckController
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
import java.util.*

/**
 * Vehicles API implementation
 */
@RequestScoped
@OptIn(ExperimentalCoroutinesApi::class)
@WithSession
class VehiclesApiImpl: VehiclesApi, AbstractApi() {

    @Inject
    lateinit var vehicleController: VehicleController

    @Inject
    lateinit var truckController: TruckController

    @Inject
    lateinit var towableController: TowableController

    @Inject
    lateinit var vehicleTranslator: VehicleTranslator

    @Inject
    lateinit var vertx: Vertx

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listVehicles(truckId: UUID?, archived: Boolean?, first: Int?, max: Int?): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val truckFilter = if (truckId != null) {
            truckController.findTruck(truckId) ?: return@async createBadRequest(createNotFoundMessage(TRUCK, truckId))
        } else {
            null
        }

        val ( vehicles, count ) = vehicleController.listVehicles(truckFilter, archived, first, max)
        createOk(vehicleTranslator.translate(vehicles), count)
    }.asUni()

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createVehicle(vehicle: Vehicle): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        isInvalidVehicle(vehicle)?.let {
            return@async it
        }

        val truck = truckController.findTruck(vehicle.truckId) ?: return@async createBadRequest(createNotFoundMessage(TRUCK, vehicle.truckId))
        val towables = vehicle.towableIds.map {
            val towable = towableController.findTowable(it) ?: return@async createBadRequest(createNotFoundMessage(TOWABLE, it))
            towable
        }
        val createdVehicle = vehicleController.create(
            truck = truck,
            towables = towables,
            userId = userId
        )

        createOk(vehicleTranslator.translate(createdVehicle))
    }.asUni()

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findVehicle(vehicleId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val vehicle = vehicleController.find(vehicleId) ?: return@async createNotFound(createNotFoundMessage(VEHICLE, vehicleId))

        createOk(vehicleTranslator.translate(vehicle))
    }.asUni()

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateVehicle(vehicleId: UUID, vehicle: Vehicle): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        isInvalidVehicle(vehicle)?.let {
            return@async it
        }

        val foundVehicle = vehicleController.find(vehicleId) ?: return@async createNotFound(createNotFoundMessage(VEHICLE, vehicleId))
        if (foundVehicle.archivedAt != null && vehicle.archivedAt != null) {
            return@async createConflict("Archived vehicle cannot be updated")
        }

        val newTruck = truckController.findTruck(vehicle.truckId) ?: return@async createBadRequest(createNotFoundMessage(TRUCK, vehicle.truckId))
        val newTowables = vehicle.towableIds.map {
            val towable = towableController.findTowable(it) ?: return@async createBadRequest(createNotFoundMessage(TOWABLE, it))
            towable
        }

        val updatedVehicle = vehicleController.update(
            existingVehicle = foundVehicle,
            vehicleUpdateData = vehicle,
            newTruck = newTruck,
            newTowables = newTowables,
            userId = userId
        )

        createOk(vehicleTranslator.translate(updatedVehicle))
    }.asUni()

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteVehicle(vehicleId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        if (isProduction) return@async createForbidden(FORBIDDEN)
        val vehicle = vehicleController.find(vehicleId) ?: return@async createNotFound(createNotFoundMessage(VEHICLE, vehicleId))
        vehicleController.delete(vehicle)
        createNoContent()
    }.asUni()


    /**
     * Checks if the given vehicle is valid
     *
     * @param vehicle vehicle to be checked
     * @return response if invalid, null if valid
     */
    private fun isInvalidVehicle(vehicle: Vehicle): Response? {
        val distinctTowables = vehicle.towableIds.distinct().size
        if (distinctTowables != vehicle.towableIds.size) {
            return createBadRequest("Vehicle cannot have duplicate towables")
        }

        return null
    }
}
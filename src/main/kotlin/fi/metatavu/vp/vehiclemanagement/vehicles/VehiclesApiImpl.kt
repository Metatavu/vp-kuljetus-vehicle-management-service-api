package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.api.model.Vehicle
import fi.metatavu.vp.api.spec.VehiclesApi
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.towables.TowableController
import fi.metatavu.vp.vehiclemanagement.trucks.TruckController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.util.*

/**
 * Vehicles API implementation
 */
@RequestScoped
@WithSession
@Suppress("unused")
class VehiclesApiImpl: VehiclesApi, AbstractApi() {

    @Inject
    lateinit var vehicleController: VehicleController

    @Inject
    lateinit var truckController: TruckController

    @Inject
    lateinit var towableController: TowableController

    @Inject
    lateinit var vehicleTranslator: VehicleTranslator

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listVehicles(truckId: UUID?, archived: Boolean?, first: Int?, max: Int?): Uni<Response> = withCoroutineScope {
        val truckFilter = if (truckId != null) {
            truckController.findTruck(truckId) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(TRUCK, truckId))
        } else null

        val ( vehicles, count ) = vehicleController.listVehicles(truckFilter, archived, first, max)
        createOk(vehicleTranslator.translate(vehicles), count)
    }

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    @WithTransaction
    override fun createVehicle(vehicle: Vehicle): Uni<Response> = withCoroutineScope {
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        isInvalidVehicle(vehicle)?.let {
            return@withCoroutineScope it
        }

        val truck = truckController.findTruck(vehicle.truckId) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(TRUCK, vehicle.truckId))
        val towables = vehicle.towableIds.map {
            towableController.findTowable(it) ?: return@withCoroutineScope createBadRequest(createNotFoundMessage(TOWABLE, it))
        }
        val createdVehicle = vehicleController.create(
            truck = truck,
            towables = towables,
            userId = userId
        )

        createOk(vehicleTranslator.translate(createdVehicle))
    }

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findVehicle(vehicleId: UUID): Uni<Response> = withCoroutineScope {
        val vehicle = vehicleController.find(vehicleId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(VEHICLE, vehicleId))

        createOk(vehicleTranslator.translate(vehicle))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteVehicle(vehicleId: UUID): Uni<Response> = withCoroutineScope {
        if (isProduction) return@withCoroutineScope createForbidden(FORBIDDEN)
        val vehicle = vehicleController.find(vehicleId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(VEHICLE, vehicleId))
        vehicleController.delete(vehicle)
        createNoContent()
    }

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
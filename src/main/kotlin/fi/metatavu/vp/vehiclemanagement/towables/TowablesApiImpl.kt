package fi.metatavu.vp.vehiclemanagement.towables

import fi.metatavu.vp.api.spec.TowablesApi
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.vehicles.VehicleController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.util.*

/**
 * Towables API implementation
 */
@RequestScoped
@WithSession
@Suppress("unused")
class TowablesApiImpl : TowablesApi, AbstractApi() {

    @Inject
    lateinit var towableTranslator: TowableTranslator

    @Inject
    lateinit var towableController: TowableController

    @Inject
    lateinit var vehicleController: VehicleController

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createTowable(towable: fi.metatavu.vp.api.model.Towable): Uni<Response> =
        withCoroutineScope {
            val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

            if (!vehicleController.isPlateNumberValid(towable.plateNumber)) {
                return@withCoroutineScope createBadRequest(INVALID_PLATE_NUMBER)
            }
            if (towable.vin.isEmpty()) {
                return@withCoroutineScope createBadRequest(INVALID_VIN)
            }

            if (!vehicleController.isPlateNumberUnique(towable.plateNumber)) return@withCoroutineScope createBadRequest(
                NOT_UNIQUE_PLATE_NUMBER
            )
            if (!vehicleController.isVinUnique(towable.vin)) return@withCoroutineScope createBadRequest(NOT_UNIQUE_VIN)

            val createdTruck = towableController.createTowable(
                plateNumber = towable.plateNumber,
                type = towable.type,
                vin = towable.vin,
                name = towable.name,
                userId = userId
            )
            createOk(towableTranslator.translate(createdTruck))
        }

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findTowable(towableId: UUID): Uni<Response> = withCoroutineScope {
        val truck = towableController.findTowable(towableId) ?: return@withCoroutineScope createNotFound(
            createNotFoundMessage(
                TOWABLE,
                towableId
            )
        )

        createOk(towableTranslator.translate(truck))
    }

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listTowables(plateNumber: String?, archived: Boolean?, first: Int?, max: Int?): Uni<Response> =
        withCoroutineScope {
            val (trucks, count) = towableController.listTowables(plateNumber, archived, first, max)
            createOk(trucks.map { towableTranslator.translate(it) }, count)
        }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateTowable(towableId: UUID, towable: fi.metatavu.vp.api.model.Towable): Uni<Response> =
        withCoroutineScope {
            val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

            if (!vehicleController.isPlateNumberValid(towable.plateNumber)) {
                return@withCoroutineScope createBadRequest(INVALID_PLATE_NUMBER)
            }
            if (towable.vin.isEmpty()) {
                return@withCoroutineScope createBadRequest(INVALID_VIN)
            }

            val existingTowable = towableController.findTowable(towableId) ?: return@withCoroutineScope createNotFound(
                createNotFoundMessage(
                    TOWABLE,
                    towableId
                )
            )

            if (existingTowable.archivedAt != null && towable.archivedAt != null) {
                return@withCoroutineScope createConflict("Archived towable cannot be updated")
            }

            if (!vehicleController.isPlateNumberUnique(towable.plateNumber) && existingTowable.plateNumber != towable.plateNumber) {
                return@withCoroutineScope createBadRequest(NOT_UNIQUE_PLATE_NUMBER)
            }
            if (!vehicleController.isVinUnique(towable.vin) && existingTowable.vin != towable.vin) {
                return@withCoroutineScope createBadRequest(NOT_UNIQUE_VIN)
            }

            val updatedTowable = towableController.updateTowable(existingTowable, towable, userId)

            createOk(towableTranslator.translate(updatedTowable))
        }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteTowable(towableId: UUID): Uni<Response> = withCoroutineScope {
        loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        if (isProduction) return@withCoroutineScope createForbidden(FORBIDDEN)
        val existingTowable = towableController.findTowable(towableId) ?: return@withCoroutineScope createNotFound(
            createNotFoundMessage(
                TOWABLE,
                towableId
            )
        )

        val partOfVehicles = vehicleController.listTowableToVehicles(existingTowable)
        if (partOfVehicles.isNotEmpty()) {
            return@withCoroutineScope createBadRequest("Towable is part of a vehicle")
        }

        towableController.deleteTowable(existingTowable)
        createNoContent()
    }
}
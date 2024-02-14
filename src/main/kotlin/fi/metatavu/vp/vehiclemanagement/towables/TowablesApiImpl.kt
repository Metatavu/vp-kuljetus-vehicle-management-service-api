package fi.metatavu.vp.vehiclemanagement.towables

import fi.metatavu.vp.api.spec.TowablesApi
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
import java.util.*

/**
 * Towables API implementation
 */
@RequestScoped
@OptIn(ExperimentalCoroutinesApi::class)
@WithSession
class TowablesApiImpl : TowablesApi, AbstractApi() {

    @Inject
    lateinit var towableTranslator: TowableTranslator

    @Inject
    lateinit var towableController: TowableController

    @Inject
    lateinit var vehicleController: VehicleController

    @Inject
    lateinit var vertx: Vertx

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createTowable(towable: fi.metatavu.vp.api.model.Towable): Uni<Response> =
        CoroutineScope(vertx.dispatcher()).async {
            val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

            if (!vehicleController.isPlateNumberValid(towable.plateNumber)) {
                return@async createBadRequest(INVALID_PLATE_NUMBER)
            }
            if (towable.vin.isEmpty()) {
                return@async createBadRequest(INVALID_VIN)
            }

            if (!vehicleController.isPlateNumberUnique(towable.plateNumber)) return@async createBadRequest(NOT_UNIQUE_PLATE_NUMBER)
            if (!vehicleController.isVinUnique(towable.vin)) return@async createBadRequest(NOT_UNIQUE_VIN)

            val createdTruck = towableController.createTowable(
                plateNumber = towable.plateNumber,
                type = towable.type,
                vin = towable.vin,
                userId = userId
            )
            createOk(towableTranslator.translate(createdTruck))
        }.asUni()

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findTowable(towableId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val truck = towableController.findTowable(towableId) ?: return@async createNotFound(
            createNotFoundMessage(
                TOWABLE,
                towableId
            )
        )

        createOk(towableTranslator.translate(truck))
    }.asUni()

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listTowables(plateNumber: String?, first: Int?, max: Int?): Uni<Response> =
        CoroutineScope(vertx.dispatcher()).async {
            val (trucks, count) = towableController.listTowables(plateNumber, first, max)
            createOk(trucks.map { towableTranslator.translate(it) }, count)
        }.asUni()

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateTowable(towableId: UUID, towable: fi.metatavu.vp.api.model.Towable): Uni<Response> =
        CoroutineScope(vertx.dispatcher()).async {
            val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

            if (!vehicleController.isPlateNumberValid(towable.plateNumber)) {
                return@async createBadRequest(INVALID_PLATE_NUMBER)
            }
            if (towable.vin.isEmpty()) {
                return@async createBadRequest(INVALID_VIN)
            }

            val existingTowable = towableController.findTowable(towableId) ?: return@async createNotFound(
                createNotFoundMessage(
                    TOWABLE,
                    towableId
                )
            )

            if (!vehicleController.isPlateNumberUnique(towable.plateNumber) && existingTowable.plateNumber != towable.plateNumber) {
                return@async createBadRequest(NOT_UNIQUE_PLATE_NUMBER)
            }
            if (!vehicleController.isVinUnique(towable.vin) && existingTowable.vin != towable.vin) {
                return@async createBadRequest(NOT_UNIQUE_VIN)
            }

            val updatedTowable = towableController.updateTowable(existingTowable, towable, userId)

            createOk(towableTranslator.translate(updatedTowable))
        }.asUni()

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun deleteTowable(towableId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        val existingTowable = towableController.findTowable(towableId) ?: return@async createNotFound(
            createNotFoundMessage(
                TOWABLE,
                towableId
            )
        )

        val partOfVehicles = vehicleController.listTowableToVehicles(existingTowable)
        if (partOfVehicles.isNotEmpty()) {
            return@async createBadRequest("Towable is part of a vehicle")
        }

        towableController.deleteTowable(existingTowable)
        createNoContent()
    }.asUni()
}
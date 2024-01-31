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
class TowablesApiImpl : TowablesApi, AbstractApi() {

    @Inject
    lateinit var towableTranslator: TowableTranslator

    @Inject
    lateinit var towableController: TowableController

    @Inject
    lateinit var vehicleController: VehicleController

    @Inject
    lateinit var vertx: Vertx

    @WithTransaction
    override fun createTowable(towable: fi.metatavu.vp.api.model.Towable): Uni<Response> =
        CoroutineScope(vertx.dispatcher()).async {
            val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

            if (vehicleController.isPlateNumberValid(towable.plateNumber).not()) {
                return@async createBadRequest(INVALID_PLATE_NUMBER)
            }

            val createdTruck = towableController.createTowable(
                plateNumber = towable.plateNumber,
                type = towable.type,
                userId = userId
            )
            createOk(towableTranslator.translate(createdTruck))
        }.asUni()

    override fun findTowable(towabelId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val truck = towableController.findTowable(towabelId) ?: return@async createNotFound(
            createNotFoundMessage(
                TOWABLE,
                towabelId
            )
        )

        createOk(towableTranslator.translate(truck))
    }.asUni()

    override fun listTowables(plateNumber: String?, first: Int?, max: Int?): Uni<Response> =
        CoroutineScope(vertx.dispatcher()).async {
            val (trucks, count) = towableController.listTowables(plateNumber, first, max)
            createOk(trucks.map { towableTranslator.translate(it) }, count)
        }.asUni()

    @WithTransaction
    override fun updateTowable(towableId: UUID, towable: fi.metatavu.vp.api.model.Towable): Uni<Response> =
        CoroutineScope(vertx.dispatcher()).async {
            val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

            if (vehicleController.isPlateNumberValid(towable.plateNumber).not()) {
                return@async createBadRequest(INVALID_PLATE_NUMBER)
            }

            val existingTowable = towableController.findTowable(towableId) ?: return@async createNotFound(
                createNotFoundMessage(
                    TOWABLE,
                    towableId
                )
            )

            val updatedTowable = towableController.updateTowable(existingTowable, towable, userId)

            createOk(towableTranslator.translate(updatedTowable))
        }.asUni()

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
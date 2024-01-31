package fi.metatavu.vp.vehicleManagement.trailers

import fi.metatavu.vp.api.spec.TrailersApi
import fi.metatavu.vp.vehicleManagement.rest.AbstractApi
import fi.metatavu.vp.vehicleManagement.vehicles.VehicleController
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
class TrailersApiImpl : TrailersApi, AbstractApi() {

    @Inject
    lateinit var trailerTranslator: TrailerTranslator

    @Inject
    lateinit var trailerController: TrailerController

    @Inject
    lateinit var vehicleController: VehicleController

    @Inject
    lateinit var vertx: Vertx

    @WithTransaction
    override fun createTrailer(trailer: fi.metatavu.vp.api.model.Trailer): Uni<Response> =
        CoroutineScope(vertx.dispatcher()).async {
            val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

            if (!vehicleController.isPlateNumberValid(trailer.plateNumber)) {
                return@async createBadRequest(INVALID_PLATE_NUMBER)
            }

            if (!vehicleController.isPlateNumberUnique(trailer.plateNumber)) {
                return@async createBadRequest(NOT_UNIQUE_PLATE_NUMBER)
            }

            val createdTruck = trailerController.createTrailer(
                plateNumber = trailer.plateNumber,
                userId = userId
            )
            createOk(trailerTranslator.translate(createdTruck))
        }.asUni()

    override fun findTrailer(trailerId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        val truck = trailerController.findTrailer(trailerId) ?: return@async createNotFound(
            createNotFoundMessage(
                TRAILER,
                trailerId
            )
        )

        createOk(trailerTranslator.translate(truck))
    }.asUni()

    override fun listTrailers(plateNumber: String?, first: Int?, max: Int?): Uni<Response> =
        CoroutineScope(vertx.dispatcher()).async {
            val (trucks, count) = trailerController.listTrailers(plateNumber, first, max)
            createOk(trucks.map { trailerTranslator.translate(it) }, count)
        }.asUni()

    @WithTransaction
    override fun updateTrailer(trailerId: UUID, trailer: fi.metatavu.vp.api.model.Trailer): Uni<Response> =
        CoroutineScope(vertx.dispatcher()).async {
            val userId = loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

            if (!vehicleController.isPlateNumberValid(trailer.plateNumber)) {
                return@async createBadRequest(INVALID_PLATE_NUMBER)
            }

            val existingTrailer = trailerController.findTrailer(trailerId) ?: return@async createNotFound(
                createNotFoundMessage(
                    TRAILER,
                    trailerId
                )
            )

            if (!vehicleController.isPlateNumberUnique(trailer.plateNumber) && existingTrailer.plateNumber != trailer.plateNumber) {
                return@async createBadRequest(NOT_UNIQUE_PLATE_NUMBER)
            }

            val updatedTrailer = trailerController.updateTrailer(existingTrailer, trailer, userId)

            createOk(trailerTranslator.translate(updatedTrailer))
        }.asUni()

    @WithTransaction
    override fun deleteTrailer(trailerId: UUID): Uni<Response> = CoroutineScope(vertx.dispatcher()).async {
        loggedUserId ?: return@async createUnauthorized(UNAUTHORIZED)

        val existingTrailer = trailerController.findTrailer(trailerId) ?: return@async createNotFound(
            createNotFoundMessage(
                TRAILER,
                trailerId
            )
        )

        val partOfVehicles = vehicleController.listTrailerVehicles(existingTrailer)
        if (partOfVehicles.isNotEmpty()) {
            return@async createBadRequest("Trailer is part of a vehicle")
        }

        trailerController.deleteTrailer(existingTrailer)
        createNoContent()
    }.asUni()
}
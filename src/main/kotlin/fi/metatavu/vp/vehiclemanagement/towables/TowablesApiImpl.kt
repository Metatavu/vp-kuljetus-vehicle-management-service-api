package fi.metatavu.vp.vehiclemanagement.towables

import fi.metatavu.vp.vehiclemanagement.model.Towable
import fi.metatavu.vp.vehiclemanagement.spec.TowablesApi
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.thermometers.ThermometerController
import fi.metatavu.vp.vehiclemanagement.thermometers.temperatureReadings.TemperatureReadingController
import fi.metatavu.vp.vehiclemanagement.thermometers.temperatureReadings.TemperatureTranslator
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

    @Inject
    lateinit var temperatureReadingController: TemperatureReadingController

    @Inject
    lateinit var temperatureTranslator: TemperatureTranslator

    @Inject
    lateinit var thermometerController: ThermometerController

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createTowable(towable: Towable): Uni<Response> =
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
            if (!vehicleController.isImeiUnique(towable.imei)) return@withCoroutineScope createBadRequest(NOT_UNIQUE_IMEI)

            val createdTruck = towableController.createTowable(
                plateNumber = towable.plateNumber,
                type = towable.type,
                vin = towable.vin,
                imei = towable.imei,
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
    override fun listTowables(plateNumber: String?, archived: Boolean?, thermometerId: UUID?, first: Int?, max: Int?): Uni<Response> =
        withCoroutineScope {
            if (thermometerId != null) {
                val thermometer = thermometerController.findThermometer(thermometerId)
                    ?: return@withCoroutineScope createOk(emptyList<Towable>())

                val towable = thermometer.towable ?: return@withCoroutineScope createOk(emptyList<Towable>())

                val returnList = mutableListOf<Towable>()
                val archivedParameterMatches = (archived == true && towable.archivedAt != null) || (archived != true && towable.archivedAt == null)
                val plateNumberParameterMatches = plateNumber.isNullOrEmpty() || plateNumber == towable.plateNumber
                if ((first == 0 || first == null) && (max == null || max > 0) && archivedParameterMatches && plateNumberParameterMatches) {
                    returnList.add(towableTranslator.translate(towable))
                }

                return@withCoroutineScope createOk(returnList, returnList.size.toLong())
            } else {
                val (towables, count) = towableController.listTowables(plateNumber, archived, first, max)
                createOk(towables.map { towableTranslator.translate(it) }, count)
            }
        }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateTowable(towableId: UUID, towable: Towable): Uni<Response> =
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
            if (!vehicleController.isImeiUnique(towable.imei) && existingTowable.imei != towable.imei) {
                return@withCoroutineScope createBadRequest(NOT_UNIQUE_IMEI)
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

    @RolesAllowed(MANAGER_ROLE)
    override fun listTowableTemperatures(
        towableId: UUID,
        includeArchived: Boolean,
        first: Int?,
        max: Int?
    ): Uni<Response> = withCoroutineScope {
        val towable = towableController.findTowable(towableId) ?: return@withCoroutineScope createNotFound(
            createNotFoundMessage(
                TOWABLE,
                towableId
            )
        )

        val (temperatures, count) = temperatureReadingController.listTowableTemperatures(
            towable = towable,
            includeArchived = includeArchived,
            first = first,
            max = max
        )

        createOk(temperatureTranslator.translate(temperatures), count)
    }
}
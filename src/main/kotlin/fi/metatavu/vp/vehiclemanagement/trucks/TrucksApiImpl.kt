package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.api.model.*
import fi.metatavu.vp.api.spec.TrucksApi
import fi.metatavu.vp.vehiclemanagement.integrations.UserManagementService
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.trucks.drivercards.DriverCardController
import fi.metatavu.vp.vehiclemanagement.trucks.drivercards.DriverCardTranslator
import fi.metatavu.vp.vehiclemanagement.trucks.drivestate.TruckDriveStateController
import fi.metatavu.vp.vehiclemanagement.trucks.drivestate.TruckDriveStateTranslator
import fi.metatavu.vp.vehiclemanagement.trucks.location.TruckLocationController
import fi.metatavu.vp.vehiclemanagement.trucks.location.TruckLocationTranslator
import fi.metatavu.vp.vehiclemanagement.trucks.truckspeed.TruckSpeedController
import fi.metatavu.vp.vehiclemanagement.trucks.truckspeed.TruckSpeedTranslator
import fi.metatavu.vp.vehiclemanagement.vehicles.VehicleController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.time.OffsetDateTime
import java.util.*

/**
 * Trucks API implementation
 */
@RequestScoped
@WithSession
@Suppress("unused")
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
    lateinit var truckLocationController: TruckLocationController

    @Inject
    lateinit var truckLocationTranslator: TruckLocationTranslator

    @Inject
    lateinit var truckSpeedController: TruckSpeedController

    @Inject
    lateinit var truckSpeedTranslator: TruckSpeedTranslator

    @Inject
    lateinit var truckDriveStateController: TruckDriveStateController

    @Inject
    lateinit var truckDriveStateTranslator: TruckDriveStateTranslator

    @Inject
    lateinit var userManagementService: UserManagementService

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun createTruck(truck: fi.metatavu.vp.api.model.Truck): Uni<Response> = withCoroutineScope({
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        if (!vehicleController.isPlateNumberValid(truck.plateNumber) || truck.vin.isEmpty()) {
            return@withCoroutineScope createBadRequest(INVALID_PLATE_NUMBER)
        }

        if (!vehicleController.isPlateNumberUnique(truck.plateNumber)) return@withCoroutineScope createBadRequest(NOT_UNIQUE_PLATE_NUMBER)
        if (!vehicleController.isVinUnique(truck.vin)) return@withCoroutineScope createBadRequest(NOT_UNIQUE_VIN)

        val createdTruck = truckController.createTruck(
            plateNumber = truck.plateNumber,
            type = truck.type,
            vin = truck.vin,
            name = truck.name,
            userId = userId
        )
        createOk(truckTranslator.translate(createdTruck))
    })

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun findTruck(truckId: UUID): Uni<Response> = withCoroutineScope({
        val truck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))

        createOk(truckTranslator.translate(truck))
    })

    @RolesAllowed(DRIVER_ROLE, MANAGER_ROLE)
    override fun listTrucks(
        plateNumber: String?,
        archived: Boolean?,
        sortBy: TruckSortByField?,
        sortDirection: SortOrder?,
        first: Int?,
        max: Int?
    ): Uni<Response> = withCoroutineScope({
        val ( trucks, count ) = truckController.listTrucks(
            plateNumber = plateNumber,
            archived = archived,
            sortBy = sortBy,
            sortDirection = sortDirection,
            firstResult = first,
            maxResults = max
        )
        createOk(trucks.map { truckTranslator.translate(it) }, count)
    })

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateTruck(truckId: UUID, truck: fi.metatavu.vp.api.model.Truck): Uni<Response> = withCoroutineScope({
        val userId = loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)

        if (!vehicleController.isPlateNumberValid(truck.plateNumber) || truck.vin.isEmpty()) {
            return@withCoroutineScope createBadRequest(INVALID_PLATE_NUMBER)
        }

        val existingTruck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))

        if (existingTruck.archivedAt != null && truck.archivedAt != null) {
            return@withCoroutineScope createConflict("Archived truck cannot be updated")
        }

        if (!vehicleController.isPlateNumberUnique(truck.plateNumber) && existingTruck.plateNumber != truck.plateNumber) {
            return@withCoroutineScope createBadRequest(NOT_UNIQUE_PLATE_NUMBER)
        }
        if (!vehicleController.isVinUnique(truck.vin) && existingTruck.vin != truck.vin) {
            return@withCoroutineScope createBadRequest(NOT_UNIQUE_VIN)
        }

        val updated = truckController.updateTruck(existingTruck, truck, userId)

        createOk(truckTranslator.translate(updated))
    })

   @RolesAllowed(MANAGER_ROLE)
   @WithTransaction
   override fun deleteTruck(truckId: UUID): Uni<Response> = withCoroutineScope({
       val existingTruck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))
       if (isProduction) return@withCoroutineScope createForbidden(FORBIDDEN)
       val partOfVehicles = vehicleController.listVehicles(existingTruck)
       if (partOfVehicles.isNotEmpty()) {
           return@withCoroutineScope createBadRequest("Truck is part of a vehicle")
       }
       truckController.deleteTruck(existingTruck)
       createNoContent()
   })

    // Driver cards

    override fun listTruckDriverCards(truckId: UUID): Uni<Response> = withCoroutineScope({
        if (loggedUserId == null && requestApiKey == null) return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        if (requestApiKey != null && requestApiKey != apiKey) return@withCoroutineScope createForbidden(INVALID_API_KEY)
        if (loggedUserId != null && !hasRealmRole(DRIVER_ROLE)) return@withCoroutineScope createForbidden(FORBIDDEN)

        val truck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))

        val (cards, count) = driverCardController.listDriverCards(truck)
        createOk(driverCardTranslator.translate(cards), count)
    })

    @WithTransaction
    override fun createTruckDriverCard(truckId: UUID, truckDriverCard: TruckDriverCard): Uni<Response> = withCoroutineScope({
        if (requestApiKey != apiKey) return@withCoroutineScope createForbidden(INVALID_API_KEY)

        val driverCardWithId = driverCardController.findDriverCard(truckDriverCard.id)
        if (driverCardWithId != null) {
            return@withCoroutineScope createConflict("Driver card already exists")
        }

        val truck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))
        if (driverCardController.listDriverCards(truck).second > 0) {
            return@withCoroutineScope createConflict("Truck already contains driver's card")
        }

        val insertedCard = driverCardController.createDriverCard(
            driverCardId = truckDriverCard.id,
            truck = truck
        )
        createOk(driverCardTranslator.translate(insertedCard))
    })

    @WithTransaction
    override fun deleteTruckDriverCard(
        truckId: UUID,
        driverCardId: String
    ): Uni<Response> = withCoroutineScope({
        if (requestApiKey != apiKey) return@withCoroutineScope createForbidden(INVALID_API_KEY)
        val truck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))
        val insertedDriverCard = driverCardController.findDriverCard(driverCardId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(DRIVER_CARD, driverCardId))
        if (insertedDriverCard.truck.id != truck.id) {
            return@withCoroutineScope createNotFound(createNotFoundMessage(DRIVER_CARD, driverCardId))
        }

        driverCardController.deleteDriverCard(insertedDriverCard)
        createNoContent()
    })

    // Truck Speed endpoints

    @RolesAllowed(MANAGER_ROLE)
    override fun listTruckSpeeds(
        truckId: UUID,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Uni<Response> = withCoroutineScope({
        val truck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))
        val ( speeds, count ) = truckSpeedController.listTruckSpeeds(truck, after, before, first, max)
        createOk(truckSpeedTranslator.translate(speeds), count)
    })

    @WithTransaction
    override fun createTruckSpeed(truckId: UUID, truckSpeed: TruckSpeed): Uni<Response> = withCoroutineScope({
        if (requestApiKey != apiKey) return@withCoroutineScope createForbidden(INVALID_API_KEY)
        val truck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))
        truckSpeedController.createTruckSpeed(truck, truckSpeed)  ?: return@withCoroutineScope createAccepted(null)
        createCreated()
    })

    // Truck location endpoints
    @RolesAllowed(MANAGER_ROLE)
    override fun listTruckLocations(
        truckId: UUID,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Uni<Response> = withCoroutineScope({
        val truck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))
        val (locations, count) = truckLocationController.listTruckLocations(truck, after, before, first, max)
        createOk(truckLocationTranslator.translate(locations), count)
    })

    @WithTransaction
    override fun createTruckLocation(truckId: UUID, truckLocation: TruckLocation): Uni<Response> = withCoroutineScope({
        if (requestApiKey != apiKey) return@withCoroutineScope createForbidden(INVALID_API_KEY)
        val truck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))
        truckLocationController.createTruckLocation(truck, truckLocation) ?: return@withCoroutineScope createAccepted(null)
        createCreated()
    })

    // Truck Drive State endpoints

    @RolesAllowed(MANAGER_ROLE, DRIVER_ROLE)
    override fun listDriveStates(
      truckId: UUID,
      driverId: UUID?,
      state: List<TruckDriveStateEnum>?,
      after: OffsetDateTime?,
      before: OffsetDateTime?,
      first: Int?,
      max: Int?
  ) = withCoroutineScope({
        val truck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))
        val (states, count) = truckDriveStateController.listDriveStates(
            truck = truck,
            driverId = driverId,
            state = state,
            after = after,
            before = before,
            first = first,
            max = max
        )
        createOk(truckDriveStateTranslator.translate(states), count)
    })

    @WithTransaction
    override fun createDriveState(truckId: UUID, truckDriveState: TruckDriveState): Uni<Response> = withCoroutineScope({
        if (requestApiKey != apiKey) return@withCoroutineScope createForbidden(INVALID_API_KEY)
        val truck = truckController.findTruck(truckId) ?: return@withCoroutineScope createNotFound(createNotFoundMessage(TRUCK, truckId))

        truckDriveStateController.createDriveState(truck, truckDriveState) ?: return@withCoroutineScope createAccepted(null)
        createCreated()
    })
}
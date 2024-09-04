package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.model.Vehicle
import fi.metatavu.vp.vehiclemanagement.towables.TowableEntity
import fi.metatavu.vp.vehiclemanagement.towables.TowableRepository
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import fi.metatavu.vp.vehiclemanagement.trucks.TruckRepository
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.*

/**
 * Controller for vehicle related operations
 */
@ApplicationScoped
class VehicleController {

    @Inject
    lateinit var vehicleRepository: VehicleRepository

    @Inject
    lateinit var vehicleTowableRepository: VehicleTowableRepository

    @Inject
    lateinit var truckRepository: TruckRepository

    @Inject
    lateinit var towableRepository: TowableRepository

    /**
     * Lists vehicles
     *
     * @param truckEntity truck
     * @param archived archived
     * @param first first
     * @param max max
     * @return list of vehicles that contains the truck
     */
    suspend fun listVehicles(truckEntity: TruckEntity?, archived: Boolean?, first: Int? = null, max: Int? = null): Pair<List<VehicleEntity>, Long> {
        return vehicleRepository.list(truckEntity, archived, first, max)
    }

    /**
     * Lists vehicles
     *
     * @param truckEntity truck
     * @return list of vehicles that contains the truck
     */
    suspend fun listVehicles(truckEntity: TruckEntity?): List<VehicleEntity> {
        return vehicleRepository.listByTruck(truckEntity)
    }

    /**
     * Creates a new vehicle
     *
     * @param truckEntity truck for which vehicle is created
     * @param towableEntities towables
     * @param userId user id
     * @return created vehicle
     */
    suspend fun create(truckEntity: TruckEntity, towableEntities: List<TowableEntity>, userId: UUID): VehicleEntity {
        // Archive the vehicles that if their truck got reserved for this newly created one
        val truckVehicles = vehicleRepository.listByTruck(truckEntity)
        truckVehicles.forEach {
            it.archivedAt = OffsetDateTime.now()
            vehicleRepository.persistSuspending(it)
        }

        val createdVehicle = vehicleRepository.create(
            id = UUID.randomUUID(),
            truckEntity = truckEntity,
            userId = userId
        )

        towableEntities.forEachIndexed { index, towable ->
            vehicleTowableRepository.create(
                id = UUID.randomUUID(),
                vehicleEntity = createdVehicle,
                towableEntity = towable,
                order = index,
                userId = userId
            )
        }

        return createdVehicle
    }

    /**
     * Finds a vehicle by id
     *
     * @param vehicleId vehicle id
     * @return found vehicle or null if not found
     */
    suspend fun find(vehicleId: UUID): VehicleEntity? {
        return vehicleRepository.findByIdSuspending(vehicleId)
    }

    /**
     * Updates a vehicle
     *
     * @param existingVehicleEntity existing vehicle
     * @param vehicleUpdateData vehicle rest data
     * @param newTruckEntity new truck
     * @param newTowableEntities new towables
     * @param userId user id
     * @return updated vehicle
     */
    suspend fun update(
        existingVehicleEntity: VehicleEntity,
        vehicleUpdateData: Vehicle,
        newTruckEntity: TruckEntity,
        newTowableEntities: List<TowableEntity>,
        userId: UUID
    ): VehicleEntity {
        existingVehicleEntity.truck = newTruckEntity
        existingVehicleEntity.archivedAt = vehicleUpdateData.archivedAt
        existingVehicleEntity.lastModifierId = userId

        //remove connection to towables
        val towableVehicles = vehicleTowableRepository.listByVehicle(existingVehicleEntity)
        towableVehicles.forEach {
            vehicleTowableRepository.deleteSuspending(it)
        }
        //add new connections
        newTowableEntities.forEachIndexed { index, towable ->
            vehicleTowableRepository.create(
                id = UUID.randomUUID(),
                vehicleEntity = existingVehicleEntity,
                towableEntity = towable,
                order = index,
                userId = userId
            )
        }

        return vehicleRepository.persistSuspending(existingVehicleEntity)
    }

    /**
     * Deletes a vehicle
     *
     * @param vehicleEntity vehicle to be deleted
     */
    suspend fun delete(vehicleEntity: VehicleEntity) {
        val towableVehicles = vehicleTowableRepository.listByVehicle(vehicleEntity)
        towableVehicles.forEach {
            vehicleTowableRepository.deleteSuspending(it)
        }
        vehicleRepository.deleteSuspending(vehicleEntity)
        vehicleTowableRepository.flush().awaitSuspending()
    }

    /**
     * Checks if the given plate number is valid
     *
     * @param plateNumber plate number
     * @return true if the plate number is valid
     */
    fun isPlateNumberValid(plateNumber: String): Boolean {
        val isValidLength = plateNumber.isNotEmpty()
            && plateNumber.isNotBlank()
            && plateNumber.length >= 2
        if (!isValidLength) {
            return false
        }
        val illegalSymbols = plateNumber.contains("?") || plateNumber.contains("*") || plateNumber.contains("!")
        if (illegalSymbols) {
            return false
        }

        return true
    }

    /**
     * Checks if plate number is unique (not present in both trucks and trailers)
     *
     * @param plateNumber plate number
     * @return true if unique
     */
    suspend fun isPlateNumberUnique(plateNumber: String): Boolean {
        val duplicateTruckPlates = truckRepository.countByPlateNumber(plateNumber)
        if (duplicateTruckPlates > 0) {
            return false
        }
        val duplicateTrailerPlates = towableRepository.countByPlateNumber(plateNumber)
        if (duplicateTrailerPlates > 0) {
            return false
        }

        return true
    }

    /**
     * Checks if VIN is unique (not present in both trucks and trailers)
     *
     * @param vin VIN
     * @return true if unique
     */
    suspend fun isVinUnique(vin: String?): Boolean {
        if (vin == null) {
            return true
        }

        val duplicateTruckVins = truckRepository.countByVin(vin)
        if (duplicateTruckVins > 0) {
            return false
        }
        val duplicateTrailerVins = towableRepository.countByVin(vin)
        if (duplicateTrailerVins > 0) {
            return false
        }

        return true
    }

    /**
     * Lists towable connections to vehicles
     *
     * @param towableEntity towable
     * @return list of towable vehicles
     */
    suspend fun listTowableToVehicles(towableEntity: TowableEntity): List<VehicleTowableEntity> {
        return vehicleTowableRepository.listByTowable(towableEntity)
    }
}
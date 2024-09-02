package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.towables.Towable
import fi.metatavu.vp.vehiclemanagement.towables.TowableRepository
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
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
     * @param truck truck
     * @param archived archived
     * @param first first
     * @param max max
     * @return list of vehicles that contains the truck
     */
    suspend fun listVehicles(truck: Truck?, archived: Boolean?, first: Int? = null, max: Int? = null): Pair<List<Vehicle>, Long> {
        return vehicleRepository.list(truck, archived, first, max)
    }

    /**
     * Lists vehicles
     *
     * @param truck truck
     * @return list of vehicles that contains the truck
     */
    suspend fun listVehicles(truck: Truck?): List<Vehicle> {
        return vehicleRepository.listByTruck(truck)
    }

    /**
     * Creates a new vehicle
     *
     * @param truck truck for which vehicle is created
     * @param towables towables
     * @param userId user id
     * @return created vehicle
     */
    suspend fun create(truck: Truck, towables: List<Towable>, userId: UUID): Vehicle {
        // Archive the vehicles that if their truck got reserved for this newly created one
        val truckVehicles = vehicleRepository.listByTruck(truck)
        truckVehicles.forEach {
            it.archivedAt = OffsetDateTime.now()
            vehicleRepository.persistSuspending(it)
        }

        val createdVehicle = vehicleRepository.create(
            id = UUID.randomUUID(),
            truck = truck,
            userId = userId
        )

        towables.forEachIndexed { index, towable ->
            vehicleTowableRepository.create(
                id = UUID.randomUUID(),
                vehicle = createdVehicle,
                towable = towable,
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
    suspend fun find(vehicleId: UUID): Vehicle? {
        return vehicleRepository.findByIdSuspending(vehicleId)
    }

    /**
     * Updates a vehicle
     *
     * @param existingVehicle existing vehicle
     * @param vehicleUpdateData vehicle rest data
     * @param newTruck new truck
     * @param newTowables new towables
     * @param userId user id
     * @return updated vehicle
     */
    suspend fun update(
        existingVehicle: Vehicle,
        vehicleUpdateData: fi.metatavu.vp.vehiclemanagement.model.Vehicle,
        newTruck: Truck,
        newTowables: List<Towable>,
        userId: UUID
    ): Vehicle {
        existingVehicle.truck = newTruck
        existingVehicle.archivedAt = vehicleUpdateData.archivedAt
        existingVehicle.lastModifierId = userId

        //remove connection to towables
        val towableVehicles = vehicleTowableRepository.listByVehicle(existingVehicle)
        towableVehicles.forEach {
            vehicleTowableRepository.deleteSuspending(it)
        }
        //add new connections
        newTowables.forEachIndexed { index, towable ->
            vehicleTowableRepository.create(
                id = UUID.randomUUID(),
                vehicle = existingVehicle,
                towable = towable,
                order = index,
                userId = userId
            )
        }

        return vehicleRepository.persistSuspending(existingVehicle)
    }

    /**
     * Deletes a vehicle
     *
     * @param vehicle vehicle to be deleted
     */
    suspend fun delete(vehicle: Vehicle) {
        val towableVehicles = vehicleTowableRepository.listByVehicle(vehicle)
        towableVehicles.forEach {
            vehicleTowableRepository.deleteSuspending(it)
        }
        vehicleRepository.deleteSuspending(vehicle)
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
     * @param towable towable
     * @return list of towable vehicles
     */
    suspend fun listTowableToVehicles(towable: Towable): List<VehicleTowable> {
        return vehicleTowableRepository.listByTowable(towable)
    }
}
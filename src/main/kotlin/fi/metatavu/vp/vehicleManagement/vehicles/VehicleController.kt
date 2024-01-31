package fi.metatavu.vp.vehicleManagement.vehicles

import fi.metatavu.vp.vehicleManagement.trailers.Trailer
import fi.metatavu.vp.vehicleManagement.trailers.TrailerRepository
import fi.metatavu.vp.vehicleManagement.trucks.Truck
import fi.metatavu.vp.vehicleManagement.trucks.TruckRepository
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Controller for vehicle related operations
 */
@ApplicationScoped
class VehicleController {

    @Inject
    lateinit var vehicleRepository: VehicleRepository

    @Inject
    lateinit var trailerVehicleRepository: TrailerVehicleRepository

    @Inject
    lateinit var truckRepository: TruckRepository

    @Inject
    lateinit var trailerRepository: TrailerRepository

    /**
     * Lists vehicles
     *
     * @param truck truck
     * @return list of vehicles that contains the truck
     */
    suspend fun listVehicles(truck: Truck?, first: Int? = null, max: Int? = null): Pair<List<Vehicle>, Long> {
        return vehicleRepository.list(truck, first, max)
    }

    /**
     * Creates a new vehicle
     *
     * @param truck truck
     * @param trailers trailers
     * @param userId user id
     * @return created vehicle
     */
    suspend fun create(truck: Truck, trailers: List<Trailer>, userId: UUID): Vehicle {
        val createdVehicle = vehicleRepository.create(
            id = UUID.randomUUID(),
            truck = truck,
            userId = userId
        )

        trailers.forEachIndexed { index, trailer ->
            trailerVehicleRepository.create(
                id = UUID.randomUUID(),
                vehicle = createdVehicle,
                trailer = trailer,
                order = index,
                userId = userId
            )
        }
        trailerVehicleRepository.flush().awaitSuspending()

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
     * @param newTruck new truck
     * @param newTrailers new trailers
     * @param userId user id
     * @return updated vehicle
     */
    suspend fun update(existingVehicle: Vehicle, newTruck: Truck, newTrailers: List<Trailer>, userId: UUID): Vehicle {
        existingVehicle.truck = newTruck
        existingVehicle.lastModifierId = userId

        //remove connection to trailers
        val trailerVehicles = trailerVehicleRepository.listByVehicle(existingVehicle)
        trailerVehicles.forEach {
            trailerVehicleRepository.deleteSuspending(it)
        }
        //add new connections
        newTrailers.forEachIndexed { index, trailer ->
            trailerVehicleRepository.create(
                id = UUID.randomUUID(),
                vehicle = existingVehicle,
                trailer = trailer,
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
        val trailerVehicles = trailerVehicleRepository.listByVehicle(vehicle)
        trailerVehicles.forEach {
            trailerVehicleRepository.deleteSuspending(it)
        }
        vehicleRepository.deleteSuspending(vehicle)
        trailerVehicleRepository.flush().awaitSuspending()
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
        val duplicateTrailerPlates = trailerRepository.countByPlateNumber(plateNumber)
        if (duplicateTrailerPlates > 0) {
            return false
        }

        return true
    }

    /**
     * Lists trailer connections to vehicles
     *
     * @param trailer trailer
     * @return list of trailer vehicles
     */
    suspend fun listTrailerVehicles(trailer: Trailer): List<TrailerVehicle> {
        return trailerVehicleRepository.listByTrailer(trailer)
    }
}
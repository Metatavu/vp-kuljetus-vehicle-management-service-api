package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.telematics.trucks.TruckTelematicDataRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Controller for trucks
 */
@ApplicationScoped
class TruckController {

    @Inject
    lateinit var truckRepository: TruckRepository

    @Inject
    lateinit var truckTelematicDataRepository: TruckTelematicDataRepository

    /**
     * Creates new truck
     *
     * @param plateNumber plate number
     * @param type truck type
     * @param vin vin
     * @param userId user id
     * @return created truck
     */
    suspend fun createTruck(plateNumber: String, type: fi.metatavu.vp.api.model.Truck.Type, vin: String, userId: UUID): Truck {
        return truckRepository.create(
            id = UUID.randomUUID(),
            plateNumber = plateNumber,
            type = type,
            vin = vin,
            creatorId = userId,
            lastModifierId = userId
        )
    }

    /**
     * Finds truck by id
     *
     * @param truckId truck id
     * @return found truck or null if not found
     */
    suspend fun findTruck(truckId: UUID): Truck? {
        return truckRepository.findByIdSuspending(truckId)
    }

    /**
     * Finds truck by vin
     *
     * @param vin vin
     * @return found truck or null if not found
     */
    suspend fun findTruck(vin: String): Truck? {
        return truckRepository.findByVin(vin)
    }

    /**
     * Lists trucks
     *
     * @param plateNumber plate number
     * @param archived archived
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trucks
     */
    suspend fun listTrucks(
        plateNumber: String?,
        archived: Boolean?,
        firstResult: Int?,
        maxResults: Int?
    ): Pair<List<Truck>, Long> {
        return truckRepository.list(
            plateNumber = plateNumber,
            archived = archived,
            firstResult = firstResult,
            maxResults = maxResults
        )
    }

    /**
     * Updates truck
     *
     * @param existingTruck existing truck
     * @param newTruckData new truck data
     * @param userId user id
     * @return updated trailer
     */
    suspend fun updateTruck(existingTruck: Truck, newTruckData: fi.metatavu.vp.api.model.Truck, userId: UUID): Truck {
        existingTruck.plateNumber = newTruckData.plateNumber
        existingTruck.archivedAt = newTruckData.archivedAt
        existingTruck.type = newTruckData.type
        existingTruck.vin = newTruckData.vin
        existingTruck.lastModifierId = userId
        return truckRepository.persistSuspending(existingTruck)
    }

    /**
     * Deletes truck
     *
     * @param truck truck to be deleted
     */
    suspend fun deleteTruck(truck: Truck) {
        truckTelematicDataRepository.listByTruck(truck).forEach {
            truckTelematicDataRepository.deleteSuspending(it)
        }
        truckRepository.deleteSuspending(truck)
    }

}
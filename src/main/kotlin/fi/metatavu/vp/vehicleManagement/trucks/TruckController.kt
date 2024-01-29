package fi.metatavu.vp.vehicleManagement.trucks

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

    /**
     * Creates new truck
     *
     * @param plateNumber plate number
     * @param userId user id
     * @return created truck
     */
    suspend fun createTruck(plateNumber: String, userId: UUID): Truck {
        return truckRepository.create(
            id = UUID.randomUUID(),
            plateNumber = plateNumber,
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
     * Lists trucks
     *
     * @param plateNumber plate number
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trucks
     */
    suspend fun listTrucks(plateNumber: String?, firstResult: Int?, maxResults: Int?): Pair<List<Truck>, Long> {
        return truckRepository.list(plateNumber = plateNumber, firstResult = firstResult, maxResults = maxResults)
    }

    /**
     * Updates truck
     *
     * @param existingTruck existing truck
     * @param newTruckData new truck data
     * @param userId user id
     */
    suspend fun updateTruck(existingTruck: Truck, newTruckData: fi.metatavu.vp.api.model.Truck, userId: UUID) {
        existingTruck.plateNumber = newTruckData.plateNumber
        existingTruck.lastModifierId = userId
        truckRepository.persistSuspending(existingTruck)
    }

    /**
     * Deletes truck
     *
     * @param truck truck to be deleted
     */
    suspend fun deleteTruck(truck: Truck) {
        truckRepository.deleteSuspending(truck)
    }

}
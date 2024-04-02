package fi.metatavu.vp.vehiclemanagement.trucks.location

import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.*

/**
 * Controller for truck locations
 */
@ApplicationScoped
class TruckLocationController {

    @Inject
    lateinit var truckLocationRepository: TruckLocationRepository

    /**
     * Creates new truck location
     *
     * @param truck truck
     * @param truckLocation truck location REST object
     * @return created truck location
     */
    suspend fun createTruckLocation(
        truck: Truck,
        truckLocation: fi.metatavu.vp.api.model.TruckLocation
    ): TruckLocation {
        return truckLocationRepository.create(
            id = UUID.randomUUID(),
            timestamp = truckLocation.timestamp,
            latitude = truckLocation.latitude,
            longitude = truckLocation.longitude,
            heading = truckLocation.heading,
            truck = truck
        )
    }

    /**
     * Lists truck locations
     *
     * @param truck truck
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return truck locations
     */
    suspend fun listTruckLocations(
        truck: Truck,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<TruckLocation>, Long> {
        return truckLocationRepository.listTruckLocations(
            truck = truck,
            after = after,
            before = before,
            first = first,
            max = max
        )
    }

    /**
     * Deletes truck location
     *
     * @param truckLocation truck location
     */
    suspend fun deleteTruckLocation(truckLocation: TruckLocation) {
        return truckLocationRepository.deleteSuspending(truckLocation)
    }
}
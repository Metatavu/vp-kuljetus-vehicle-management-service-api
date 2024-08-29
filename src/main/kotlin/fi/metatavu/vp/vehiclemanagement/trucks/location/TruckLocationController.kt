package fi.metatavu.vp.vehiclemanagement.trucks.location

import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.*
import kotlin.math.abs

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
        truckLocation: fi.metatavu.vp.vehiclemanagement.model.TruckLocation
    ): TruckLocation? {
        val existingRecord = truckLocationRepository.find(
            "truck = :truck and timestamp = :timestamp",
            Parameters.with("truck", truck).and("timestamp", truckLocation.timestamp)
        ).firstResult<TruckLocation>().awaitSuspending()
        if (existingRecord != null) {
            return existingRecord
        }

        val latestRecord = truckLocationRepository.find(
            "truck = :truck and timestamp <= :timestamp order by timestamp desc limit 1",
            Parameters.with("truck", truck).and("timestamp", truckLocation.timestamp)
        ).firstResult<TruckLocation>().awaitSuspending()
        if (latestRecord != null &&
            latestRecord.timestamp!! < truckLocation.timestamp &&
            abs(latestRecord.latitude!! - truckLocation.latitude) < 0.0001 &&
            abs(latestRecord.longitude!! - truckLocation.longitude) < 0.0001) {
            return null
        }


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
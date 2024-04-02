package fi.metatavu.vp.vehiclemanagement.trucks.location

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.*

@ApplicationScoped
class TruckLocationRepository : AbstractRepository<TruckLocation, UUID>() {

    suspend fun create(
        id: UUID,
        timestamp: Long,
        latitude: Double,
        longitude: Double,
        heading: Double,
        truck: Truck
    ): TruckLocation {
        val truckLocation = TruckLocation()
        truckLocation.id = id
        truckLocation.timestamp = timestamp
        truckLocation.latitude = latitude
        truckLocation.longitude = longitude
        truckLocation.heading = heading
        truckLocation.truck = truck
        return persistSuspending(truckLocation)
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
        after: OffsetDateTime?,
        before: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Pair<List<TruckLocation>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        stringBuilder.append("truck = :truck")
        parameters.and("truck", truck)

        if (after != null) {
            stringBuilder.append(" AND timestamp >= :after")
            parameters.and("after", after.toEpochSecond() * 1000)
        }

        if (before != null) {
            stringBuilder.append(" AND timestamp <= :before")
            parameters.and("before", before.toEpochSecond() * 1000)
        }

        return applyFirstMaxToQuery(
            query = find(stringBuilder.toString(), Sort.descending("timestamp"), parameters),
            firstIndex = first,
            maxResults = max
        )
    }

}
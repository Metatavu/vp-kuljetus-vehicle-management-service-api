package fi.metatavu.vp.vehiclemanagement.trucks.truckspeed

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.*

/**
 * Repository for truck speeds
 */
@ApplicationScoped
class TruckSpeedRepository : AbstractRepository<TruckSpeed, UUID>() {

    /**
     * Creates truck speed
     *
     * @param id id
     * @param timestamp timestamp
     * @param speed speed
     * @param truck truck
     * @return created truck speed
     */
    suspend fun create(
        id: UUID,
        timestamp: Long,
        speed: Float,
        truck: Truck
    ): TruckSpeed {
        val truckSpeed = TruckSpeed()
        truckSpeed.id = id
        truckSpeed.timestamp = timestamp
        truckSpeed.speed = speed
        truckSpeed.truck = truck
        return persistSuspending(truckSpeed)
    }

    /**
     * Lists truck speeds
     *
     * @param truck truck
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return pair of list of truck speeds and total count
     */
    suspend fun listTruckSpeeds(
        truck: Truck,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Pair<List<TruckSpeed>, Long> {
        val sb = StringBuilder()
        val parameters = Parameters()

        sb.append("truck = :truck")
        parameters.and("truck", truck)

        if (after != null) {
            sb.append(" AND timestamp >= :after")
            parameters.and("after", after.toEpochSecond()*1000)
        }

        if (before != null) {
            sb.append(" AND timestamp <= :before")
            parameters.and("before", before.toEpochSecond()*1000)
        }

        return applyFirstMaxToQuery(
            query = find(sb.toString(), Sort.descending("timestamp"), parameters),
            firstIndex = first,
            maxResults = max
        )
    }
}
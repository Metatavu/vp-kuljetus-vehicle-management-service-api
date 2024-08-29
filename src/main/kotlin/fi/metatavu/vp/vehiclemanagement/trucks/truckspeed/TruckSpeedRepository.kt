package fi.metatavu.vp.vehiclemanagement.trucks.truckspeed

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.*

/**
 * Repository for truck speeds
 */
@ApplicationScoped
class TruckSpeedRepository : AbstractRepository<TruckSpeedEntity, UUID>() {

    /**
     * Creates truck speed
     *
     * @param id id
     * @param timestamp timestamp
     * @param speed speed
     * @param truckEntity truck
     * @return created truck speed
     */
    suspend fun create(
        id: UUID,
        timestamp: Long,
        speed: Float,
        truckEntity: TruckEntity
    ): TruckSpeedEntity {
        val truckSpeedEntity = TruckSpeedEntity()
        truckSpeedEntity.id = id
        truckSpeedEntity.timestamp = timestamp
        truckSpeedEntity.speed = speed
        truckSpeedEntity.truck = truckEntity
        return persistSuspending(truckSpeedEntity)
    }

    /**
     * Lists truck speeds
     *
     * @param truckEntity truck
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return pair of list of truck speeds and total count
     */
    suspend fun listTruckSpeeds(
        truckEntity: TruckEntity,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Pair<List<TruckSpeedEntity>, Long> {
        val sb = StringBuilder()
        val parameters = Parameters()

        sb.append("truck = :truck")
        parameters.and("truck", truckEntity)

        if (after != null) {
            sb.append(" AND timestamp >= :after")
            parameters.and("after", after.toEpochSecond())
        }

        if (before != null) {
            sb.append(" AND timestamp <= :before")
            parameters.and("before", before.toEpochSecond())
        }

        return applyFirstMaxToQuery(
            query = find(sb.toString(), Sort.descending("timestamp"), parameters),
            firstIndex = first,
            maxResults = max
        )
    }
}
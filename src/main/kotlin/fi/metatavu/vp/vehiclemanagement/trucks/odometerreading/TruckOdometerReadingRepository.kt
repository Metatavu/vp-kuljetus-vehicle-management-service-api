package fi.metatavu.vp.vehiclemanagement.trucks.odometerreading

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.*

/**
 * Repository for truck odometer readings
 */
@ApplicationScoped
class TruckOdometerReadingRepository: AbstractRepository<TruckOdometerReadingEntity, UUID>() {

    /**
     * Creates truck odometer reading
     *
     * @param id id
     * @param timestamp timestamp
     * @param odometerReading odometer reading
     * @param truck truck
     * @return created truck speed
     */
    suspend fun createTruckOdometerReading(
        id: UUID,
        timestamp: Long,
        odometerReading: Int,
        truck: TruckEntity
    ): TruckOdometerReadingEntity {
        val odometerReadingEntity = TruckOdometerReadingEntity()
        odometerReadingEntity.id = id
        odometerReadingEntity.timestamp = timestamp
        odometerReadingEntity.odometerReading = odometerReading
        odometerReadingEntity.truck = truck
        return persistSuspending(odometerReadingEntity)
    }

    /**
     * Lists truck odometer readings
     *
     * @param truckEntity truck
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return pair of list of truck odometer readings and total count
     */
    suspend fun listTruckOdometerReadings(
        truckEntity: TruckEntity,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Pair<List<TruckOdometerReadingEntity>, Long> {
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
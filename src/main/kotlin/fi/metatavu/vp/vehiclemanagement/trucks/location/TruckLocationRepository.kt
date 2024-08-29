package fi.metatavu.vp.vehiclemanagement.trucks.location

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.*

/**
 * Repository for truck locations
 */
@ApplicationScoped
class TruckLocationRepository : AbstractRepository<TruckLocationEntity, UUID>() {

    /**
     * Creates new truck location
     *
     * @param id id
     * @param timestamp timestamp
     * @param latitude latitude
     * @param longitude longitude
     * @param heading heading
     * @param truckEntity truck
     * @return created truck location
     */
    suspend fun create(
        id: UUID,
        timestamp: Long,
        latitude: Double,
        longitude: Double,
        heading: Double,
        truckEntity: TruckEntity
    ): TruckLocationEntity {
        val truckLocationEntity = TruckLocationEntity()
        truckLocationEntity.id = id
        truckLocationEntity.timestamp = timestamp
        truckLocationEntity.latitude = latitude
        truckLocationEntity.longitude = longitude
        truckLocationEntity.heading = heading
        truckLocationEntity.truck = truckEntity
        return persistSuspending(truckLocationEntity)
    }

    /**
     * Lists truck locations
     *
     * @param truckEntity truck
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return truck locations
     */
    suspend fun listTruckLocations(
        truckEntity: TruckEntity,
        after: OffsetDateTime?,
        before: OffsetDateTime?,
        first: Int?,
        max: Int?
    ): Pair<List<TruckLocationEntity>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        stringBuilder.append("truck = :truck")
        parameters.and("truck", truckEntity)

        if (after != null) {
            stringBuilder.append(" AND timestamp >= :after")
            parameters.and("after", after.toEpochSecond())
        }

        if (before != null) {
            stringBuilder.append(" AND timestamp <= :before")
            parameters.and("before", before.toEpochSecond())
        }

        return applyFirstMaxToQuery(
            query = find(stringBuilder.toString(), Sort.descending("timestamp"), parameters),
            firstIndex = first,
            maxResults = max
        )
    }

}
package fi.metatavu.vp.vehiclemanagement.trucks.drivestate

import fi.metatavu.vp.vehiclemanagement.model.TruckDriveStateEnum
import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.*

/**
 * Repository class for TruckDriveState
 */
@ApplicationScoped
class TruckDriveStateRepository : AbstractRepository<TruckDriveState, UUID>() {

    /**
     * Creates a new truck drive state
     *
     * @param id id
     * @param state state
     * @param timestamp timestamp
     * @param driverCardId driver card id
     * @param driverId driver id
     * @param truck truck
     * @return created truck drive state
     */
    suspend fun create(
        id: UUID,
        state: TruckDriveStateEnum,
        timestamp: Long,
        driverCardId: String?,
        driverId: UUID?,
        truck: Truck
    ): TruckDriveState {
        val truckDriveState = TruckDriveState()
        truckDriveState.id = id
        truckDriveState.state = state
        truckDriveState.timestamp = timestamp
        truckDriveState.driverCardId = driverCardId
        truckDriveState.driverId = driverId
        truckDriveState.truck = truck
        return persistSuspending(truckDriveState)
    }

    /**
     * Lists truck drive states
     *
     * @param truck truck
     * @param driverId driver id
     * @param state state
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return truck drive states
     */
    suspend fun list(
        truck: Truck,
        driverId: UUID? = null,
        state: List<TruckDriveStateEnum>? = null,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<TruckDriveState>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        stringBuilder.append("truck = :truck")
        parameters.and("truck", truck)

        if (driverId != null) {
            stringBuilder.append(" AND driverId = :driverId")
            parameters.and("driverId", driverId)
        }

        if (!state.isNullOrEmpty()) {
            stringBuilder.append(" AND state IN :state")
            parameters.and("state", state)
        }

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
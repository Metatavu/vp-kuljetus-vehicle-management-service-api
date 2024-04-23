package fi.metatavu.vp.vehiclemanagement.trucks.drivestate

import fi.metatavu.vp.api.model.TruckDriveState
import fi.metatavu.vp.api.model.TruckDriveStateEnum
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.*

/**
 * Controller for truck drive states
 */
@ApplicationScoped
class TruckDriveStateController {

    @Inject
    lateinit var truckDriveStateRepository: TruckDriveStateRepository

    /**
     * Creates a new truck drive state
     *
     * @param truck truck
     * @param truckDriveState truck drive state
     * @return created truck drive state
     */
    suspend fun createDriveState(
        truck: Truck,
        truckDriveState: TruckDriveState
    ): fi.metatavu.vp.vehiclemanagement.trucks.drivestate.TruckDriveState? {
        val duplicates = truckDriveStateRepository.count(
            "(timestamp = :timestamp and truck = :truck) or " +
                "(truck = :truck and state = :state and driverCardId = :driverCardId and driverId = :driverId and timestamp = (select max(timestamp)))",
            Parameters.with("timestamp", truckDriveState.timestamp)
                .and("state", truckDriveState.state)
                .and("driverCardId", truckDriveState.driverCardId)
                .and("driverId", truckDriveState.driverId)
                .and("truck", truck)
        ).awaitSuspending()
        if (duplicates > 0) {
            return null
        }

        return truckDriveStateRepository.create(
            id = UUID.randomUUID(),
            state = truckDriveState.state,
            timestamp = truckDriveState.timestamp,
            driverCardId = truckDriveState.driverCardId,
            driverId = truckDriveState.driverId,
            truck = truck
        )
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
    suspend fun listDriveStates(
        truck: Truck,
        driverId: UUID? = null,
        state: List<TruckDriveStateEnum>? = null,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<fi.metatavu.vp.vehiclemanagement.trucks.drivestate.TruckDriveState>, Long> {
        return truckDriveStateRepository.list(
            truck = truck,
            driverId = driverId,
            state = state,
            after = after,
            before = before,
            first = first,
            max = max
        )
    }

    /**
     * Deletes truck drive state
     *
     * @param truckDriveState truck drive state
     */
    suspend fun deleteDriveState(truckDriveState: fi.metatavu.vp.vehiclemanagement.trucks.drivestate.TruckDriveState) {
        truckDriveStateRepository.deleteSuspending(truckDriveState)
    }
}

package fi.metatavu.vp.vehiclemanagement.trucks.drivestate

import fi.metatavu.vp.api.model.TruckDriveState
import fi.metatavu.vp.api.model.TruckDriveStateEnum
import fi.metatavu.vp.usermanagement.model.Driver
import fi.metatavu.vp.usermanagement.spec.DriversApi
import fi.metatavu.vp.vehiclemanagement.integrations.UserManagementService
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.time.OffsetDateTime
import java.util.*

/**
 * Controller for truck drive states
 */
@ApplicationScoped
class TruckDriveStateController {

    @Inject
    lateinit var truckDriveStateRepository: TruckDriveStateRepository

    @Inject
    lateinit var userManagementService: UserManagementService

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
        val existingRecord = truckDriveStateRepository.find(
            "truck = :truck and timestamp = :timestamp",
            Parameters.with("truck", truck).and("timestamp", truckDriveState.timestamp)
        ).firstResult<fi.metatavu.vp.vehiclemanagement.trucks.drivestate.TruckDriveState>().awaitSuspending()
        if (existingRecord != null) {
            return existingRecord
        }

        val latestRecord = truckDriveStateRepository.find(
            "truck = :truck and timestamp <= :timestamp order by timestamp desc limit 1",
            Parameters.with("truck", truck).and("timestamp", truckDriveState.timestamp)
        ).firstResult<fi.metatavu.vp.vehiclemanagement.trucks.drivestate.TruckDriveState>().awaitSuspending()
        if (latestRecord != null &&
            latestRecord.timestamp!! < truckDriveState.timestamp &&
            latestRecord.state == truckDriveState.state &&
            truckDriveState.driverCardId == latestRecord.driverCardId &&
            truckDriveState.driverId == latestRecord.driverId) {
            return null
        }


//        val foundDriverId = truckDriveState.driverCardId?.let { userManagementService.findDriver(it)?.id }

        return truckDriveStateRepository.create(
            id = UUID.randomUUID(),
            state = truckDriveState.state,
            timestamp = truckDriveState.timestamp,
            driverCardId = truckDriveState.driverCardId,
            driverId = null,
//            driverId = foundDriverId,
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

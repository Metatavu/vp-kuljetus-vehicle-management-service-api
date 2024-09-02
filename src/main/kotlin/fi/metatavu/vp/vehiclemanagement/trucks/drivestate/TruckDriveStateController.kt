package fi.metatavu.vp.vehiclemanagement.trucks.drivestate

import fi.metatavu.vp.vehiclemanagement.event.TruckDriveStateCreatedConsumer
import fi.metatavu.vp.vehiclemanagement.model.TruckDriveState
import fi.metatavu.vp.vehiclemanagement.model.TruckDriveStateEnum
import fi.metatavu.vp.vehiclemanagement.integrations.UserManagementService
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.core.eventbus.EventBus
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

    @Inject
    lateinit var userManagementService: UserManagementService

    @Inject
    lateinit var eventBus: EventBus

    /**
     * Creates a new truck drive state
     *
     * @param truckEntity truck
     * @param truckDriveState truck drive state
     * @return created truck drive state
     */
    suspend fun createDriveState(
        truckEntity: TruckEntity,
        truckDriveState: TruckDriveState
    ): TruckDriveStateEntity? {
        val existingRecord = truckDriveStateRepository.find(
            "truck = :truck and timestamp = :timestamp",
            Parameters.with("truck", truckEntity).and("timestamp", truckDriveState.timestamp)
        ).firstResult<TruckDriveStateEntity>().awaitSuspending()
        if (existingRecord != null) {
            return existingRecord
        }


        val foundDriverId = truckDriveState.driverCardId?.let { userManagementService.findDriverByDriverCardId(it)?.id }

        val latestRecord = truckDriveStateRepository.find(
            "truck = :truck and timestamp <= :timestamp order by timestamp desc limit 1",
            Parameters.with("truck", truckEntity).and("timestamp", truckDriveState.timestamp)
        ).firstResult<TruckDriveStateEntity>().awaitSuspending()
        if (latestRecord != null &&
            latestRecord.timestamp!! < truckDriveState.timestamp &&
            latestRecord.state == truckDriveState.state &&
            truckDriveState.driverCardId == latestRecord.driverCardId &&
            foundDriverId == latestRecord.driverId) {
            return null
        }

        val createdDriveState = truckDriveStateRepository.create(
            id = UUID.randomUUID(),
            state = truckDriveState.state,
            timestamp = truckDriveState.timestamp,
            driverCardId = truckDriveState.driverCardId,
            driverId = foundDriverId,
            truckEntity = truckEntity
        )

        eventBus.publish(TruckDriveStateCreatedConsumer.TRUCK_DRIVE_STATE_CREATED, createdDriveState)

        return createdDriveState
    }

    /**
     * Lists truck drive states
     *
     * @param truckEntity truck
     * @param driverId driver id
     * @param state state
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return truck drive states
     */
    suspend fun listDriveStates(
        truckEntity: TruckEntity,
        driverId: UUID? = null,
        state: List<TruckDriveStateEnum>? = null,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<TruckDriveStateEntity>, Long> {
        return truckDriveStateRepository.list(
            truckEntity = truckEntity,
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
     * @param truckDriveStateEntity truck drive state
     */
    suspend fun deleteDriveState(truckDriveStateEntity: TruckDriveStateEntity) {
        truckDriveStateRepository.deleteSuspending(truckDriveStateEntity)
    }
}

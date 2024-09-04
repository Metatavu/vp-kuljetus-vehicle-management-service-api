package fi.metatavu.vp.vehiclemanagement.event

import fi.metatavu.vp.vehiclemanagement.model.TruckDriveStateEnum
import fi.metatavu.vp.messaging.GlobalEventController
import fi.metatavu.vp.messaging.events.DriverWorkEventGlobalEvent
import fi.metatavu.vp.usermanagement.model.WorkEventType
import fi.metatavu.vp.vehiclemanagement.WithCoroutineScope
import fi.metatavu.vp.vehiclemanagement.trucks.drivestate.TruckDriveStateEntity
import io.quarkus.vertx.ConsumeEvent
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Event bus consumer for truck drive state created events
 */
@ApplicationScoped
class TruckDriveStateCreatedConsumer: WithCoroutineScope() {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var globalEventController: GlobalEventController

    /**
     * Event bus consumer for truck drive state created events
     *
     * Consumes truck drive state created events and publishes driver work events over RabbitMQ
     *
     * @param driveState truck drive state
     * @return a uni that completes when the event has been processed
     */
    @ConsumeEvent(TRUCK_DRIVE_STATE_CREATED)
    @Suppress("unused")
    fun onTruckDriveStateCreated(driveState: TruckDriveStateEntity): Uni<Void> = withCoroutineScope {
        logger.info("Truck drive state created: ${driveState.id}")
        val driverId = driveState.driverId
        val timestamp = driveState.timestamp

        if (driverId == null) {
            logger.warn("Truck drive state created without driver id: ${driveState.id}")
            return@withCoroutineScope
        }

        if (timestamp == null) {
            logger.warn("Truck drive state created without timestamp: ${driveState.id}")
            return@withCoroutineScope
        }

        globalEventController.publish(
            DriverWorkEventGlobalEvent(
                driverId = driverId,
                workEventType = getWorkEventType(driveState),
                time = OffsetDateTime.of(LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC), ZoneOffset.UTC)
            )
        )
    }.replaceWithVoid()

    /**
     * Returns work event type based on truck drive state
     *
     * @param driveState truck drive state
     * @return work event type
     */
    private fun getWorkEventType(driveState: TruckDriveStateEntity): WorkEventType {
        return when (driveState.state) {
            TruckDriveStateEnum.DRIVE -> WorkEventType.DRIVE
            TruckDriveStateEnum.WORK -> WorkEventType.OTHER_WORK
            TruckDriveStateEnum.REST -> WorkEventType.BREAK
            else -> WorkEventType.UNKNOWN

        }
    }

    companion object {
        const val TRUCK_DRIVE_STATE_CREATED = "truck-drive-state-created"
    }
}
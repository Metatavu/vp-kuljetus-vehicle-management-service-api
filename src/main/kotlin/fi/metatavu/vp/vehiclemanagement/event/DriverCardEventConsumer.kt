package fi.metatavu.vp.vehiclemanagement.event

import fi.metatavu.vp.messaging.GlobalEventController
import fi.metatavu.vp.messaging.events.DriverWorkEventGlobalEvent
import fi.metatavu.vp.usermanagement.model.Driver
import fi.metatavu.vp.usermanagement.model.WorkEventType
import fi.metatavu.vp.vehiclemanagement.WithCoroutineScope
import fi.metatavu.vp.vehiclemanagement.trucks.drivercards.DriverCard
import io.quarkus.vertx.ConsumeEvent
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Event bus consumer for driver card events
 */
@ApplicationScoped
class DriverCardEventConsumer: WithCoroutineScope() {

    data class DriverCardEvent(val driverCard: DriverCard, val removed: Boolean, val driver: Driver?)

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var globalEventController: GlobalEventController

    /**
     * Event bus consumer for driver card events
     *
     * Consumes driver card events and publishes driver work events over RabbitMQ
     *
     * @param driverCardEvent driver card event
     */
    @ConsumeEvent(DRIVER_CARD_EVENT)
    @Suppress("unused")
    fun onDriverCardEvent(driverCardEvent: DriverCardEvent): Uni<Void> = withCoroutineScope {
        logger.info("Driver card event: $driverCardEvent")
        val driverCard = driverCardEvent.driverCard
        val removed = driverCardEvent.removed
        val driver = driverCardEvent.driver
        val timestamp = driverCard.timestamp

        if (driver?.id == null) {
            logger.warn("Driver not found for driver card id: ${driverCard.driverCardId}")
            return@withCoroutineScope
        }

        if (timestamp == null) {
            logger.warn("Driver card event without timestamp: ${driverCard.id}")
            return@withCoroutineScope
        }

        globalEventController.publish(
            DriverWorkEventGlobalEvent(
                driverId = driver.id,
                workEventType = if (removed) WorkEventType.DRIVER_CARD_REMOVED else WorkEventType.DRIVER_CARD_INSERTED,
                time = OffsetDateTime.of(LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC), ZoneOffset.UTC)
            )
        )
    }.replaceWithVoid()

    companion object {
        const val DRIVER_CARD_EVENT = "driver-card-event"
    }
}
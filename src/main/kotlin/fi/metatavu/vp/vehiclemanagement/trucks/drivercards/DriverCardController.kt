package fi.metatavu.vp.vehiclemanagement.trucks.drivercards

import fi.metatavu.vp.vehiclemanagement.WithCoroutineScope
import fi.metatavu.vp.vehiclemanagement.event.DriverCardEventConsumer
import fi.metatavu.vp.vehiclemanagement.event.model.DriverCardEvent
import fi.metatavu.vp.vehiclemanagement.integrations.UserManagementService
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.scheduler.Scheduled
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.core.eventbus.EventBus
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

/**
 * Controller for driver cards
 */
@ApplicationScoped
class DriverCardController : WithCoroutineScope() {

    @Inject
    lateinit var driverCardRepository: DriverCardRepository

    @Inject
    lateinit var userManagementService: UserManagementService

    @Inject
    lateinit var eventBus: EventBus

    @Inject
    lateinit var logger: Logger

    @ConfigProperty(name = "clearOldRemovedDriverCards.gracePeriod.minutes")
    var gracePeriodMinutes: Long? = 0

    /**
     * Cleans driver cards which were marked as removed longer than a selected grace period ago
     */
    @Scheduled(
        every = "\${clearOldRemovedDriverCards.every}",
        delayed = "\${clearOldRemovedDriverCards.delay}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP
    )
    @WithTransaction
    fun clearOldRemovedDriverCards() = withCoroutineScope(60000) {
        if (gracePeriodMinutes == null) return@withCoroutineScope null
        logger.info("Deleting the outdated truck driver cards")
        val cutoffDate = OffsetDateTime.now().minus(gracePeriodMinutes!!, ChronoUnit.MINUTES)

        val oldDriverCards = driverCardRepository.listRemovedBefore(cutoffDate)
        oldDriverCards.forEach { driverCard ->
            driverCardRepository.deleteSuspending(driverCard)
        }
        logger.info("Removed ${oldDriverCards.size} truck driver cards")
    }.replaceWithVoid()

    /**
     * Lists driver cards
     *
     * @param truckEntity truck
     * @return list of driver cards
     */
    suspend fun listDriverCards(truckEntity: TruckEntity): Pair<List<DriverCard>, Long> {
        return driverCardRepository.list(truckEntity)
    }

    /**
     * Inserts a driver truck inside the truck
     *
     * @param driverCardId driver card id
     * @param truckEntity truck
     * @param timestamp timestamp
     * @param currentDriverCardInserted the record of the current driver card already being inserted somewhere
     * @return created driver card
     */
    suspend fun createDriverCard(
        driverCardId: String,
        truckEntity: TruckEntity,
        timestamp: Long,
        currentDriverCardInserted: DriverCard?
    ): DriverCard {
        val driverCard = currentDriverCardInserted?.let {
            handleExistingDriverCard(it, truckEntity)
        } ?: driverCardRepository.create(
            driverCardId = driverCardId,
            truckEntity = truckEntity,
            timestamp = timestamp
        )

        val foundDriver = driverCard.driverCardId.let { userManagementService.findDriverByDriverCardId(it) }

        eventBus.publish(
            DriverCardEventConsumer.DRIVER_CARD_EVENT,
            DriverCardEvent(driverCard, false, foundDriver)
        )

        return driverCard
    }

    /**
     * Additional logic for inserting driver card when it is already inserted somewhere
     *
     * @param insertedDriverCard driver card
     * @param truckEntity target truck
     * @return driver card if it can be re-used, null it is should be created
     */
    private suspend fun handleExistingDriverCard(
        insertedDriverCard: DriverCard,
        truckEntity: TruckEntity
    ): DriverCard? {
        return when {
            insertedDriverCard.truck.id == truckEntity.id && insertedDriverCard.removedAt != null -> {
                // Restore the existing card because it is being inserted in the truck where it just was
                driverCardRepository.restore(insertedDriverCard)
            }

            insertedDriverCard.truck.id != truckEntity.id && insertedDriverCard.removedAt != null -> {
                // Already soft deleted from another truck
                driverCardRepository.deleteSuspending(insertedDriverCard)
                driverCardRepository.flush().awaitSuspending()  // otherwise re-creating the driver card later from different truck will fail

                null
            }

            else -> null
        }
    }

    /**
     * Finds driver card by driver card id
     *
     * @param driverCardId driver card id
     * @return found driver card or null if not found
     */
    suspend fun findDriverCard(driverCardId: String): DriverCard? {
        return driverCardRepository.find("driverCardId", driverCardId)
            .firstResult<DriverCard>()
            .awaitSuspending()
    }

    /**
     * Deletes driver card
     *
     * @param driverCard driver card to delete
     * @param removedAt removed at
     * @return deleted driver card
     */
    suspend fun removeDriverCard(driverCard: DriverCard, removedAt: OffsetDateTime) {
        driverCard.removedAt = removedAt

        sendRemovedEvent(driverCard, removedAt)
    }

    /**
     * Delete driver card from the database
     *
     * @param driverCard driver card
     */
    suspend fun deleteDriverCard(driverCard: DriverCard, removedAt: OffsetDateTime) {
        driverCardRepository.deleteSuspending(driverCard)

        sendRemovedEvent(driverCard, removedAt)
    }

    /**
     * Sends global message that a card was removed
     */
    private suspend fun sendRemovedEvent(driverCard: DriverCard, removedAt: OffsetDateTime) {
        val foundDriver = driverCard.driverCardId.let { userManagementService.findDriverByDriverCardId(it) }
        eventBus.publish(
            DriverCardEventConsumer.DRIVER_CARD_EVENT,
            DriverCardEvent(driverCard.apply { timestamp = removedAt.toEpochSecond() }, true, foundDriver)
        )
    }

}

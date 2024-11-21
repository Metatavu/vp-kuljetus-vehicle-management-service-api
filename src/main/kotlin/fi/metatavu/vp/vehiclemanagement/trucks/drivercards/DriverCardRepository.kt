package fi.metatavu.vp.vehiclemanagement.trucks.drivercards

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.*

/**
 * Repository for driver cards
 */
@ApplicationScoped
class DriverCardRepository: AbstractRepository<DriverCard, UUID>() {

    /**
     * Creates driver card
     *
     * @param driverCardId driver card id
     * @param truckEntity truck
     * @param timestamp timestamp
     * @return created driver card
     */
    suspend fun create(
        driverCardId: String,
        truckEntity: TruckEntity,
        timestamp: Long
    ): DriverCard {
        val driverCard = DriverCard()
        driverCard.id = UUID.randomUUID()
        driverCard.driverCardId = driverCardId
        driverCard.truck = truckEntity
        driverCard.timestamp = timestamp

        return persistSuspending(driverCard)
    }

    /**
     * Lists driver cards
     *
     * @param truckEntity truck
     * @return list of driver cards
     */
    suspend fun list(truckEntity: TruckEntity): Pair<List<DriverCard>, Long> {
        return applyCountToQuery(find("truck", truckEntity))
    }

    /**
     * Lists driver cards that were removed before certain date
     *
     * @param date date filter
     * @return driver card list
     */
    suspend fun listRemovedBefore(date: OffsetDateTime): List<DriverCard> {
        return find("removedAt <= :date", Parameters().and("date", date)).list<DriverCard>().awaitSuspending()
    }

    /**
     * Re-enables driver card (sets removed at to null)
     *
     * @param driverCard driver card
     */
    suspend fun restore(driverCard: DriverCard): DriverCard {
        driverCard.removedAt = null
        return persistSuspending(driverCard)
    }
}
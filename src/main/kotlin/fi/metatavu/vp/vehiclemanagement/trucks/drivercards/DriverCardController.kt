package fi.metatavu.vp.vehiclemanagement.trucks.drivercards

import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Controller for driver cards
 */
@ApplicationScoped
class DriverCardController {

    @Inject
    lateinit var driverCardRepository: DriverCardRepository

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
     * Creates driver card
     *
     * @param driverCardId driver card id
     * @param truckEntity truck
     * @param timestamp timestamp
     * @return created driver card
     */
    suspend fun createDriverCard(driverCardId: String, truckEntity: TruckEntity, timestamp: Long): DriverCard {
        return driverCardRepository.create(
            driverCardId = driverCardId,
            truckEntity = truckEntity,
            timestamp = timestamp
        )
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
     * @return deleted driver card
     */
    suspend fun deleteDriverCard(driverCard: DriverCard) {
        driverCardRepository.deleteSuspending(driverCard)
    }
}

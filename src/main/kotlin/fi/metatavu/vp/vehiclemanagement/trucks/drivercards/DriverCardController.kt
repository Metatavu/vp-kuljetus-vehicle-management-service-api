package fi.metatavu.vp.vehiclemanagement.trucks.drivercards

import fi.metatavu.vp.vehiclemanagement.trucks.Truck
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
     * @param truck truck
     * @return list of driver cards
     */
    suspend fun listDriverCards(truck: Truck): Pair<List<DriverCard>, Long> {
        return driverCardRepository.list(truck)
    }

    /**
     * Creates driver card
     *
     * @param driverCardId driver card id
     * @param truck truck
     * @return created driver card
     */
    suspend fun createDriverCard(driverCardId: String, truck: Truck): DriverCard {
        return driverCardRepository.create(
            driverCardId = driverCardId,
            truck = truck
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

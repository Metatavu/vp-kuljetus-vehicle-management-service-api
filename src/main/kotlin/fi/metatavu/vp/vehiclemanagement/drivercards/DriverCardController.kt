package fi.metatavu.vp.vehiclemanagement.drivercards

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
     * Creates driver card
     *
     * @param driverCardId driver card id
     * @param driverCard driver card data
     * @return created driver card
     */
    suspend fun createDriverCard(driverCardId: String, driverCard: fi.metatavu.vp.api.model.DriverCard): DriverCard {
        return driverCardRepository.create(
            driverCardId = driverCardId,
            truckVin = driverCard.truckVin
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
     * Updates driver card
     *
     * @param existingCard existing driver card
     * @param driverCard driver card data
     * @return updated driver card
     */
    suspend fun updateDriverCard(existingCard: DriverCard, driverCard: fi.metatavu.vp.api.model.DriverCard): DriverCard {
        existingCard.truckVin = driverCard.truckVin
        return driverCardRepository.persist(existingCard).awaitSuspending()
    }

}

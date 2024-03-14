package fi.metatavu.vp.vehiclemanagement.drivercards

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

/**
 * Repository for driver cards
 */
@ApplicationScoped
class DriverCardRepository: AbstractRepository<DriverCard, UUID>() {

    /**
     * Creates driver card
     *
     * @param driverCardId driver card id
     * @param truckVin truck vin
     * @return created driver card
     */
    suspend fun create(
        driverCardId: String,
        truckVin: String
    ): DriverCard {
        val driverCard = DriverCard()
        driverCard.id = UUID.randomUUID()
        driverCard.driverCardId = driverCardId
        driverCard.truckVin = truckVin
        return persistSuspending(driverCard)
    }

}

package fi.metatavu.vp.vehiclemanagement.trucks.drivercards

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
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
     * @param truckEntity truck
     * @return created driver card
     */
    suspend fun create(
        driverCardId: String,
        truckEntity: TruckEntity
    ): DriverCard {
        val driverCard = DriverCard()
        driverCard.id = UUID.randomUUID()
        driverCard.driverCardId = driverCardId
        driverCard.truck = truckEntity
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
}
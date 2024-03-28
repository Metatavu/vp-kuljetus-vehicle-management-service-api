package fi.metatavu.vp.vehiclemanagement.trucks.drivercards

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.smallrye.mutiny.coroutines.awaitSuspending
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
     * @param truckId truck id
     * @return created driver card
     */
    suspend fun create(
        driverCardId: String,
        truck: Truck
    ): DriverCard {
        val driverCard = DriverCard()
        driverCard.id = UUID.randomUUID()
        driverCard.driverCardId = driverCardId
        driverCard.truck = truck
        return persistSuspending(driverCard)
    }

    /**
     * Lists driver cards
     *
     * @param truck truck
     * @return list of driver cards
     */
    suspend fun list(truck: Truck): Pair<List<DriverCard>, Long> {
        return applyCountToQuery(find("truck", truck))
    }
}
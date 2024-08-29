package fi.metatavu.vp.vehiclemanagement.trucks.drivercards

import fi.metatavu.vp.vehiclemanagement.model.TruckDriverCard
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for driver cards
 */
@ApplicationScoped
class DriverCardTranslator: AbstractTranslator<DriverCard, TruckDriverCard>()   {
    override suspend fun translate(entity: DriverCard): TruckDriverCard {
        return TruckDriverCard(
            id = entity.driverCardId
        )
    }

}

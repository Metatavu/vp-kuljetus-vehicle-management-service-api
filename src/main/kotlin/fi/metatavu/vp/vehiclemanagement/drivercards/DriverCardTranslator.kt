package fi.metatavu.vp.vehiclemanagement.drivercards

import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for driver cards
 */
@ApplicationScoped
class DriverCardTranslator: AbstractTranslator<DriverCard, fi.metatavu.vp.api.model.DriverCard>()   {
    override suspend fun translate(entity: DriverCard): fi.metatavu.vp.api.model.DriverCard {
        return fi.metatavu.vp.api.model.DriverCard(
            driverCardId = entity.driverCardId,
            truckVin = entity.truckVin
        )
    }

}

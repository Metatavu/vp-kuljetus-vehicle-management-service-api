package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.model.PublicTruck
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for translating Truck entities into PublicTruck REST resources
 */
@ApplicationScoped
class PublicTruckTranslator : AbstractTranslator<TruckEntity, PublicTruck>() {

    override suspend fun translate(entity: TruckEntity): PublicTruck {
        return PublicTruck(
            id = entity.id,
            plateNumber = entity.plateNumber,
            vin = entity.vin,
            name = entity.name
        )
    }

}
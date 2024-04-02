package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for translating Truck entities into PublicTruck REST resources
 */
@ApplicationScoped
class PublicTruckTranslator : AbstractTranslator<Truck, fi.metatavu.vp.api.model.PublicTruck>() {

    override suspend fun translate(entity: Truck): fi.metatavu.vp.api.model.PublicTruck {
        return fi.metatavu.vp.api.model.PublicTruck(
            id = entity.id,
            plateNumber = entity.plateNumber,
            vin = entity.vin,
            name = entity.name
        )
    }

}
package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for translating Truck entities into REST resources
 */
@ApplicationScoped
class TruckTranslator : AbstractTranslator<Truck, fi.metatavu.vp.api.model.Truck>() {

    override suspend fun translate(entity: Truck): fi.metatavu.vp.api.model.Truck {
        return fi.metatavu.vp.api.model.Truck(
            id = entity.id,
            plateNumber = entity.plateNumber,
            type = entity.type
        )
    }

}
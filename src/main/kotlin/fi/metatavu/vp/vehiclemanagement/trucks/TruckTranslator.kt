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
            vin = entity.vin,
            type = entity.type,
            archivedAt = entity.archivedAt,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            lastModifierId = entity.lastModifierId,
            creatorId = entity.creatorId
        )
    }

}
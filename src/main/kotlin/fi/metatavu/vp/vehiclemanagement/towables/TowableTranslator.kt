package fi.metatavu.vp.vehiclemanagement.towables

import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for translating towable entities into REST resources
 */
@ApplicationScoped
class TowableTranslator : AbstractTranslator<Towable, fi.metatavu.vp.api.model.Towable>() {

    override suspend fun translate(entity: Towable): fi.metatavu.vp.api.model.Towable {
        return fi.metatavu.vp.api.model.Towable(
            id = entity.id,
            plateNumber = entity.plateNumber,
            vin = entity.vin,
            type = entity.type,
            name = entity.name,
            archivedAt = entity.archivedAt,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            lastModifierId = entity.lastModifierId,
            creatorId = entity.creatorId
        )
    }

}
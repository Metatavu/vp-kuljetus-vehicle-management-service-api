package fi.metatavu.vp.vehiclemanagement.towables

import fi.metatavu.vp.vehiclemanagement.model.Towable
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for translating towable entities into REST resources
 */
@ApplicationScoped
class TowableTranslator : AbstractTranslator<TowableEntity, Towable>() {

    override suspend fun translate(entity: TowableEntity): Towable {
        return Towable(
            id = entity.id,
            plateNumber = entity.plateNumber,
            vin = entity.vin,
            type = entity.type,
            name = entity.name,
            imei = entity.imei,
            archivedAt = entity.archivedAt,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            lastModifierId = entity.lastModifierId,
            creatorId = entity.creatorId
        )
    }

}
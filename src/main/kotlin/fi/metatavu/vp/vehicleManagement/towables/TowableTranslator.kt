package fi.metatavu.vp.vehicleManagement.towables

import fi.metatavu.vp.vehicleManagement.rest.AbstractTranslator
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
            type = entity.type
        )
    }

}
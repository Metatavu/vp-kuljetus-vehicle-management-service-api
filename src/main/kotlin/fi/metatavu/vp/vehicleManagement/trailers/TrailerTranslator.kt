package fi.metatavu.vp.vehicleManagement.trailers

import fi.metatavu.vp.vehicleManagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for translating trailer entities into REST resources
 */
@ApplicationScoped
class TrailerTranslator : AbstractTranslator<Trailer, fi.metatavu.vp.api.model.Trailer>() {

    override fun translate(entity: Trailer): fi.metatavu.vp.api.model.Trailer {
        return fi.metatavu.vp.api.model.Trailer(
            id = entity.id,
            plateNumber = entity.plateNumber
        )
    }

}
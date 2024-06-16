package fi.metatavu.vp.vehiclemanagement.trucks.location

import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for truck locations
 */
@ApplicationScoped
class TruckLocationTranslator : AbstractTranslator<TruckLocation, fi.metatavu.vp.api.model.TruckLocation>() {

    override suspend fun translate(entity: TruckLocation): fi.metatavu.vp.api.model.TruckLocation {
        return fi.metatavu.vp.api.model.TruckLocation(
            id = entity.id,
            latitude = entity.latitude!!,
            longitude = entity.longitude!!,
            heading = entity.heading!!,
            timestamp = entity.timestamp!!
        )
    }
}
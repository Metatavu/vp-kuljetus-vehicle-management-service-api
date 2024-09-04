package fi.metatavu.vp.vehiclemanagement.trucks.location

import fi.metatavu.vp.vehiclemanagement.model.TruckLocation
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for truck locations
 */
@ApplicationScoped
class TruckLocationTranslator : AbstractTranslator<TruckLocationEntity, TruckLocation>() {

    override suspend fun translate(entity: TruckLocationEntity): TruckLocation {
        return TruckLocation(
            id = entity.id,
            latitude = entity.latitude!!,
            longitude = entity.longitude!!,
            heading = entity.heading!!,
            timestamp = entity.timestamp!!
        )
    }
}
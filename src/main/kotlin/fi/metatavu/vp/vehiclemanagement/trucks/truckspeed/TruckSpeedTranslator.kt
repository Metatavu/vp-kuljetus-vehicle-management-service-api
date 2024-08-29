package fi.metatavu.vp.vehiclemanagement.trucks.truckspeed

import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for truck speed
 */
@ApplicationScoped
class TruckSpeedTranslator : AbstractTranslator<TruckSpeed, fi.metatavu.vp.vehiclemanagement.model.TruckSpeed>() {
    override suspend fun translate(entity: TruckSpeed): fi.metatavu.vp.vehiclemanagement.model.TruckSpeed {
        return fi.metatavu.vp.vehiclemanagement.model.TruckSpeed(
            id = entity.id!!,
            timestamp = entity.timestamp!!,
            speed = entity.speed!!
        )
    }

}

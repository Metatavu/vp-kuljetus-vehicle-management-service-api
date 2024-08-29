package fi.metatavu.vp.vehiclemanagement.trucks.truckspeed

import fi.metatavu.vp.vehiclemanagement.model.TruckSpeed
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for truck speed
 */
@ApplicationScoped
class TruckSpeedTranslator : AbstractTranslator<TruckSpeedEntity, TruckSpeed>() {
    override suspend fun translate(entity: TruckSpeedEntity): TruckSpeed {
        return TruckSpeed(
            id = entity.id!!,
            timestamp = entity.timestamp!!,
            speed = entity.speed!!
        )
    }

}

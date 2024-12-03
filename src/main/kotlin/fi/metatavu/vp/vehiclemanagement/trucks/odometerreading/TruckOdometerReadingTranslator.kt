package fi.metatavu.vp.vehiclemanagement.trucks.odometerreading

import fi.metatavu.vp.vehiclemanagement.model.TruckOdometerReading
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for truck odometer readings
 */
@ApplicationScoped
class TruckOdometerReadingTranslator : AbstractTranslator<TruckOdometerReadingEntity, TruckOdometerReading>() {

    override suspend fun translate(entity: TruckOdometerReadingEntity): TruckOdometerReading {
        return TruckOdometerReading(
            id = entity.id,
            timestamp = entity.timestamp!!,
            odometerReading = entity.odometerReading!!
        )
    }
}
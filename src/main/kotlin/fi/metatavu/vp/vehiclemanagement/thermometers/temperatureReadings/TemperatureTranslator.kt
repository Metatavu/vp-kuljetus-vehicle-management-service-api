package fi.metatavu.vp.vehiclemanagement.thermometers.temperatureReadings

import fi.metatavu.vp.vehiclemanagement.model.Temperature
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for translating temperature reading entities into REST resources
 */
@ApplicationScoped
class TemperatureTranslator : AbstractTranslator<TemperatureReadingEntity, Temperature>() {
    override suspend fun translate(entity: TemperatureReadingEntity): Temperature {
        return Temperature(
            id = entity.id,
            thermometerId = entity.thermometer.id,
            value = entity.value!!,
            timestamp = entity.timestamp
        )
    }
}
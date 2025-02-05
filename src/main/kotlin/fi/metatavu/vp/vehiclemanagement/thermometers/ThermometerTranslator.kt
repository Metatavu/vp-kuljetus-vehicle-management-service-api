package fi.metatavu.vp.vehiclemanagement.thermometers

import fi.metatavu.vp.vehiclemanagement.model.TruckOrTowableThermometer
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for Thermometer
 */
@ApplicationScoped
class ThermometerTranslator : AbstractTranslator<ThermometerEntity, TruckOrTowableThermometer>() {
    override suspend fun translate(entity: ThermometerEntity): TruckOrTowableThermometer {
        val entityId = if (entity.truck != null) entity.truck!!.id else entity.towable!!.id
        val entityType =
            if (entity.truck != null) TruckOrTowableThermometer.EntityType.TRUCK
            else TruckOrTowableThermometer.EntityType.TOWABLE
        return TruckOrTowableThermometer(
            id = entity.id,
            macAddress = entity.hardwareSensorId,
            entityId = entityId!!,
            entityType = entityType,
            archivedAt = entity.archivedAt,
            modifiedAt = entity.modifiedAt,
            createdAt = entity.createdAt
        )
    }
}
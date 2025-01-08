package fi.metatavu.vp.vehiclemanagement.thermometers

import fi.metatavu.vp.vehiclemanagement.model.Thermometer
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for Thermometer
 */
@ApplicationScoped
class ThermometerTranslator : AbstractTranslator<ThermometerEntity, Thermometer>() {
    override suspend fun translate(entity: ThermometerEntity): Thermometer {
        val entityId = if (entity.truck != null) entity.truck!!.id else entity.towable!!.id
        val entityType = if (entity.truck != null) Thermometer.EntityType.TRUCK else Thermometer.EntityType.TOWABLE
        return Thermometer(
            id = entity.id,
            macAddress = entity.macAddress,
            entityId = entityId!!,
            entityType = entityType,
            archivedAt = entity.archivedAt,
            modifiedAt = entity.modifiedAt,
            createdAt = entity.createdAt
        )
    }
}
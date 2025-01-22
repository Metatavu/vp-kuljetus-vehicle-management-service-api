package fi.metatavu.vp.vehiclemanagement.trackables

import fi.metatavu.vp.vehiclemanagement.model.Trackable
import fi.metatavu.vp.vehiclemanagement.model.TrackableType
import fi.metatavu.vp.vehiclemanagement.persistence.ITrackable
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import fi.metatavu.vp.vehiclemanagement.towables.TowableEntity
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for trackable entities
 */
@ApplicationScoped
class TrackableTranslator: AbstractTranslator<ITrackable, Trackable>() {

    override suspend fun translate(entity: ITrackable): Trackable =
        Trackable(
            id = entity.id!!,
            trackableType = when (entity) {
                is TowableEntity -> TrackableType.TOWABLE
                is TruckEntity -> TrackableType.TRUCK
                else -> throw IllegalArgumentException("Unknown trackable type")
            },
            imei = entity.imei!!
        )
}

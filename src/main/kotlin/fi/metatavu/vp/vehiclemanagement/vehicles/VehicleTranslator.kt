package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.model.Vehicle
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Translates vehicle entity to REST
 */
@ApplicationScoped
class VehicleTranslator : AbstractTranslator<VehicleEntity, Vehicle>() {

    @Inject
    lateinit var vehicleTowableRepository: VehicleTowableRepository

    override suspend fun translate(entity: VehicleEntity): Vehicle {
        return Vehicle(
            id = entity.id,
            truckId = entity.truck.id!!,
            towableIds = vehicleTowableRepository.listByVehicle(entity).map { it.towable.id!! },
            archivedAt = entity.archivedAt,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            lastModifierId = entity.lastModifierId,
            creatorId = entity.creatorId
        )
    }

}
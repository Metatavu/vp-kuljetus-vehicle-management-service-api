package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import fi.metatavu.vp.vehiclemanagement.vehicles.VehicleRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Translator for translating Truck entities into REST resources
 */
@ApplicationScoped
class TruckTranslator : AbstractTranslator<Truck, fi.metatavu.vp.api.model.Truck>() {

    @Inject
    lateinit var vehicleRepository: VehicleRepository

    override suspend fun translate(entity: Truck): fi.metatavu.vp.api.model.Truck {
        return fi.metatavu.vp.api.model.Truck(
            id = entity.id,
            plateNumber = entity.plateNumber,
            vin = entity.vin,
            type = entity.type,
            activeVehicleId = vehicleRepository.findActiveForTruck(entity)!!.id!!,
            name = entity.name,
            archivedAt = entity.archivedAt,
            createdAt = entity.createdAt,
            modifiedAt = entity.modifiedAt,
            lastModifierId = entity.lastModifierId,
            creatorId = entity.creatorId
        )
    }

}
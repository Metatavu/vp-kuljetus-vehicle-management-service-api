package fi.metatavu.vp.vehicleManagement.vehicles

import fi.metatavu.vp.vehicleManagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class VehicleTranslator : AbstractTranslator<Vehicle, fi.metatavu.vp.api.model.Vehicle>() {

    @Inject
    lateinit var towableToVehicleRepository: TowableToVehicleRepository

    override suspend fun translate(entity: Vehicle): fi.metatavu.vp.api.model.Vehicle {
        return fi.metatavu.vp.api.model.Vehicle(
            id = entity.id,
            truckId = entity.truck.id!!,
            towableIds = towableToVehicleRepository.listByVehicle(entity).map { it.towable.id!! }
        )
    }

}
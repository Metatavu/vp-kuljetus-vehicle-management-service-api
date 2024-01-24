package fi.metatavu.vp.vehicleManagement.trucks

import fi.metatavu.vp.vehicleManagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for translating Truck entities into REST resources
 */
@ApplicationScoped
class TruckTranslator : AbstractTranslator<Truck, fi.metatavu.vp.vehicleManagement.api.spec.model.Truck>() {

    override fun translate(entity: Truck): fi.metatavu.vp.vehicleManagement.api.spec.model.Truck {
        return fi.metatavu.vp.vehicleManagement.api.spec.model.Truck(
            id = entity.id,
            plateNumber = entity.plateNumber
        )
    }

}
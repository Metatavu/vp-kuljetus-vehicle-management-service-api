package fi.metatavu.vp.vehiclemanagement.telematics

import fi.metatavu.vp.api.model.TelematicData
import fi.metatavu.vp.vehiclemanagement.telematics.trucks.TruckTelematicData
import fi.metatavu.vp.vehiclemanagement.telematics.trucks.TruckTelematicDataRepository
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Controller for telematics
 */
@ApplicationScoped
class TelematicsController {

    @Inject
    lateinit var truckTelematicDataRepository: TruckTelematicDataRepository

    /**
     * Creates a new telematic entry
     *
     * @param truck truck
     * @param telematicData telematic data
     * @return created telematics
     */
    suspend fun createTruckTelematicData(truck: Truck, telematicData: TelematicData): TruckTelematicData {
        return truckTelematicDataRepository.create(
            id = UUID.randomUUID(),
            truck = truck,
            timestamp = telematicData.timestamp,
            imei = telematicData.imei,
            speed = telematicData.speed,
            latitude = telematicData.latitude,
            longitude = telematicData.longitude
        )

    }
}

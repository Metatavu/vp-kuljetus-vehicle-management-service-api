package fi.metatavu.vp.vehiclemanagement.telematics

import fi.metatavu.vp.api.model.TelematicData
import fi.metatavu.vp.vehiclemanagement.towables.Towable
import fi.metatavu.vp.vehiclemanagement.towables.TowableRepository
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import fi.metatavu.vp.vehiclemanagement.trucks.TruckRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Controller for telematics
 */
@ApplicationScoped
class TelematicsController {

    @Inject
    lateinit var telematicsRepository: TelematicsRepository

    @Inject
    lateinit var towableRepository: TowableRepository

    @Inject
    lateinit var truckRepository: TruckRepository

    /**
     * Finds a truck/towable by VIN
     *
     * @param vin VIN
     * @return found car/towable
     */
    suspend fun fineDeviceByVin(vin: String): TelematicsDevice? {
        val foundTruck = truckRepository.findByVin(vin)
        if (foundTruck != null) {
            return foundTruck
        }
        val foundTowable = towableRepository.findByVin(vin)
        if (foundTowable != null) {
            return foundTowable
        }

        return null
    }

    /**
     * Creates a new telematic entry
     *
     * @param telematicsDevice telematics device
     * @param telematicData telematic data
     * @return created telematics
     */
    suspend fun create(telematicsDevice: TelematicsDevice, telematicData: TelematicData): Telematics {
        return telematicsRepository.create(
            id = UUID.randomUUID(),
            truck = telematicsDevice as? Truck,
            towable = telematicsDevice as? Towable,
            timestamp = telematicData.timestamp,
            imei = telematicData.imei,
            speed = telematicData.speed,
            latitude = telematicData.latitude,
            longitude = telematicData.longitude
        )

    }
}

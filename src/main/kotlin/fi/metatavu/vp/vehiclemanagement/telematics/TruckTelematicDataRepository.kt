package fi.metatavu.vp.vehiclemanagement.telematics

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.telematics.trucks.TruckTelematicData
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository class for Telematics
 */
@ApplicationScoped
class TruckTelematicDataRepository : AbstractRepository<TruckTelematicData, UUID>() {

    /**
     * Saves a new Telematics entity to the database
     *
     * @param id id
     * @param truck truck
     * @param timestamp timestamp
     * @param imei imei
     * @param speed speed
     * @param latitude latitude
     * @param longitude longitude
     * @return created telematics
     */
    suspend fun create(
        id: UUID,
        truck: Truck,
        timestamp: Long,
        imei: String,
        speed: Float,
        latitude: Double,
        longitude: Double
    ): TruckTelematicData {
        val truckTelematicData = TruckTelematicData()
        truckTelematicData.id = id
        truckTelematicData.truck = truck
        truckTelematicData.timestamp = timestamp
        truckTelematicData.imei = imei
        truckTelematicData.speed = speed
        truckTelematicData.latitude = latitude
        truckTelematicData.longitude = longitude
        return persistSuspending(truckTelematicData)
    }

    /**
     * Lists telematics by device (truck or trailer)
     *
     * @param truck truck
     * @return list of telematic truck data
     */
    suspend fun listByTruck(truck: Truck): List<TruckTelematicData> {
         return list("truck", truck).awaitSuspending()
    }
}

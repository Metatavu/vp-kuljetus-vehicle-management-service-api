package fi.metatavu.vp.vehiclemanagement.telematics

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.towables.Towable
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository class for Telematics
 */
@ApplicationScoped
class TelematicsRepository : AbstractRepository<Telematics, UUID>() {

    /**
     * Saves a new Telematics entity to the database
     *
     * @param id id
     * @param truck truck
     * @param towable towable
     * @param timestamp timestamp
     * @param imei imei
     * @param speed speed
     * @param latitude latitude
     * @param longitude longitude
     * @return created telematics
     */
    suspend fun create(
        id: UUID,
        truck: Truck?,
        towable: Towable?,
        timestamp: Long,
        imei: String,
        speed: Float,
        latitude: Double,
        longitude: Double
    ): Telematics {
        val telematics = Telematics()
        telematics.id = id
        telematics.truck = truck
        telematics.towable = towable
        telematics.timestamp = timestamp
        telematics.imei = imei
        telematics.speed = speed
        telematics.latitude = latitude
        telematics.longitude = longitude
        return persistSuspending(telematics)
    }

    /**
     * Lists telematics by device (truck or trailer)
     *
     * @param telematicsDevice truck or trailer
     * @return list of telematics
     */
    suspend fun listByDevice(telematicsDevice: TelematicsDevice): List<Telematics> {
        return when (telematicsDevice) {
            is Truck -> {
                list("truck", telematicsDevice).awaitSuspending()
            }

            is Towable -> {
                list("towable", telematicsDevice).awaitSuspending()
            }

            else -> {
                emptyList()
            }
        }
    }
}

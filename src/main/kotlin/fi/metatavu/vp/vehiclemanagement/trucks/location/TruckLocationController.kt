package fi.metatavu.vp.vehiclemanagement.trucks.location

import fi.metatavu.vp.api.model.TruckLocation
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.*

/**
 * Controller for truck locations
 */
@ApplicationScoped
class TruckLocationController {

    @Inject
    lateinit var truckLocationRepository: TruckLocationRepository

    /**
     * Creates new truck location
     *
     * @param truck truck
     * @param truckLocation truck location REST object
     * @return created truck location
     */
    suspend fun createTruckLocation(
        truck: Truck,
        truckLocation: TruckLocation
    ): fi.metatavu.vp.vehiclemanagement.trucks.location.TruckLocation {
        return truckLocationRepository.create(
            id = UUID.randomUUID(),
            timestamp = truckLocation.id,
            latitude = truckLocation.latitude,
            longitude = truckLocation.longitude,
            heading = truckLocation.heading,
            truck = truck
        )
    }

    fun listTruckLocations(truck: Truck, after: OffsetDateTime?, before: OffsetDateTime?, first: Int?, max: Int?) {
        return truckLocationRepository.listTruckLocations(truck, after, before, first, max)
    }
}
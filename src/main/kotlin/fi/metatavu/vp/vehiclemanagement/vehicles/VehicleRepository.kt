package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository for vehicles
 */
@ApplicationScoped
class VehicleRepository: AbstractRepository<Vehicle, UUID>() {

    /**
     * Creates a vehicle
     *
     * @param id id
     * @param truck truck of the vehicle
     * @param userId creator id
     * @return created vehicle
     */
    suspend fun create(
        id: UUID,
        truck: Truck,
        userId: UUID
    ): Vehicle {
        val vehicle = Vehicle()
        vehicle.id = UUID.randomUUID()
        vehicle.truck = truck
        vehicle.creatorId = userId
        vehicle.lastModifierId = userId
        return persistSuspending(vehicle)
    }

    /**
     * Lists vehicles
     *
     * @param truck truck
     * @param first first result
     * @param max max results
     * @return vehicles
     */
    suspend fun list(truck: Truck?, first: Int?, max: Int?): Pair<List<Vehicle>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (truck != null) {
            addCondition(stringBuilder, "truck = :truck")
            parameters.and("truck", truck)
        }

        return applyFirstMaxToQuery(
            query = find(stringBuilder.toString(), parameters),
            firstIndex = first,
            maxResults = max
        )
    }

}
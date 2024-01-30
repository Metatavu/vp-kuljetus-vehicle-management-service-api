package fi.metatavu.vp.vehicleManagement.vehicles

import fi.metatavu.vp.vehicleManagement.persistence.AbstractRepository
import fi.metatavu.vp.vehicleManagement.trucks.Truck
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
        val sb = StringBuilder()
        val parameters = Parameters()

        if (truck != null) {
            addCondition(sb, "truck = :truck")
            parameters.and("truck", truck)
        }

        return applyFirstMaxToQuery(
            query = find(sb.toString(), parameters),
            firstIndex = first,
            maxResults = max
        )
    }

}
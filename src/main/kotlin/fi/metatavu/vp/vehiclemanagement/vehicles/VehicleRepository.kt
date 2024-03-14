package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
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
     * @param archived archived
     * @param first first result
     * @param max max results
     * @return vehicles
     */
    suspend fun list(truck: Truck?, archived: Boolean?, first: Int?, max: Int?): Pair<List<Vehicle>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (truck != null) {
            addCondition(stringBuilder, "truck = :truck")
            parameters.and("truck", truck)
        }

        if (archived == null || archived == false) {
            addCondition(stringBuilder, "archivedAt IS NULL")
        } else if (archived == true) {
            addCondition(stringBuilder, "archivedAt IS NOT NULL")
        }

        stringBuilder.append("ORDER BY createdAt DESC")
        return applyFirstMaxToQuery(
            query = find(stringBuilder.toString(), parameters),
            firstIndex = first,
            maxResults = max
        )
    }

    /**
     * Finds the active vehicle for a truck
     *
     * @param truck truck
     * @return active vehicle for the truck
     */
    suspend fun findActiveForTruck(truck: Truck): Vehicle? {
        val query = "truck = :truck AND archivedAt IS NULL"
        val parameters = Parameters()
        parameters.and("truck", truck)
        return find(query, parameters).firstResult<Vehicle>().awaitSuspending()
    }

    /**
     * Lists vehicles
     *
     * @param truck truck
     * @return list of vehicles that contains the truck
     */
    suspend fun listByTruck(truck: Truck?): List<Vehicle> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (truck != null) {
            addCondition(stringBuilder, "truck = :truck")
            parameters.and("truck", truck)
        }

        stringBuilder.append("ORDER BY createdAt DESC")
        return find(stringBuilder.toString(), parameters).list<Vehicle>().awaitSuspending()
    }

}
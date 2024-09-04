package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository for vehicles
 */
@ApplicationScoped
class VehicleRepository: AbstractRepository<VehicleEntity, UUID>() {

    /**
     * Creates a vehicle
     *
     * @param id id
     * @param truckEntity truck of the vehicle
     * @param userId creator id
     * @return created vehicle
     */
    suspend fun create(
        id: UUID,
        truckEntity: TruckEntity,
        userId: UUID
    ): VehicleEntity {
        val vehicleEntity = VehicleEntity()
        vehicleEntity.id = UUID.randomUUID()
        vehicleEntity.truck = truckEntity
        vehicleEntity.creatorId = userId
        vehicleEntity.lastModifierId = userId
        return persistSuspending(vehicleEntity)
    }

    /**
     * Lists vehicles
     *
     * @param truckEntity truck
     * @param archived archived
     * @param first first result
     * @param max max results
     * @return vehicles
     */
    suspend fun list(truckEntity: TruckEntity?, archived: Boolean?, first: Int?, max: Int?): Pair<List<VehicleEntity>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (truckEntity != null) {
            addCondition(stringBuilder, "truck = :truck")
            parameters.and("truck", truckEntity)
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
     * @param truckEntity truck
     * @return active vehicle for the truck
     */
    suspend fun findActiveForTruck(truckEntity: TruckEntity): VehicleEntity? {
        val query = "truck = :truck AND archivedAt IS NULL"
        val parameters = Parameters()
        parameters.and("truck", truckEntity)
        return find(query, parameters).firstResult<VehicleEntity>().awaitSuspending()
    }

    /**
     * Lists vehicles
     *
     * @param truckEntity truck
     * @return list of vehicles that contains the truck
     */
    suspend fun listByTruck(truckEntity: TruckEntity?): List<VehicleEntity> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (truckEntity != null) {
            addCondition(stringBuilder, "truck = :truck")
            parameters.and("truck", truckEntity)
        }

        stringBuilder.append("ORDER BY createdAt DESC")
        return find(stringBuilder.toString(), parameters).list<VehicleEntity>().awaitSuspending()
    }

}
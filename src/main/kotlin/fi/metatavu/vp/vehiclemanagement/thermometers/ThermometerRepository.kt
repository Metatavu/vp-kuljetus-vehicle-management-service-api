package fi.metatavu.vp.vehiclemanagement.thermometers

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.towables.TowableEntity
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository class for Thermometer
 */
@ApplicationScoped
class ThermometerRepository : AbstractRepository<ThermometerEntity, UUID>() {

    /**
     * Creates thermometer
     *
     * @param id thermometer id
     * @param hardwareSensorId mac address
     * @param truck truck
     * @param towable towable
     * @return created thermometer
     */
    suspend fun create(
        id: UUID,
        hardwareSensorId: String,
        truck: TruckEntity?,
        towable: TowableEntity?
    ): ThermometerEntity {
        val thermometer = ThermometerEntity()
        thermometer.id = id
        thermometer.hardwareSensorId = hardwareSensorId
        thermometer.truck = truck
        thermometer.towable = towable
        return persistSuspending(thermometer)
    }

    /**
     * Finds thermometer by mac address
     *
     * @param hardwareSensorId mac address
     * @return found thermometer or null if not found
     */
    suspend fun listUnarchivedByMac(hardwareSensorId: String): List<ThermometerEntity> {
        return list("hardwareSensorId = ?1 and archivedAt is null", hardwareSensorId).awaitSuspending()
    }

    /**
     * Lists thermometers
     *
     * @param truckId truck id
     * @param towableId towable id
     * @param includeArchived include archived
     * @param first first
     * @param max max
     * @return list of thermometers and the total count
     */
    suspend fun listThermometers(
        truckId: UUID? = null,
        towableId: UUID? = null,
        includeArchived: Boolean,
        first: Int?,
        max: Int?
    ): Pair<List<ThermometerEntity>, Long> {
        val queryBuilder = StringBuilder()
        val queryParameters = Parameters()

        if (truckId != null) {
            addCondition(queryBuilder, "truck.id = :truckId")
            queryParameters.and("truckId", truckId)
        }

        if (towableId != null) {
            addCondition(queryBuilder, "towable.id = :towableId")
            queryParameters.and("towableId", towableId)
        }

        if (!includeArchived) {
            addCondition(queryBuilder, "archivedAt is null")
        }

        queryBuilder.append(" ORDER BY modifiedAt DESC")
        return applyFirstMaxToQuery(
            firstIndex = first,
            maxResults = max,
            query = find(queryBuilder.toString(), queryParameters)
        )
    }

    /**
     * Finds thermometer by truck
     *
     * @param targetTruck target truck
     * @return found thermometer or null if not found
     */
    suspend fun findByTruck(targetTruck: TruckEntity): ThermometerEntity? {
        return find("truck = ?1", targetTruck).firstResult<ThermometerEntity>().awaitSuspending()
    }

    /**
     * Finds thermometer by towable
     *
     * @param targetTowable target towable
     * @return found thermometer or null if not found
     */
    suspend fun findByTowable(targetTowable: TowableEntity): ThermometerEntity? {
        return find("towable = ?1", targetTowable).firstResult<ThermometerEntity>().awaitSuspending()
    }


}
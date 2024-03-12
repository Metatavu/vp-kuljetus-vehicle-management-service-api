package fi.metatavu.vp.vehiclemanagement.towables

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository for towables
 */
@ApplicationScoped
class TowableRepository: AbstractRepository<Towable, UUID>() {

    /**
     * Creates new towable
     *
     * @param id id
     * @param plateNumber plate number
     * @param type type
     * @param vin vin
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created towable
     */
    suspend fun create(
        id: UUID,
        plateNumber: String,
        type: fi.metatavu.vp.api.model.Towable.Type,
        vin: String,
        name: String?,
        creatorId: UUID,
        lastModifierId: UUID
    ): Towable {
        val towable = Towable()
        towable.id = id
        towable.plateNumber = plateNumber
        towable.type = type
        towable.vin = vin
        towable.name = name
        towable.creatorId = creatorId
        towable.lastModifierId = lastModifierId
        return persistSuspending(towable)
    }

    /**
     * Lists towables
     *
     * @param plateNumber plate number
     * @param archived archived
     * @param firstResult first result
     * @param maxResults max results
     * @return list of towables and the total count
     */
    suspend fun list(plateNumber: String?, archived: Boolean?, firstResult: Int?, maxResults: Int?): Pair<List<Towable>, Long> {
        val stringBuilder = StringBuilder()
        val parameters = Parameters()

        if (plateNumber != null) {
            addCondition(stringBuilder, "plateNumber = :plateNumber")
            parameters.and("plateNumber", plateNumber)
        }

        if (archived == null || archived == false) {
            addCondition(stringBuilder, "archivedAt IS NULL")
        } else if (archived == true) {
            addCondition(stringBuilder, "archivedAt IS NOT NULL")
        }

        stringBuilder.append("ORDER BY createdAt DESC")
        return applyFirstMaxToQuery(
            query = find(stringBuilder.toString(), parameters),
            firstIndex = firstResult,
            maxResults = maxResults
        )
    }

    /**
     * Counts trailers by plate number
     *
     * @param plateNumber plate number
     * @return number of trailers with the given plate number
     */
    suspend fun countByPlateNumber(plateNumber: String): Long {
        return count("plateNumber", plateNumber).awaitSuspending()
    }

    /**
     * Finds a towable by VIN
     *
     * @param vin VIN
     * @return found towable or null if not found
     */
    suspend fun findByVin(vin: String): Towable? {
        return find("vin", vin).firstResult<Towable>().awaitSuspending()
    }

    /**
     * Counts towables by VIN
     *
     * @param vin VIN
     * @return number of towables with the given VIN
     */
    suspend fun countByVin(vin: String): Long {
        return count("vin", vin).awaitSuspending()
    }
}
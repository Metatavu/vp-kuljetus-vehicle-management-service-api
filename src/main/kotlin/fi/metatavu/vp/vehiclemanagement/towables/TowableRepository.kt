package fi.metatavu.vp.vehiclemanagement.towables

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

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
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created towable
     */
    suspend fun create(
        id: UUID,
        plateNumber: String,
        type: fi.metatavu.vp.api.model.Towable.Type,
        creatorId: UUID,
        lastModifierId: UUID
    ): Towable {
        val towable = Towable()
        towable.id = id
        towable.plateNumber = plateNumber
        towable.type = type
        towable.creatorId = creatorId
        towable.lastModifierId = lastModifierId
        return persistSuspending(towable)
    }

    /**
     * Lists towables
     *
     * @param plateNumber plate number
     * @param firstResult first result
     * @param maxResults max results
     * @return list of towables
     */
    suspend fun list(plateNumber: String?, firstResult: Int?, maxResults: Int?): Pair<List<Towable>, Long> {
        val sb = StringBuilder()
        val parameters = Parameters()

        if (plateNumber != null) {
            addCondition(sb, "plateNumber = :plateNumber")
            parameters.and("plateNumber", plateNumber)
        }

        return applyFirstMaxToQuery(
            query = find(sb.toString(), parameters),
            firstIndex = firstResult,
            maxResults = maxResults
        )
    }

}
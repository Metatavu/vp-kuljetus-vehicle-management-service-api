package fi.metatavu.vp.vehicleManagement.trailers

import fi.metatavu.vp.vehicleManagement.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

/**
 * Repository for trailers
 */
@ApplicationScoped
class TrailerRepository: AbstractRepository<Trailer, UUID>() {

    /**
     * Creates new trailer
     *
     * @param id id
     * @param plateNumber plate number
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created trailer
     */
    suspend fun create(
        id: UUID,
        plateNumber: String,
        creatorId: UUID,
        lastModifierId: UUID
    ): Trailer {
        val trailer = Trailer()
        trailer.id = id
        trailer.plateNumber = plateNumber
        trailer.creatorId = creatorId
        trailer.lastModifierId = lastModifierId
        return persistSuspending(trailer)
    }

    /**
     * Lists trailers
     *
     * @param plateNumber plate number
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trailers
     */
    suspend fun list(plateNumber: String?, firstResult: Int?, maxResults: Int?): Pair<List<Trailer>, Long> {
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
package fi.metatavu.vp.vehicleManagement.trucks

import fi.metatavu.vp.vehicleManagement.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

/**
 * Repository class for Truck
 */
@ApplicationScoped
class TruckRepository: AbstractRepository<Truck, UUID>() {

    /**
     * Saves a new truck to the database
     *
     * @param id id
     * @param plateNumber plate number
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created truck
     */
    suspend fun create(
        id: UUID,
        plateNumber: String,
        creatorId: UUID,
        lastModifierId: UUID
    ): Truck {
        val truck = Truck()
        truck.id = id
        truck.plateNumber = plateNumber
        truck.creatorId = creatorId
        truck.lastModifierId = lastModifierId
        return persistSuspending(truck)
    }

    /**
     * Lists trucks
     *
     * @param plateNumber plate number
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trucks
     */
    suspend fun list(plateNumber: String?, firstResult: Int?, maxResults: Int?): Pair<List<Truck>, Long> {
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
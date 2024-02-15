package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
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
     * @param type truck type
     * @param vin vin
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created truck
     */
    suspend fun create(
        id: UUID,
        plateNumber: String,
        type: fi.metatavu.vp.api.model.Truck.Type,
        vin: String,
        creatorId: UUID,
        lastModifierId: UUID
    ): Truck {
        val truck = Truck()
        truck.id = id
        truck.plateNumber = plateNumber
        truck.type = type
        truck.vin = vin
        truck.creatorId = creatorId
        truck.lastModifierId = lastModifierId
        return persistSuspending(truck)
    }

    /**
     * Lists trucks
     *
     * @param plateNumber plate number
     * @param archived archived
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trucks
     */
    suspend fun list(plateNumber: String?, archived: Boolean?, firstResult: Int?, maxResults: Int?): Pair<List<Truck>, Long> {
        val sb = StringBuilder()
        val parameters = Parameters()

        if (plateNumber != null) {
            addCondition(sb, "plateNumber = :plateNumber")
            parameters.and("plateNumber", plateNumber)
        }

        if (archived == null || archived == false) {
            addCondition(sb, "archivedAt IS NULL")
        } else if (archived == true) {
            addCondition(sb, "archivedAt IS NOT NULL")
        }

        return applyFirstMaxToQuery(
            query = find(sb.toString(), parameters),
            firstIndex = firstResult,
            maxResults = maxResults
        )
    }

    /**
     * Counts trucks by plate number
     *
     * @param plateNumber plate number
     * @return number of trucks with the given plate number
     */
    suspend fun countByPlateNumber(plateNumber: String): Long {
        return count("plateNumber", plateNumber).awaitSuspending()
    }

    /**
     * Finds a truck by vin
     *
     * @param vin vin
     * @return found truck or null if not found
     */
    suspend fun findByVin(vin: String): Truck? {
        return find("vin", vin).firstResult<Truck>().awaitSuspending()
    }

    /**
     * Counts trucks by vin
     *
     * @param vin vin
     * @return number of trucks with the given vin
     */
    suspend fun countByVin(vin: String): Long {
        return count("vin", vin).awaitSuspending()
    }
}
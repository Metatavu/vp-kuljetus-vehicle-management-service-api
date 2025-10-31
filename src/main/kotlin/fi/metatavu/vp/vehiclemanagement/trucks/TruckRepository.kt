package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.model.SortOrder
import fi.metatavu.vp.vehiclemanagement.model.Truck
import fi.metatavu.vp.vehiclemanagement.model.TruckSortByField
import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

/**
 * Repository class for Truck
 */
@ApplicationScoped
class TruckRepository: AbstractRepository<TruckEntity, UUID>() {

    /**
     * Saves a new truck to the database
     *
     * @param id id
     * @param plateNumber plate number
     * @param type truck type
     * @param vin vin
     * @param name name
     * @param imei imei
     * @param costCenter cost center
     * @param creatorId creator id
     * @param lastModifierId last modifier id
     * @return created truck
     */
    suspend fun create(
        id: UUID,
        plateNumber: String,
        type: Truck.Type,
        vin: String,
        name: String?,
        imei: String?,
        costCenter: String?,
        creatorId: UUID,
        lastModifierId: UUID
    ): TruckEntity {
        val truckEntity = TruckEntity()
        truckEntity.id = id
        truckEntity.plateNumber = plateNumber
        truckEntity.type = type
        truckEntity.vin = vin
        truckEntity.name = name
        truckEntity.imei = imei
        truckEntity.costCenter = costCenter
        truckEntity.creatorId = creatorId
        truckEntity.lastModifierId = lastModifierId
        return persistSuspending(truckEntity)
    }

    /**
     * Lists trucks
     *
     * @param plateNumber plate number
     * @param archived archived
     * @param vin vin
     * @param sortBy sort by field
     * @param sortDirection sort direction
     * @param textSearch text search
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trucks
     */
    suspend fun list(
        plateNumber: String?,
        archived: Boolean?,
        vin: String?,
        sortBy: TruckSortByField?,
        sortDirection: SortOrder?,
        textSearch: String?,
        firstResult: Int?,
        maxResults: Int?
    ): Pair<List<TruckEntity>, Long> {
        val sb = StringBuilder()
        val parameters = Parameters()

        val validSortDirection = convertRestSortOrderToJpa(sortDirection)
        val validSortBy = convertRestSortByToJpa(sortBy)

        if (textSearch != null) {
            addCondition(sb, "(plateNumber LIKE :textSearch OR name LIKE :textSearch)")
            parameters.and("textSearch", "%$textSearch%")
        }

        if (plateNumber != null) {
            addCondition(sb, "plateNumber = :plateNumber")
            parameters.and("plateNumber", plateNumber)
        }

        if (vin != null) {
            addCondition(sb, "vin = :vin")
            parameters.and("vin", vin)
        }

        if (archived == null || archived == false) {
            addCondition(sb, "archivedAt IS NULL")
        } else if (archived == true) {
            addCondition(sb, "archivedAt IS NOT NULL")
        }

        if (validSortDirection != null) {
            sb.append("ORDER BY $validSortBy $validSortDirection")
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
    suspend fun findByVin(vin: String): TruckEntity? {
        return find("vin", vin).firstResult<TruckEntity>().awaitSuspending()
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

    /**
     * Finds a truck by imei
     *
     * @param imei imei
     * @return found truck or null if not found
     */
    suspend fun findByImei(imei: String): TruckEntity? {
        return find("imei", imei).firstResult<TruckEntity>().awaitSuspending()
    }

    /**
     * Converts REST sort by field to JPA sort by field
     *
     * Default sort by field is name
     *
     * @param sortBy REST sort by field
     * @return JPA sort by field
     */
    private fun convertRestSortByToJpa(sortBy: TruckSortByField?): String {
        return when (sortBy) {
            TruckSortByField.PLATE_NUMBER -> "plateNumber"
            else -> "name"
        }
    }
}
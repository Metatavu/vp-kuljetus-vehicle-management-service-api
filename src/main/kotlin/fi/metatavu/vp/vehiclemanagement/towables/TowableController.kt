package fi.metatavu.vp.vehiclemanagement.towables

import fi.metatavu.vp.vehiclemanagement.model.Towable
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Controller for towables
 */
@ApplicationScoped
class TowableController {

    @Inject
    lateinit var towableRepository: TowableRepository

    /**
     * Creates new towable
     *
     * @param plateNumber plate number
     * @param type type
     * @param vin vin
     * @param name name
     * @param userId user id
     * @return created towable
     */
    suspend fun createTowable(
        plateNumber: String,
        type: Towable.Type,
        vin: String,
        name: String?,
        userId: UUID
    ): TowableEntity {
        return towableRepository.create(
            id = UUID.randomUUID(),
            plateNumber = plateNumber,
            type = type,
            vin = vin,
            name = name,
            creatorId = userId,
            lastModifierId = userId
        )
    }

    /**
     * Finds towable by id
     *
     * @param towableId towable id
     * @return found towable or null if not found
     */
    suspend fun findTowable(towableId: UUID): TowableEntity? {
        return towableRepository.findByIdSuspending(towableId)
    }

    /**
     * Lists towables
     *
     * @param plateNumber plate number
     * @param archived archived
     * @param firstResult first result
     * @param maxResults max results
     * @return list of towables
     */
    suspend fun listTowables(
        plateNumber: String?,
        archived: Boolean?,
        firstResult: Int?,
        maxResults: Int?
    ): Pair<List<TowableEntity>, Long> {
        return towableRepository.list(
            plateNumber = plateNumber,
            archived = archived,
            firstResult = firstResult,
            maxResults = maxResults
        )
    }

    /**
     * Updates towable
     *
     * @param existingTowableEntity existing towable
     * @param newTowableData new towable data
     * @param userId user id
     */
    suspend fun updateTowable(
        existingTowableEntity: TowableEntity,
        newTowableData: Towable,
        userId: UUID
    ): TowableEntity {
        existingTowableEntity.plateNumber = newTowableData.plateNumber
        existingTowableEntity.archivedAt = newTowableData.archivedAt
        existingTowableEntity.type = newTowableData.type
        existingTowableEntity.vin = newTowableData.vin
        existingTowableEntity.name = newTowableData.name
        existingTowableEntity.lastModifierId = userId
        return towableRepository.persistSuspending(existingTowableEntity)
    }

    /**
     * Deletes towable
     *
     * @param towableEntity towable to be deleted
     */
    suspend fun deleteTowable(towableEntity: TowableEntity) {
        towableRepository.deleteSuspending(towableEntity)
    }

}
package fi.metatavu.vp.vehiclemanagement.towables

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
     * @param userId user id
     * @return created towable
     */
    suspend fun createTowable(plateNumber: String, type: fi.metatavu.vp.api.model.Towable.Type, vin: String, userId: UUID): Towable {
        return towableRepository.create(
            id = UUID.randomUUID(),
            plateNumber = plateNumber,
            type = type,
            vin = vin,
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
    suspend fun findTowable(towableId: UUID): Towable? {
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
    ): Pair<List<Towable>, Long> {
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
     * @param existingTowable existing towable
     * @param newTowableData new towable data
     * @param userId user id
     */
    suspend fun updateTowable(
        existingTowable: Towable,
        newTowableData: fi.metatavu.vp.api.model.Towable,
        userId: UUID
    ): Towable {
        existingTowable.plateNumber = newTowableData.plateNumber
        existingTowable.archivedAt = newTowableData.archivedAt
        existingTowable.type = newTowableData.type
        existingTowable.vin = newTowableData.vin
        existingTowable.lastModifierId = userId
        return towableRepository.persistSuspending(existingTowable)
    }

    /**
     * Deletes towable
     *
     * @param towable towable to be deleted
     */
    suspend fun deleteTowable(towable: Towable) {
        towableRepository.deleteSuspending(towable)
    }

}
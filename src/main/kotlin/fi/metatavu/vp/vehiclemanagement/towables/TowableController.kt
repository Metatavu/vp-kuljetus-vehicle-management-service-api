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
     * @param userId user id
     * @return created towable
     */
    suspend fun createTowable(plateNumber: String, type: fi.metatavu.vp.api.model.Towable.Type, userId: UUID): Towable {
        return towableRepository.create(
            id = UUID.randomUUID(),
            plateNumber = plateNumber,
            type = type,
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
    suspend fun findTrailer(towableId: UUID): Towable? {
        return towableRepository.findByIdSuspending(towableId)
    }

    /**
     * Lists towables
     *
     * @param plateNumber plate number
     * @param firstResult first result
     * @param maxResults max results
     * @return list of towables
     */
    suspend fun listTrailers(plateNumber: String?, firstResult: Int?, maxResults: Int?): Pair<List<Towable>, Long> {
        return towableRepository.list(plateNumber = plateNumber, firstResult = firstResult, maxResults = maxResults)
    }

    /**
     * Updates towable
     *
     * @param existingTowable existing towable
     * @param newTrailerData new towable data
     * @param userId user id
     */
    suspend fun updateTrailer(
        existingTowable: Towable,
        newTrailerData: fi.metatavu.vp.api.model.Towable,
        userId: UUID
    ): Towable {
        existingTowable.plateNumber = newTrailerData.plateNumber
        existingTowable.type = newTrailerData.type
        existingTowable.lastModifierId = userId
        return towableRepository.persistSuspending(existingTowable)
    }

    /**
     * Deletes towable
     *
     * @param towable towable to be deleted
     */
    suspend fun deleteTrailer(towable: Towable) {
        towableRepository.deleteSuspending(towable)
    }

}
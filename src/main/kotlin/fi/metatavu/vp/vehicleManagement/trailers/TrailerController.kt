package fi.metatavu.vp.vehicleManagement.trailers

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.util.*

/**
 * Controller for trailers
 */
@ApplicationScoped
class TrailerController {

    @Inject
    lateinit var trailerRepository: TrailerRepository

    /**
     * Creates new trailer
     *
     * @param plateNumber plate number
     * @param userId user id
     * @return created trailer
     */
    suspend fun createTrailer(plateNumber: String, userId: UUID): Trailer {
        return trailerRepository.create(
            id = UUID.randomUUID(),
            plateNumber = plateNumber,
            creatorId = userId,
            lastModifierId = userId
        )
    }

    /**
     * Finds trailer by id
     *
     * @param trailerId trailer id
     * @return found trailer or null if not found
     */
    suspend fun findTrailer(trailerId: UUID): Trailer? {
        return trailerRepository.findByIdSuspending(trailerId)
    }

    /**
     * Lists trailers
     *
     * @param plateNumber plate number
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trailers
     */
    suspend fun listTrailers(plateNumber: String?, firstResult: Int?, maxResults: Int?): Pair<List<Trailer>, Long> {
        return trailerRepository.list(plateNumber = plateNumber, firstResult = firstResult, maxResults = maxResults)
    }

    /**
     * Updates trailer
     *
     * @param existingTrailer existing trailer
     * @param newTrailerData new trailer data
     * @param userId user id
     */
    suspend fun updateTrailer(
        existingTrailer: Trailer,
        newTrailerData: fi.metatavu.vp.api.model.Trailer,
        userId: UUID
    ): Trailer {
        existingTrailer.plateNumber = newTrailerData.plateNumber
        existingTrailer.lastModifierId = userId
        return trailerRepository.persistSuspending(existingTrailer)
    }

    /**
     * Deletes trailer
     *
     * @param trailer trailer to be deleted
     */
    suspend fun deleteTrailer(trailer: Trailer) {
        trailerRepository.deleteSuspending(trailer)
    }

}
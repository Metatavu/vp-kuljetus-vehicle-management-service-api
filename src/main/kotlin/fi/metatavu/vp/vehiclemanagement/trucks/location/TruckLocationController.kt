package fi.metatavu.vp.vehiclemanagement.trucks.location

import fi.metatavu.vp.vehiclemanagement.model.TruckLocation
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.OffsetDateTime
import java.util.*
import kotlin.math.abs

/**
 * Controller for truck locations
 */
@ApplicationScoped
class TruckLocationController {

    @Inject
    lateinit var truckLocationRepository: TruckLocationRepository

    @Inject
    lateinit var logger: Logger

    /**
     * Creates new truck location
     *
     * @param truckEntity truck
     * @param truckLocation truck location REST object
     * @return created truck location
     */
    suspend fun createTruckLocation(
        truckEntity: TruckEntity,
        truckLocation: TruckLocation
    ): TruckLocationEntity? {
        val existingRecord = truckLocationRepository.find(
            "truck = :truck and timestamp = :timestamp",
            Parameters.with("truck", truckEntity).and("timestamp", truckLocation.timestamp)
        ).firstResult<TruckLocationEntity>().awaitSuspending()
        if (existingRecord != null) {
            logger.debug("Truck location $truckLocation already exists for truck with id ${truckEntity.id}")
            return existingRecord
        }

        val latestRecord = truckLocationRepository.find(
            "truck = :truck and timestamp <= :timestamp order by timestamp desc limit 1",
            Parameters.with("truck", truckEntity).and("timestamp", truckLocation.timestamp)
        ).firstResult<TruckLocationEntity>().awaitSuspending()
        if (latestRecord != null &&
            latestRecord.timestamp!! < truckLocation.timestamp &&
            abs(latestRecord.latitude!! - truckLocation.latitude) < 0.0001 &&
            abs(latestRecord.longitude!! - truckLocation.longitude) < 0.0001) {
            logger.debug("Latest truck location $truckLocation for truck with id ${truckEntity.id} was the same")
            return null
        }


        val createdTruckLocation = truckLocationRepository.create(
            id = UUID.randomUUID(),
            timestamp = truckLocation.timestamp,
            latitude = truckLocation.latitude,
            longitude = truckLocation.longitude,
            heading = truckLocation.heading,
            truckEntity = truckEntity
        )

        logger.debug("Created truck location $truckLocation for truck with id ${truckEntity.id}")

        return createdTruckLocation
    }

    /**
     * Lists truck locations
     *
     * @param truckEntity truck
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return truck locations
     */
    suspend fun listTruckLocations(
        truckEntity: TruckEntity,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<TruckLocationEntity>, Long> {
        return truckLocationRepository.listTruckLocations(
            truckEntity = truckEntity,
            after = after,
            before = before,
            first = first,
            max = max
        )
    }

    /**
     * Deletes truck location
     *
     * @param truckLocationEntity truck location
     */
    suspend fun deleteTruckLocation(truckLocationEntity: TruckLocationEntity) {
        return truckLocationRepository.deleteSuspending(truckLocationEntity)
    }
}
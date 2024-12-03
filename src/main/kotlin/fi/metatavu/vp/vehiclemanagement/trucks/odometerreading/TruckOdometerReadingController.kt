package fi.metatavu.vp.vehiclemanagement.trucks.odometerreading

import fi.metatavu.vp.vehiclemanagement.model.TruckOdometerReading
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
 * Controller for truck odometer readings
 */
@ApplicationScoped
class TruckOdometerReadingController {

    @Inject
    lateinit var truckOdometerReadingRepository: TruckOdometerReadingRepository

    @Inject
    lateinit var logger: Logger

    /**
     * Creates truck odometer reading
     *
     * @param truckEntity truck
     * @param truckOdometerReading truck odometer reading
     * @return created truck odometer reading
     */
    suspend fun createTruckOdometerReading(
        truckEntity: TruckEntity,
        truckOdometerReading: TruckOdometerReading
    ): TruckOdometerReadingEntity? {
        val existingRecord = truckOdometerReadingRepository.find(
            "truck = :truck and timestamp = :timestamp",
            Parameters.with("truck", truckEntity).and("timestamp", truckOdometerReading.timestamp)
        ).firstResult<TruckOdometerReadingEntity>().awaitSuspending()
        if (existingRecord != null) {
            logger.debug("Truck odometer reading $truckOdometerReading already exists for truck with id ${truckEntity.id}")
            return existingRecord
        }

        val latestRecord = truckOdometerReadingRepository.find(
            "truck = :truck and timestamp <= :timestamp order by timestamp desc limit 1",
            Parameters.with("truck", truckEntity).and("timestamp", truckOdometerReading.timestamp)
        ).firstResult<TruckOdometerReadingEntity>().awaitSuspending()
        if (latestRecord != null &&
            latestRecord.timestamp!! < truckOdometerReading.timestamp &&
            abs(latestRecord.odometerReading!! - truckOdometerReading.odometerReading) < 0.0001
        ) {
            logger.debug("Latest truck odometer reading $truckOdometerReading for truck with id ${truckEntity.id} was the same")
            return null
        }

        val createdTruckOdometerReading = truckOdometerReadingRepository.createTruckOdometerReading(
            id = UUID.randomUUID(),
            timestamp = truckOdometerReading.timestamp,
            odometerReading = truckOdometerReading.odometerReading,
            truck = truckEntity
        )

        logger.debug("Created truck odometer reading $truckOdometerReading for truck with id ${truckEntity.id}")

        return createdTruckOdometerReading
    }

    /**
     * Lists truck odometer readings
     *
     * @param truck truck
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return pair of list truck odometer readings and count
     */
    suspend fun list(
        truck: TruckEntity,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<TruckOdometerReadingEntity>, Long> {
        return truckOdometerReadingRepository.listTruckOdometerReadings(
            truckEntity = truck,
            after = after,
            before = before,
            first = first,
            max = max
        )
    }

    /**
     * Deletes truck odometer reading
     *
     * @param truckOdometerReading truck odometer reading
     */
    suspend fun deleteTruckOdometerReading(truckOdometerReading: TruckOdometerReadingEntity) {
        truckOdometerReadingRepository.deleteSuspending(truckOdometerReading)
    }
}
package fi.metatavu.vp.vehiclemanagement.trucks.truckspeed

import fi.metatavu.vp.vehiclemanagement.model.TruckSpeed
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.*
import kotlin.math.abs

/**
 * Controller for truck speeds
 */
@ApplicationScoped
class TruckSpeedController {

    @Inject
    lateinit var truckSpeedRepository: TruckSpeedRepository

    /**
     * Creates truck speed
     *
     * @param truckEntity truck
     * @param truckSpeed truck speed
     * @return created truck speed
     */
    suspend fun createTruckSpeed(truckEntity: TruckEntity, truckSpeed: TruckSpeed): TruckSpeedEntity? {
        val existingRecord = truckSpeedRepository.find(
            "truck = :truck and timestamp = :timestamp",
            Parameters.with("truck", truckEntity).and("timestamp", truckSpeed.timestamp)
        ).firstResult<TruckSpeedEntity>().awaitSuspending()
        if (existingRecord != null) {
            return existingRecord
        }

        val latestRecord = truckSpeedRepository.find(
            "truck = :truck and timestamp <= :timestamp order by timestamp desc limit 1",
            Parameters.with("truck", truckEntity).and("timestamp", truckSpeed.timestamp)
        ).firstResult<TruckSpeedEntity>().awaitSuspending()
        if (latestRecord != null &&
            latestRecord.timestamp!! < truckSpeed.timestamp &&
            abs(latestRecord.speed!! - truckSpeed.speed) < 0.0001) {
            return null
        }

        return truckSpeedRepository.create(
            id = UUID.randomUUID(),
            timestamp = truckSpeed.timestamp,
            speed = truckSpeed.speed,
            truckEntity = truckEntity
        )
    }

    /**
     * Lists truck speeds
     *
     * @param truckEntity truck
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return pair of list of truck speeds and count
     */
    suspend fun listTruckSpeeds(
        truckEntity: TruckEntity,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<TruckSpeedEntity>, Long> {
        return truckSpeedRepository.listTruckSpeeds(
            truckEntity = truckEntity,
            after = after,
            before = before,
            first = first,
            max = max
        )
    }

    /**
     * Deletes truck speed
     *
     * @param speed truck speed
     */
    suspend fun deleteTruckSpeed(speed: TruckSpeedEntity) {
        return truckSpeedRepository.deleteSuspending(speed)
    }
}
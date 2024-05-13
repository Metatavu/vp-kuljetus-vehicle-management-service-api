package fi.metatavu.vp.vehiclemanagement.trucks.truckspeed

import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import fi.metatavu.vp.vehiclemanagement.trucks.location.TruckLocation
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
     * @param truck truck
     * @param truckSpeed truck speed
     * @return created truck speed
     */
    suspend fun createTruckSpeed(truck: Truck, truckSpeed: fi.metatavu.vp.api.model.TruckSpeed): TruckSpeed? {
        val latestRecord = truckSpeedRepository.find(
            "truck = :truck order by timestamp desc limit 1",
            Parameters.with("truck", truck)
        ).firstResult<TruckSpeed>().awaitSuspending()

        if (latestRecord?.timestamp != null && truckSpeed.timestamp <= latestRecord.timestamp!!) {
            return null
        }
        if (latestRecord != null && abs(latestRecord.speed!! - truckSpeed.speed) < 0.0001) {
            return null
        }

        return truckSpeedRepository.create(
            id = UUID.randomUUID(),
            timestamp = truckSpeed.timestamp,
            speed = truckSpeed.speed,
            truck = truck
        )
    }

    /**
     * Lists truck speeds
     *
     * @param truck truck
     * @param after after
     * @param before before
     * @param first first
     * @param max max
     * @return pair of list of truck speeds and count
     */
    suspend fun listTruckSpeeds(
        truck: Truck,
        after: OffsetDateTime? = null,
        before: OffsetDateTime? = null,
        first: Int? = null,
        max: Int? = null
    ): Pair<List<TruckSpeed>, Long> {
        return truckSpeedRepository.listTruckSpeeds(
            truck = truck,
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
    suspend fun deleteTruckSpeed(speed: TruckSpeed) {
        return truckSpeedRepository.deleteSuspending(speed)
    }
}

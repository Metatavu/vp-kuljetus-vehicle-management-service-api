package fi.metatavu.vp.vehiclemanagement.thermometers.temperatureReadings

import fi.metatavu.vp.messaging.GlobalEventController
import fi.metatavu.vp.messaging.events.TemperatureGlobalEvent
import fi.metatavu.vp.vehiclemanagement.model.TruckOrTowableTemperatureReading
import fi.metatavu.vp.vehiclemanagement.thermometers.ThermometerEntity
import fi.metatavu.vp.vehiclemanagement.towables.TowableEntity
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.hibernate.reactive.panache.Panache
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.util.*

/**
 * Controller for temperature readings management
 */
@ApplicationScoped
class TemperatureReadingController {

    @Inject
    lateinit var temperatureReadingRepository: TemperatureReadingRepository

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var globalEventController: GlobalEventController

    /**
     * Lists truck temperatures
     *
     * @param truck truck
     * @param includeArchived include archived
     * @param first first
     * @param max max
     * @return list of truck temperatures and the total count
     */
    suspend fun listTruckTemperatures(
        truck: TruckEntity,
        includeArchived: Boolean,
        first: Int?,
        max: Int?
    ): Pair<List<TemperatureReadingEntity>, Long> {
        return temperatureReadingRepository.list(
            truck = truck,
            towable = null,
            includeArchived = includeArchived,
            first = first,
            max = max
        )
    }

    /**
     * Lists towable temperatures
     *
     * @param towable towable
     * @param includeArchived include archived
     * @param first first
     * @param max max
     * @return list of towable temperatures and the total count
     */
    suspend fun listTowableTemperatures(
        towable: TowableEntity,
        includeArchived: Boolean,
        first: Int?,
        max: Int?
    ): Pair<List<TemperatureReadingEntity>, Long> {
        return temperatureReadingRepository.list(
            truck = null,
            towable = towable,
            includeArchived = includeArchived,
            first = first,
            max = max
        )
    }

    /**
     * Lists thermometer temperatures
     *
     * @param thermometer thermometer
     * @return list of thermometer temperatures and the total count
     */
    suspend fun listByThermometer(
        thermometer: ThermometerEntity
    ): List<TemperatureReadingEntity> {
        return temperatureReadingRepository.list(
            "thermometer = :thermometer",
            Parameters.with("thermometer", thermometer)
        ).awaitSuspending()
    }

    /**
     * Creates temperature reading record
     *
     * @param thermometer thermometer
     * @param temperatureReading temperature reading request body
     * @return created record
     */
    suspend fun create(
        thermometer: ThermometerEntity,
        temperatureReading: TruckOrTowableTemperatureReading
    ): TemperatureReadingEntity? {
        val existingRecord = temperatureReadingRepository.find(
            "thermometer = :thermometer and timestamp = :timestamp",
            Parameters.with("thermometer", thermometer).and("timestamp", temperatureReading.timestamp)
        ).firstResult<TemperatureReadingEntity>().awaitSuspending()
        if (existingRecord != null) {
            logger.info("Truck temperature reading $temperatureReading already exists for thermometer with id ${thermometer.id}")
            Panache.currentTransaction().awaitSuspending().markForRollback()
            return null
        }

        globalEventController.publish(
            TemperatureGlobalEvent(
                sensorId = temperatureReading.hardwareSensorId,
                temperature = temperatureReading.value
            )
        )

        return temperatureReadingRepository.create(
            id = UUID.randomUUID(),
            thermometer = thermometer,
            value = temperatureReading.value,
            timestamp = temperatureReading.timestamp
        )
    }

    /**
     * Deletes temperature reading (not used in production)
     *
     * @param temperatureReading temperature reading
     */
    suspend fun deleteTemperatureReading(temperatureReading: TemperatureReadingEntity) {
        temperatureReadingRepository.deleteSuspending(temperatureReading)
    }
}
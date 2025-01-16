package fi.metatavu.vp.vehiclemanagement.thermometers.temperatureReadings

import fi.metatavu.vp.vehiclemanagement.model.TemperatureReadingSourceType
import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.thermometers.ThermometerEntity
import fi.metatavu.vp.vehiclemanagement.towables.TowableEntity
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import jakarta.enterprise.context.ApplicationScoped
import java.time.OffsetDateTime
import java.util.UUID

/**
 * Repository for temperature readings
 */
@ApplicationScoped
class TemperatureReadingRepository: AbstractRepository<TemperatureReadingEntity, UUID>() {

    /**
     * Creates new temperature reading
     *
     * @param id id
     * @param thermometer thermometer
     * @param value value
     * @param timestamp timestamp
     * @return created temperature reading
     */
    suspend fun create(
        id: UUID,
        thermometer: ThermometerEntity,
        value: Float,
        timestamp: Long
    ): TemperatureReadingEntity {
        val temperatureReading = TemperatureReadingEntity()
        temperatureReading.id = id
        temperatureReading.thermometer = thermometer
        temperatureReading.value = value
        temperatureReading.timestamp = timestamp
        return persistSuspending(temperatureReading)
    }

    /**
     * Lists temperature readings
     *
     * @param truck truck
     * @param towable towable
     * @param includeArchived include archived
     * @param first first
     * @param max max
     * @return list of temperature readings and the total count
     */
    suspend fun list(truck: TruckEntity?, towable: TowableEntity?, includeArchived: Boolean, first: Int?, max: Int?): Pair<List<TemperatureReadingEntity>, Long> {
        val queryBuilder = StringBuilder()
        val parameters = Parameters()

        if (truck != null) {
            queryBuilder.append("thermometer.truck = :truck")
            parameters.and("truck", truck)
        }

        if (towable != null) {
            addCondition(queryBuilder, "thermometer.towable = :towable")
            parameters.and("towable", towable)
        }

        if (!includeArchived) {
            addCondition(queryBuilder, "thermometer.archivedAt is null")
        }

        queryBuilder.append(" ORDER BY timestamp DESC")
        return applyFirstMaxToQuery(
            query = find(queryBuilder.toString(), parameters),
            firstIndex = first,
            maxResults = max
        )
    }
}
package fi.metatavu.vp.vehiclemanagement.thermometers

import fi.metatavu.vp.vehiclemanagement.model.UpdateThermometerRequest
import fi.metatavu.vp.vehiclemanagement.thermometers.temperatureReadings.TemperatureReadingController
import fi.metatavu.vp.vehiclemanagement.towables.TowableEntity
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import io.quarkus.panache.common.Parameters
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.*

/**
 * Thermometer controller
 */
@ApplicationScoped
class ThermometerController {

    @Inject
    lateinit var thermometerRepository: ThermometerRepository

    @Inject
    lateinit var temperatureReadingController: TemperatureReadingController

    /**
     * Creates thermometer
     *
     * @param macAddress mac address
     * @param truck truck
     * @param towable towable
     * @return created thermometer
     */
    suspend fun create(
        macAddress: String,
        truck: TruckEntity?,
        towable: TowableEntity?,
    ): ThermometerEntity {
        return thermometerRepository.create(
            id = UUID.randomUUID(),
            macAddress = macAddress,
            truck = truck,
            towable = towable
        )
    }

    /**
     * Finds thermometer or creates a new one if needed.
     * TargetTruck or targetTowable must be present.
     *
     * @param macAddress thermometer mac address
     * @param deviceIdentifier measurement device identifier (imei for trucks and towables)
     * @param targetTruck truck the deviceIdentifier belongs to
     * @param targetTowable towable the device identifier belongs to
     * @throws IllegalArgumentException if targetTruck and targetTowable are both null
     */
    suspend fun findOrCreate(
        macAddress: String,
        deviceIdentifier: String,
        targetTruck: TruckEntity?,
        targetTowable: TowableEntity?
    ): ThermometerEntity {
        require(targetTruck != null || targetTowable != null) { "TargetTruck or targetTowable must be present" }
        // Find the current thermometer associated with the targetTruck or targetTowable
        val currentThermometer = targetTruck?.let { thermometerRepository.findByTruck(it) }
            ?: targetTowable?.let { thermometerRepository.findByTowable(it) }

        // Archive the current thermometer if it exists and has a different macAddress, it it is same, use it
        if (currentThermometer != null && currentThermometer.macAddress != macAddress) {
            archiveThermometer(currentThermometer)
        } else if (currentThermometer != null) {
            return currentThermometer
        }

        // Find any unarchived thermometer by the provided macAddress
        val unarchivedThermometersByMac = thermometerRepository.listUnarchivedByMac(macAddress)
        require(unarchivedThermometersByMac.size <= 1) { "Multiple active thermometers with the same mac address should not happen" }
        val thermometerByMac = unarchivedThermometersByMac.firstOrNull()

        // If an unarchived thermometer is associated with the same deviceIdentifier, return it
        if (thermometerByMac != null &&
            (thermometerByMac.truck?.imei == deviceIdentifier || thermometerByMac.towable?.imei == deviceIdentifier)) {
            return thermometerByMac
        }

        thermometerByMac?.let { archiveThermometer(it) }

        return thermometerRepository.create(
            id = UUID.randomUUID(),
            macAddress = macAddress,
            truck = targetTruck,
            towable = targetTowable
        )
    }

    /**
     * Archives thermometer
     *
     * @param thermometer thermometer to archive
     */
    suspend fun archiveThermometer(thermometer: ThermometerEntity) {
        if (thermometer.archivedAt == null) {
            thermometer.archivedAt = OffsetDateTime.now()
            thermometerRepository.persistSuspending(thermometer)
        }
    }

    /**
     * Lists thermometers
     *
     * @param entityId entity id
     * @param entityType entity type
     * @param includeArchived include archived
     * @param first first
     * @param max max
     * @return pair of list of thermometers and count
     */
    suspend fun listThermometers(
        entityId: UUID?,
        entityType: String?,
        includeArchived: Boolean,
        first: Int?,
        max: Int?
    ): Pair<List<ThermometerEntity>, Long> {
        return when (entityType) {
            "truck" -> thermometerRepository.listThermometers(
                truckId = entityId!!,
                includeArchived = includeArchived,
                first = first,
                max = max
            )
            "towable" -> thermometerRepository.listThermometers(
                towableId = entityId!!,
                includeArchived = includeArchived,
                first = first,
                max = max
            )
            else -> thermometerRepository.listThermometers(
                includeArchived = includeArchived,
                first = first,
                max = max
            )
        }
    }

    /**
     * Lists thermometers by towable
     *
     * @param towable towable
     * @return list of thermometers that contains the towable
     */
    suspend fun listByTowable(towable: TowableEntity): List<ThermometerEntity> {
        return thermometerRepository.list(
            "towable = :towable",
            Parameters.with("towable", towable)
        ).awaitSuspending()
    }

    /**
     * Lists thermometers by truck
     *
     * @param truck truck
     * @return list of thermometers that contains the truck
     */
    suspend fun listByTruck(truck: TruckEntity): List<ThermometerEntity> {
        return thermometerRepository.list(
            "truck = :truck",
            Parameters.with("truck", truck)
        ).awaitSuspending()
    }

    /**
     * Finds thermometer by id
     *
     * @param thermometerId thermometer id
     * @return found thermometer or null if not found
     */
    suspend fun findThermometer(thermometerId: UUID): ThermometerEntity? {
        return thermometerRepository.findByIdSuspending(thermometerId)
    }

    /**
     * Updates thermometer
     *
     * @param found found thermometer
     * @param updateThermometerRequest update thermometer request
     * @param userId user id
     * @return updated thermometer
     */
    suspend fun update(
        found: ThermometerEntity,
        updateThermometerRequest: UpdateThermometerRequest,
        userId: UUID
    ): ThermometerEntity {
        found.name = updateThermometerRequest.name
        return thermometerRepository.persistSuspending(found)
    }

    /**
     * Deletes a thermometer (not used in production) and connected readings
     *
     * @param thermometer thermometer to delete
     */
    suspend fun deleteThermometer(thermometer: ThermometerEntity) {
        temperatureReadingController.listByThermometer(thermometer).forEach {
            temperatureReadingController.deleteTemperatureReading(it)
        }
        thermometerRepository.deleteSuspending(thermometer)
    }
}
package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.model.SortOrder
import fi.metatavu.vp.vehiclemanagement.model.Truck
import fi.metatavu.vp.vehiclemanagement.model.TruckSortByField
import fi.metatavu.vp.vehiclemanagement.thermometers.ThermometerController
import fi.metatavu.vp.vehiclemanagement.thermometers.ThermometerRepository
import fi.metatavu.vp.vehiclemanagement.trucks.drivercards.DriverCardController
import fi.metatavu.vp.vehiclemanagement.trucks.drivestate.TruckDriveStateController
import fi.metatavu.vp.vehiclemanagement.trucks.location.TruckLocationController
import fi.metatavu.vp.vehiclemanagement.trucks.odometerreading.TruckOdometerReadingController
import fi.metatavu.vp.vehiclemanagement.trucks.truckspeed.TruckSpeedController
import fi.metatavu.vp.vehiclemanagement.vehicles.VehicleController
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.OffsetDateTime
import java.util.*

/**
 * Controller for trucks
 */
@ApplicationScoped
class TruckController {

    @Inject
    lateinit var truckRepository: TruckRepository

    @Inject
    lateinit var vehicleController: VehicleController

    @Inject
    lateinit var driverCardController: DriverCardController

    @Inject
    lateinit var truckSpeedController: TruckSpeedController

    @Inject
    lateinit var truckLocationController: TruckLocationController

    @Inject
    lateinit var truckDriveStateController: TruckDriveStateController

    @Inject
    lateinit var truckOdometerReadingController: TruckOdometerReadingController

    @Inject
    lateinit var thermometerController: ThermometerController

    /**
     * Creates new truck and a vehicle that is attached to it.
     *
     * @param plateNumber plate number
     * @param type truck type
     * @param vin vin
     * @param name name
     * @param imei imei
     * @param costCenter cost center
     * @param userId user id
     * @return created truck
     */
    suspend fun createTruck(
        plateNumber: String,
        type: Truck.Type,
        vin: String,
        name: String?,
        imei: String?,
        costCenter: String?,
        userId: UUID
    ): TruckEntity {
        val truck = truckRepository.create(
            id = UUID.randomUUID(),
            plateNumber = plateNumber,
            type = type,
            vin = vin,
            name = name,
            imei = imei,
            creatorId = userId,
            costCenter = costCenter,
            lastModifierId = userId
        )
        vehicleController.create(truck, emptyList(), userId)
        return truck
    }

    /**
     * Finds truck by id
     *
     * @param truckId truck id
     * @return found truck or null if not found
     */
    suspend fun findTruck(truckId: UUID): TruckEntity? {
        return truckRepository.findByIdSuspending(truckId)
    }

    /**
     * Finds truck by vin
     *
     * @param vin vin
     * @return found truck or null if not found
     */
    suspend fun findTruck(vin: String): TruckEntity? {
        return truckRepository.findByVin(vin)
    }

    /**
     * Finds truck by imei
     *
     * @param imei imei
     * @return found truck or null if not found
     */
    suspend fun findTruckByImei(imei: String): TruckEntity? {
        return truckRepository.findByImei(imei)
    }

    /**
     * Lists trucks
     *
     * @param plateNumber plate number
     * @param archived archived
     * @param vin vin
     * @param sortBy sort by field
     * @param sortDirection sort direction
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trucks
     */
    suspend fun listTrucks(
        plateNumber: String? = null,
        archived: Boolean? = null,
        vin: String? = null,
        sortBy: TruckSortByField? = null,
        sortDirection: SortOrder? = null,
        firstResult: Int? = null,
        maxResults: Int? = null,
    ): Pair<List<TruckEntity>, Long> {
        return truckRepository.list(
            plateNumber = plateNumber,
            archived = archived,
            vin = vin,
            sortBy = sortBy,
            sortDirection = sortDirection,
            firstResult = firstResult,
            maxResults = maxResults
        )
    }

    /**
     * Updates truck
     *
     * @param existingTruckEntity existing truck
     * @param newTruckData new truck data
     * @param userId user id
     * @return updated trailer
     */
    suspend fun updateTruck(existingTruckEntity: TruckEntity, newTruckData: Truck, userId: UUID): TruckEntity {
        existingTruckEntity.plateNumber = newTruckData.plateNumber
        existingTruckEntity.archivedAt = newTruckData.archivedAt
        existingTruckEntity.type = newTruckData.type
        existingTruckEntity.vin = newTruckData.vin
        existingTruckEntity.name = newTruckData.name
        existingTruckEntity.imei = newTruckData.imei
        existingTruckEntity.costCenter = newTruckData.costCenter
        existingTruckEntity.lastModifierId = userId
        return truckRepository.persistSuspending(existingTruckEntity)
    }

    /**
     * Deletes truck (and all related entities)
     * Not available in production.
     *
     * @param truckEntity truck to be deleted
     */
    suspend fun deleteTruck(truckEntity: TruckEntity) {
        driverCardController.listDriverCards(truckEntity).first.forEach {
            driverCardController.deleteDriverCard(it, OffsetDateTime.now())
        }
        truckSpeedController.listTruckSpeeds(truckEntity).first.forEach {
            truckSpeedController.deleteTruckSpeed(it)
        }
        truckLocationController.listTruckLocations(truckEntity).first.forEach {
            truckLocationController.deleteTruckLocation(it)
        }
        truckDriveStateController.listDriveStates(truckEntity = truckEntity).first.forEach {
            truckDriveStateController.deleteDriveState(it)
        }
        truckOdometerReadingController.list(truck = truckEntity).first.forEach {
            truckOdometerReadingController.deleteTruckOdometerReading(it)
        }
        thermometerController.listByTruck(truckEntity).forEach {
            thermometerController.deleteThermometer(it)
        }
        truckRepository.deleteSuspending(truckEntity)
    }

}
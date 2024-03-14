package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.towables.Towable
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository class for TowableToVehicle
 */
@ApplicationScoped
class VehicleTowableRepository: AbstractRepository<VehicleTowable, UUID>() {

    /**
     * Creates a new VehicleTowable
     *
     * @param id id
     * @param vehicle vehicle
     * @param towable towable
     * @param order order
     * @param userId user id
     * @return created TowableToVehicle
     */
    suspend fun create(
        id: UUID,
        vehicle: Vehicle,
        towable: Towable,
        order: Int,
        userId: UUID
    ): VehicleTowable {
        val towableVehicle = VehicleTowable()
        towableVehicle.id = UUID.randomUUID()
        towableVehicle.vehicle = vehicle
        towableVehicle.towable = towable
        towableVehicle.orderNumber = order
        return persistSuspending(towableVehicle)
    }

    /**
     * Lists TowableToVehicles by vehicle
     *
     * @param vehicle vehicle
     * @return list of TowableToVehicles
     */
    suspend fun listByVehicle(vehicle: Vehicle): List<VehicleTowable> {
        val sb = StringBuilder()
        val parameters = Parameters()

        addCondition(sb, "vehicle = :vehicle")
        parameters.and("vehicle", vehicle)
        return find(sb.toString(), Sort.ascending("orderNumber"), parameters).list<VehicleTowable>().awaitSuspending()
    }

    /**
     * Lists TowableToVehicles by towable
     *
     * @param towable towable
     * @return list of TowableToVehicles
     */
    suspend fun listByTowable(towable: Towable): List<VehicleTowable> {
        val sb = StringBuilder()
        val parameters = Parameters()

            addCondition(sb, "towable = :towable")
            parameters.and("towable", towable)


        return find(sb.toString(), parameters).list<VehicleTowable>().awaitSuspending()
    }

}
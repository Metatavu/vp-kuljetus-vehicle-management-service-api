package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.persistence.AbstractRepository
import fi.metatavu.vp.vehiclemanagement.towables.TowableEntity
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository class for TowableToVehicle
 */
@ApplicationScoped
class VehicleTowableRepository: AbstractRepository<VehicleTowableEntity, UUID>() {

    /**
     * Creates a new VehicleTowable
     *
     * @param id id
     * @param vehicleEntity vehicle
     * @param towableEntity towable
     * @param order order
     * @param userId user id
     * @return created TowableToVehicle
     */
    suspend fun create(
        id: UUID,
        vehicleEntity: VehicleEntity,
        towableEntity: TowableEntity,
        order: Int,
        userId: UUID
    ): VehicleTowableEntity {
        val towableVehicle = VehicleTowableEntity()
        towableVehicle.id = UUID.randomUUID()
        towableVehicle.vehicle = vehicleEntity
        towableVehicle.towable = towableEntity
        towableVehicle.orderNumber = order
        return persistSuspending(towableVehicle)
    }

    /**
     * Lists TowableToVehicles by vehicle
     *
     * @param vehicleEntity vehicle
     * @return list of TowableToVehicles
     */
    suspend fun listByVehicle(vehicleEntity: VehicleEntity): List<VehicleTowableEntity> {
        val sb = StringBuilder()
        val parameters = Parameters()

        addCondition(sb, "vehicle = :vehicle")
        parameters.and("vehicle", vehicleEntity)
        return find(sb.toString(), Sort.ascending("orderNumber"), parameters).list<VehicleTowableEntity>().awaitSuspending()
    }

    /**
     * Lists TowableToVehicles by towable
     *
     * @param towableEntity towable
     * @return list of TowableToVehicles
     */
    suspend fun listByTowable(towableEntity: TowableEntity): List<VehicleTowableEntity> {
        val sb = StringBuilder()
        val parameters = Parameters()

            addCondition(sb, "towable = :towable")
            parameters.and("towable", towableEntity)


        return find(sb.toString(), parameters).list<VehicleTowableEntity>().awaitSuspending()
    }

}
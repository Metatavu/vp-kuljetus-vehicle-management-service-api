package fi.metatavu.vp.vehicleManagement.vehicles

import fi.metatavu.vp.vehicleManagement.persistence.AbstractRepository
import fi.metatavu.vp.vehicleManagement.towables.Towable
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository class for TrailerVehicle
 */
@ApplicationScoped
class TowableToVehicleRepository: AbstractRepository<TowableToVehicle, UUID>() {

    /**
     * Creates a new TrailerVehicle
     *
     * @param id id
     * @param vehicle vehicle
     * @param towable towable
     * @param order order
     * @param userId user id
     * @return created TrailerVehicle
     */
    suspend fun create(
        id: UUID,
        vehicle: Vehicle,
        towable: Towable,
        order: Int,
        userId: UUID
    ): TowableToVehicle {
        val towableVehicle = TowableToVehicle()
        towableVehicle.id = UUID.randomUUID()
        towableVehicle.vehicle = vehicle
        towableVehicle.towable = towable
        towableVehicle.orderNumber = order
        return persistSuspending(towableVehicle)
    }

    /**
     * Lists TrailerVehicles by vehicle
     *
     * @param vehicle vehicle
     * @return list of TrailerVehicles
     */
    suspend fun listByVehicle(vehicle: Vehicle): List<TowableToVehicle> {
        val sb = StringBuilder()
        val parameters = Parameters()

        addCondition(sb, "vehicle = :vehicle")
        parameters.and("vehicle", vehicle)
        return find(sb.toString(), Sort.ascending("orderNumber"), parameters).list<TowableToVehicle>().awaitSuspending()
    }

    /**
     * Lists TrailerVehicles by towable
     *
     * @param towable towable
     * @return list of TrailerVehicles
     */
    suspend fun listByTrailer(towable: Towable): List<TowableToVehicle> {
        val sb = StringBuilder()
        val parameters = Parameters()

            addCondition(sb, "towable = :towable")
            parameters.and("towable", towable)


        return find(sb.toString(), parameters).list<TowableToVehicle>().awaitSuspending()
    }

}
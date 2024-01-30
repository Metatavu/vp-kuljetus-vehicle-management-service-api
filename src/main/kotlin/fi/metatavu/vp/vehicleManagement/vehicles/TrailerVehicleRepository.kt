package fi.metatavu.vp.vehicleManagement.vehicles

import fi.metatavu.vp.vehicleManagement.persistence.AbstractRepository
import fi.metatavu.vp.vehicleManagement.trailers.Trailer
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

/**
 * Repository class for TrailerVehicle
 */
@ApplicationScoped
class TrailerVehicleRepository: AbstractRepository<TrailerVehicle, UUID>() {

    /**
     * Creates a new TrailerVehicle
     *
     * @param id id
     * @param vehicle vehicle
     * @param trailer trailer
     * @param order order
     * @param userId user id
     * @return created TrailerVehicle
     */
    suspend fun create(
        id: UUID,
        vehicle: Vehicle,
        trailer: Trailer,
        order: Int,
        userId: UUID
    ): TrailerVehicle {
        val trailerVehicle = TrailerVehicle()
        trailerVehicle.id = UUID.randomUUID()
        trailerVehicle.vehicle = vehicle
        trailerVehicle.trailer = trailer
        trailerVehicle.orderNumber = order
        return persistSuspending(trailerVehicle)
    }

    /**
     * Lists TrailerVehicles by vehicle
     *
     * @param vehicle vehicle
     * @return list of TrailerVehicles
     */
    suspend fun listByVehicle(vehicle: Vehicle): List<TrailerVehicle> {
        val sb = StringBuilder()
        val parameters = Parameters()

        addCondition(sb, "vehicle = :vehicle")
        parameters.and("vehicle", vehicle)
        return find(sb.toString(), Sort.ascending("orderNumber"), parameters).list<TrailerVehicle>().awaitSuspending()
    }

    /**
     * Lists TrailerVehicles by trailer
     *
     * @param trailer trailer
     * @return list of TrailerVehicles
     */
    suspend fun listByTrailer(trailer: Trailer): List<TrailerVehicle> {
        val sb = StringBuilder()
        val parameters = Parameters()

            addCondition(sb, "trailer = :trailer")
            parameters.and("trailer", trailer)


        return find(sb.toString(), parameters).list<TrailerVehicle>().awaitSuspending()
    }

}
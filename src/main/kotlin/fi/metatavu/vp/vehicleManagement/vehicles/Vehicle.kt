package fi.metatavu.vp.vehicleManagement.vehicles

import fi.metatavu.vp.vehicleManagement.persistence.Metadata
import fi.metatavu.vp.vehicleManagement.trucks.Truck
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.*

/**
 * Entity for vehicles
 * Vehicle contains 1 truck and 0-2 trailers (connection to trailers is done via TrailerVehicle entity)
 */
@Entity
class Vehicle: Metadata() {

    @Id
    var id: UUID? = null

    @ManyToOne
    lateinit var truck: Truck

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}
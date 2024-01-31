package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.persistence.Metadata
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.*

/**
 * Entity for vehicles
 * Vehicle contains 1 truck and 0-2 towables (connection to towables is done via TrailerVehicle entity)
 */
@Entity(name = "vehicle")
class Vehicle: Metadata() {

    @Id
    var id: UUID? = null

    @ManyToOne
    lateinit var truck: Truck

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}
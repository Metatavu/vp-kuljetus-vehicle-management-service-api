package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.persistence.Metadata
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.*

/**
 * Entity for vehicles
 * Vehicle contains 1 truck and N towables (connection to towables is done via VehicleTowable entity)
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
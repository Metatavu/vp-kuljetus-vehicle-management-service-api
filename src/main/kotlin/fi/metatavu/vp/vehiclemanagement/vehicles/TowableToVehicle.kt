package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.towables.Towable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.*

/**
 * Entity connecting a towable to a vehicle
 */
@Entity(name = "towabletovehicle")
class TowableToVehicle {

    @Id
    var id: UUID? = null

    @ManyToOne
    lateinit var vehicle: Vehicle

    @ManyToOne
    lateinit var towable: Towable

    @Column(nullable = false)
    var orderNumber: Int? = null
}
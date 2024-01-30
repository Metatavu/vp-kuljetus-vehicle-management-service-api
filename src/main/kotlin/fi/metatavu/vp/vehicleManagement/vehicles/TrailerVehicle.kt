package fi.metatavu.vp.vehicleManagement.vehicles

import fi.metatavu.vp.vehicleManagement.trailers.Trailer
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.*

/**
 * Entity connecting a trailer to a vehicle
 */
@Entity
class TrailerVehicle {

    @Id
    var id: UUID? = null

    @ManyToOne
    lateinit var vehicle: Vehicle

    @ManyToOne
    lateinit var trailer: Trailer

    @Column(nullable = false)
    var orderNumber: Int? = null
}
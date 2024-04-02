package fi.metatavu.vp.vehiclemanagement.trucks.location

import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Entity for driver cards
 */
@Entity
class TruckLocation {

    @Id
    var id: UUID? = null

    @Column(nullable = false)
    var timestamp: Long? = null

    @Column(nullable = false)
    var latitude: Double? = 0.0

    @Column(nullable = false)
    var longitude: Double? = 0.0

    @Column(nullable = false)
    var heading: Double? = 0.0

    @ManyToOne
    lateinit var truck: Truck

}

package fi.metatavu.vp.vehiclemanagement.trucks.odometerreading

import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import jakarta.persistence.*
import java.util.*

/**
 * Odometer reading for a truck
 */
@Entity
@Table(name = "truckodometerreading")
class TruckOdometerReadingEntity {

    @Id
    var id: UUID? = null

    @Column(nullable = false)
    var timestamp: Long? = null

    @Column(nullable = false)
    var odometerReading: Int? = 0

    @ManyToOne
    lateinit var truck: TruckEntity
}
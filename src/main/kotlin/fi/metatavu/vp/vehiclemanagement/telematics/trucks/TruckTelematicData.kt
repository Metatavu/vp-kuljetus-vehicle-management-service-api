package fi.metatavu.vp.vehiclemanagement.telematics.trucks

import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.*

/**
 * TruckTelematicData entity
 */
@Entity
class TruckTelematicData {

    @Id
    var id: UUID? = null

    @ManyToOne
    lateinit var truck: Truck

    @Column(nullable = false)
    var timestamp: Long = 0

    @Column(nullable = false)
    lateinit var imei: String

    @Column(nullable = false)
    var speed: Float = 0.0f

    @Column(nullable = false)
    var latitude: Double = 0.0

    @Column(nullable = false)
    var longitude: Double = 0.0

}
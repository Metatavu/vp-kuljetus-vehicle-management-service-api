package fi.metatavu.vp.vehiclemanagement.telematics

import fi.metatavu.vp.vehiclemanagement.towables.Towable
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.util.*

/**
 * Telematics entity
 */
@Entity(name = "telematics")
class Telematics {

    @Id
    var id: UUID? = null

    @ManyToOne
    var truck: Truck? = null

    @ManyToOne
    var towable: Towable? = null

    @Column(nullable = false)
    var timestamp: Long? = null

    @Column(nullable = false)
    lateinit var imei: String

    @Column(nullable = false)
    var speed: Float? = null

    @Column(nullable = false)
    var latitude: Double? = null

    @Column(nullable = false)
    var longitude: Double? = null

}
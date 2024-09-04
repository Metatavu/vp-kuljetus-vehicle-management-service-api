package fi.metatavu.vp.vehiclemanagement.trucks.truckspeed

import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

/**
 * Entity representing truck speed
 */
@Entity
@Table(name = "truckspeed")
class TruckSpeedEntity {

    @Id
    var id: UUID? = null

    @Column(nullable = false)
    var timestamp: Long? = null

    @Column(nullable = false)
    var speed: Float? = 0.0f

    @ManyToOne
    lateinit var truck: TruckEntity
}
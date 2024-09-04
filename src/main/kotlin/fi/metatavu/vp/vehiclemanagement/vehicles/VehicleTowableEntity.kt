package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.towables.TowableEntity
import jakarta.persistence.*
import java.util.*

/**
 * Entity connecting a towable to a vehicle
 */
@Entity
@Table(name = "vehicletowable")
class VehicleTowableEntity {

    @Id
    var id: UUID? = null

    @OneToOne
    lateinit var vehicle: VehicleEntity

    @ManyToOne
    lateinit var towable: TowableEntity

    @Column(nullable = false)
    var orderNumber: Int = 0
}
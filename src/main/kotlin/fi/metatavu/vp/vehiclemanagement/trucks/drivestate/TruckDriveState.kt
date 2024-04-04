package fi.metatavu.vp.vehiclemanagement.trucks.drivestate

import fi.metatavu.vp.api.model.TruckDriveStateEnum
import fi.metatavu.vp.vehiclemanagement.trucks.Truck
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Entity class for TruckDriveState
 */
@Entity
class TruckDriveState {

    @Id
    lateinit var id: UUID

    @Enumerated(EnumType.STRING)
    lateinit var state: TruckDriveStateEnum

    @Column(nullable = false)
    var timestamp: Long? = null

    @Column
    var driverCardId: String? = null

    @Column
    var driverId: UUID? = null

    @ManyToOne
    lateinit var truck: Truck
}
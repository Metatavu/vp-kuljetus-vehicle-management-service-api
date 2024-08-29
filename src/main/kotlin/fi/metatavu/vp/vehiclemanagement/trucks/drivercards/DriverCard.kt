package fi.metatavu.vp.vehiclemanagement.trucks.drivercards

import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
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
class DriverCard {

    @Id
    lateinit var id: UUID

    @Column(nullable = false, unique = true)
    @NotEmpty
    lateinit var driverCardId: String

    @ManyToOne
    lateinit var truck: TruckEntity

}

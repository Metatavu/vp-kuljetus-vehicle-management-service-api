package fi.metatavu.vp.vehiclemanagement.drivercards

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Entity for driver cards
 */
@Entity
class DriverCard {

    @Id
    var id: UUID? = null

    @Column(nullable = false, unique = true)
    @NotEmpty
    lateinit var driverCardId: String

    @Column(nullable = false, unique = true)
    @NotEmpty
    lateinit var truckVin: String

}

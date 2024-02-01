package fi.metatavu.vp.vehiclemanagement.towables

import fi.metatavu.vp.vehiclemanagement.persistence.Metadata
import fi.metatavu.vp.vehiclemanagement.telematics.TelematicsDevice
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Entity for towables
 */
@Entity(name = "towable")
class Towable : Metadata(), TelematicsDevice {

    @Id
    var id: UUID? = null

    @Column(nullable = false, unique = true)
    @NotEmpty
    lateinit var plateNumber: String

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var type: fi.metatavu.vp.api.model.Towable.Type

    @Column
    var vin: String? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID

}
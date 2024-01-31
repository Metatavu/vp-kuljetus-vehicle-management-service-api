package fi.metatavu.vp.vehicleManagement.towables

import fi.metatavu.vp.vehicleManagement.persistence.Metadata
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Entity for towables
 */
@Entity
class Towable : Metadata() {

    @Id
    var id: UUID? = null

    @Column(nullable = false)
    @NotEmpty
    lateinit var plateNumber: String

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var type: fi.metatavu.vp.api.model.Towable.Type

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID

}
package fi.metatavu.vp.vehicleManagement.trailers

import fi.metatavu.vp.vehicleManagement.persistence.Metadata
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Entity for trailers
 */
@Entity
class Trailer: Metadata() {

    @Id
    var id: UUID? = null

    @Column(nullable = false, unique = true)
    @NotEmpty
    lateinit var plateNumber: String

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID

}
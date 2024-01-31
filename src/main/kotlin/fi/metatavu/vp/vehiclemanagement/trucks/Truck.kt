package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.persistence.Metadata
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import java.util.*

/**
 * Entity representing a truck
 */
@Entity(name = "truck")
class Truck: Metadata() {

    @Id
    var id: UUID? = null

    @Column(nullable = false)
    @NotEmpty
    lateinit var plateNumber: String

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var type: fi.metatavu.vp.api.model.Truck.Type

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID

}
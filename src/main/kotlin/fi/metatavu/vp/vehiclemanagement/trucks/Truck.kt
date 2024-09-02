package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.persistence.Metadata
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import java.time.OffsetDateTime
import java.util.*

/**
 * Entity representing a truck
 */
@Entity
class Truck: Metadata() {

    @Id
    var id: UUID? = null

    @Column(nullable = false, unique = true)
    @NotEmpty
    lateinit var plateNumber: String

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var type: fi.metatavu.vp.vehiclemanagement.model.Truck.Type

    @Column(unique = true, nullable = false)
    @NotEmpty
    lateinit var vin: String

    @Column
    var name: String? = null

    @Column
    var archivedAt: OffsetDateTime? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID

}
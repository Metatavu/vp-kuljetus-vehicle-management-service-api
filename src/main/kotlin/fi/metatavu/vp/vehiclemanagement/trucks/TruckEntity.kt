package fi.metatavu.vp.vehiclemanagement.trucks

import fi.metatavu.vp.vehiclemanagement.model.Truck
import fi.metatavu.vp.vehiclemanagement.persistence.Metadata
import fi.metatavu.vp.vehiclemanagement.persistence.TrackableEntity
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import java.time.OffsetDateTime
import java.util.*

/**
 * Entity representing a truck
 */
@Entity
@Table(name = "truck")
class TruckEntity: TrackableEntity() {

    @Column(nullable = false, unique = true)
    @NotEmpty
    lateinit var plateNumber: String

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var type: Truck.Type

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
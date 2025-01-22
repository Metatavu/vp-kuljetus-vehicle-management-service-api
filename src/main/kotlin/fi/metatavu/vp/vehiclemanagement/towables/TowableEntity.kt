package fi.metatavu.vp.vehiclemanagement.towables

import fi.metatavu.vp.vehiclemanagement.model.Towable
import fi.metatavu.vp.vehiclemanagement.persistence.Metadata
import fi.metatavu.vp.vehiclemanagement.persistence.ITrackable
import jakarta.persistence.*
import jakarta.validation.constraints.NotEmpty
import java.time.OffsetDateTime
import java.util.*

/**
 * Entity for towables
 */
@Entity
@Table(name = "towable")
class TowableEntity : Metadata(), ITrackable {

    @Id
    override var id: UUID? = null

    @Column(unique = true)
    override var imei: String? = null

    @Column(nullable = false, unique = true)
    @NotEmpty
    lateinit var plateNumber: String

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var type: Towable.Type

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
package fi.metatavu.vp.vehiclemanagement.vehicles

import fi.metatavu.vp.vehiclemanagement.persistence.Metadata
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

/**
 * Entity for vehicles
 * Vehicle contains 1 truck and N towables (connection to towables is done via VehicleTowable entity)
 */
@Entity
@Table(name = "vehicle")
class VehicleEntity: Metadata() {

    @Id
    var id: UUID? = null

    @ManyToOne
    lateinit var truck: TruckEntity

    @Column
    var archivedAt: OffsetDateTime? = null

    override lateinit var creatorId: UUID

    override lateinit var lastModifierId: UUID
}
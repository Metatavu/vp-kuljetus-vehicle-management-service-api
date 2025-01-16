package fi.metatavu.vp.vehiclemanagement.thermometers

import fi.metatavu.vp.vehiclemanagement.towables.TowableEntity
import fi.metatavu.vp.vehiclemanagement.trucks.TruckEntity
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

/**
 * Entity for thermometers
 */
@Entity
@Table(name = "thermometer")
class ThermometerEntity {

    @Id
    lateinit var id: UUID

    @Column(nullable = false)
    lateinit var hardwareSensorId: String

    @Column
    var name: String? = null

    @ManyToOne
    var truck: TruckEntity? = null

    @ManyToOne
    var towable: TowableEntity? = null

    @Column
    var archivedAt: OffsetDateTime? = null

    @Column(nullable = false)
    var createdAt: OffsetDateTime? = null

    @Column(nullable = false)
    var modifiedAt: OffsetDateTime? = null

    /**
     * JPA pre-persist event handler
     */
    @PrePersist
    fun onCreate() {
        val odtNow = OffsetDateTime.now()
        createdAt = odtNow
        modifiedAt = odtNow
    }

    /**
     * JPA pre-update event handler
     */
    @PreUpdate
    fun onUpdate() {
        modifiedAt = OffsetDateTime.now()
    }

}
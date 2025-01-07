package fi.metatavu.vp.vehiclemanagement.thermometers.temperatureReadings

import fi.metatavu.vp.vehiclemanagement.thermometers.ThermometerEntity
import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

/**
 * Temperature reading record
 */
@Entity
@Table(name = "termperaturereading")
class TemperatureReadingEntity {

    @Id
    lateinit var id: UUID

    @ManyToOne
    lateinit var thermometer: ThermometerEntity

    @Column(nullable = false)
    var value: Float? = 0.0F

    @Column(nullable = false)
    lateinit var timestamp: OffsetDateTime
}
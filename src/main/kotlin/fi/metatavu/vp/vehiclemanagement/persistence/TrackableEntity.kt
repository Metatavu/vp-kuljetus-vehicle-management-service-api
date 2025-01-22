package fi.metatavu.vp.vehiclemanagement.persistence

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.util.UUID

@MappedSuperclass
abstract class TrackableEntity: Metadata() {

    @Id
    var id: UUID? = null

    @Column(unique = true)
    var imei: String? = null
}
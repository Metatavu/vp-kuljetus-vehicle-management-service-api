package fi.metatavu.vp.vehiclemanagement.persistence

import java.util.UUID

/**
 * Interface describing a Trackable e.g. truck or towable
 */
interface ITrackable {
    var id: UUID?
    var imei: String?
}
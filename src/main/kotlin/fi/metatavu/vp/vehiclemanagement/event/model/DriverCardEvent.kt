package fi.metatavu.vp.vehiclemanagement.event.model

import fi.metatavu.vp.usermanagement.model.Driver
import fi.metatavu.vp.vehiclemanagement.trucks.drivercards.DriverCard

data class DriverCardEvent(val driverCard: DriverCard, val removed: Boolean, val driver: Driver?)
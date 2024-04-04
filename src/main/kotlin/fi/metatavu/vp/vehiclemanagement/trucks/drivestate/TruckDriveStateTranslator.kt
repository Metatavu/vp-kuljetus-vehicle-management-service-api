package fi.metatavu.vp.vehiclemanagement.trucks.drivestate

import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for TruckDriveState
 */
@ApplicationScoped
class TruckDriveStateTranslator: AbstractTranslator<TruckDriveState, fi.metatavu.vp.api.model.TruckDriveState>(){
    override suspend fun translate(entity: TruckDriveState): fi.metatavu.vp.api.model.TruckDriveState {
        return fi.metatavu.vp.api.model.TruckDriveState(
            id = entity.id,
            state = entity.state,
            timestamp = entity.timestamp!!,
            driverCardId = entity.driverCardId,
            driverId = entity.driverId
        )
    }
}
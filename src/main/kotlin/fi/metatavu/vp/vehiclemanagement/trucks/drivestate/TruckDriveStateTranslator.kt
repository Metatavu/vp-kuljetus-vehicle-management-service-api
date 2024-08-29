package fi.metatavu.vp.vehiclemanagement.trucks.drivestate

import fi.metatavu.vp.vehiclemanagement.model.TruckDriveState
import fi.metatavu.vp.vehiclemanagement.rest.AbstractTranslator
import jakarta.enterprise.context.ApplicationScoped

/**
 * Translator for TruckDriveState
 */
@ApplicationScoped
class TruckDriveStateTranslator: AbstractTranslator<TruckDriveStateEntity, TruckDriveState>(){
    override suspend fun translate(entity: TruckDriveStateEntity): TruckDriveState {
        return TruckDriveState(
            id = entity.id,
            state = entity.state,
            timestamp = entity.timestamp!!,
            driverCardId = entity.driverCardId,
            driverId = entity.driverId
        )
    }
}
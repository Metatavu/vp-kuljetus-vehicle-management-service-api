package fi.metatavu.vp.vehicleManagement.vehicles

import jakarta.enterprise.context.ApplicationScoped

/**
 * Controller for vehicle related operations
 */
@ApplicationScoped
class VehicleController {

    /**
     * Checks if the given plate number is valid
     *
     * @param plateNumber plate number
     * @return true if the plate number is valid
     */
    fun isPlateNumberValid(plateNumber: String): Boolean {
        val lengthCheck = plateNumber.isNotEmpty()
            && plateNumber.isNotBlank()
            && plateNumber.length >= 2
        if (!lengthCheck) {
            return false
        }
        return !(plateNumber.contains("?") || plateNumber.contains("*") || plateNumber.contains("!"))
    }
}
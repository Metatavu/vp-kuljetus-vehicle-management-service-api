package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.invalid.providers.SimpleInvalidValueProvider
import fi.metatavu.vp.test.client.models.Towable
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.test.client.models.Vehicle
import java.util.*

class InvalidTestValues: InvalidValues() {

    /**
     * Invalid values for testing Towables API
     */
    class Towables {
        companion object {
            val INVALID_TRAILERS = listOf(
                Towable(plateNumber = "3", vin = "001", type = Towable.Type.TRAILER),
                Towable(plateNumber = "", vin = "001", type = Towable.Type.TRAILER),
                Towable(plateNumber = "hello*", vin = "001", type = Towable.Type.SEMI_TRAILER),
                Towable(plateNumber = "platenumber", vin = "", type = Towable.Type.SEMI_TRAILER)
            ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }
        }
    }

    /**
     * Invalid values for testing Trucks API
     */
    class Trucks {
        companion object {
            val INVALID_TRUCKS = listOf(
                Truck(plateNumber = "3", vin = "001", type = Truck.Type.TRUCK),
                Truck(plateNumber = "", vin = "001", type = Truck.Type.TRUCK),
                Truck(plateNumber = "hello*", vin = "001", type = Truck.Type.TRUCK),
                Truck(plateNumber = "platenumber", vin = "", type = Truck.Type.TRUCK)
            ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }
        }
    }

    /**
     * Invalid values methods for Vehicles
     */
    class Vehicles {
        companion object {
            fun createVehicle(towableIds: Array<UUID>, truckId: UUID): SimpleInvalidValueProvider {
                return SimpleInvalidValueProvider(
                    jacksonObjectMapper().writeValueAsString(
                        Vehicle(
                            towableIds = towableIds,
                            truckId = truckId
                        )
                    )
                )
            }
        }
    }
}
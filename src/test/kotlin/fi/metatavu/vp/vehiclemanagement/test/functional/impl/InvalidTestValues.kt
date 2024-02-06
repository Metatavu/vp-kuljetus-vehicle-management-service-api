package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.invalid.providers.SimpleInvalidValueProvider
import fi.metatavu.vp.test.client.models.Towable
import fi.metatavu.vp.test.client.models.Vehicle
import java.util.*

class InvalidTestValues: InvalidValues() {

    /**
     * Invalid values for testing Towables API
     */
    class Towables {
        companion object {
            val INVALID_TRAILERS = listOf(
                Towable(plateNumber = "3", Towable.Type.TRAILER), Towable(plateNumber = "", Towable.Type.TRAILER), Towable(plateNumber = "hello*", Towable.Type.SEMI_TRAILER)
            ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }
        }
    }

    /**
     * Invalid values for testing Trucks API (same because they both have the same plateNumber field)
     */
    class Trucks {
        companion object {
            val INVALID_TRUCKS = Towables.INVALID_TRAILERS
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
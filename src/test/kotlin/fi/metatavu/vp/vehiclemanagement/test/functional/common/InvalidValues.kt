package fi.metatavu.vp.vehiclemanagement.test.functional.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.metaform.server.test.functional.common.providers.SimpleInvalidValueProvider
import fi.metatavu.vp.test.client.models.Towable
import fi.metatavu.vp.test.client.models.Vehicle
import fi.metatavu.vp.vehiclemanagement.test.functional.common.providers.MaxCharsInvalidValueProvider
import java.util.*

/**
 * Class containing commonly used invalid values
 */
class InvalidValues {

    companion object {
        val NULL: Collection<InvalidValueProvider> = listOf(null).map { SimpleInvalidValueProvider(it) }
        val DATE: Collection<InvalidValueProvider> = listOf(null, "invalid_date", "0021-83-97T08:24:30.695066Z", "2021-03-17T25:75:30.695066Z", "").map { SimpleInvalidValueProvider(it) }.plus(
            MaxCharsInvalidValueProvider(255)
        )
        val TIME: Collection<InvalidValueProvider> = listOf(null, "invalid_time", "25:00", "22:90", "").map { SimpleInvalidValueProvider(it) }.plus(
            MaxCharsInvalidValueProvider(255)
        )
        val DATE_TIME: Collection<InvalidValueProvider> = listOf(null, "invalid_date", "0021-83-97T08:24:30.695066Z", "2021-03-17T25:75:30.695066Z", "").map { SimpleInvalidValueProvider(it) }.plus(
            MaxCharsInvalidValueProvider(255)
        )
        val STRING_NOT_NULL: Collection<InvalidValueProvider> = listOf("Правда", "झूठ", "🤮").map { SimpleInvalidValueProvider(it) }
    }

    /**
     * Invalid values for testing Trailers API
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
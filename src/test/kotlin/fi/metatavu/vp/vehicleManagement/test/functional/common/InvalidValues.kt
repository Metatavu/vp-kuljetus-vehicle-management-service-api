package fi.metatavu.vp.vehicleManagement.test.functional.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.vp.vehicleManagement.test.functional.common.providers.MaxCharsInvalidValueProvider
import fi.metatavu.metaform.server.test.functional.common.providers.SimpleInvalidValueProvider
import fi.metatavu.vp.test.client.models.Trailer
import fi.metatavu.vp.test.client.models.Vehicle
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
    class Trailers {
        companion object {
            val INVALID_TRAILERS = listOf(
                Trailer(plateNumber = "3"), Trailer(plateNumber = ""), Trailer(plateNumber = "hello*")
            ).map { jacksonObjectMapper().writeValueAsString(it) }.map { SimpleInvalidValueProvider(it) }
        }
    }

    /**
     * Invalid values for testing Trucks API (same because they both have the same plateNumber field)
     */
    class Trucks {
        companion object {
            val INVALID_TRUCKS = Trailers.INVALID_TRAILERS
        }
    }

    /**
     * Invalid values methods for Vehicles
     */
    class Vehicles {
        companion object {
            fun createVehicle(trailerIds: Array<UUID>, truckId: UUID): SimpleInvalidValueProvider {
                return SimpleInvalidValueProvider(
                    jacksonObjectMapper().writeValueAsString(
                        Vehicle(
                            trailerIds = trailerIds,
                            truckId = truckId
                        )
                    )
                )
            }
        }
    }

}
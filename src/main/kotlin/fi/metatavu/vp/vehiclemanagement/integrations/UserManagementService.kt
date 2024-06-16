package fi.metatavu.vp.vehiclemanagement.integrations

import fi.metatavu.vp.usermanagement.model.Driver
import fi.metatavu.vp.usermanagement.spec.DriversApi
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class UserManagementService {

    @RestClient
    lateinit var driversApi: DriversApi

    @Inject
    lateinit var logger: org.jboss.logging.Logger

    /**
     * Finds driver by driver card id
     *
     * @param driverCardId driver card id
     * @return found driver
     */
    suspend fun findDriverByDriverCardId(driverCardId: String): Driver? {
        return try {
            val foundDrivers = driversApi.listDrivers(driverCardId = driverCardId, archived = false, first = null, max = null)
                .awaitSuspending()
                .readEntity(Array<Driver>::class.java)

            foundDrivers?.firstOrNull()
        } catch (e: Exception) {
            logger.error("Error while searching for driver $driverCardId", e)
            null
        }
    }
}
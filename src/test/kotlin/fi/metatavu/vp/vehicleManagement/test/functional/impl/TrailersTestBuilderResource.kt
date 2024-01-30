package fi.metatavu.vp.vehicleManagement.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.apis.TrailersApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.Trailer
import fi.metatavu.vp.vehicleManagement.test.functional.TestBuilder
import fi.metatavu.vp.vehicleManagement.test.functional.settings.ApiTestSettings
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Trailers API
 */
class TrailersTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<Trailer, ApiClient>(testBuilder, apiClient) {

    /**
     * Creates new trailer
     *
     * @return created trailer
     */
    fun create(trailer: Trailer): Trailer {
        return addClosable(api.createTrailer(trailer))
    }

    /**
     * Creates new Trailer with default values
     *
     * @return created Trailer
     */
    fun create(): Trailer {
        return create(Trailer(plateNumber = "ABC-123"))
    }

    /**
     * Finds trailer
     *
     * @param trailerId trailer id
     * @return found trailer
     */
    fun find(trailerId: UUID): Trailer {
        return api.findTrailer(trailerId)
    }

    /**
     * Lists trailers
     *
     * @param plateNumber plate number
     * @param firstResult first result
     * @param maxResults max results
     * @return list of trailers
     */
    fun list(
        plateNumber: String? = null,
        firstResult: Int? = null,
        maxResults: Int? = null
    ): Array<Trailer> {
        return api.listTrailers(
            plateNumber,
            firstResult,
            maxResults
        )
    }

    /**
     * Updates trailer
     *
     * @param trailerId trailer id
     * @param updateData update data
     * @return updated trailer
     */
    fun update(trailerId: UUID, updateData: Trailer): Trailer {
        return api.updateTrailer(trailerId, updateData)
    }

    /**
     * Deletes trailer
     *
     * @param trailerId trailer id
     */
    fun delete(trailerId: UUID) {
        api.deleteTrailer(trailerId)
        removeCloseable { closable: Any ->
            if (closable !is Trailer) {
                return@removeCloseable false
            }

            closable.id == trailerId
        }
    }

    fun assertFindFail(expectedStatus: Int, trailerId: UUID) {
        try {
            api.findTrailer(trailerId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertListFail(
        expectedStatus: Int,
        plateNumber: String? = null,
        firstResult: Int? = null,
        maxResults: Int? = null
    ) {
        try {
            api.listTrailers(plateNumber, firstResult, maxResults)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertUpdateFail(expectedStatus: Int, trailerId: UUID, updateData: Trailer) {
        try {
            api.updateTrailer(trailerId, updateData)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertDeleteFail(expectedStatus: Int, trailerId: UUID) {
        try {
            api.deleteTrailer(trailerId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertCreateFail(expectedStatus: Int, trailer: Trailer) {
        try {
            api.createTrailer(trailer)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    override fun clean(p0: Trailer?) {
        delete(p0!!.id!!)
    }

    override fun getApi(): TrailersApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return TrailersApi(ApiTestSettings.apiBasePath)
    }


}
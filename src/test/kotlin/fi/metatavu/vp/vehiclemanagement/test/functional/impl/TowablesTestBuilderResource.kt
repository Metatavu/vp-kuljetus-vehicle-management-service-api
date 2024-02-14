package fi.metatavu.vp.vehiclemanagement.test.functional.impl

import fi.metatavu.jaxrs.test.functional.builder.auth.AccessTokenProvider
import fi.metatavu.vp.test.client.apis.TowablesApi
import fi.metatavu.vp.test.client.infrastructure.ApiClient
import fi.metatavu.vp.test.client.infrastructure.ClientException
import fi.metatavu.vp.test.client.models.Towable
import fi.metatavu.vp.vehiclemanagement.test.functional.TestBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import org.junit.Assert
import java.util.*

/**
 * Test builder resource for Towables API
 */
class TowablesTestBuilderResource(
    testBuilder: TestBuilder,
    private val accessTokenProvider: AccessTokenProvider?,
    apiClient: ApiClient
) : ApiTestBuilderResource<Towable, ApiClient>(testBuilder, apiClient) {

    /**
     * Creates new towable
     *
     * @return created towable
     */
    fun create(towable: Towable): Towable {
        return addClosable(api.createTowable(towable))
    }

    /**
     * Creates new Towable with default values
     *
     * @return created Towable
     */
    fun create(): Towable {
        return create(Towable(plateNumber = "ABC-123", vin = "001", type = Towable.Type.TRAILER))
    }

    /**
     * Creates new towable
     *
     * @param plateNumber plate number
     * @param vin VIN
     * @return created towable
     */
    fun create(plateNumber: String, vin: String): Towable {
        return create(Towable(plateNumber = plateNumber, vin = vin, type = Towable.Type.TRAILER))
    }

    /**
     * Finds towable
     *
     * @param towableId towable id
     * @return found towable
     */
    fun find(towableId: UUID): Towable {
        return api.findTowable(towableId)
    }

    /**
     * Lists towables
     *
     * @param plateNumber plate number
     * @param archived archived
     * @param firstResult first result
     * @param maxResults max results
     * @return list of towables
     */
    fun list(
        plateNumber: String? = null,
        firstResult: Int? = null,
        archived: Boolean? = null,
        maxResults: Int? = null
    ): Array<Towable> {
        return api.listTowables(
            plateNumber,
            archived,
            firstResult,
            maxResults
        )
    }

    /**
     * Updates towable
     *
     * @param towableId towable id
     * @param updateData update data
     * @return updated towable
     */
    fun update(towableId: UUID, updateData: Towable): Towable {
        return api.updateTowable(towableId, updateData)
    }

    /**
     * Deletes towable
     *
     * @param towableId towable id
     */
    fun delete(towableId: UUID) {
        api.deleteTowable(towableId)
        removeCloseable { closable: Any ->
            if (closable !is Towable) {
                return@removeCloseable false
            }

            closable.id == towableId
        }
    }

    fun assertFindFail(expectedStatus: Int, towableId: UUID) {
        try {
            api.findTowable(towableId)
            Assert.fail(String.format("Expected find to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertListFail(
        expectedStatus: Int,
        plateNumber: String? = null,
        archived: Boolean? = null,
        firstResult: Int? = null,
        maxResults: Int? = null
    ) {
        try {
            api.listTowables(plateNumber, archived, firstResult, maxResults)
            Assert.fail(String.format("Expected list to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertUpdateFail(expectedStatus: Int, towableId: UUID, updateData: Towable) {
        try {
            api.updateTowable(towableId, updateData)
            Assert.fail(String.format("Expected update to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertDeleteFail(expectedStatus: Int, towableId: UUID) {
        try {
            api.deleteTowable(towableId)
            Assert.fail(String.format("Expected delete to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    fun assertCreateFail(expectedStatus: Int, towable: Towable) {
        try {
            api.createTowable(towable)
            Assert.fail(String.format("Expected create to fail with status %d", expectedStatus))
        } catch (ex: ClientException) {
            assertClientExceptionStatus(expectedStatus, ex)
        }
    }

    override fun clean(p0: Towable?) {
        delete(p0!!.id!!)
    }

    override fun getApi(): TowablesApi {
        ApiClient.accessToken = accessTokenProvider?.accessToken
        return TowablesApi(ApiTestSettings.apiBasePath)
    }


}
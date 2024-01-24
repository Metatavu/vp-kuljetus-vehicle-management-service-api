package fi.metatavu.example.api.test.functional.impl

import fi.metatavu.example.client.infrastructure.ApiClient
import fi.metatavu.example.client.infrastructure.ClientException
import fi.metatavu.example.client.infrastructure.ServerException
import fi.metatavu.example.api.test.functional.TestBuilder
import org.junit.Assert

/**
 * Abstract base class for API test resource builders
 *
 * @author Jari Nykänen
 */
abstract class ApiTestBuilderResource<T, A>(
    testBuilder: TestBuilder,
    private val apiClient: ApiClient
):fi.metatavu.jaxrs.test.functional.builder.AbstractAccessTokenApiTestBuilderResource<T, A, ApiClient>(testBuilder) {

    /**
     * Returns API client
     *
     * @return API client
     */
    override fun getApiClient(): ApiClient {
        return apiClient
    }

    /**
     * Asserts that client exception has expected status code
     *
     * @param expectedStatus expected status code
     * @param e client exception
     */
    protected fun assertClientExceptionStatus(expectedStatus: Int, e: ClientException) {
        Assert.assertEquals(expectedStatus, e.statusCode)
    }

    /**
     * Asserts that server exception has expected status code
     *
     * @param expectedStatus expected status code
     * @param e server exception
     */
    protected fun assertServerExceptionStatus(expectedStatus: Int, e: ServerException) {
        Assert.assertEquals(expectedStatus, e.statusCode)
    }
}
package fi.metatavu.vp.vehiclemanagement.test.functional.resources.wiremock

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import fi.metatavu.vp.vehiclemanagement.test.functional.resources.TestData

/**
 * A response transformer used to transform list drivers responses by query parameters
 *
 * Reduces the amount of stubs to write by allowing to return different responses based on query parameters.
 */
class ListDriversResponseTransformer: ResponseTransformerV2 {
    override fun getName(): String {
        return NAME
    }

    override fun transform(response: Response?, serveEvent: ServeEvent?): Response {
        val queryParams = serveEvent?.request?.queryParams
        val driverCardId = queryParams?.get("driverCardId")?.values()?.firstOrNull()
        val driver = TestData.driverCardDriverMap[driverCardId]

        if (driver != null) {
            val driverJson = jacksonObjectMapper().writeValueAsString(listOf(driver))
            return Response.Builder.like(response).but().body(driverJson).build()
        }

        val driverJson = jacksonObjectMapper().writeValueAsString(TestData.driverCardDriverMap.values)

        return Response.Builder.like(response).but().body(driverJson).build()
    }

    companion object {
        const val NAME = "list-drivers-response-transformer"
    }
}
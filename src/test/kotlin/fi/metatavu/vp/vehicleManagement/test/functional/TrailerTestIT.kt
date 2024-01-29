package fi.metatavu.vp.vehicleManagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.vp.test.client.models.Trailer
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioBody
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioBuilder
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValueTestScenarioPath
import fi.metatavu.vp.vehicleManagement.test.functional.common.InvalidValues
import fi.metatavu.vp.vehicleManagement.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@QuarkusTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class)
)
class TrailerTestIT : AbstractFunctionalTest() {

    @Test
    fun testList() {
        createTestBuilder().use { builder ->
            builder.user.trailers.create(Trailer(plateNumber = plateNumber))
            builder.user.trailers.create(Trailer(plateNumber = "DEF-456"))
            builder.user.trailers.create(Trailer(plateNumber = "GHI-789"))
            val totalList = builder.user.trailers.list()
            Assertions.assertEquals(3, totalList.size)

            val pagedList = builder.user.trailers.list(firstResult = 1, maxResults = 1)
            Assertions.assertEquals(1, pagedList.size)

            val pagedList2 = builder.user.trailers.list(firstResult = 0, maxResults = 3)
            Assertions.assertEquals(3, pagedList2.size)

            val pagedList3 = builder.user.trailers.list(firstResult = 0, maxResults = 2)
            Assertions.assertEquals(2, pagedList3.size)

            val pagedList4 = builder.user.trailers.list(firstResult = 0, maxResults = 0)
            Assertions.assertEquals(0, pagedList4.size)

            val filteredList = builder.user.trailers.list(plateNumber = plateNumber)
            Assertions.assertEquals(1, filteredList.size)
        }
    }

    @Test
    fun testFind() {
        createTestBuilder().use { builder ->
            val trailerData = Trailer(plateNumber = plateNumber)
            val createdTrailer = builder.user.trailers.create(trailerData)
            Assertions.assertNotNull(createdTrailer)
            Assertions.assertEquals(trailerData.plateNumber, createdTrailer!!.plateNumber)
        }
    }

    @Test
    fun testFindFail() {
        createTestBuilder().use { builder ->
            val createdTrailer = builder.user.trailers.create(Trailer(plateNumber = plateNumber))

            InvalidValueTestScenarioBuilder(
                path = "v1/trailers/{trailerId}",
                method = Method.GET,
                token = builder.user.accessTokenProvider.accessToken
            )
                .path(
                    InvalidValueTestScenarioPath(
                        name = "trailerId",
                        values = InvalidValues.STRING_NOT_NULL,
                        default = createdTrailer!!.id,
                        expectedStatus = 404
                    )
                )
                .build()
                .test()
        }
    }

    @Test
    fun testCreate() {
        createTestBuilder().use { builder ->
            val createdTrailer = builder.user.trailers.create(Trailer(plateNumber = plateNumber))
            val foundTrailer = builder.user.trailers.find(createdTrailer!!.id!!)
            Assertions.assertNotNull(foundTrailer)
            Assertions.assertEquals(plateNumber, foundTrailer.plateNumber)
        }
    }

    @Test
    fun testCreateFail() {
        createTestBuilder().use { builder ->
            InvalidValueTestScenarioBuilder(
                path = "v1/trailers",
                method = Method.POST,
                token = builder.user.accessTokenProvider.accessToken
            )
                .body(
                    InvalidValueTestScenarioBody(
                        values = InvalidValues.Trailers.INVALID_TRAILERS,
                        expectedStatus = 400
                    )
                )
                .build()
                .test()
        }
    }

    @Test
    fun testUpdate() {
        createTestBuilder().use { builder ->
            val createdTrailer = builder.user.trailers.create(Trailer(plateNumber = plateNumber))
            val updatedTrailer = builder.user.trailers.update(createdTrailer!!.id!!, Trailer(plateNumber = "DEF-456"))
            Assertions.assertEquals("DEF-456", updatedTrailer.plateNumber)
        }
    }

    @Test
    fun testUpdateFail() {
        createTestBuilder().use { builder ->
            val createdTrailer = builder.user.trailers.create(Trailer(plateNumber = plateNumber))
            InvalidValueTestScenarioBuilder(
                path = "v1/trailers/{trailerId}",
                method = Method.PUT,
                token = builder.user.accessTokenProvider.accessToken
            )
                .path(
                    InvalidValueTestScenarioPath(
                        name = "trailerId",
                        values = InvalidValues.STRING_NOT_NULL,
                        default = createdTrailer!!.id,
                        expectedStatus = 404
                    )
                )
                .body(
                    InvalidValueTestScenarioBody(
                        values = InvalidValues.Trailers.INVALID_TRAILERS,
                        expectedStatus = 400
                    )
                )
                .build()
                .test()
        }
    }

    @Test
    fun testDelete() {
        createTestBuilder().use { builder ->
            val createdTrailer = builder.user.trailers.create(Trailer(plateNumber = plateNumber))
            builder.user.trailers.delete(createdTrailer!!.id!!)
            builder.user.trailers.assertFindFail(404, createdTrailer.id!!)
        }
    }

    @Test
    fun testDeleteFail() {
        createTestBuilder().use { builder ->
            val createdTrailer = builder.user.trailers.create(Trailer(plateNumber = plateNumber))
            InvalidValueTestScenarioBuilder(
                path = "v1/trailers/{trailerId}",
                method = Method.DELETE,
                token = builder.user.accessTokenProvider.accessToken
            )
                .path(
                    InvalidValueTestScenarioPath(
                        name = "trailerId",
                        values = InvalidValues.STRING_NOT_NULL,
                        default = createdTrailer!!.id,
                        expectedStatus = 404
                    )
                )
                .build()
                .test()
        }
    }

}
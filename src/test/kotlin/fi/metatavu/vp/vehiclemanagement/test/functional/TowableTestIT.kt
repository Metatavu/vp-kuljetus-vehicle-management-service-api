package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.vp.test.client.models.Towable
import fi.metatavu.vp.vehiclemanagement.test.functional.common.InvalidValueTestScenarioBody
import fi.metatavu.vp.vehiclemanagement.test.functional.common.InvalidValueTestScenarioBuilder
import fi.metatavu.vp.vehiclemanagement.test.functional.common.InvalidValueTestScenarioPath
import fi.metatavu.vp.vehiclemanagement.test.functional.common.InvalidValues
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@QuarkusTest
class TowableTestIT : AbstractFunctionalTest() {

    @Test
    fun testList() {
        createTestBuilder().use { builder ->
            builder.user.towables.create(Towable(plateNumber = plateNumber, type = Towable.Type.TRAILER))
            builder.user.towables.create(Towable(plateNumber = "DEF-456", type = Towable.Type.TRAILER))
            builder.user.towables.create(Towable(plateNumber = "GHI-789", type = Towable.Type.TRAILER))
            val totalList = builder.user.towables.list()
            assertEquals(3, totalList.size)

            val pagedList = builder.user.towables.list(firstResult = 1, maxResults = 1)
            assertEquals(1, pagedList.size)

            val pagedList2 = builder.user.towables.list(firstResult = 0, maxResults = 3)
            assertEquals(3, pagedList2.size)

            val pagedList3 = builder.user.towables.list(firstResult = 0, maxResults = 2)
            assertEquals(2, pagedList3.size)

            val pagedList4 = builder.user.towables.list(firstResult = 0, maxResults = 0)
            assertEquals(0, pagedList4.size)

            val filteredList = builder.user.towables.list(plateNumber = plateNumber)
            assertEquals(1, filteredList.size)
        }
    }

    @Test
    fun testCreate() {
        createTestBuilder().use { builder ->
            val towableData = Towable(plateNumber = plateNumber, type = Towable.Type.TRAILER)
            val createdTowable = builder.user.towables.create(towableData)
            assertNotNull(createdTowable)
            assertEquals(towableData.plateNumber, createdTowable.plateNumber)
            assertEquals(towableData.type, createdTowable.type)
            assertNotNull(createdTowable.id)
        }
    }

    @Test
    fun testCreateFail() {
        createTestBuilder().use { builder ->
            InvalidValueTestScenarioBuilder(
                path = "v1/towables",
                method = Method.POST,
                token = builder.user.accessTokenProvider.accessToken
            )
                .body(
                    InvalidValueTestScenarioBody(
                        values = InvalidValues.Towables.INVALID_TRAILERS,
                        expectedStatus = 400
                    )
                )
                .build()
                .test()
        }
    }

    @Test
    fun testFind() {
        createTestBuilder().use { builder ->
            val createdTowable = builder.user.towables.create()
            val foundTowable = builder.user.towables.find(createdTowable.id!!)
            assertNotNull(foundTowable)
            assertEquals(plateNumber, foundTowable.plateNumber)
        }
    }

    @Test
    fun testFindFail() {
        createTestBuilder().use { builder ->
            val createdTowable = builder.user.towables.create(Towable(plateNumber = plateNumber, type = Towable.Type.TRAILER))

            InvalidValueTestScenarioBuilder(
                path = "v1/towables/{towableId}",
                method = Method.GET,
                token = builder.user.accessTokenProvider.accessToken
            )
                .path(
                    InvalidValueTestScenarioPath(
                        name = "towableId",
                        values = InvalidValues.STRING_NOT_NULL,
                        default = createdTowable!!.id,
                        expectedStatus = 404
                    )
                )
                .build()
                .test()
        }
    }

    @Test
    fun testUpdate() {
        createTestBuilder().use { builder ->
            val createdTowable = builder.user.towables.create()
            val updateData = Towable(plateNumber = "DEF-456", type = Towable.Type.DOLLY)
            val updatedTowable = builder.user.towables.update(createdTowable.id!!, updateData)
            assertEquals(updateData.plateNumber, updatedTowable.plateNumber)
            assertEquals(updateData.type, updatedTowable.type)
        }
    }

    @Test
    fun testUpdateFail() {
        createTestBuilder().use { builder ->
            val createdTowable = builder.user.towables.create()
            InvalidValueTestScenarioBuilder(
                path = "v1/towables/{towableId}",
                method = Method.PUT,
                token = builder.user.accessTokenProvider.accessToken
            )
                .path(
                    InvalidValueTestScenarioPath(
                        name = "towableId",
                        values = InvalidValues.STRING_NOT_NULL,
                        default = createdTowable.id,
                        expectedStatus = 404
                    )
                )
                .body(
                    InvalidValueTestScenarioBody(
                        values = InvalidValues.Towables.INVALID_TRAILERS,
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
            val createdTowable = builder.user.towables.create()
            builder.user.towables.delete(createdTowable.id!!)
            builder.user.towables.assertFindFail(404, createdTowable.id)
        }
    }

    @Test
    fun testDeleteFail() {
        createTestBuilder().use { builder ->
            val createdTowable = builder.user.towables.create()
            InvalidValueTestScenarioBuilder(
                path = "v1/towables/{towableId}",
                method = Method.DELETE,
                token = builder.user.accessTokenProvider.accessToken
            )
                .path(
                    InvalidValueTestScenarioPath(
                        name = "towableId",
                        values = InvalidValues.STRING_NOT_NULL,
                        default = createdTowable.id,
                        expectedStatus = 404
                    )
                )
                .build()
                .test()
        }
    }

}
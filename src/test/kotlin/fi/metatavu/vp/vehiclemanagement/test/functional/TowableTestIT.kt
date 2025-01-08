package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.invalid.InvalidValueTestScenarioBody
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.test.client.models.Towable
import fi.metatavu.vp.vehiclemanagement.test.functional.impl.InvalidTestValues
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Test class for testing Towables API
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class TowableTestIT : AbstractFunctionalTest() {

    @Test
    fun testList() = createTestBuilder().use { builder ->
        builder.manager.towables.create(Towable(plateNumber = plateNumber, vin= "001", type = Towable.Type.TRAILER))
        builder.manager.towables.create(Towable(plateNumber = "DEF-456", vin= "002", type = Towable.Type.TRAILER))
        builder.manager.towables.create(Towable(plateNumber = "GHI-789", vin = "003", type = Towable.Type.TRAILER))
        val totalList = builder.manager.towables.list()
        assertEquals(3, totalList.size)

        val pagedList = builder.manager.towables.list(firstResult = 1, maxResults = 1)
        assertEquals(1, pagedList.size)

        val pagedList2 = builder.manager.towables.list(firstResult = 0, maxResults = 3)
        assertEquals(3, pagedList2.size)

        val pagedList3 = builder.manager.towables.list(firstResult = 0, maxResults = 2)
        assertEquals(2, pagedList3.size)

        val pagedList4 = builder.manager.towables.list(firstResult = 0, maxResults = 0)
        assertEquals(0, pagedList4.size)

        val filteredList = builder.manager.towables.list(plateNumber = plateNumber)
        assertEquals(1, filteredList.size)
    }

    @Test
    fun testListFail(): Unit = createTestBuilder().use {
        it.user.towables.assertListFail(403)
        assertNotNull(it.driver.towables.list())
        assertNotNull(it.manager.towables.list())
    }

    @Test
    fun testCreate() = createTestBuilder().use { builder ->
        val towableData = Towable(
            plateNumber = plateNumber,
            type = Towable.Type.TRAILER,
            vin = "someVinNumber",
            name = "Some towable",
            imei = "someImei"
        )
        val createdTowable = builder.manager.towables.create(towableData)
        assertNotNull(createdTowable)
        assertEquals(towableData.plateNumber, createdTowable.plateNumber)
        assertEquals(towableData.type, createdTowable.type)
        assertEquals(towableData.vin, createdTowable.vin)
        assertEquals(towableData.name, "Some towable")
        assertNotNull(createdTowable.id)
        assertNotNull(createdTowable.createdAt)

        // duplicate imei
        builder.manager.towables.assertCreateFail(400, towableData.copy(vin = "new"))
    }

    @Test
    fun testCreateFail() = createTestBuilder().use { builder ->
        val towableData = Towable(plateNumber = plateNumber, type = Towable.Type.TRAILER, vin = "someVinNumber")

        builder.user.towables.assertCreateFail(403, towableData)
        builder.driver.towables.assertCreateFail(403, towableData)
        InvalidValueTestScenarioBuilder(
            path = "v1/towables",
            method = Method.POST,
            token = builder.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .body(
                InvalidValueTestScenarioBody(
                    values = InvalidTestValues.Towables.INVALID_TRAILERS,
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testFind() = createTestBuilder().use { builder ->
        val createdTowable = builder.manager.towables.create()
        val foundTowable = builder.manager.towables.find(createdTowable.id!!)
        assertNotNull(foundTowable)
        assertEquals(plateNumber, foundTowable.plateNumber)
    }

    @Test
    fun testFindFail() = createTestBuilder().use { builder ->
        val createdTowable = builder.manager.towables.create(Towable(plateNumber = plateNumber, vin = "001", type = Towable.Type.TRAILER))

        builder.user.towables.assertFindFail(403, createdTowable.id!!)
        assertNotNull(builder.driver.towables.find(createdTowable.id))

        InvalidValueTestScenarioBuilder(
            path = "v1/towables/{towableId}",
            method = Method.GET,
            token = builder.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "towableId",
                    values = InvalidValues.STRING_NOT_NULL,
                    default = createdTowable.id,
                    expectedStatus = 404,
                )
            )
            .build()
            .test()
    }

    @Test
    fun testUpdate() = createTestBuilder().use { builder ->
        val createdTowable = builder.manager.towables.create()
        val updateData = Towable(plateNumber = "DEF-456", type = Towable.Type.DOLLY, vin = "updatedVin", name = "Updated towable name")
        val updatedTowable = builder.manager.towables.update(createdTowable.id!!, updateData)
        assertNull(createdTowable.name)
        assertEquals(updateData.name, "Updated towable name")
        assertEquals(updateData.plateNumber, updatedTowable.plateNumber)
        assertEquals(updateData.type, updatedTowable.type)
        assertEquals(updateData.vin, updatedTowable.vin)
    }

    @Test
    fun testArchiving() = createTestBuilder().use { builder ->
        val createdTowable = builder.manager.towables.create()
        var total = builder.manager.towables.list()
        assertEquals(1, total.size)

        //archiving
        val archived = builder.manager.towables.update(
            towableId = createdTowable.id!!,
            updateData = createdTowable.copy(archivedAt = createdTowable.createdAt)
        )
        assertNotNull(archived.archivedAt)
        total = builder.manager.towables.list()
        assertEquals(0, total.size)
        val totalUnarchived = builder.manager.towables.list(archived = false)
        assertEquals(0, totalUnarchived.size)
        val totalArchived = builder.manager.towables.list(archived = true)
        assertEquals(1, totalArchived.size)

        //cannot update archived data
        builder.manager.towables.assertUpdateFail(
            409,
            createdTowable.id,
            archived
        )

        //can un-archive towable
        val unarchived = builder.manager.towables.update(
            towableId = createdTowable.id,
            updateData = archived.copy(archivedAt = null)
        )
        assertNull(unarchived.archivedAt)
        total = builder.manager.towables.list()
        assertEquals(1, total.size)
    }

    @Test
    fun testUpdateFail() = createTestBuilder().use { builder ->
        val createdTowable = builder.manager.towables.create()

        builder.user.towables.assertUpdateFail(403, createdTowable.id!!, createdTowable)
        builder.driver.towables.assertUpdateFail(403, createdTowable.id, createdTowable)

        InvalidValueTestScenarioBuilder(
            path = "v1/towables/{towableId}",
            method = Method.PUT,
            token = builder.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
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
                    values = InvalidTestValues.Towables.INVALID_TRAILERS,
                    expectedStatus = 400
                )
            )
            .build()
            .test()
    }

    @Test
    fun testDelete() = createTestBuilder().use { builder ->
        val createdTowable = builder.manager.towables.create()
        builder.manager.towables.delete(createdTowable.id!!)
        builder.manager.towables.assertFindFail(404, createdTowable.id)
    }

    @Test
    fun testDeleteFail() = createTestBuilder().use { builder ->
        val createdTowable = builder.manager.towables.create()

        builder.user.towables.assertDeleteFail(403, createdTowable.id!!)
        builder.driver.towables.assertDeleteFail(403, createdTowable.id)

        InvalidValueTestScenarioBuilder(
            path = "v1/towables/{towableId}",
            method = Method.DELETE,
            token = builder.manager.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath
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
package fi.metatavu.vp.vehiclemanagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.test.client.models.TelematicData
import fi.metatavu.vp.test.client.models.Towable
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.Method
import org.junit.jupiter.api.Test

/**
 * A test class for testing telematics
 */
@QuarkusTest
class TruckTelematicDataTestIT : AbstractFunctionalTest() {

    private final val temelaticData = TelematicData(
        timestamp = System.currentTimeMillis(),
        latitude = 123.0,
        longitude = 123.0,
        speed = 123.0.toFloat(),
        imei = "123",
    )

    @Test
    fun testCreate() = createTestBuilder().use {
        val truck = it.user.trucks.create(Truck(plateNumber = "ABC-124", type = Truck.Type.SEMI_TRUCK, vin = "0001"))
        val towable = it.user.towables.create(Towable(plateNumber = "ABC-123", type = Towable.Type.TRAILER, vin = "0000"))

        it.user.telematics.receiveTelematicData(
            vin = towable.vin!!,
            telematicData = temelaticData
        )

        it.user.telematics.receiveTelematicData(
            vin = truck.vin!!,
            telematicData = temelaticData
        )
    }

    @Test
    fun testCreateFail() = createTestBuilder().use { builder ->
        InvalidValueTestScenarioBuilder(
            path = "v1/telematics/{vin}",
            method = Method.POST,
            token = builder.user.accessTokenProvider.accessToken,
            basePath = ApiTestSettings.apiBasePath,
            body = jacksonObjectMapper().writeValueAsString(temelaticData)
        )
            .path(
                InvalidValueTestScenarioPath(
                    name = "vin",
                    values = InvalidValues.STRING_NOT_NULL,
                    expectedStatus = 404
                )
            )
            .build()
            .test()

    }

}
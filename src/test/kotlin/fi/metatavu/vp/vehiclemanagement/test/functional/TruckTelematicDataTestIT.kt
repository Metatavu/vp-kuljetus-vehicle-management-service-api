package fi.metatavu.vp.vehiclemanagement.test.functional

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.metatavu.invalid.InvalidValueTestScenarioBuilder
import fi.metatavu.invalid.InvalidValueTestScenarioPath
import fi.metatavu.invalid.InvalidValues
import fi.metatavu.vp.test.client.models.TelematicData
import fi.metatavu.vp.test.client.models.Truck
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.ApiTestSettings
import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import io.restassured.http.Method
import org.junit.jupiter.api.Test
import java.util.*

/**
 * A test class for testing telematics
 */
@QuarkusTest
@TestProfile(DefaultTestProfile::class)
class TruckTelematicDataTestIT : AbstractFunctionalTest() {

    private final val temelaticData = TelematicData(
        timestamp = System.currentTimeMillis(),
        latitude = 123.0,
        longitude = 123.0,
        speed = 123.0.toFloat(),
        imei = "123",
    )

    @Test
    fun testCreateTruckTelematicData() = createTestBuilder().use {
        val truck = it.manager.trucks.create(
            Truck(
                plateNumber = "ABC-124",
                type = Truck.Type.SEMI_TRUCK,
                vin = "0001"
            ),
            it.manager.vehicles
        )

        it.setApiKey().telematics.receiveTelematicData(
            vin = truck.vin,
            telematicData = temelaticData
        )

        it.setApiKey("invalid-api-key").telematics.assertReceiveDataFail(
            vin = truck.vin,
            telematicData = temelaticData,
            expectedStatus = 403
        )

        it.setApiKey("").telematics.assertReceiveDataFail(
            vin = truck.vin,
            telematicData = temelaticData,
            expectedStatus = 403
        )
    }

    @Test
    fun testCreateFail() = createTestBuilder().use {
        InvalidValueTestScenarioBuilder(
            path = "v1/telematics/{vin}",
            method = Method.POST,
            header = "X-API-Key" to "test-api-key",
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
package fi.metatavu.vp.vehiclemanagement.test.functional

import fi.metatavu.vp.vehiclemanagement.test.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for telematic API
 */
@QuarkusIntegrationTest
@TestProfile(DefaultTestProfile::class)
class NativeTruckTelematicDataTestIT: TruckTelematicDataTestIT()

package fi.metatavu.vp.vehicleManagement.test.functional

import fi.metatavu.vp.vehicleManagement.test.functional.resources.MysqlResource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest

/**
 * Native tests for Truck API
 */
@QuarkusIntegrationTest
@QuarkusTestResource.List(
    QuarkusTestResource(MysqlResource::class)
)
class NativeTruckTestIT: TruckTestIT()

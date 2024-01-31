package fi.metatavu.vp.vehiclemanagement.test.functional.common

/**
 * Class for invalid body test scenarios
 *
 * @param values values to test
 * @param default default value
 * @param expectedStatus expected status code
 */
class InvalidValueTestScenarioBody(
    values: Collection<InvalidValueProvider>,
    default: Any? = null,
    expectedStatus: Int
) : InvalidValueTestScenarioBase("", values, emptyList(), default, expectedStatus)
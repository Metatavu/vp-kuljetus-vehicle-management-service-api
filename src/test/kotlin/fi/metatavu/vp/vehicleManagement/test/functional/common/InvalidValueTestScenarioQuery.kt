package fi.metatavu.vp.vehicleManagement.test.functional.common

/**
 * Single query parameter for invalid value test scenario builder
 *
 * @param name parameter name
 * @param values tested values
 * @param except values excluded from the values
 * @param expectedStatus expected status
 * @param default default value
 */
class InvalidValueTestScenarioQuery(
    name: String,
    values: Collection<InvalidValueProvider>,
    except: Collection<Any?> = emptyList(),
    default: Any? = null,
    expectedStatus: Int
): InvalidValueTestScenarioBase(name, values, except, default, expectedStatus) {

}
package fi.metatavu.vp.vehicleManagement.test.functional.common

import io.restassured.http.Method

/**
 * Builder for building test scenarios for asserting that invalid values are not accepted by the API
 *
 * @param path path
 * @param method request method
 * @param token token if applicable
 * @param body body if needed
 */
class InvalidValueTestScenarioBuilder(
    private val path: String,
    private val method: Method,
    private val token: String? = null,
    private val body: String? = null
) {

    // todo this includes body too
    private val parameters: MutableList<InvalidValueTestScenarioBase> = mutableListOf()

    /**
     * Adds a query parameter to the scenario
     *
     * @param query query
     * @return builder instance
     */
    fun query(query: InvalidValueTestScenarioQuery): InvalidValueTestScenarioBuilder {
        parameters.add(query)
        return this
    }

    /**
     * Adds a path parameter to the scenario
     *
     * @param path query
     * @return builder instance
     */
    fun path(path: InvalidValueTestScenarioPath): InvalidValueTestScenarioBuilder {
        parameters.add(path)
        return this
    }

    /**
     * Adds a body parameter to the scenario
     *
     * @param body body
     * @return builder instance
     */
    fun body(body: InvalidValueTestScenarioBody): InvalidValueTestScenarioBuilder {
        parameters.add(body)
        return this
    }

    /**
     * Builds test scenarios
     *
     * @return test scenarios
     */
    fun build(): InvalidValueTestScenarios {
        val scenarios: MutableList<InvalidValueTestScenario> = mutableListOf()

        parameters.forEach { parameter ->
            val parameterValues = parameter.values.map(InvalidValueProvider::value)

            parameterValues.minus(parameter.except.toSet()).forEach { parameterValue ->
                val queryParams = buildDefaultQueryParams().toMutableMap()
                val pathParams = buildDefaultPathParams().toMutableMap()
                var newBody = body
                when (parameter) {
                    is InvalidValueTestScenarioQuery -> {
                        queryParams[parameter.name] = parameterValue
                    }

                    is InvalidValueTestScenarioPath -> {
                        pathParams[parameter.name] = parameterValue
                    }

                    is InvalidValueTestScenarioBody -> {
                        // If there is body parameter, it should be used instead of the default provided one
                        newBody = parameterValue.toString()
                    }
                }

                scenarios.add(
                    InvalidValueTestScenario(
                    path = path,
                    method = method,
                    token = token,
                    body = newBody,
                    queryParams = queryParams,
                    pathParams = pathParams,
                    expectedStatus = parameter.expectedStatus
                )
                )
            }
        }

        return InvalidValueTestScenarios(scenarios = scenarios)
    }

    /**
     * Builds default query parameter map
     *
     * @return default query parameter map
     */
    private fun buildDefaultQueryParams(): Map<String, Any?> {
        return parameters.filterIsInstance<InvalidValueTestScenarioQuery>().associate {
            it.name to it.default
        }
    }

    /**
     * Builds default path parameter map
     *
     * @return default path parameter map
     */
    private fun buildDefaultPathParams(): Map<String, Any?> {
        return parameters.filterIsInstance<InvalidValueTestScenarioPath>().associate {
            it.name to it.default
        }
    }

}

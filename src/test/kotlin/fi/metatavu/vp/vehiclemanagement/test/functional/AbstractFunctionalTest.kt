package fi.metatavu.vp.vehiclemanagement.test.functional

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.quarkus.test.common.DevServicesContext
import org.eclipse.microprofile.config.ConfigProvider
import org.json.JSONException
import org.junit.Assert
import org.skyscreamer.jsonassert.JSONCompare
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.JSONCompareResult
import org.skyscreamer.jsonassert.comparator.CustomComparator
import java.io.File
import java.time.OffsetDateTime
import java.util.UUID
import java.util.zip.ZipFile

/**
 * Abstract base class for functional tests
 */
abstract class AbstractFunctionalTest {

    val plateNumber = "ABC-123"

    /**
     * Compares objects as serialized JSONs
     *
     * @param expected expected
     * @param actual actual
     * @return comparison result
     * @throws JSONException
     * @throws JsonProcessingException
     */
    @Throws(JSONException::class, JsonProcessingException::class)
    private fun jsonCompare(expected: Any?, actual: Any?): JSONCompareResult? {
        val customComparator = CustomComparator(JSONCompareMode.LENIENT)
        return JSONCompare.compareJSON(toJSONString(expected), toJSONString(actual), customComparator)
    }

    /**
     * Serializes an object into JSON
     *
     * @param object object
     * @return JSON string
     * @throws JsonProcessingException
     */
    @Throws(JsonProcessingException::class)
    private fun toJSONString(`object`: Any?): String? {
        return if (`object` == null) {
            null
        } else getObjectMapper().writeValueAsString(`object`)
    }

    /**
     * Returns object mapper with default modules and settings
     *
     * @return object mapper
     */
    private fun getObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        return objectMapper
    }

    /**
     * Reads lines from a file inside a zip archive
     *
     * @param zipFile ZIP-file
     * @param entryName ZIP-entry (file) name
     *
     * @return lines
     */
    fun readLinesFromZipEntry (zipFile: File, entryName: String): List<String> {
        return ZipFile(zipFile).use { zip ->
            val zipEntry = zip.getEntry(entryName)
            zip.getInputStream(zipEntry).use {
                it.reader().readLines()
            }
        }
    }

    /**
     * Asserts that two objects are equal
     *
     * @param expected expected
     * @param actual actual
     */
    fun assertOffsetDatetimeEquals(expected: String, actual: String) {
        val expectedTimestamp = OffsetDateTime.parse(expected)
        val actualTimestamp = OffsetDateTime.parse(actual)
        Assert.assertEquals(expectedTimestamp.toEpochSecond(), actualTimestamp.toEpochSecond())
    }

    private var devServicesContext: DevServicesContext? = null

    /**
     * Creates new test builder
     *
     * @return new test builder
     */
    protected fun createTestBuilder(): TestBuilder {
        return TestBuilder(getConfig())
    }

    /**
     * Returns config for tests.
     *
     * If tests are running in native mode, method returns config from devServicesContext and
     * when tests are runnig in JVM mode method returns config from the Quarkus config
     *
     * @return config for tests
     */
    private fun getConfig(): Map<String, String> {
        return getDevServiceConfig() ?: getQuarkusConfig()
    }

    /**
     * Returns test config from dev services
     *
     * @return test config from dev services
     */
    private fun getDevServiceConfig(): Map<String, String>? {
        return devServicesContext?.devServicesProperties()
    }

    /**
     * Returns test config from Quarkus
     *
     * @return test config from Quarkus
     */
    private fun getQuarkusConfig(): Map<String, String> {
        val config = ConfigProvider.getConfig()
        return config.propertyNames.associateWith { config.getConfigValue(it).rawValue }
    }

    companion object {
        val driver1Id: UUID = UUID.fromString("95dd89a2-da9a-4ce4-979d-8897b7603b2e")
        const val driver1CardId = "001"
        val driver2Id: UUID = UUID.fromString("0bb1039a-7688-453d-b4e8-4f5ba277db0c")
        const val driver2CardId = "002"
    }
}
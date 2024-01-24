package fi.metatavu.vp.vehicleManagement.test.functional.resources
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import org.testcontainers.containers.MySQLContainer

internal class SpecifiedMySQLContainer(image: String) : MySQLContainer<SpecifiedMySQLContainer>(image)

/**
 * Quarkus test resource for providing MySQL database
 */
class MysqlResource : QuarkusTestResourceLifecycleManager {

    private val db: MySQLContainer<*> = SpecifiedMySQLContainer("mysql:5.7")
        .withDatabaseName(DATABASE)
        .withUsername(USERNAME)
        .withPassword(PASSWORD)
        .withCommand(
            "--character-set-server=utf8mb4",
            "--collation-server=utf8mb4_unicode_ci",
            "--lower_case_table_names=1"
        )

    override fun start(): Map<String, String> {
        db.start()
        val config: MutableMap<String, String> = HashMap()
        config["quarkus.datasource.username"] = USERNAME
        config["quarkus.datasource.password"] = PASSWORD
        config["quarkus.datasource.jdbc.url"] = db.jdbcUrl
        config["quarkus.datasource.reactive.url"] = db.jdbcUrl.replace("jdbc:", "")
        return config
    }

    override fun stop() {
        db.stop()
    }

    companion object {
        const val DATABASE = "vehicle-management"
        const val USERNAME = "admin"
        const val PASSWORD = "password"
    }

}
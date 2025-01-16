package fi.metatavu.vp.vehiclemanagement.thermometers.temperatureReadings

import fi.metatavu.vp.vehiclemanagement.model.TemperatureReading
import fi.metatavu.vp.vehiclemanagement.model.TemperatureReadingSourceType
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.spec.TemperatureReadingsApi
import fi.metatavu.vp.vehiclemanagement.thermometers.ThermometerController
import fi.metatavu.vp.vehiclemanagement.towables.TowableController
import fi.metatavu.vp.vehiclemanagement.trucks.TruckController
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response

/**
 * API implementation for temperature readings
 */
@RequestScoped
class TemperatureReadingsApiImpl : TemperatureReadingsApi, AbstractApi() {

    @Inject
    lateinit var thermometerController: ThermometerController

    @Inject
    lateinit var temperatureReadingController: TemperatureReadingController

    @Inject
    lateinit var truckController: TruckController

    @Inject
    lateinit var towableController: TowableController

    @WithTransaction
    override fun createTemperatureReading(temperatureReading: TemperatureReading): Uni<Response> = withCoroutineScope {
        if (requestDataReceiverKey != dataReceiverApiKeyValue) return@withCoroutineScope createForbidden(INVALID_API_KEY)

        val truckByIdentifier = truckController.findTruckByImei(temperatureReading.deviceIdentifier)
        val towableByIdentifier = if (truckByIdentifier == null) {
            towableController.findTowableByImei(temperatureReading.deviceIdentifier)
        } else null
        if (truckByIdentifier == null && towableByIdentifier == null) {
            return@withCoroutineScope createBadRequest("Truck or towable not found")
        }

        val selectedThermometer = thermometerController.findOrCreate(
            hardwareSensorId = temperatureReading.hardwareSensorId,
            deviceIdentifier = temperatureReading.deviceIdentifier,
            targetTruck = truckByIdentifier,
            targetTowable = towableByIdentifier
        )

        temperatureReadingController.create(selectedThermometer, temperatureReading)
            ?: return@withCoroutineScope createBadRequest("Failed to create temperature reading")
        createNoContent()
    }
}
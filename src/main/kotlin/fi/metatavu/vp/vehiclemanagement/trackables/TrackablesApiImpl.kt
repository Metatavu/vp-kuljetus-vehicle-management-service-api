package fi.metatavu.vp.vehiclemanagement.trackables

import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.spec.TrackablesApi
import fi.metatavu.vp.vehiclemanagement.towables.TowableController
import fi.metatavu.vp.vehiclemanagement.trucks.TruckController
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response

@RequestScoped
@WithSession
@Suppress("unused")
class TrackablesApiImpl: TrackablesApi, AbstractApi() {

    @Inject
    lateinit var truckController: TruckController

    @Inject
    lateinit var towableController: TowableController

    @Inject
    lateinit var trackableTranslator: TrackableTranslator

    override fun getTrackableByImei(imei: String): Uni<Response> = withCoroutineScope {
        if (requestDataReceiverKey != dataReceiverApiKeyValue) return@withCoroutineScope createForbidden(INVALID_API_KEY)

        val foundEntity = truckController.findTruckByImei(imei)
            ?: towableController.findTowableByImei(imei)
            ?: return@withCoroutineScope createNotFound("No trackable found with IMEI $imei")

        createOk(trackableTranslator.translate(foundEntity))

    }
}
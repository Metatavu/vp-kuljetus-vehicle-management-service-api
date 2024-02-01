package fi.metatavu.vp.vehiclemanagement.telematics

import fi.metatavu.vp.api.model.TelematicData
import fi.metatavu.vp.api.spec.TelematicsApi
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.asUni
import io.vertx.kotlin.coroutines.dispatcher
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async

@RequestScoped
@OptIn(ExperimentalCoroutinesApi::class)
@WithSession
class TelematicsApiImpl : TelematicsApi, AbstractApi() {

    @Inject
    lateinit var telematicsController: TelematicsController

    @Inject
    lateinit var vertx: io.vertx.core.Vertx

    @WithTransaction
    override fun receiveTelematicData(vin: String, telematicData: TelematicData): Uni<Response> =
        CoroutineScope(vertx.dispatcher()).async {
            val telematicsDevice = telematicsController.fineDeviceByVin(vin) ?: return@async createNotFound("Device with $vin not found")
            telematicsController.create(telematicsDevice, telematicData)
            createNoContent()
        }.asUni()
}
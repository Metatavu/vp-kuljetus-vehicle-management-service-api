package fi.metatavu.vp.vehiclemanagement.thermometers

import fi.metatavu.vp.vehiclemanagement.model.UpdateTruckOrTowableThermometerRequest
import fi.metatavu.vp.vehiclemanagement.rest.AbstractApi
import fi.metatavu.vp.vehiclemanagement.spec.ThermometersApi
import io.quarkus.hibernate.reactive.panache.common.WithSession
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.smallrye.mutiny.Uni
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import java.util.*

/**
 * Thermometers API implementation
 */
@RequestScoped
@WithSession
@Suppress("unused")
class ThermometersApiImpl: ThermometersApi, AbstractApi() {

    @Inject
    lateinit var thermometerController: ThermometerController

    @Inject
    lateinit var thermometerTranslator: ThermometerTranslator

    @RolesAllowed(MANAGER_ROLE)
    override fun listTruckOrTowableThermometers(
        entityId: UUID?,
        entityType: String?,
        includeArchived: Boolean,
        first: Int?,
        max: Int?
    ): Uni<Response> = withCoroutineScope {
        if (entityId != null && entityType == null) return@withCoroutineScope createBadRequest(
            BOTH_ENTITY_ENTITYTYPE_NEEDED
        )
        if (entityId == null && entityType != null) return@withCoroutineScope createBadRequest(
            BOTH_ENTITY_ENTITYTYPE_NEEDED
        )

        val (thermometers, count) = thermometerController.listThermometers(
            entityId = entityId,
            entityType = entityType,
            includeArchived = includeArchived,
            first = first,
            max = max
        )
        createOk(thermometerTranslator.translate(thermometers), count)
    }

    @RolesAllowed(MANAGER_ROLE)
    override fun findTruckOrTowableThermometer(thermometerId: UUID): Uni<Response> = withCoroutineScope {
        val thermometer =
            thermometerController.findThermometer(thermometerId) ?: return@withCoroutineScope createNotFound(
                createNotFoundMessage(THERMOMETER, thermometerId)
            )

        createOk(thermometerTranslator.translate(thermometer))
    }

    @RolesAllowed(MANAGER_ROLE)
    @WithTransaction
    override fun updateTruckOrTowableThermometer(
        thermometerId: UUID,
        updateThermometerRequest: UpdateTruckOrTowableThermometerRequest
    ): Uni<Response> = withCoroutineScope {
        loggedUserId ?: return@withCoroutineScope createUnauthorized(UNAUTHORIZED)
        val found = thermometerController.findThermometer(thermometerId) ?: return@withCoroutineScope createNotFound(
            createNotFoundMessage(THERMOMETER, thermometerId)
        )
        val updated = thermometerController.update(found, updateThermometerRequest)
        createOk(thermometerTranslator.translate(updated))
    }
}
package sa.factory.masarrtknavigation.domain.usecase

import sa.factory.masarrtknavigation.domain.repository.MasarRtkRepository
import sa.factory.masarrtknavigation.model.MasarRtkItem
import sa.factory.masarrtknavigation.model.MasarRtkOrder
import sa.factory.masarrtknavigation.model.MasarRtkTelemetry
import kotlinx.coroutines.flow.Flow

class GetCatalogUseCase(private val repository: MasarRtkRepository) {
    operator fun invoke(): Flow<List<MasarRtkItem>> = repository.catalogFlow
}

class ManageCartUseCase(private val repository: MasarRtkRepository) {
    val cart = repository.activeCart
    fun add(item: MasarRtkItem) = repository.addToCart(item)
    fun remove(item: MasarRtkItem) = repository.removeFromCart(item)
    fun clear() = repository.clearCart()
}

class ProcessCheckoutUseCase(private val repository: MasarRtkRepository) {
    suspend operator fun invoke(paymentMethod: String = "Mada Contactless EMV"): MasarRtkOrder =
        repository.checkout(paymentMethod)
}

class ObserveHardwareTelemetryUseCase(private val repository: MasarRtkRepository) {
    operator fun invoke(): Flow<MasarRtkTelemetry> = repository.telemetryFlow
}

package sa.factory.masarrtknavigation.domain.repository

import sa.factory.masarrtknavigation.model.MasarRtkItem
import sa.factory.masarrtknavigation.model.MasarRtkOrder
import sa.factory.masarrtknavigation.model.MasarRtkTelemetry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MasarRtkRepository {
    val catalogFlow: Flow<List<MasarRtkItem>>
    val activeCart: StateFlow<List<MasarRtkItem>>
    val telemetryFlow: Flow<MasarRtkTelemetry>
    
    fun addToCart(item: MasarRtkItem)
    fun removeFromCart(item: MasarRtkItem)
    fun clearCart()
    suspend fun checkout(paymentMethod: String): MasarRtkOrder
    suspend fun refreshCatalog()
}

package sa.factory.masarrtknavigation.data.remote

import sa.factory.masarrtknavigation.model.MasarRtkItem
import sa.factory.masarrtknavigation.model.MasarRtkOrder

interface MasarRtkApiService {
    suspend fun fetchCatalog(): List<MasarRtkItem>
    suspend fun submitOrder(order: MasarRtkOrder): Boolean
}

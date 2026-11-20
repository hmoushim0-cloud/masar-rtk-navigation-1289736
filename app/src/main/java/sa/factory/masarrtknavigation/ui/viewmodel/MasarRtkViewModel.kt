package sa.factory.masarrtknavigation.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import sa.factory.masarrtknavigation.domain.usecase.*
import sa.factory.masarrtknavigation.model.MasarRtkItem
import sa.factory.masarrtknavigation.model.MasarRtkOrder
import sa.factory.masarrtknavigation.model.MasarRtkTelemetry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MasarRtkUiState(
    val catalog: List<MasarRtkItem> = emptyList(),
    val filteredCatalog: List<MasarRtkItem> = emptyList(),
    val selectedCategory: String = "الكل",
    val cart: List<MasarRtkItem> = emptyList(),
    val telemetry: MasarRtkTelemetry? = null,
    val lastOrder: MasarRtkOrder? = null,
    val isCheckingOut: Boolean = false,
    val activeTab: String = "home"
)

class MasarRtkViewModel(
    private val getCatalogUseCase: GetCatalogUseCase,
    private val manageCartUseCase: ManageCartUseCase,
    private val processCheckoutUseCase: ProcessCheckoutUseCase,
    private val observeHardwareTelemetryUseCase: ObserveHardwareTelemetryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MasarRtkUiState())
    val uiState: StateFlow<MasarRtkUiState> = _uiState.asStateFlow()

    init {
        // Observe Catalog
        getCatalogUseCase().onEach { list ->
            _uiState.update { it.copy(catalog = list, filteredCatalog = list) }
        }.launchIn(viewModelScope)

        // Observe Cart
        manageCartUseCase.cart.onEach { cartItems ->
            _uiState.update { it.copy(cart = cartItems) }
        }.launchIn(viewModelScope)

        // Observe Telemetry
        observeHardwareTelemetryUseCase().onEach { telem ->
            _uiState.update { it.copy(telemetry = telem) }
        }.launchIn(viewModelScope)
    }

    fun selectCategory(category: String) {
        _uiState.update { state ->
            val filtered = if (category == "الكل") state.catalog else state.catalog.filter { it.category == category }
            state.copy(selectedCategory = category, filteredCatalog = filtered)
        }
    }

    fun switchTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun addToCart(item: MasarRtkItem) = manageCartUseCase.add(item)
    fun removeFromCart(item: MasarRtkItem) = manageCartUseCase.remove(item)

    fun checkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true) }
            val order = processCheckoutUseCase()
            _uiState.update { it.copy(isCheckingOut = false, lastOrder = order, activeTab = "checkout") }
        }
    }
}

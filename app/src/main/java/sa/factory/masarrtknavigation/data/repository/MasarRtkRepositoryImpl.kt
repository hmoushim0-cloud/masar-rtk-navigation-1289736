package sa.factory.masarrtknavigation.data.repository

import sa.factory.masarrtknavigation.domain.repository.MasarRtkRepository
import sa.factory.masarrtknavigation.hardware.HardwareSensorDriver
import sa.factory.masarrtknavigation.model.MasarRtkItem
import sa.factory.masarrtknavigation.model.MasarRtkOrder
import sa.factory.masarrtknavigation.model.MasarRtkTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MasarRtkRepositoryImpl(
    private val sensorDriver: HardwareSensorDriver
) : MasarRtkRepository {

    private val _catalog = MutableStateFlow<List<MasarRtkItem>>(listOf(
        MasarRtkItem("1", "معايرة زوايا الإضاءة والعمق البصري الحركي", "توجيه فوري لموقع الكاميرا استنادا إلى إحداثيات GPS RTK وميل الجيروسكوب لضمان ثبات الإطار", 450.0, "دقة عالية", "توجيه الزوايا الجيومكانية"),
        MasarRtkItem("2", "هندسة السيناريو التفاعلي لصناع المحتوى", "تصميم سيناريوهات مرئية مترابطة زمنيا ومكانيا مع مسارات الحركة المدعومة بالمستشعرات", 320.0, "تخصصي", "هندسة السيناريو الحركي"),
        MasarRtkItem("3", "المسح الجيومكاني لموقع التصوير الخارجي", "تحديد النقاط البؤرية المثلى للكاميرا باستخدام مسح الأقمار الصناعية عالي الدقة وتوجيه العتاد الميداني", 600.0, "حصري", "معايرة المستشعرات الجيروسكوبية")
    ))
    override val catalogFlow: Flow<List<MasarRtkItem>> = _catalog.asStateFlow()

    private val _cart = MutableStateFlow<List<MasarRtkItem>>(emptyList())
    override val activeCart: StateFlow<List<MasarRtkItem>> = _cart.asStateFlow()

    override val telemetryFlow: Flow<MasarRtkTelemetry> = sensorDriver.observePeripheralStream()
        .map { frame ->
            MasarRtkTelemetry(
                peripheralName = frame.peripheralName,
                status = frame.connectionState,
                liveReading = frame.livePayload,
                batteryPct = 96,
                signalDbm = -54
            )
        }

    override fun addToCart(item: MasarRtkItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    override fun removeFromCart(item: MasarRtkItem) {
        val current = _cart.value.toMutableList()
        current.remove(item)
        _cart.value = current
    }

    override fun clearCart() {
        _cart.value = emptyList()
    }

    override suspend fun checkout(paymentMethod: String): MasarRtkOrder {
        val currentItems = _cart.value.toList()
        val subtotal = currentItems.sumOf { it.priceSar }
        val vat = subtotal * 0.15
        val total = subtotal + vat
        val order = MasarRtkOrder(
            orderId = "ORD-${System.currentTimeMillis() % 100000}",
            items = currentItems,
            subtotalSar = subtotal,
            vatSar = vat,
            totalSar = total,
            paymentMethod = paymentMethod
        )
        clearCart()
        return order
    }

    override suspend fun refreshCatalog() {
        // Offline-first sync logic
    }
}

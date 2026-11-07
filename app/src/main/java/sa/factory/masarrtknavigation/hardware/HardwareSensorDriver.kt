package sa.factory.masarrtknavigation.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import sa.factory.masarrtknavigation.engine.DynamicFleetDispatchEngine
import sa.factory.masarrtknavigation.engine.FleetDispatchMission
import sa.factory.masarrtknavigation.engine.RtkCarrierPhaseProtocolEngine
import sa.factory.masarrtknavigation.engine.RtkFixQuality
import kotlin.random.Random

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver {
    private val peripheral = "GPS RTK High-Precision Receiver + Gyroscope"
    private val rtkEngine = RtkCarrierPhaseProtocolEngine()
    private val dispatchEngine = DynamicFleetDispatchEngine()
    private var currentMission: FleetDispatchMission? = null

    init {
        // Initialize an active commercial logistics dispatch mission
        currentMission = dispatchEngine.dispatchMission("TRK-770-SA", "KKIA-CARGO", "KAFD-CTR")
    }

    fun getRtkEngine(): RtkCarrierPhaseProtocolEngine = rtkEngine
    fun getDispatchEngine(): DynamicFleetDispatchEngine = dispatchEngine
    fun getCurrentMission(): FleetDispatchMission? = currentMission

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "تهيئة اتصال طور الحامل RTK ومزامنة المحطة المرجعية..."))
        delay(300)
        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "نظام RTK متصل بالمحطة BASE-RUH-KAFD-01 | الدقة 1.8 سم"))

        var step = 0
        var simLat = 24.9576
        var simLon = 46.6988

        while (true) {
            delay(1500)
            step++
            // Interpolate vehicle movement towards KAFD
            simLat -= 0.003
            simLon -= 0.001
            val fix = rtkEngine.generateSimulatedRtkFix(lat = simLat, lon = simLon, speed = 54.0 + Random.nextInt(0, 12))
            val updatedMission = currentMission?.let {
                dispatchEngine.updateMissionProgress(it.missionId, fix.latitude, fix.longitude)
            }
            if (updatedMission != null) {
                currentMission = updatedMission
            }

            val remainingKm = updatedMission?.let { "%.1f".format(it.totalDistanceKm) } ?: "24.5"
            val etaMin = updatedMission?.estimatedArrivalMinutes ?: 25
            val payload = "RTK FIX: ${fix.fixQuality.name} | أقمار: ${fix.satellitesTracked} | HDOP: ${fix.hdop} | سرعة: ${fix.speedKmh.toInt()} كم/س | المسار: KKIA-CARGO -> KAFD-CTR | المتبقي: $remainingKm كم ($etaMin د)"

            emit(HardwareTelemetryFrame(peripheral, "ACTIVE_STREAMING", payload))
        }
    }
}

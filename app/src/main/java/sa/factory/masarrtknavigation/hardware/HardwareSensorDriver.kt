package sa.factory.masarrtknavigation.hardware

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

data class HardwareTelemetryFrame(
    val peripheralName: String,
    val connectionState: String,
    val livePayload: String,
    val timestamp: Long = System.currentTimeMillis()
)

class HardwareSensorDriver {
    private val peripheral = "GPS RTK High-Precision Receiver + Gyroscope"

    fun observePeripheralStream(): Flow<HardwareTelemetryFrame> = flow {
        emit(HardwareTelemetryFrame(peripheral, "CONNECTING", "Handshake initializing..."))
        delay(300)
        emit(HardwareTelemetryFrame(peripheral, "ONLINE", "RTK FIX 3D | Lat: 24.7136 N, Lon: 46.6753 E | Gyro Pitch: -12.4 deg, Roll: +2.1 deg, Azimuth: 142.8 deg"))

        while (true) {
            delay(1500)
            val jitter = Random.nextInt(1, 10)
            emit(HardwareTelemetryFrame(peripheral, "ACTIVE_STREAMING", "RTK FIX 3D | Lat: 24.7136 N, Lon: 46.6753 E | Gyro Pitch: -12.4 deg, Roll: +2.1 deg, Azimuth: 142.8 deg | Frame #${System.currentTimeMillis() % 10000} (Jitter: +${jitter}ms)"))
        }
    }
}

package sa.factory.masarrtknavigation

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import sa.factory.masarrtknavigation.data.repository.MasarRtkRepositoryImpl
import sa.factory.masarrtknavigation.engine.DynamicFleetDispatchEngine
import sa.factory.masarrtknavigation.engine.MissionDispatchStatus
import sa.factory.masarrtknavigation.engine.RtkCarrierPhaseProtocolEngine
import sa.factory.masarrtknavigation.engine.RtkFixQuality
import sa.factory.masarrtknavigation.hardware.HardwareSensorDriver
import sa.factory.masarrtknavigation.model.MasarRtkItem

class MasarRtkRegressionTest {

    @Test
    fun testCheckoutCalculationAndTax() = runBlocking {
        val sensorDriver = HardwareSensorDriver()
        val repository = MasarRtkRepositoryImpl(sensorDriver)

        val item1 = MasarRtkItem("t1", "خدمة الملاحة الدقيقة أ", "الوصف", 100.0, "معتمد", "عام")
        val item2 = MasarRtkItem("t2", "خدمة الملاحة الدقيقة ب", "الوصف", 200.0, "معتمد", "عام")

        repository.addToCart(item1)
        repository.addToCart(item2)

        val order = repository.checkout("Mada EMV")

        assertEquals(300.0, order.subtotalSar, 0.01)
        assertEquals(45.0, order.vatSar, 0.01)
        assertEquals(345.0, order.totalSar, 0.01)
        assertEquals(0, repository.activeCart.value.size)
    }

    @Test
    fun testNmeaDegreesCoordinateConversion() {
        val engine = RtkCarrierPhaseProtocolEngine()
        val lat = engine.parseNmeaDegrees("2442.8160", "N")
        val lon = engine.parseNmeaDegrees("04640.5180", "E")
        val lonWest = engine.parseNmeaDegrees("04640.5180", "W")

        assertEquals(24.7136, lat, 0.0001)
        assertEquals(46.6753, lon, 0.0001)
        assertEquals(-46.6753, lonWest, 0.0001)
    }

    @Test
    fun testGgaSentenceParsingAndFixQuality() {
        val engine = RtkCarrierPhaseProtocolEngine()
        val gga = "$GNGGA,120530.00,2442.8160,N,04640.5180,E,4,28,0.72,612.4,M,-21.2,M,1.2,0042*48"
        val fix = engine.parseGgaSentence(gga)

        assertNotNull(fix)
        assertEquals(RtkFixQuality.RTK_FIXED, fix!!.fixQuality)
        assertEquals(28, fix.satellitesTracked)
        assertEquals(0.72, fix.hdop, 0.01)
        assertEquals(612.4, fix.altitudeMslMeters, 0.1)
        assertTrue(engine.isHighPrecisionFixed())
    }

    @Test
    fun testRtkFixedCarrierPhaseAmbiguity() {
        val engine = RtkCarrierPhaseProtocolEngine()
        val fix = engine.generateSimulatedRtkFix(lat = 24.7136, lon = 46.6753, speed = 65.0)

        assertEquals(RtkFixQuality.RTK_FIXED, fix.fixQuality)
        assertEquals(1.8, fix.fixQuality.typicalAccuracyCm, 0.01)
        assertTrue(fix.ambiguityRatio > 10.0)
        assertEquals(31, fix.satellitesTracked)
    }

    @Test
    fun testHaversineDistanceCalculationAccuracy() {
        val fleet = DynamicFleetDispatchEngine()
        val dist = fleet.calculateDistanceKm(24.9576, 46.6988, 24.7645, 46.6432)

        assertTrue(dist in 20.0..25.0)
    }

    @Test
    fun testBearingAngleCalculation() {
        val fleet = DynamicFleetDispatchEngine()
        val bearing = fleet.calculateBearingDegrees(24.9576, 46.6988, 24.7645, 46.6432)

        assertTrue(bearing in 180.0..210.0)
    }

    @Test
    fun testFleetDispatchWaypointRoster() {
        val fleet = DynamicFleetDispatchEngine()
        val hubs = fleet.getHubs()

        assertEquals(4, hubs.size)
        assertNotNull(fleet.findHubById("KKIA-CARGO"))
        assertNotNull(fleet.findHubById("DRY-PORT"))
        assertNotNull(fleet.findHubById("SULAY-HUB"))
        assertNotNull(fleet.findHubById("KAFD-CTR"))
    }

    @Test
    fun testMissionDispatchLifecycle() {
        val fleet = DynamicFleetDispatchEngine()
        val mission = fleet.dispatchMission("TRK-881", "KKIA-CARGO", "KAFD-CTR")

        assertNotNull(mission)
        assertEquals("TRK-881", mission!!.vehicleId)
        assertEquals(MissionDispatchStatus.IN_TRANSIT, mission.status)
        assertTrue(mission.totalDistanceKm > 20.0)
        assertTrue(mission.estimatedArrivalMinutes > 0)
    }

    @Test
    fun testMissionProgressAndArrivalTrigger() {
        val fleet = DynamicFleetDispatchEngine()
        val mission = fleet.dispatchMission("TRK-900", "KKIA-CARGO", "KAFD-CTR")
        assertNotNull(mission)

        val dest = fleet.findHubById("KAFD-CTR")!!
        val arrived = fleet.updateMissionProgress(mission!!.missionId, dest.latitude, dest.longitude)

        assertNotNull(arrived)
        assertEquals(MissionDispatchStatus.DELIVERED, arrived!!.status)
        assertTrue(arrived.remainingDistanceKm < 0.05)
    }

    @Test
    fun testHardwareSensorDriverLiveStreamFrames() = runBlocking {
        val driver = HardwareSensorDriver()
        assertNotNull(driver.getRtkEngine())
        assertNotNull(driver.getDispatchEngine())
        assertNotNull(driver.getCurrentMission())

        val firstFrame = driver.observePeripheralStream().first()
        assertEquals("GPS RTK High-Precision Receiver + Gyroscope", firstFrame.peripheralName)
        assertEquals("CONNECTING", firstFrame.connectionState)
    }
}

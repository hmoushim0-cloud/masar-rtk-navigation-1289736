package sa.factory.masarrtknavigation.engine

import kotlin.math.*

enum class MissionDispatchStatus {
    ASSIGNED,
    IN_TRANSIT,
    DELIVERED,
    ROUTE_DEVIATION
}

data class LogisticsWaypoint(
    val hubId: String,
    val hubName: String,
    val latitude: Double,
    val longitude: Double,
    val hubType: String
)

data class FleetDispatchMission(
    val missionId: String,
    val vehicleId: String,
    val originHubId: String,
    val destinationHubId: String,
    val totalDistanceKm: Double,
    val remainingDistanceKm: Double,
    val estimatedArrivalMinutes: Int,
    val status: MissionDispatchStatus = MissionDispatchStatus.ASSIGNED
)

class DynamicFleetDispatchEngine {
    private val hubs = listOf(
        LogisticsWaypoint("KKIA-CARGO", "قرية الشحن الجوي بمطار الملك خالد", 24.9576, 46.6988, "مطار دولي"),
        LogisticsWaypoint("DRY-PORT", "الميناء الجاف بالرياض", 24.6612, 46.7489, "ميناء لوجستي"),
        LogisticsWaypoint("SULAY-HUB", "المنطقة اللوجستية بالسلي", 24.5880, 46.8520, "مستودعات مركزية"),
        LogisticsWaypoint("KAFD-CTR", "مركز توزيع مركز الملك عبد الله المالي", 24.7645, 46.6432, "توصيل ذكي")
    )

    private val activeMissions = mutableListOf<FleetDispatchMission>()

    fun getHubs(): List<LogisticsWaypoint> = hubs
    fun getActiveMissions(): List<FleetDispatchMission> = activeMissions.toList()

    fun findHubById(id: String): LogisticsWaypoint? = hubs.find { it.hubId == id }

    // Geodesic Haversine distance formula
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2.0).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2.0).pow(2)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return r * c
    }

    // Initial bearing in degrees (0 to 360)
    fun calculateBearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)
        return (Math.toDegrees(theta) + 360.0) % 360.0
    }

    fun dispatchMission(vehicleId: String, originId: String, destId: String): FleetDispatchMission? {
        val origin = findHubById(originId) ?: return null
        val dest = findHubById(destId) ?: return null

        val distance = calculateDistanceKm(origin.latitude, origin.longitude, dest.latitude, dest.longitude)
        // Average commercial fleet speed in metropolitan corridors ~ 60 km/h
        val etaMinutes = max(5, ((distance / 60.0) * 60.0).toInt())

        val mission = FleetDispatchMission(
            missionId = "MSN-${System.currentTimeMillis() % 100000}",
            vehicleId = vehicleId,
            originHubId = originId,
            destinationHubId = destId,
            totalDistanceKm = distance,
            remainingDistanceKm = distance,
            estimatedArrivalMinutes = etaMinutes,
            status = MissionDispatchStatus.IN_TRANSIT
        )
        activeMissions.add(mission)
        return mission
    }

    fun updateMissionProgress(missionId: String, currentLat: Double, currentLon: Double): FleetDispatchMission? {
        val index = activeMissions.indexOfFirst { it.missionId == missionId }
        if (index == -1) return null

        val mission = activeMissions[index]
        val dest = findHubById(mission.destinationHubId) ?: return null

        val remaining = calculateDistanceKm(currentLat, currentLon, dest.latitude, dest.longitude)
        val newStatus = if (remaining <= 0.05) { // Within 50 meters
            MissionDispatchStatus.DELIVERED
        } else {
            MissionDispatchStatus.IN_TRANSIT
        }
        val eta = max(0, ((remaining / 60.0) * 60.0).toInt())

        val updated = mission.copy(
            remainingDistanceKm = remaining,
            estimatedArrivalMinutes = eta,
            status = newStatus
        )
        activeMissions[index] = updated
        return updated
    }
}

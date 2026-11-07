package sa.factory.masarrtknavigation.engine

enum class RtkFixQuality(val code: Int, val description: String, val typicalAccuracyCm: Double) {
    NO_FIX(0, "لا توجد إشارة", 1000.0),
    GPS_SPS(1, "تحديد قياسي SPS", 300.0),
    DGPS(2, "نظام تفاضلي DGPS", 80.0),
    RTK_FIXED(4, "طور حامل ثابت RTK Fixed", 1.8),
    RTK_FLOAT(5, "طور حامل عائم RTK Float", 18.0),
    DEAD_RECKONING(6, "تقدير مسار ميت DR", 250.0)
}

data class GnssPositionFix(
    val latitude: Double,
    val longitude: Double,
    val altitudeMslMeters: Double,
    val fixQuality: RtkFixQuality,
    val satellitesTracked: Int,
    val hdop: Double,
    val speedKmh: Double,
    val headingDegrees: Double,
    val differentialAgeSeconds: Double,
    val ambiguityRatio: Double,
    val timestampUtc: String
)

class RtkCarrierPhaseProtocolEngine {
    private var currentFixQuality: RtkFixQuality = RtkFixQuality.RTK_FIXED
    private var baseStationId: String = "BASE-RUH-KAFD-01"
    private var baselineDistanceKm: Double = 4.2

    fun getFixQuality(): RtkFixQuality = currentFixQuality
    fun getBaseStationId(): String = baseStationId
    fun getBaselineDistanceKm(): Double = baselineDistanceKm

    fun parseNmeaDegrees(rawDmm: String, direction: String): Double {
        if (rawDmm.isBlank()) return 0.0
        val dotIndex = rawDmm.indexOf('.')
        if (dotIndex < 2) return 0.0

        val degDigits = dotIndex - 2
        val degrees = rawDmm.substring(0, degDigits).toDoubleOrNull() ?: 0.0
        val minutes = rawDmm.substring(degDigits).toDoubleOrNull() ?: 0.0
        var decimal = degrees + (minutes / 60.0)

        if (direction.equals("S", ignoreCase = true) || direction.equals("W", ignoreCase = true)) {
            decimal = -decimal
        }
        return decimal
    }

    fun parseGgaSentence(nmeaSentence: String): GnssPositionFix? {
        // Example: $GNGGA,120530.00,2442.8160,N,04640.5180,E,4,28,0.72,612.4,M,-21.2,M,1.2,0042*48
        if (!nmeaSentence.startsWith("$GNGGA") && !nmeaSentence.startsWith("$GPGGA")) {
            return null
        }
        val parts = nmeaSentence.split(",")
        if (parts.size < 14) return null

        val utc = parts[1]
        val lat = parseNmeaDegrees(parts[2], parts[3])
        val lon = parseNmeaDegrees(parts[4], parts[5])
        val qualityCode = parts[6].toIntOrNull() ?: 0
        val sats = parts[7].toIntOrNull() ?: 0
        val hdop = parts[8].toDoubleOrNull() ?: 1.0
        val alt = parts[9].toDoubleOrNull() ?: 0.0
        val diffAge = parts[13].substringBefore("*").toDoubleOrNull() ?: 1.0

        val quality = when (qualityCode) {
            4 -> RtkFixQuality.RTK_FIXED
            5 -> RtkFixQuality.RTK_FLOAT
            2 -> RtkFixQuality.DGPS
            1 -> RtkFixQuality.GPS_SPS
            6 -> RtkFixQuality.DEAD_RECKONING
            else -> RtkFixQuality.NO_FIX
        }
        currentFixQuality = quality

        val ambiguity = if (quality == RtkFixQuality.RTK_FIXED) 12.4 else 2.1

        return GnssPositionFix(
            latitude = lat,
            longitude = lon,
            altitudeMslMeters = alt,
            fixQuality = quality,
            satellitesTracked = sats,
            hdop = hdop,
            speedKmh = 0.0,
            headingDegrees = 0.0,
            differentialAgeSeconds = diffAge,
            ambiguityRatio = ambiguity,
            timestampUtc = utc
        )
    }

    fun isHighPrecisionFixed(): Boolean {
        return currentFixQuality == RtkFixQuality.RTK_FIXED
    }

    fun generateSimulatedRtkFix(
        lat: Double = 24.7136,
        lon: Double = 46.6753,
        speed: Double = 48.5
    ): GnssPositionFix {
        return GnssPositionFix(
            latitude = lat,
            longitude = lon,
            altitudeMslMeters = 612.8,
            fixQuality = RtkFixQuality.RTK_FIXED,
            satellitesTracked = 31,
            hdop = 0.65,
            speedKmh = speed,
            headingDegrees = 42.0,
            differentialAgeSeconds = 0.8,
            ambiguityRatio = 14.8,
            timestampUtc = "190500.00"
        )
    }
}

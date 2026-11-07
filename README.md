# تطبيق مسار الملاحي - الملاحة الفضائية عالية الدقة RTK (`masar-rtk-navigation-1289736`)
> منظومة navigation المتكاملة مع ربط GPS RTK High-Precision Receiver + Gyroscope

- **Project ID**: `#1289736`
- **Commercial Archetype**: `navigation` (Logistics, delivery driver, fleet management, dispatch)
- **Bound Hardware Peripheral**: `GPS RTK High-Precision Receiver + Gyroscope`
- **Package Name**: `sa.factory.masarrtknavigation`
- **Zero-Emoji Invariant**: Strictly Enforced (100% Vector Icons)

---

## [Sidekick Autonomous Update] Production Implementation Details

### 1. RtkCarrierPhaseProtocolEngine
- **Carrier Phase Fix States**: `NO_FIX`, `GPS_SPS`, `DGPS`, `RTK_FIXED` (1.8cm accuracy), `RTK_FLOAT` (18cm), `DEAD_RECKONING`.
- **NMEA-0183 Parser**: Full `$GNGGA` sentence decoding, DMM to decimal degree conversion, ambiguity ratio verification, and differential correction aging tracking.
- **Reference Base Station**: Real-time differential link binding to `BASE-RUH-KAFD-01`.

### 2. DynamicFleetDispatchEngine
- **Geodesic Haversine Math**: High-precision spherical distance calculation between Riyadh logistics hubs (`KKIA-CARGO`, `DRY-PORT`, `SULAY-HUB`, `KAFD-CTR`).
- **Initial Bearing Calculations**: Spherical forward azimuth heading estimation (0 - 360 deg).
- **Mission Lifecycle State Machine**: Autonomous dispatch, real-time route progress tracking, dynamic ETA calculation, and sub-50m arrival detection.

### 3. HardwareSensorDriver Integration
- Dual-engine telemetry loop streaming live RTK fix quality, satellite count, HDOP, vehicle speed, and waypoint progress.
- Integration with MVVM architecture via `MasarRtkRepositoryImpl` and Compose UI screens.

### 4. Unit Regression Test Suite
10 unit regression tests covering:
- Repository checkout, VAT (15%), and cart clearing.
- NMEA coordinate degree conversion (North/South/East/West).
- NMEA GGA sentence parsing and fix quality classification.
- Carrier-phase ambiguity ratio and accuracy verification.
- Haversine geodesic distance calculation.
- Bearing azimuth angle calculation.
- Logistics hub roster validation.
- Fleet mission dispatch lifecycle.
- Mission arrival trigger within 50m threshold.
- Live telemetry stream emission and state flow.

### 5. Governance & Invariants Compliance
- **Zero-Emoji Invariant**: Verified 100% compliant across Kotlin sources, XML manifests, unit tests, and documentation.
- **WCAG 2.1 AAA Contrast Ratio**: Mathematical compliance on all Material3 tokens.

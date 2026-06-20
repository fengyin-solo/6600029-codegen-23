export interface Waypoint {
  id: string;
  lat: number;
  lng: number;
  altitude: number;   // meters AGL
  speed: number;      // m/s
  action: 'hover' | 'photo' | 'video' | 'none';
}

export interface FlightPlan {
  id: string;
  name: string;
  waypoints: Waypoint[];
  totalDistance: number;
  estimatedTime: number;
  batteryUsage: number;  // percentage
}

export interface NoFlyZone {
  id: string;
  name: string;
  center: [number, number];
  radius: number;  // meters
  type: 'airport' | 'military' | 'restricted';
}

export interface TerrainPoint {
  lat: number;
  lng: number;
  elevation: number;
}

export interface DroneConfig {
  maxAltitude: number;
  maxSpeed: number;
  batteryCapacity: number;  // mAh
  consumptionRate: number;  // mAh/min
  safeDistance: number;     // meters from obstacles
  cameraFov: number;        // horizontal field of view in degrees
}

// ─── Coverage Check ──────────────────────────────────────────────────────────
export interface CoverageFootprint {
  waypointId: string;
  waypointIndex: number;
  lat: number;
  lng: number;
  altitude: number;
  radius: number;  // ground footprint radius in meters
  action: Waypoint['action'];
}

export interface ReflightSegment {
  fromIndex: number;
  toIndex: number;
  fromId: string;
  toId: string;
  from: [number, number];
  to: [number, number];
  distance: number;  // segment length in meters
}

export interface CoverageGap {
  id: string;
  centroidLat: number;
  centroidLng: number;
  sampleCount: number;
  approxArea: number;       // m²
  distanceToRoute: number;  // meters from gap centroid to nearest segment
  reflight: ReflightSegment;
  message: string;
}

export interface CoverageResult {
  coveragePercent: number;
  totalSamples: number;
  coveredSamples: number;
  uncoveredSamples: number;
  footprintCount: number;
  targetArea: { minLat: number; maxLat: number; minLng: number; maxLng: number };
  footprints: CoverageFootprint[];
  gaps: CoverageGap[];
}

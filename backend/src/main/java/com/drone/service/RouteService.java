package com.drone.service;

import com.drone.model.CoverageGap;
import com.drone.model.CoverageResult;
import com.drone.model.Waypoint;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RouteService {

    // ─── A* Pathfinding (simplified server-side) ────────────────────────────
    public List<Waypoint> planRoute(double startLat, double startLng,
                                     double goalLat, double goalLng,
                                     String algorithm) {
        List<double[]> noFlyZones = getMockNoFlyZoneCoords();
        List<Waypoint> path = new ArrayList<>();

        // Simple grid-based A*
        int gridSize = 20;
        double minLat = Math.min(startLat, goalLat) - 0.02;
        double maxLat = Math.max(startLat, goalLat) + 0.02;
        double minLng = Math.min(startLng, goalLng) - 0.02;
        double maxLng = Math.max(startLng, goalLng) + 0.02;
        double dLat = (maxLat - minLat) / gridSize;
        double dLng = (maxLng - minLng) / gridSize;

        int startRow = (int) ((startLat - minLat) / dLat);
        int startCol = (int) ((startLng - minLng) / dLng);
        int goalRow = (int) ((goalLat - minLat) / dLat);
        int goalCol = (int) ((goalLng - minLng) / dLng);

        // Clamp
        startRow = Math.max(0, Math.min(gridSize - 1, startRow));
        startCol = Math.max(0, Math.min(gridSize - 1, startCol));
        goalRow = Math.max(0, Math.min(gridSize - 1, goalRow));
        goalCol = Math.max(0, Math.min(gridSize - 1, goalCol));

        int[][] g = new int[gridSize][gridSize];
        int[][] parent = new int[gridSize][gridSize];
        for (int[] row : g) Arrays.fill(row, Integer.MAX_VALUE);
        for (int[] row : parent) Arrays.fill(row, -1);

        boolean[][] blocked = new boolean[gridSize][gridSize];
        for (double[] zone : noFlyZones) {
            for (int r = 0; r < gridSize; r++) {
                for (int c = 0; c < gridSize; c++) {
                    double lat = minLat + r * dLat;
                    double lng = minLng + c * dLng;
                    double dist = haversine(lat, lng, zone[0], zone[1]);
                    if (dist < zone[2]) blocked[r][c] = true;
                }
            }
        }

        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[2] - b[2]);
        g[startRow][startCol] = 0;
        pq.offer(new int[]{startRow, startCol, heuristic(startRow, startCol, goalRow, goalCol)});

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}};

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int cr = curr[0], cc = curr[1];

            if (cr == goalRow && cc == goalCol) break;

            for (int[] d : dirs) {
                int nr = cr + d[0], nc = cc + d[1];
                if (nr < 0 || nr >= gridSize || nc < 0 || nc >= gridSize) continue;
                if (blocked[nr][nc]) continue;

                int cost = (d[0] != 0 && d[1] != 0) ? 14 : 10;
                int newG = g[cr][cc] + cost;
                if (newG < g[nr][nc]) {
                    g[nr][nc] = newG;
                    parent[nr][nc] = cr * gridSize + cc;
                    int h = heuristic(nr, nc, goalRow, goalCol);
                    pq.offer(new int[]{nr, nc, newG + h});
                }
            }
        }

        // Trace path
        List<int[]> rawPath = new ArrayList<>();
        int r = goalRow, c = goalCol;
        while (r != startRow || c != startCol) {
            rawPath.add(0, new int[]{r, c});
            int p = parent[r][c];
            if (p == -1) break;
            r = p / gridSize;
            c = p % gridSize;
        }
        rawPath.add(0, new int[]{startRow, startCol});

        int idx = 0;
        for (int[] p : rawPath) {
            double lat = minLat + p[0] * dLat;
            double lng = minLng + p[1] * dLng;
            path.add(new Waypoint("wp-" + idx++, lat, lng, 100, 10, "none"));
        }

        if (path.isEmpty()) {
            path.add(new Waypoint("wp-0", startLat, startLng, 100, 10, "none"));
            path.add(new Waypoint("wp-1", goalLat, goalLng, 100, 10, "none"));
        }

        return path;
    }

    private int heuristic(int r1, int c1, int r2, int c2) {
        return (int) (Math.sqrt((r1 - r2) * (r1 - r2) + (c1 - c2) * (c1 - c2)) * 10);
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ─── Mock Data ──────────────────────────────────────────────────────────
    public List<Map<String, Object>> getNoFlyZones() {
        List<Map<String, Object>> zones = new ArrayList<>();
        zones.add(Map.of("id", "nfz-1", "name", "首都国际机场",
                "center", List.of(40.0799, 116.6031), "radius", 8000, "type", "airport"));
        zones.add(Map.of("id", "nfz-2", "name", "南苑军事区",
                "center", List.of(39.7833, 116.3833), "radius", 5000, "type", "military"));
        zones.add(Map.of("id", "nfz-3", "name", "中南海限制区",
                "center", List.of(39.9139, 116.3741), "radius", 3000, "type", "restricted"));
        return zones;
    }

    private List<double[]> getMockNoFlyZoneCoords() {
        return List.of(
            new double[]{40.0799, 116.6031, 8000},
            new double[]{39.7833, 116.3833, 5000},
            new double[]{39.9139, 116.3741, 3000}
        );
    }

    public List<Map<String, Object>> getTerrain() {
        List<Map<String, Object>> terrain = new ArrayList<>();
        double baseLat = 39.85;
        double baseLng = 116.35;
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                double lat = baseLat + i * 0.005;
                double lng = baseLng + j * 0.005;
                double elevation = 50 +
                    30 * Math.sin(i * 0.5) * Math.cos(j * 0.4) +
                    20 * Math.sin(i * 0.3 + j * 0.2) +
                    10 * Math.cos(i * 0.7 - j * 0.5);
                terrain.add(Map.of("lat", lat, "lng", lng, "elevation", elevation));
            }
        }
        return terrain;
    }

    public String exportKML(List<Waypoint> waypoints, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n  <Document>\n");
        sb.append("    <name>").append(name).append("</name>\n");
        sb.append("    <Placemark>\n      <name>Flight Route</name>\n");
        sb.append("      <LineString>\n        <altitudeMode>absolute</altitudeMode>\n");
        sb.append("        <coordinates>\n");
        for (Waypoint w : waypoints) {
            sb.append("          ").append(w.getLng()).append(",").append(w.getLat())
              .append(",").append(w.getAltitude()).append("\n");
        }
        sb.append("        </coordinates>\n      </LineString>\n    </Placemark>\n");
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint w = waypoints.get(i);
            sb.append("    <Placemark>\n      <name>WP").append(i + 1).append("</name>\n");
            sb.append("      <Point><coordinates>").append(w.getLng()).append(",")
              .append(w.getLat()).append(",").append(w.getAltitude())
              .append("</coordinates></Point>\n    </Placemark>\n");
        }
        sb.append("  </Document>\n</kml>");
        return sb.toString();
    }

    // ─── Shooting Coverage Check ───────────────────────────────────────────
    public CoverageResult checkCoverage(List<Waypoint> waypoints, double cameraFov, int resolution) {
        if (waypoints == null || waypoints.isEmpty()) {
            return new CoverageResult(0, 0, 0, 0, 0, 0, 0, 0, 0, new ArrayList<>());
        }
        if (resolution < 4) resolution = 4;

        // Target bounding box from all waypoints
        double minLat = Double.POSITIVE_INFINITY, maxLat = Double.NEGATIVE_INFINITY;
        double minLng = Double.POSITIVE_INFINITY, maxLng = Double.NEGATIVE_INFINITY;
        for (Waypoint w : waypoints) {
            minLat = Math.min(minLat, w.getLat());
            maxLat = Math.max(maxLat, w.getLat());
            minLng = Math.min(minLng, w.getLng());
            maxLng = Math.max(maxLng, w.getLng());
        }
        double spanLat = (maxLat - minLat) == 0 ? 0.01 : (maxLat - minLat);
        double spanLng = (maxLng - minLng) == 0 ? 0.01 : (maxLng - minLng);
        double marginLat = Math.max(spanLat * 0.1, 0.003);
        double marginLng = Math.max(spanLng * 0.1, 0.003);
        minLat -= marginLat;
        maxLat += marginLat;
        minLng -= marginLng;
        maxLng += marginLng;

        // Ground footprints for shooting waypoints (photo / video only)
        double fov = cameraFov > 0 ? cameraFov : 75;
        double tanHalf = Math.tan(Math.toRadians(fov) / 2.0);
        List<double[]> footprints = new ArrayList<>();
        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint w = waypoints.get(i);
            String action = w.getAction();
            if (!"photo".equals(action) && !"video".equals(action)) continue;
            double radius = Math.max(1, w.getAltitude() * tanHalf);
            footprints.add(new double[]{w.getLat(), w.getLng(), radius, i});
        }

        // Grid-sample target area and test coverage
        int rows = resolution, cols = resolution;
        double dLat = (maxLat - minLat) / rows;
        double dLng = (maxLng - minLng) / cols;
        double cellArea = dLat * 110540.0 * dLng * 111320.0 * Math.cos(Math.toRadians(minLat));

        boolean[][] uncovered = new boolean[rows][cols];
        int total = 0, covered = 0;
        for (int i = 0; i < rows; i++) {
            double lat = minLat + (i + 0.5) * dLat;
            for (int j = 0; j < cols; j++) {
                double lng = minLng + (j + 0.5) * dLng;
                total++;
                boolean isCovered = false;
                for (double[] f : footprints) {
                    if (haversine(lat, lng, f[0], f[1]) <= f[2]) {
                        isCovered = true;
                        break;
                    }
                }
                if (isCovered) covered++;
                else uncovered[i][j] = true;
            }
        }

        // Cluster uncovered samples into gaps (4-connected flood fill)
        boolean[][] visited = new boolean[rows][cols];
        List<CoverageGap> gaps = new ArrayList<>();
        int gapIdx = 0;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!uncovered[i][j] || visited[i][j]) continue;
                Deque<int[]> queue = new ArrayDeque<>();
                queue.push(new int[]{i, j});
                visited[i][j] = true;
                int count = 0;
                double sumLat = 0, sumLng = 0;
                while (!queue.isEmpty()) {
                    int[] cur = queue.pop();
                    int ci = cur[0], cj = cur[1];
                    count++;
                    sumLat += minLat + (ci + 0.5) * dLat;
                    sumLng += minLng + (cj + 0.5) * dLng;
                    for (int[] d : dirs) {
                        int ni = ci + d[0], nj = cj + d[1];
                        if (ni < 0 || ni >= rows || nj < 0 || nj >= cols) continue;
                        if (visited[ni][nj] || !uncovered[ni][nj]) continue;
                        visited[ni][nj] = true;
                        queue.push(new int[]{ni, nj});
                    }
                }
                double centroidLat = sumLat / count;
                double centroidLng = sumLng / count;
                double approxArea = count * cellArea;
                double[] nearest = findNearestSegment(waypoints, centroidLat, centroidLng);
                int fromIndex = (int) nearest[0];
                int toIndex = (int) nearest[1];
                double distanceToRoute = nearest[2];
                double segmentDistance = nearest[3];
                Waypoint a = waypoints.get(fromIndex);
                Waypoint b = waypoints.get(toIndex);
                String segLabel = fromIndex == toIndex
                        ? "WP" + (fromIndex + 1)
                        : "WP" + (fromIndex + 1) + "→WP" + (toIndex + 1);
                String message = String.format(
                        "%s 附近存在约 %s 漏拍，距航线 %.0f m，建议补飞该区段",
                        segLabel, formatAreaReadable(approxArea), distanceToRoute
                );
                gaps.add(new CoverageGap(
                        "gap-" + (gapIdx++),
                        centroidLat, centroidLng, count, approxArea, distanceToRoute,
                        fromIndex, toIndex, a.getId(), b.getId(),
                        a.getLat(), a.getLng(), b.getLat(), b.getLng(),
                        segmentDistance, message
                ));
            }
        }

        gaps.sort((g1, g2) -> Double.compare(g2.getApproxArea(), g1.getApproxArea()));
        double coveragePercent = total > 0 ? (covered * 100.0 / total) : 0;
        return new CoverageResult(
                coveragePercent, total, covered, total - covered, footprints.size(),
                minLat, maxLat, minLng, maxLng, gaps
        );
    }

    private double[] toPlanar(double lat, double lng, double refLat) {
        double refLatRad = Math.toRadians(refLat);
        double x = lng * 111320.0 * Math.cos(refLatRad);
        double y = lat * 110540.0;
        return new double[]{x, y};
    }

    private double pointToSegmentDistance(double pLat, double pLng,
                                          double aLat, double aLng,
                                          double bLat, double bLng) {
        double[] p = toPlanar(pLat, pLng, pLat);
        double[] a = toPlanar(aLat, aLng, pLat);
        double[] b = toPlanar(bLat, bLng, pLat);
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double segLenSq = dx * dx + dy * dy;
        double t = 0;
        if (segLenSq > 0) {
            t = ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / segLenSq;
            t = Math.max(0, Math.min(1, t));
        }
        double cx = a[0] + t * dx;
        double cy = a[1] + t * dy;
        return Math.sqrt((p[0] - cx) * (p[0] - cx) + (p[1] - cy) * (p[1] - cy));
    }

    // Returns {fromIndex, toIndex, distanceToSegment, segmentDistance}
    private double[] findNearestSegment(List<Waypoint> waypoints, double lat, double lng) {
        int n = waypoints.size();
        if (n == 1) {
            Waypoint w = waypoints.get(0);
            return new double[]{0, 0, haversine(lat, lng, w.getLat(), w.getLng()), 0};
        }
        double best = Double.POSITIVE_INFINITY;
        int bestFrom = 0, bestTo = 0;
        double bestSegDist = 0;
        for (int i = 0; i < n - 1; i++) {
            Waypoint a = waypoints.get(i);
            Waypoint b = waypoints.get(i + 1);
            double dist = pointToSegmentDistance(lat, lng, a.getLat(), a.getLng(), b.getLat(), b.getLng());
            if (dist < best) {
                best = dist;
                bestFrom = i;
                bestTo = i + 1;
                bestSegDist = haversine(a.getLat(), a.getLng(), b.getLat(), b.getLng());
            }
        }
        return new double[]{bestFrom, bestTo, best, bestSegDist};
    }

    private String formatAreaReadable(double m2) {
        if (m2 >= 1000000) return String.format("%.2f km²", m2 / 1000000.0);
        if (m2 >= 1000) return String.format("%.1f 千 m²", m2 / 1000.0);
        return String.format("%.0f m²", m2);
    }
}

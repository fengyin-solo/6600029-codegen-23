package com.drone.model;

import java.util.List;

public class CoverageResult {
    private double coveragePercent;
    private int totalSamples;
    private int coveredSamples;
    private int uncoveredSamples;
    private int footprintCount;
    private double minLat;
    private double maxLat;
    private double minLng;
    private double maxLng;
    private List<CoverageGap> gaps;

    public CoverageResult() {}

    public CoverageResult(double coveragePercent, int totalSamples, int coveredSamples,
                          int uncoveredSamples, int footprintCount,
                          double minLat, double maxLat, double minLng, double maxLng,
                          List<CoverageGap> gaps) {
        this.coveragePercent = coveragePercent;
        this.totalSamples = totalSamples;
        this.coveredSamples = coveredSamples;
        this.uncoveredSamples = uncoveredSamples;
        this.footprintCount = footprintCount;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLng = minLng;
        this.maxLng = maxLng;
        this.gaps = gaps;
    }

    public double getCoveragePercent() { return coveragePercent; }
    public void setCoveragePercent(double coveragePercent) { this.coveragePercent = coveragePercent; }
    public int getTotalSamples() { return totalSamples; }
    public void setTotalSamples(int totalSamples) { this.totalSamples = totalSamples; }
    public int getCoveredSamples() { return coveredSamples; }
    public void setCoveredSamples(int coveredSamples) { this.coveredSamples = coveredSamples; }
    public int getUncoveredSamples() { return uncoveredSamples; }
    public void setUncoveredSamples(int uncoveredSamples) { this.uncoveredSamples = uncoveredSamples; }
    public int getFootprintCount() { return footprintCount; }
    public void setFootprintCount(int footprintCount) { this.footprintCount = footprintCount; }
    public double getMinLat() { return minLat; }
    public void setMinLat(double minLat) { this.minLat = minLat; }
    public double getMaxLat() { return maxLat; }
    public void setMaxLat(double maxLat) { this.maxLat = maxLat; }
    public double getMinLng() { return minLng; }
    public void setMinLng(double minLng) { this.minLng = minLng; }
    public double getMaxLng() { return maxLng; }
    public void setMaxLng(double maxLng) { this.maxLng = maxLng; }
    public List<CoverageGap> getGaps() { return gaps; }
    public void setGaps(List<CoverageGap> gaps) { this.gaps = gaps; }
}

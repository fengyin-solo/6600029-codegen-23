package com.drone.model;

public class CoverageGap {
    private String id;
    private double centroidLat;
    private double centroidLng;
    private int sampleCount;
    private double approxArea;
    private double distanceToRoute;
    private int fromIndex;
    private int toIndex;
    private String fromId;
    private String toId;
    private double fromLat;
    private double fromLng;
    private double toLat;
    private double toLng;
    private double segmentDistance;
    private String message;

    public CoverageGap() {}

    public CoverageGap(String id, double centroidLat, double centroidLng, int sampleCount,
                      double approxArea, double distanceToRoute,
                      int fromIndex, int toIndex, String fromId, String toId,
                      double fromLat, double fromLng, double toLat, double toLng,
                      double segmentDistance, String message) {
        this.id = id;
        this.centroidLat = centroidLat;
        this.centroidLng = centroidLng;
        this.sampleCount = sampleCount;
        this.approxArea = approxArea;
        this.distanceToRoute = distanceToRoute;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.fromId = fromId;
        this.toId = toId;
        this.fromLat = fromLat;
        this.fromLng = fromLng;
        this.toLat = toLat;
        this.toLng = toLng;
        this.segmentDistance = segmentDistance;
        this.message = message;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getCentroidLat() { return centroidLat; }
    public void setCentroidLat(double centroidLat) { this.centroidLat = centroidLat; }
    public double getCentroidLng() { return centroidLng; }
    public void setCentroidLng(double centroidLng) { this.centroidLng = centroidLng; }
    public int getSampleCount() { return sampleCount; }
    public void setSampleCount(int sampleCount) { this.sampleCount = sampleCount; }
    public double getApproxArea() { return approxArea; }
    public void setApproxArea(double approxArea) { this.approxArea = approxArea; }
    public double getDistanceToRoute() { return distanceToRoute; }
    public void setDistanceToRoute(double distanceToRoute) { this.distanceToRoute = distanceToRoute; }
    public int getFromIndex() { return fromIndex; }
    public void setFromIndex(int fromIndex) { this.fromIndex = fromIndex; }
    public int getToIndex() { return toIndex; }
    public void setToIndex(int toIndex) { this.toIndex = toIndex; }
    public String getFromId() { return fromId; }
    public void setFromId(String fromId) { this.fromId = fromId; }
    public String getToId() { return toId; }
    public void setToId(String toId) { this.toId = toId; }
    public double getFromLat() { return fromLat; }
    public void setFromLat(double fromLat) { this.fromLat = fromLat; }
    public double getFromLng() { return fromLng; }
    public void setFromLng(double fromLng) { this.fromLng = fromLng; }
    public double getToLat() { return toLat; }
    public void setToLat(double toLat) { this.toLat = toLat; }
    public double getToLng() { return toLng; }
    public void setToLng(double toLng) { this.toLng = toLng; }
    public double getSegmentDistance() { return segmentDistance; }
    public void setSegmentDistance(double segmentDistance) { this.segmentDistance = segmentDistance; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

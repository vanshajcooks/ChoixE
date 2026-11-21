package com.campusconnect.analytics.dto;

public class PlacementOverviewStats {

    private long totalStudents;
    private long verifiedStudents;
    private long placedStudents;
    private double placementPercentage; // (placedStudents / verifiedStudents) * 100
    private double overallPlacementPercentage; // (placedStudents / totalStudents) * 100
    private long totalApplications;
    private long totalOffers; // total SELECTED applications
    private long totalApprovedJobs;
    private long totalApprovedCompanies;

    public PlacementOverviewStats() {
    }

    public PlacementOverviewStats(long totalStudents, long verifiedStudents, long placedStudents,
                                  double placementPercentage, double overallPlacementPercentage,
                                  long totalApplications, long totalOffers,
                                  long totalApprovedJobs, long totalApprovedCompanies) {
        this.totalStudents = totalStudents;
        this.verifiedStudents = verifiedStudents;
        this.placedStudents = placedStudents;
        this.placementPercentage = placementPercentage;
        this.overallPlacementPercentage = overallPlacementPercentage;
        this.totalApplications = totalApplications;
        this.totalOffers = totalOffers;
        this.totalApprovedJobs = totalApprovedJobs;
        this.totalApprovedCompanies = totalApprovedCompanies;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getVerifiedStudents() {
        return verifiedStudents;
    }

    public void setVerifiedStudents(long verifiedStudents) {
        this.verifiedStudents = verifiedStudents;
    }

    public long getPlacedStudents() {
        return placedStudents;
    }

    public void setPlacedStudents(long placedStudents) {
        this.placedStudents = placedStudents;
    }

    public double getPlacementPercentage() {
        return placementPercentage;
    }

    public void setPlacementPercentage(double placementPercentage) {
        this.placementPercentage = placementPercentage;
    }

    public double getOverallPlacementPercentage() {
        return overallPlacementPercentage;
    }

    public void setOverallPlacementPercentage(double overallPlacementPercentage) {
        this.overallPlacementPercentage = overallPlacementPercentage;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getTotalOffers() {
        return totalOffers;
    }

    public void setTotalOffers(long totalOffers) {
        this.totalOffers = totalOffers;
    }

    public long getTotalApprovedJobs() {
        return totalApprovedJobs;
    }

    public void setTotalApprovedJobs(long totalApprovedJobs) {
        this.totalApprovedJobs = totalApprovedJobs;
    }

    public long getTotalApprovedCompanies() {
        return totalApprovedCompanies;
    }

    public void setTotalApprovedCompanies(long totalApprovedCompanies) {
        this.totalApprovedCompanies = totalApprovedCompanies;
    }
}

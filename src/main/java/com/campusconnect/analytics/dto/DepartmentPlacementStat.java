package com.campusconnect.analytics.dto;

public class DepartmentPlacementStat {

    private String departmentName;
    private long totalStudents;
    private long verifiedStudents;
    private long placedStudents;
    private double placementPercentage;

    public DepartmentPlacementStat() {
    }

    public DepartmentPlacementStat(String departmentName, long totalStudents, long verifiedStudents,
                                   long placedStudents, double placementPercentage) {
        this.departmentName = departmentName;
        this.totalStudents = totalStudents;
        this.verifiedStudents = verifiedStudents;
        this.placedStudents = placedStudents;
        this.placementPercentage = placementPercentage;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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
}

package com.campusconnect.analytics.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlacementAnalyticsReport {

    private PlacementOverviewStats overview;
    private List<DepartmentPlacementStat> departmentStats = new ArrayList<>();
    private List<CompanyPlacementStat> companyStats = new ArrayList<>();
    private SalaryPackageStats salaryStats;
    private LocalDateTime generatedAt;

    public PlacementAnalyticsReport() {
        this.generatedAt = LocalDateTime.now();
    }

    public PlacementAnalyticsReport(PlacementOverviewStats overview,
                                    List<DepartmentPlacementStat> departmentStats,
                                    List<CompanyPlacementStat> companyStats,
                                    SalaryPackageStats salaryStats) {
        this.overview = overview;
        this.departmentStats = departmentStats != null ? departmentStats : new ArrayList<>();
        this.companyStats = companyStats != null ? companyStats : new ArrayList<>();
        this.salaryStats = salaryStats;
        this.generatedAt = LocalDateTime.now();
    }

    public PlacementOverviewStats getOverview() {
        return overview;
    }

    public void setOverview(PlacementOverviewStats overview) {
        this.overview = overview;
    }

    public List<DepartmentPlacementStat> getDepartmentStats() {
        return departmentStats;
    }

    public void setDepartmentStats(List<DepartmentPlacementStat> departmentStats) {
        this.departmentStats = departmentStats;
    }

    public List<CompanyPlacementStat> getCompanyStats() {
        return companyStats;
    }

    public void setCompanyStats(List<CompanyPlacementStat> companyStats) {
        this.companyStats = companyStats;
    }

    public SalaryPackageStats getSalaryStats() {
        return salaryStats;
    }

    public void setSalaryStats(SalaryPackageStats salaryStats) {
        this.salaryStats = salaryStats;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}

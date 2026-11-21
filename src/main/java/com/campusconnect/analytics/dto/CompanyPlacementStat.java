package com.campusconnect.analytics.dto;

public class CompanyPlacementStat {

    private String companyName;
    private String industry;
    private long totalOffers;
    private long uniqueStudentsHired;

    public CompanyPlacementStat() {
    }

    public CompanyPlacementStat(String companyName, String industry, long totalOffers, long uniqueStudentsHired) {
        this.companyName = companyName;
        this.industry = industry;
        this.totalOffers = totalOffers;
        this.uniqueStudentsHired = uniqueStudentsHired;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public long getTotalOffers() {
        return totalOffers;
    }

    public void setTotalOffers(long totalOffers) {
        this.totalOffers = totalOffers;
    }

    public long getUniqueStudentsHired() {
        return uniqueStudentsHired;
    }

    public void setUniqueStudentsHired(long uniqueStudentsHired) {
        this.uniqueStudentsHired = uniqueStudentsHired;
    }
}

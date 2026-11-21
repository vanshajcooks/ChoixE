package com.campusconnect.analytics.dto;

public class SalaryPackageStats {

    private Double highestPackageLpa;
    private Double averagePackageLpa;
    private Double medianPackageLpa;
    private long tierCountAbove10Lpa;
    private long tierCount6To10Lpa;
    private long tierCountBelow6Lpa;
    private long totalAnalyzedOffers;

    public SalaryPackageStats() {
    }

    public SalaryPackageStats(Double highestPackageLpa, Double averagePackageLpa, Double medianPackageLpa,
                              long tierCountAbove10Lpa, long tierCount6To10Lpa, long tierCountBelow6Lpa,
                              long totalAnalyzedOffers) {
        this.highestPackageLpa = highestPackageLpa;
        this.averagePackageLpa = averagePackageLpa;
        this.medianPackageLpa = medianPackageLpa;
        this.tierCountAbove10Lpa = tierCountAbove10Lpa;
        this.tierCount6To10Lpa = tierCount6To10Lpa;
        this.tierCountBelow6Lpa = tierCountBelow6Lpa;
        this.totalAnalyzedOffers = totalAnalyzedOffers;
    }

    public Double getHighestPackageLpa() {
        return highestPackageLpa;
    }

    public void setHighestPackageLpa(Double highestPackageLpa) {
        this.highestPackageLpa = highestPackageLpa;
    }

    public Double getAveragePackageLpa() {
        return averagePackageLpa;
    }

    public void setAveragePackageLpa(Double averagePackageLpa) {
        this.averagePackageLpa = averagePackageLpa;
    }

    public Double getMedianPackageLpa() {
        return medianPackageLpa;
    }

    public void setMedianPackageLpa(Double medianPackageLpa) {
        this.medianPackageLpa = medianPackageLpa;
    }

    public long getTierCountAbove10Lpa() {
        return tierCountAbove10Lpa;
    }

    public void setTierCountAbove10Lpa(long tierCountAbove10Lpa) {
        this.tierCountAbove10Lpa = tierCountAbove10Lpa;
    }

    public long getTierCount6To10Lpa() {
        return tierCount6To10Lpa;
    }

    public void setTierCount6To10Lpa(long tierCount6To10Lpa) {
        this.tierCount6To10Lpa = tierCount6To10Lpa;
    }

    public long getTierCountBelow6Lpa() {
        return tierCountBelow6Lpa;
    }

    public void setTierCountBelow6Lpa(long tierCountBelow6Lpa) {
        this.tierCountBelow6Lpa = tierCountBelow6Lpa;
    }

    public long getTotalAnalyzedOffers() {
        return totalAnalyzedOffers;
    }

    public void setTotalAnalyzedOffers(long totalAnalyzedOffers) {
        this.totalAnalyzedOffers = totalAnalyzedOffers;
    }
}

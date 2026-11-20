package com.campusconnect.job.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EligibilityResult {

    private final boolean eligible;
    private final List<String> unmetCriteria;
    private final boolean profileVerified;

    public EligibilityResult(boolean eligible, List<String> unmetCriteria, boolean profileVerified) {
        this.eligible = eligible;
        this.unmetCriteria = unmetCriteria != null ? unmetCriteria : Collections.emptyList();
        this.profileVerified = profileVerified;
    }

    public static EligibilityResult eligible() {
        return new EligibilityResult(true, Collections.emptyList(), true);
    }

    public static EligibilityResult ineligible(List<String> reasons, boolean profileVerified) {
        return new EligibilityResult(false, reasons, profileVerified);
    }

    public static EligibilityResult unverifiedProfile(String currentStatus) {
        List<String> reasons = new ArrayList<>();
        reasons.add("Your student profile requires Placement Officer verification (Current status: " + currentStatus + ").");
        return new EligibilityResult(false, reasons, false);
    }

    public boolean isEligible() {
        return eligible;
    }

    public List<String> getUnmetCriteria() {
        return unmetCriteria;
    }

    public boolean isProfileVerified() {
        return profileVerified;
    }
}

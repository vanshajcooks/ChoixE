package com.campusconnect.job.service;

import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobEligibilityCriteria;
import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EligibilityService {

    public EligibilityResult evaluate(StudentProfile student, Job job) {
        if (student == null) {
            List<String> reasons = new ArrayList<>();
            reasons.add("Student academic profile does not exist.");
            return EligibilityResult.ineligible(reasons, false);
        }

        List<String> unmetCriteria = new ArrayList<>();
        boolean verified = (student.getVerificationStatus() == VerificationStatus.VERIFIED);

        if (!verified) {
            unmetCriteria.add("Your profile requires Placement Officer verification before you can apply for campus drives (Current status: "
                    + student.getVerificationStatus().name() + ").");
        }

        if (job == null) {
            unmetCriteria.add("Job posting not found.");
            return EligibilityResult.ineligible(unmetCriteria, verified);
        }

        JobEligibilityCriteria criteria = job.getEligibilityCriteria();
        if (criteria == null) {
            // No restrictions configured on job
            return verified ? EligibilityResult.eligible() : EligibilityResult.ineligible(unmetCriteria, false);
        }

        // 1. CGPA Check
        if (criteria.getMinCgpa() != null) {
            if (student.getCgpa() == null || student.getCgpa().compareTo(criteria.getMinCgpa()) < 0) {
                unmetCriteria.add("Your CGPA of " + (student.getCgpa() != null ? student.getCgpa() : "0.00")
                        + " is below the minimum required CGPA of " + criteria.getMinCgpa() + ".");
            }
        }

        // 2. Max Backlogs Check
        if (criteria.getMaxBacklogs() != null) {
            if (student.getActiveBacklogs() == null || student.getActiveBacklogs() > criteria.getMaxBacklogs()) {
                unmetCriteria.add("You have " + (student.getActiveBacklogs() != null ? student.getActiveBacklogs() : "unknown")
                        + " active backlog(s), which exceeds the maximum allowed " + criteria.getMaxBacklogs() + " backlog(s).");
            }
        }

        // 3. Graduation Year / Batch Check
        if (criteria.getGraduationYear() != null) {
            if (student.getGraduationYear() == null || !student.getGraduationYear().equals(criteria.getGraduationYear())) {
                unmetCriteria.add("Your graduation year (" + (student.getGraduationYear() != null ? student.getGraduationYear() : "unspecified")
                        + ") does not match the required batch (" + criteria.getGraduationYear() + ").");
            }
        }

        // 4. Eligible Branches Check
        if (criteria.getEligibleBranches() != null && !criteria.getEligibleBranches().isEmpty()) {
            if (!criteria.isBranchEligible(student.getDepartment())) {
                unmetCriteria.add("Your department/branch ('" + (student.getDepartment() != null ? student.getDepartment() : "Not specified")
                        + "') is not among the eligible branches: " + String.join(", ", criteria.getEligibleBranches()) + ".");
            }
        }

        if (unmetCriteria.isEmpty()) {
            return EligibilityResult.eligible();
        } else {
            return EligibilityResult.ineligible(unmetCriteria, verified);
        }
    }
}

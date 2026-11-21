package com.campusconnect.analytics.service;

import com.campusconnect.analytics.dto.*;
import com.campusconnect.analytics.util.SalaryPackageParser;
import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import com.campusconnect.application.repository.ApplicationRepository;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.company.repository.CompanyRepository;
import com.campusconnect.job.entity.JobStatus;
import com.campusconnect.job.repository.JobRepository;
import com.campusconnect.student.entity.VerificationStatus;
import com.campusconnect.student.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PlacementAnalyticsService {

    private final StudentProfileRepository studentProfileRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public PlacementAnalyticsService(StudentProfileRepository studentProfileRepository,
                                     ApplicationRepository applicationRepository,
                                     JobRepository jobRepository,
                                     CompanyRepository companyRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public PlacementAnalyticsReport getPlacementAnalyticsReport() {
        PlacementOverviewStats overview = getOverviewStats();
        List<DepartmentPlacementStat> departmentStats = getDepartmentStats();
        List<CompanyPlacementStat> companyStats = getCompanyStats();
        SalaryPackageStats salaryStats = getSalaryPackageStats();

        return new PlacementAnalyticsReport(overview, departmentStats, companyStats, salaryStats);
    }

    @Transactional(readOnly = true)
    public PlacementOverviewStats getOverviewStats() {
        long totalStudents = studentProfileRepository.count();
        long verifiedStudents = studentProfileRepository.countByVerificationStatus(VerificationStatus.VERIFIED);
        long placedStudents = applicationRepository.countDistinctPlacedStudents(ApplicationStatus.SELECTED);
        long totalApplications = applicationRepository.count();
        long totalOffers = applicationRepository.countByStatus(ApplicationStatus.SELECTED);
        long totalApprovedJobs = jobRepository.countByStatus(JobStatus.APPROVED);
        long totalApprovedCompanies = companyRepository.countByApprovalStatus(CompanyApprovalStatus.APPROVED);

        double placementPercentage = verifiedStudents > 0 ?
                Math.round(((double) placedStudents / verifiedStudents) * 10000.0) / 100.0 : 0.0;

        double overallPlacementPercentage = totalStudents > 0 ?
                Math.round(((double) placedStudents / totalStudents) * 10000.0) / 100.0 : 0.0;

        return new PlacementOverviewStats(
                totalStudents,
                verifiedStudents,
                placedStudents,
                placementPercentage,
                overallPlacementPercentage,
                totalApplications,
                totalOffers,
                totalApprovedJobs,
                totalApprovedCompanies
        );
    }

    @Transactional(readOnly = true)
    public List<DepartmentPlacementStat> getDepartmentStats() {
        List<Object[]> deptCounts = studentProfileRepository.findDepartmentStudentCounts();
        List<Object[]> placedCounts = applicationRepository.findPlacedStudentCountByDepartment(ApplicationStatus.SELECTED);

        Map<String, Long> placedMap = new HashMap<>();
        for (Object[] row : placedCounts) {
            String dept = (String) row[0];
            Long placed = ((Number) row[1]).longValue();
            placedMap.put(dept, placed);
        }

        List<DepartmentPlacementStat> stats = new ArrayList<>();
        for (Object[] row : deptCounts) {
            String dept = (String) row[0];
            long total = ((Number) row[1]).longValue();
            long verified = ((Number) row[2]).longValue();
            long placed = placedMap.getOrDefault(dept, 0L);

            double rate = verified > 0 ?
                    Math.round(((double) placed / verified) * 10000.0) / 100.0 : 0.0;

            stats.add(new DepartmentPlacementStat(dept, total, verified, placed, rate));
        }

        return stats;
    }

    @Transactional(readOnly = true)
    public List<CompanyPlacementStat> getCompanyStats() {
        return applicationRepository.findCompanyPlacementStats(ApplicationStatus.SELECTED);
    }

    @Transactional(readOnly = true)
    public SalaryPackageStats getSalaryPackageStats() {
        List<Application> selectedApps = applicationRepository.findSelectedApplicationsWithJobDetails(ApplicationStatus.SELECTED);
        List<String> salaryStrings = new ArrayList<>();
        for (Application app : selectedApps) {
            if (app.getJob() != null && app.getJob().getSalaryPackage() != null) {
                salaryStrings.add(app.getJob().getSalaryPackage());
            }
        }
        return SalaryPackageParser.computeStats(salaryStrings);
    }

    @Transactional(readOnly = true)
    public long getPlacedStudentsCount() {
        return applicationRepository.countDistinctPlacedStudents(ApplicationStatus.SELECTED);
    }
}

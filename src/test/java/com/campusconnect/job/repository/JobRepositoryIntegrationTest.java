package com.campusconnect.job.repository;

import com.campusconnect.auth.entity.Role;
import com.campusconnect.auth.entity.User;
import com.campusconnect.auth.entity.UserStatus;
import com.campusconnect.auth.repository.UserRepository;
import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import com.campusconnect.company.repository.CompanyRepository;
import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobEligibilityCriteria;
import com.campusconnect.job.entity.JobStatus;
import com.campusconnect.job.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest
@ActiveProfiles("test")
class JobRepositoryIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobService jobService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long savedJobId;
    private String recruiterEmail;

    @BeforeEach
    void setUp() {
        recruiterEmail = "recruiter.fetch.test@acme.com";

        transactionTemplate.execute(status -> {
            jobRepository.deleteAll();
            companyRepository.deleteAll();

            User recruiter = userRepository.findByEmail(recruiterEmail)
                    .orElseGet(() -> userRepository.save(new User(recruiterEmail, "password123", "Acme Recruiter", Role.RECRUITER, UserStatus.ACTIVE)));

            Company company = new Company(recruiter, "Acme Tech", "Software", "https://acme.com", "Tech enterprise");
            company.setApprovalStatus(CompanyApprovalStatus.APPROVED);
            company = companyRepository.save(company);

            Job job = new Job(company, "Backend Engineer", "Full-Time", "Remote", "Bangalore",
                    "18 LPA", "Build microservices", LocalDate.now(), LocalDate.now().plusDays(20));
            job.setStatus(JobStatus.PENDING_APPROVAL);

            JobEligibilityCriteria criteria = new JobEligibilityCriteria(job, new BigDecimal("7.50"), 0, 2026, Set.of("CSE", "IT"));
            job.setEligibilityCriteria(criteria);

            Job savedJob = jobRepository.save(job);
            savedJobId = savedJob.getId();
            return null;
        });
    }

    @Test
    void getJobById_EagerlyLoadsCompanyRecruiterWithoutSession() {
        Job fetchedJob = jobService.getJobById(savedJobId);

        assertThatNoException().isThrownBy(() -> {
            assertThat(fetchedJob.getCompany()).isNotNull();
            assertThat(fetchedJob.getCompany().getRecruiter()).isNotNull();
            assertThat(fetchedJob.getCompany().getRecruiter().getEmail()).isEqualTo(recruiterEmail);
            assertThat(fetchedJob.getCompany().getRecruiter().getFullName()).isEqualTo("Acme Recruiter");
        });
    }

    @Test
    void getJobById_EagerlyLoadsEligibilityCriteriaWithoutSession() {
        Job fetchedJob = jobService.getJobById(savedJobId);

        assertThatNoException().isThrownBy(() -> {
            assertThat(fetchedJob.getEligibilityCriteria()).isNotNull();
            assertThat(fetchedJob.getEligibilityCriteria().getMinCgpa()).isEqualByComparingTo("7.50");
            assertThat(fetchedJob.getEligibilityCriteria().getEligibleBranches()).contains("CSE", "IT");
        });
    }
}

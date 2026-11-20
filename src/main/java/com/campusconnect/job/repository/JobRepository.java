package com.campusconnect.job.repository;

import com.campusconnect.job.entity.Job;
import com.campusconnect.job.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByCompanyId(Long companyId, Pageable pageable);

    long countByCompanyId(Long companyId);

    long countByStatus(JobStatus status);

    @Query("SELECT j FROM Job j JOIN FETCH j.company c LEFT JOIN FETCH j.eligibilityCriteria ec WHERE j.id = :id")
    Optional<Job> findByIdWithCompanyAndCriteria(@Param("id") Long id);

    @Query("SELECT j FROM Job j JOIN FETCH j.company c LEFT JOIN FETCH j.eligibilityCriteria ec WHERE " +
           "j.status = 'APPROVED' AND " +
           "j.applicationOpenDate <= :today AND j.applicationCloseDate >= :today AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(j.roleType) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Job> findApprovedOpenJobs(@Param("today") LocalDate today,
                                  @Param("search") String search,
                                  Pageable pageable);

    @Query("SELECT j FROM Job j JOIN FETCH j.company c WHERE " +
           "(:status IS NULL OR j.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(j.roleType) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Job> searchOfficerJobs(@Param("status") JobStatus status,
                                @Param("search") String search,
                                Pageable pageable);
}

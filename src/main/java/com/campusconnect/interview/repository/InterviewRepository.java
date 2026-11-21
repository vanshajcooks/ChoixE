package com.campusconnect.interview.repository;

import com.campusconnect.interview.entity.Interview;
import com.campusconnect.interview.entity.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationIdOrderByScheduledAtAsc(Long applicationId);

    @Query("SELECT i FROM Interview i " +
           "JOIN FETCH i.application a " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.company c " +
           "WHERE a.student.id = :studentId " +
           "ORDER BY i.scheduledAt DESC")
    Page<Interview> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    @Query(value = "SELECT i FROM Interview i " +
           "JOIN FETCH i.application a " +
           "JOIN FETCH a.student s " +
           "JOIN FETCH s.user su " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.company c " +
           "WHERE c.id = :companyId " +
           "AND (:jobId IS NULL OR j.id = :jobId) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "ORDER BY i.scheduledAt DESC",
           countQuery = "SELECT count(i) FROM Interview i " +
           "JOIN i.application a " +
           "JOIN a.job j " +
           "JOIN j.company c " +
           "WHERE c.id = :companyId " +
           "AND (:jobId IS NULL OR j.id = :jobId) " +
           "AND (:status IS NULL OR i.status = :status)")
    Page<Interview> findCompanyInterviews(@Param("companyId") Long companyId,
                                         @Param("jobId") Long jobId,
                                         @Param("status") InterviewStatus status,
                                         Pageable pageable);

    @Query(value = "SELECT i FROM Interview i " +
           "JOIN FETCH i.application a " +
           "JOIN FETCH a.student s " +
           "JOIN FETCH s.user su " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.company c " +
           "WHERE (:companyId IS NULL OR c.id = :companyId) " +
           "AND (:jobId IS NULL OR j.id = :jobId) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "ORDER BY i.scheduledAt DESC",
           countQuery = "SELECT count(i) FROM Interview i " +
           "JOIN i.application a " +
           "JOIN a.job j " +
           "JOIN j.company c " +
           "WHERE (:companyId IS NULL OR c.id = :companyId) " +
           "AND (:jobId IS NULL OR j.id = :jobId) " +
           "AND (:status IS NULL OR i.status = :status)")
    Page<Interview> findOfficerInterviews(@Param("companyId") Long companyId,
                                         @Param("jobId") Long jobId,
                                         @Param("status") InterviewStatus status,
                                         Pageable pageable);

    @Query("SELECT i FROM Interview i " +
           "JOIN FETCH i.application a " +
           "JOIN FETCH a.student s " +
           "JOIN FETCH s.user su " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.company c " +
           "JOIN FETCH c.recruiter r " +
           "WHERE i.id = :id")
    Optional<Interview> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT i FROM Interview i " +
           "JOIN FETCH i.application a " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.company c " +
           "WHERE a.student.id = :studentId " +
           "AND i.status IN ('SCHEDULED', 'RESCHEDULED') " +
           "AND i.scheduledAt >= :now " +
           "ORDER BY i.scheduledAt ASC")
    List<Interview> findUpcomingStudentInterviews(@Param("studentId") Long studentId, @Param("now") LocalDateTime now);

    @Query("SELECT count(i) FROM Interview i " +
           "JOIN i.application a " +
           "JOIN a.job j " +
           "WHERE j.company.id = :companyId " +
           "AND i.status IN ('SCHEDULED', 'RESCHEDULED') " +
           "AND i.scheduledAt >= :now")
    long countUpcomingCompanyInterviews(@Param("companyId") Long companyId, @Param("now") LocalDateTime now);
}

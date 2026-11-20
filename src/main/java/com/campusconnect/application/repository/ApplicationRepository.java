package com.campusconnect.application.repository;

import com.campusconnect.application.entity.Application;
import com.campusconnect.application.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);

    Optional<Application> findByStudentIdAndJobId(Long studentId, Long jobId);

    Page<Application> findByStudentId(Long studentId, Pageable pageable);

    Page<Application> findByJobId(Long jobId, Pageable pageable);

    Page<Application> findByJobCompanyId(Long companyId, Pageable pageable);

    long countByStudentId(Long studentId);

    long countByJobCompanyId(Long companyId);

    long countByJobId(Long jobId);

    @Query("SELECT a FROM Application a " +
           "JOIN FETCH a.student s " +
           "JOIN FETCH s.user u " +
           "JOIN FETCH a.job j " +
           "JOIN FETCH j.company c " +
           "LEFT JOIN FETCH j.eligibilityCriteria ec " +
           "WHERE a.id = :id")
    Optional<Application> findByIdWithDetails(@Param("id") Long id);

    @Query(value = "SELECT a FROM Application a " +
                   "JOIN FETCH a.student s " +
                   "JOIN FETCH s.user u " +
                   "JOIN FETCH a.job j " +
                   "WHERE j.company.id = :companyId " +
                   "AND (:jobId IS NULL OR j.id = :jobId) " +
                   "AND (:status IS NULL OR a.status = :status) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(a) FROM Application a " +
                        "JOIN a.student s " +
                        "JOIN s.user u " +
                        "JOIN a.job j " +
                        "WHERE j.company.id = :companyId " +
                        "AND (:jobId IS NULL OR j.id = :jobId) " +
                        "AND (:status IS NULL OR a.status = :status) " +
                        "AND (:search IS NULL OR :search = '' OR " +
                        "     LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Application> searchCompanyApplicants(@Param("companyId") Long companyId,
                                              @Param("jobId") Long jobId,
                                              @Param("status") ApplicationStatus status,
                                              @Param("search") String search,
                                              Pageable pageable);

    @Query(value = "SELECT a FROM Application a " +
                   "JOIN FETCH a.student s " +
                   "JOIN FETCH s.user u " +
                   "JOIN FETCH a.job j " +
                   "JOIN FETCH j.company c " +
                   "WHERE (:status IS NULL OR a.status = :status) " +
                   "AND (:search IS NULL OR :search = '' OR " +
                   "     LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                   "     LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(a) FROM Application a " +
                        "JOIN a.student s " +
                        "JOIN s.user u " +
                        "JOIN a.job j " +
                        "JOIN j.company c " +
                        "WHERE (:status IS NULL OR a.status = :status) " +
                        "AND (:search IS NULL OR :search = '' OR " +
                        "     LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Application> searchOfficerApplications(@Param("status") ApplicationStatus status,
                                                @Param("search") String search,
                                                Pageable pageable);
}

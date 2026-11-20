package com.campusconnect.company.repository;

import com.campusconnect.company.entity.Company;
import com.campusconnect.company.entity.CompanyApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByRecruiterId(Long recruiterId);

    @Query("SELECT c FROM Company c JOIN FETCH c.recruiter WHERE c.recruiter.email = :email")
    Optional<Company> findByRecruiterEmail(@Param("email") String email);

    @Query("SELECT c FROM Company c JOIN FETCH c.recruiter WHERE c.id = :id")
    Optional<Company> findByIdWithRecruiter(@Param("id") Long id);

    long countByApprovalStatus(CompanyApprovalStatus status);

    @Query("SELECT c FROM Company c JOIN FETCH c.recruiter r WHERE " +
           "(:status IS NULL OR c.approvalStatus = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.industry) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.fullName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Company> searchCompanies(@Param("status") CompanyApprovalStatus status,
                                  @Param("search") String search,
                                  Pageable pageable);
}

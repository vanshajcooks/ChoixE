package com.campusconnect.company.repository;

import com.campusconnect.company.entity.Company;
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
}

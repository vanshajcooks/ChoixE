package com.campusconnect.student.repository;

import com.campusconnect.student.entity.StudentProfile;
import com.campusconnect.student.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    boolean existsByRollNumber(String rollNumber);

    Optional<StudentProfile> findByUserId(Long userId);

    @Query("SELECT sp FROM StudentProfile sp JOIN FETCH sp.user WHERE sp.user.email = :email")
    Optional<StudentProfile> findByUserEmail(@Param("email") String email);

    @Query("SELECT sp FROM StudentProfile sp JOIN FETCH sp.user WHERE sp.id = :id")
    Optional<StudentProfile> findByIdWithUser(@Param("id") Long id);

    Page<StudentProfile> findByVerificationStatus(VerificationStatus status, Pageable pageable);

    long countByVerificationStatus(VerificationStatus status);

    @Query("SELECT sp FROM StudentProfile sp JOIN FETCH sp.user u WHERE " +
           "(:status IS NULL OR sp.verificationStatus = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(sp.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sp.department) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<StudentProfile> searchStudents(@Param("status") VerificationStatus status,
                                        @Param("search") String search,
                                        Pageable pageable);

    @Query("SELECT s.department, COUNT(s.id), SUM(CASE WHEN s.verificationStatus = com.campusconnect.student.entity.VerificationStatus.VERIFIED THEN 1L ELSE 0L END) " +
           "FROM StudentProfile s GROUP BY s.department ORDER BY s.department ASC")
    java.util.List<Object[]> findDepartmentStudentCounts();
}

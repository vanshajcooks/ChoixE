package com.campusconnect.announcement.repository;

import com.campusconnect.announcement.entity.Announcement;
import com.campusconnect.announcement.entity.TargetAudience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @Query("SELECT a FROM Announcement a JOIN FETCH a.author WHERE a.id = :id")
    Optional<Announcement> findByIdWithAuthor(@Param("id") Long id);

    @Query("SELECT a FROM Announcement a JOIN FETCH a.author WHERE a.active = true " +
           "AND a.targetAudience IN :audiences " +
           "ORDER BY a.pinned DESC, a.createdAt DESC")
    Page<Announcement> findActiveByAudience(@Param("audiences") Collection<TargetAudience> audiences, Pageable pageable);

    @Query("SELECT a FROM Announcement a JOIN FETCH a.author WHERE a.active = true " +
           "AND a.pinned = true AND a.targetAudience IN :audiences " +
           "ORDER BY a.createdAt DESC")
    List<Announcement> findPinnedByAudience(@Param("audiences") Collection<TargetAudience> audiences);

    @Query(value = "SELECT a FROM Announcement a JOIN FETCH a.author WHERE " +
           "(:active IS NULL OR a.active = :active) AND " +
           "(:audience IS NULL OR a.targetAudience = :audience) " +
           "ORDER BY a.pinned DESC, a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM Announcement a WHERE " +
           "(:active IS NULL OR a.active = :active) AND " +
           "(:audience IS NULL OR a.targetAudience = :audience)")
    Page<Announcement> findOfficerAnnouncements(@Param("active") Boolean active,
                                               @Param("audience") TargetAudience audience,
                                               Pageable pageable);

    long countByActiveTrue();
}

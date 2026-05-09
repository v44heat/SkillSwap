package com.skillswap.repository;

import com.skillswap.model.SkillListing;
import com.skillswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillListingRepository extends JpaRepository<SkillListing, Long> {

    List<SkillListing> findByUser(User user);

    @Query("SELECT s FROM SkillListing s WHERE s.isActive = true AND s.isFlagged = false " +
            "AND (:category IS NULL OR s.category = :category) " +
            "AND (:search IS NULL OR " +
            "     LOWER(s.title)       LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<SkillListing> browseListings(
            @Param("category") SkillListing.SkillCategory category,
            @Param("search")   String search
    );

    List<SkillListing> findByIsFlaggedTrue();

    List<SkillListing> findAll();

    long countByIsActiveTrue();
}
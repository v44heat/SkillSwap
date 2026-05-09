package com.skillswap.service;

import com.skillswap.dto.SkillDTO;
import com.skillswap.exception.AppException;
import com.skillswap.model.SkillListing;
import com.skillswap.model.User;
import com.skillswap.repository.SkillListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillListingRepository listingRepo;

    // ── Public browse ────────────────────────────────────────────────
    public List<SkillDTO.ListingResponse> browse(String categoryStr, String search) {
        SkillListing.SkillCategory category = null;
        if (categoryStr != null && !categoryStr.isBlank()) {
            try { category = SkillListing.SkillCategory.valueOf(categoryStr.toUpperCase()); }
            catch (IllegalArgumentException ignored) {}
        }
        String searchTerm = (search != null && search.isBlank()) ? null : search;
        return listingRepo.browseListings(category, searchTerm)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── My listings ──────────────────────────────────────────────────
    public List<SkillDTO.ListingResponse> myListings(User user) {
        return listingRepo.findByUser(user)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Get single listing ───────────────────────────────────────────
    public SkillDTO.ListingResponse getById(Long id) {
        return toResponse(findById(id));
    }

    // ── Create ───────────────────────────────────────────────────────
    @Transactional
    public SkillDTO.ListingResponse create(User user, SkillDTO.CreateRequest req) {
        SkillListing listing = SkillListing.builder()
                .user(user)
                .title(req.getTitle().trim())
                .description(req.getDescription().trim())
                .category(parseCategory(req.getCategory()))
                .level(parseLevel(req.getLevel()))
                .sessionDuration(req.getSessionDuration())
                .availability(req.getAvailability())
                .isActive(true)
                .isFlagged(false)
                .build();
        return toResponse(listingRepo.save(listing));
    }

    // ── Update ───────────────────────────────────────────────────────
    @Transactional
    public SkillDTO.ListingResponse update(Long id, User user, SkillDTO.UpdateRequest req) {
        SkillListing listing = findById(id);
        assertOwner(listing, user);
        if (req.getTitle()           != null) listing.setTitle(req.getTitle().trim());
        if (req.getDescription()     != null) listing.setDescription(req.getDescription().trim());
        if (req.getCategory()        != null) listing.setCategory(parseCategory(req.getCategory()));
        if (req.getLevel()           != null) listing.setLevel(parseLevel(req.getLevel()));
        if (req.getSessionDuration() != null) listing.setSessionDuration(req.getSessionDuration());
        if (req.getAvailability()    != null) listing.setAvailability(req.getAvailability());
        return toResponse(listingRepo.save(listing));
    }

    // ── Delete ───────────────────────────────────────────────────────
    @Transactional
    public void delete(Long id, User user) {
        SkillListing listing = findById(id);
        assertOwner(listing, user);
        listingRepo.delete(listing);
    }

    // ── Toggle active ────────────────────────────────────────────────
    @Transactional
    public SkillDTO.ListingResponse toggle(Long id, User user) {
        SkillListing listing = findById(id);
        assertOwner(listing, user);
        listing.setIsActive(!listing.getIsActive());
        return toResponse(listingRepo.save(listing));
    }

    // ── Flag ─────────────────────────────────────────────────────────
    @Transactional
    public void flag(Long id, User reporter, String reason) {
        SkillListing listing = findById(id);
        if (listing.getUser().getId().equals(reporter.getId())) {
            throw AppException.badRequest("You cannot flag your own listing");
        }
        listing.setIsFlagged(true);
        listing.setFlagReason(reason);
        listingRepo.save(listing);
    }

    // ── Helpers ──────────────────────────────────────────────────────
    public SkillListing findById(Long id) {
        return listingRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Skill listing"));
    }

    private void assertOwner(SkillListing listing, User user) {
        if (!listing.getUser().getId().equals(user.getId())) {
            throw AppException.forbidden("You do not own this listing");
        }
    }

    private SkillListing.SkillCategory parseCategory(String cat) {
        try { return SkillListing.SkillCategory.valueOf(cat.toUpperCase()); }
        catch (Exception e) { throw AppException.badRequest("Invalid category: " + cat); }
    }

    private SkillListing.SkillLevel parseLevel(String level) {
        if (level == null) return SkillListing.SkillLevel.INTERMEDIATE;
        try { return SkillListing.SkillLevel.valueOf(level.toUpperCase()); }
        catch (Exception e) { return SkillListing.SkillLevel.INTERMEDIATE; }
    }

    public SkillDTO.ListingResponse toResponse(SkillListing s) {
        return SkillDTO.ListingResponse.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .teacherName(s.getUser().getFullName())
                .teacherDept(s.getUser().getDepartment())
                .teacherRating(s.getUser().getAverageRating())
                .title(s.getTitle())
                .description(s.getDescription())
                .category(s.getCategory().name())
                .level(s.getLevel().name())
                .sessionDuration(s.getSessionDuration())
                .availability(s.getAvailability())
                .isActive(s.getIsActive())
                .isFlagged(s.getIsFlagged())
                .flagReason(s.getFlagReason())
                .totalSessions(s.getTotalSessions())
                .averageRating(s.getAverageRating())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
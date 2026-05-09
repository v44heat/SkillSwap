package com.skillswap.service;

import com.skillswap.dto.UserDTO;
import com.skillswap.exception.AppException;
import com.skillswap.model.User;
import com.skillswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("User"));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("User"));
    }

    public UserDTO.ProfileResponse toProfile(User user) {
        return UserDTO.ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .department(user.getDepartment())
                .yearOfStudy(user.getYearOfStudy())
                .studentId(user.getStudentId())
                .bio(user.getBio())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .averageRating(user.getAverageRating())
                .totalSessions(user.getTotalSessions())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserDTO.ProfileResponse updateProfile(String email, UserDTO.UpdateRequest req) {
        User user = getByEmail(email);
        if (req.getFirstName() != null) user.setFirstName(sanitize(req.getFirstName()));
        if (req.getLastName()  != null) user.setLastName(sanitize(req.getLastName()));
        if (req.getDepartment()  != null) user.setDepartment(req.getDepartment());
        if (req.getYearOfStudy() != null) user.setYearOfStudy(req.getYearOfStudy());
        if (req.getBio()         != null) user.setBio(sanitize(req.getBio()));
        return toProfile(userRepository.save(user));
    }

    private String sanitize(String s) {
        return s == null ? null : s.trim().replaceAll("<[^>]*>", "");
    }
}
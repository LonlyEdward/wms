package com.wms.backend.service;

import com.wms.backend.controller.UserController.CreateUserRequest;
import com.wms.backend.controller.UserController.UpdateUserRequest;
import com.wms.backend.dto.auth.UserResponse;
import com.wms.backend.entity.Business;
import com.wms.backend.entity.User;
import com.wms.backend.exception.AppException;
import com.wms.backend.exception.EntityNotFoundException;
import com.wms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(UUID businessId) {
        return userRepository
                .findAllByBusiness_IdAndIsActiveTrue(businessId)
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request,
                                   UUID businessId) {
        // Check email is not already taken
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "EMAIL_TAKEN",
                    "A user with this email already exists"
            );
        }

        User user = User.builder()
                .business(Business.builder().id(businessId).build())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(request.role())
                .isActive(true)
                .notifEmail(true)
                .notifSms(false)
                .notifWhatsapp(false)
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request,
                                   UUID businessId) {
        User user = userRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName()  != null) user.setLastName(request.lastName());
        if (request.role()      != null) user.setRole(request.role());

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deactivateUser(UUID id, UUID businessId) {
        User user = userRepository
                .findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new EntityNotFoundException("User", id));

        user.setIsActive(false);
        userRepository.save(user);
    }
}
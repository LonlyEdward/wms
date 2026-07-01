package com.wms.backend.security;

import com.wms.backend.entity.Business;
import com.wms.backend.entity.Role;
import com.wms.backend.entity.User;
import com.wms.backend.repository.BusinessRepository;
import com.wms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        // Call the parent method to fetch the user profile from Google
        // DefaultOAuth2UserService makes the HTTP call to Google's userinfo API
        // and returns the profile as an OAuth2User containing all attributes
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Extract the attributes Google returned
        // Google returns: sub, name, given_name, family_name, email, picture
        String email     = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName  = oAuth2User.getAttribute("family_name");
        String sub       = oAuth2User.getAttribute("sub"); // Google's user ID

        log.debug("OAuth2 login attempt for email: {}", email);

        // Check if a user with this email already exists
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // Update OAuth fields in case they changed
            user.setOauthProvider("google");
            user.setOauthSubject(sub);
            userRepository.save(user);

            log.info("Existing user logged in via Google: {}", email);
            return oAuth2User;
        }

        // User does not exist, create a new buyer account
        // Find the default business to assign this buyer to
        // For now we use the first active business in the database.
        Business business = businessRepository
                .findFirstByIsActiveTrue()
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        "No active business found. Cannot create buyer account."
                ));

        User newUser = User.builder()
                .businessId(business.getId())
                .email(email)
                .firstName(firstName != null ? firstName : "")
                .lastName(lastName != null ? lastName : "")
                .role(Role.BUYER)
                .oauthProvider("google")
                .oauthSubject(sub)
                .isActive(true)
                .notifEmail(true)
                .notifSms(false)
                .notifWhatsapp(false)
                .build();

        userRepository.save(newUser);
        log.info("New buyer created via Google OAuth2: {}", email);

        return oAuth2User;
    }
}
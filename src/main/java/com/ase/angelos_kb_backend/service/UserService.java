package com.ase.angelos_kb_backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ase.angelos_kb_backend.dto.UserDTO;
import com.ase.angelos_kb_backend.exception.ResourceNotFoundException;
import com.ase.angelos_kb_backend.exception.UnauthorizedException;
import com.ase.angelos_kb_backend.model.Organisation;
import com.ase.angelos_kb_backend.model.User;
import com.ase.angelos_kb_backend.repository.UserRepository;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganisationService organisationService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, OrganisationService organisationService, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.organisationService = organisationService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public List<UserDTO> getAllUsersByOrgId(Long orgId) {
        return userRepository.findByOrganisationOrgID(orgId).stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public UserDTO approveUser(Long userId, Long orgId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        // Ensure the user belongs to the organisation
        if (!user.getOrganisation().getOrgID().equals(orgId)) {
            throw new UnauthorizedException("You are not authorized to edit this website.");
        }
        user.setApproved(true);
        return convertToDto(userRepository.save(user));
    }

    // Set a user to admin
    @Transactional
    public UserDTO setUserToAdmin(Long userId, Long orgId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        // Ensure the user belongs to the organisation
        if (!user.getOrganisation().getOrgID().equals(orgId)) {
            throw new UnauthorizedException("You are not authorized to edit this website.");
        }
        user.setAdmin(true);
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    @Transactional
    public UserDTO registerUser(String email, String password, Long orgId) {
        // Check if the email is already in use
        if (userRepository.findByMail(email).isPresent()) {
            throw new ResourceNotFoundException("Email already in use");
        }
        Organisation organisation = organisationService.getOrganisationById(orgId);

        // Create and save the new user
        User newUser = new User();
        newUser.setMail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setOrganisation(organisation);
        newUser.setApproved(false); 
        newUser.setAdmin(false);
        newUser.setSystemAdmin(false);
        // Generate confirmation token
        String token = UUID.randomUUID().toString();
        newUser.setConfirmationToken(token);

        User savedUser = userRepository.save(newUser);

        // Send confirmation email
        sendConfirmationEmail(savedUser);

        return convertToDto(savedUser);
    }

    public boolean confirmUserEmail(String token) {
        User user = userRepository.findByConfirmationToken(token);
        if (user == null) {
            return false;
        }
        user.setMailConfirmed(true);
        user.setConfirmationToken(null); // Invalidate the token
        userRepository.save(user);
        return true;
    }

    private void sendConfirmationEmail(User user) {
        String token = user.getConfirmationToken();
        String confirmationUrl = "http://TODO.com/api/users/confirm?token=" + token;
        String subject = "Email Confirmation";
        String message = "Please click the link below to confirm your email:\n" + confirmationUrl;
    
        // Implement your email sending logic here
        emailService.sendEmail(user.getMail(), subject, message);
    }

    private UserDTO convertToDto(User user) {
        return UserDTO.builder()
            .id(user.getUserID())
            .mail(user.getMail())
            .isAdmin(user.isAdmin())
            .isApproved(user.isApproved())
            .build();
    }
}

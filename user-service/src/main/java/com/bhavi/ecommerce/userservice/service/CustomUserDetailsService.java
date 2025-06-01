package com.bhavi.ecommerce.userservice.service;

import com.bhavi.ecommerce.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // Marks this as a Spring Service component
@RequiredArgsConstructor // Lombok: Generates a constructor with required arguments (final fields)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // Inject our UserRepository

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Spring Security will call this method with the username (which is email in our case)
        // It expects a UserDetails object in return. Our User model already implements UserDetails.
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
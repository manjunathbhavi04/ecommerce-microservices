package com.bhavi.ecommerce.userservice.config; // Or .service or your main application package

import com.bhavi.ecommerce.userservice.enums.Role; // Make sure this path is correct for your Role enum
import com.bhavi.ecommerce.userservice.model.User;
import com.bhavi.ecommerce.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component // Marks this class as a Spring component to be managed by the container
@RequiredArgsConstructor // Lombok: Generates constructor for final fields (UserRepository, PasswordEncoder)
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // This method runs automatically after the Spring Boot application starts.
        // We use it to ensure essential data (like an initial admin user) exists.

        if (userRepository.findByEmail("orderservice@example.com").isEmpty()) { // Use a distinct email
            Set<Role> serviceRoles = new HashSet<>();
            serviceRoles.add(Role.ROLE_ADMIN); // For simplicity, give it ADMIN role for now to allow stock decrease
            // OR create a custom ROLE_SERVICE if you have fine-grained permissions for decrease-stock
            // serviceRoles.add(Role.ROLE_SERVICE);

            User serviceUser = User.builder()
                    .firstName("Order")
                    .lastName("Service")
                    .email("orderservice@example.com") // Use this email in orderservice.client.username
                    .password(passwordEncoder.encode("orderservicepass")) // Use this password in orderservice.client.password
                    .roles(serviceRoles)
                    .isVerified(true)
                    .enabled(true)
                    .build();
            userRepository.save(serviceUser);
            System.out.println("Service user 'orderservice@example.com' created for inter-service calls.");
        }

        // 1. Create an Admin User if they don't already exist
        if (userRepository.findByEmail("creedassassin781@gmail.com").isEmpty()) {
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(Role.ROLE_ADMIN);
            adminRoles.add(Role.ROLE_CUSTOMER); // An admin user might also act as a customer

            User adminUser = User.builder()
                    .firstName("Super")
                    .lastName("Admin")
                    .email("creedassassin781@gmail.com")
                    .password(passwordEncoder.encode("adminpass")) // Encode the password! Choose a strong one.
                    .roles(adminRoles) // Assign the set of enum roles
                    .isVerified(true)
                    .enabled(true)
                    .build();
            userRepository.save(adminUser);
            System.out.println("Initial Admin user 'admin@example.com' created.");
        }

        // 2. Create a default Customer User if they don't already exist
        if (userRepository.findByEmail("manjunathbhavi890@gmail.com").isEmpty()) {
            Set<Role> customerRoles = new HashSet<>();
            customerRoles.add(Role.ROLE_CUSTOMER);

            User customerUser = User.builder()
                    .firstName("Default")
                    .lastName("Customer")
                    .email("manjunathbhavi890@gmail.com")
                    .password(passwordEncoder.encode("customerpass")) // Encode the password!
                    .roles(customerRoles)
                    .isVerified(true)
                    .enabled(true)
                    .build();
            userRepository.save(customerUser);
            System.out.println("Default Customer user 'customer@example.com' created.");
        }

        // 3. Create a default Seller User if they don't already exist
        if (userRepository.findByEmail("manjunathdon890@gmail.com").isEmpty()) {
            Set<Role> sellerRoles = new HashSet<>();
            sellerRoles.add(Role.ROLE_SELLER);
            sellerRoles.add(Role.ROLE_CUSTOMER); // A seller is also typically a customer

            User sellerUser = User.builder()
                    .firstName("Default")
                    .lastName("Seller")
                    .email("manjunathdon890@gmail.com")
                    .password(passwordEncoder.encode("sellerpass")) // Encode the password!
                    .roles(sellerRoles)
                    .isVerified(true)
                    .enabled(true)
                    .build();
            userRepository.save(sellerUser);
            System.out.println("Default Seller user 'seller@example.com' created.");
        }

        // You can add more initial data here if needed for testing or development.
    }
}
package com.bhavi.ecommerce.userservice.model.token;

import com.bhavi.ecommerce.userservice.model.User; // Import your User entity
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data // Lombok: Generates getters, setters, equals, hashCode, and toString
@NoArgsConstructor // Lombok: Generates a no-argument constructor
@AllArgsConstructor // Lombok: Generates an all-arguments constructor
@Builder // Lombok: Provides a builder pattern for object creation
@Entity // Mark this class as a JPA entity
@Table(name = "email_verification_tokens") // Define the table name in the database
public class EmailVerificationToken {

    @Id // Marks the field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increments the ID
    private Long id;

    @Column(nullable = false, unique = true) // Token must be unique and not null
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER) // One token per user, eager fetching
    @JoinColumn(nullable = false, name = "user_id") // Column for the foreign key
    private User user;

    @Column(nullable = false) // Expiration date must not be null
    private LocalDateTime expiryDate;

    // Helper method to check if the token is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
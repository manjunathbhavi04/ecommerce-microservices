package com.bhavi.ecommerce.userservice.model;

import com.bhavi.ecommerce.userservice.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Storing hashed password

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private boolean isVerified;

    @Column(nullable = false)
    private boolean enabled;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER) // Specify the target class for the collection
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING) // Store enum values as Strings in the database
    @Column(name = "role", nullable = false) // Ensures role column is not null
    private Set<Role> roles; // Now a Set of Role enum

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Since Role enum now implements GrantedAuthority, direct return is simpler
        return new ArrayList<>(roles); // No need for SimpleGrantedAuthority anymore
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }


}

package com.bhavi.ecommerce.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import com.bhavi.ecommerce.userservice.enums.Role; // Assuming you want to show roles

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean isVerified;
    private Set<Role> roles; // Display user roles
    private String message;
}
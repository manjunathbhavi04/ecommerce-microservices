package com.bhavi.ecommerce.userservice.dto.request;

import com.bhavi.ecommerce.userservice.enums.Role;
import lombok.*;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
}

package com.bhavi.ecommerce.userservice.controller;

import com.bhavi.ecommerce.userservice.dto.request.ChangePasswordRequest;
import com.bhavi.ecommerce.userservice.dto.request.UserProfileUpdateRequest;
import com.bhavi.ecommerce.userservice.dto.response.ApiResponse;
import com.bhavi.ecommerce.userservice.dto.response.UserProfileResponse;
import com.bhavi.ecommerce.userservice.model.User;
import com.bhavi.ecommerce.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getUserProfile(Authentication authentication) {
        Long id = ((User)authentication.getPrincipal()).getId();
        return new ResponseEntity<>(userService.getUserProfile(id), HttpStatus.OK);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@Valid @RequestBody UserProfileUpdateRequest request, Authentication authentication) {
        Long id = ((User) authentication.getPrincipal()).getId();
        UserProfileResponse response = userService.updateUserProfile(id, request);
        if(response.getMessage().contains("failed")) {
            return new ResponseEntity<>(response, HttpStatus.NOT_MODIFIED);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/profile/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        Long userId = ((User) authentication.getPrincipal()).getId(); // Get user ID from authenticated principal
        userService.changePassword(userId, request); // Call the service method

        // Return a generic success response
        return new ResponseEntity<>(new ApiResponse("Password changed successfully!"), HttpStatus.OK);
    }

    /**
     * ADMIN ONLY: Retrieves the profile of any user by ID.
     */
    @GetMapping("/admin/profile/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMINs can access this
    public ResponseEntity<UserProfileResponse> getUserProfileAsAdmin(@PathVariable("id") Long id) {
        return new ResponseEntity<>(userService.getUserProfile(id), HttpStatus.OK);
    }

    /**
     * ADMIN ONLY: Retrieves all the user profiles
     */
    // --- NEW ADMIN USER LISTING & SEARCH ENDPOINT ---
    /**
     * ADMIN ONLY: Retrieves a paginated, sortable, and searchable list of all users.
     * Requires an ADMIN role.
     *
     * @param page The page number (0-indexed, default 0).
     * @param size The number of items per page (default 10).
     * @param sortBy The field to sort by (default "id").
     * @param sortOrder The sort order ("asc" or "desc", default "asc").
     * @param searchKeyword Optional keyword to search by email, first name, or last name.
     * @return A Page of UserProfileResponse objects.
     */
    @GetMapping("/admin") // Or /admin/all or /admin/users based on preference
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserProfileResponse>> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder,
            @RequestParam(name = "searchKeyword", required = false) String searchKeyword) {

        Page<UserProfileResponse> userPage = userService.getAllUsers(page, size, sortBy, sortOrder, searchKeyword);
        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }

    /**
     * ADMIN ONLY: Updates the profile of any user by ID.
     */
    @PutMapping("/admin/profile/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMINs can update via this endpoint
    public ResponseEntity<UserProfileResponse> updateUserProfileAsAdmin(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        UserProfileResponse response = userService.updateUserProfile(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/admin/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deactivateUser(@PathVariable("id") Long id) {
        userService.deactivateUser(id);
        return new ResponseEntity<>(new ApiResponse("User account deactivated successfully."), HttpStatus.OK);
    }

    @PutMapping("/admin/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> activateUser(@PathVariable("id") Long id) {
        userService.activateUser(id);
        return new ResponseEntity<>(new ApiResponse("User account activated successfully."), HttpStatus.OK);
    }
}

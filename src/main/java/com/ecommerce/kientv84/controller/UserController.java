package com.ecommerce.kientv84.controller;

import com.ecommerce.kientv84.dtos.request.UserRequest;
import com.ecommerce.kientv84.dtos.request.UserUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.user.UserSearchRequest;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.dtos.response.UserResponse;
import com.ecommerce.kientv84.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/accounts/filter")
    public ResponseEntity<PagedResponse<UserResponse>> searchUsers(@RequestBody UserSearchRequest req) {
        return ResponseEntity.ok(userService.searchUsers(req));
    }

    @GetMapping("/accounts/suggestion")
    public ResponseEntity<List<UserResponse>> getUserSuggestions(@RequestParam String q,
                                                                 @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(userService.searchUserSuggestion(q, limit));
    }

    @PostMapping("/account")
    public ResponseEntity<UserResponse> createUser( @Valid  @RequestBody UserRequest user) {
        return  ResponseEntity.ok(userService.createUser(user));
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @PostMapping("/account/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest updatedData) {
        return ResponseEntity.ok(userService.updateUser(id, updatedData));
    }

    @PostMapping("/accounts")
    public ResponseEntity<String> deleteUser(@RequestBody List<UUID> ids) {
        return ResponseEntity.ok(userService.deleteUser(ids));
    }

    @PostMapping("/avatar/{id}")
    public ResponseEntity<UserResponse> uploadAvatar(
            @PathVariable UUID id,
            @RequestParam("avatar") MultipartFile avatar
    ) {
        return ResponseEntity.ok(userService.uploadAvatar(id, avatar));
    }

    @DeleteMapping("/avatar/{id}")
    public ResponseEntity<UserResponse> deleteAvatar(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.deleteAvatar(id));
    }
}

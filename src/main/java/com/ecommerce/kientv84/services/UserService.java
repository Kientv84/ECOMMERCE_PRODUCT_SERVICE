package com.ecommerce.kientv84.services;
import com.ecommerce.kientv84.dtos.request.UserRequest;
import com.ecommerce.kientv84.dtos.request.UserUpdateRequest;
import com.ecommerce.kientv84.dtos.request.search.user.UserSearchRequest;
import com.ecommerce.kientv84.dtos.response.PagedResponse;
import com.ecommerce.kientv84.dtos.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.UUID;


public interface UserService {
    UserResponse createUser(UserRequest user);

    UserResponse getById(UUID id);

    UserResponse updateUser(UUID id, UserUpdateRequest updatedData);


    String deleteUser(List<UUID> ids);

    PagedResponse<UserResponse> searchUsers(UserSearchRequest req);

    String hardDeleteUser(List<UUID> ids);

    List<UserResponse> searchUserSuggestion(String q, int limit);

    // ----- Avatar -----
    UserResponse uploadAvatar(UUID id, MultipartFile avatar);

    UserResponse deleteAvatar(UUID id);
}

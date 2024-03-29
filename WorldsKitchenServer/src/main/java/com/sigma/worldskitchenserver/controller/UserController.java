package com.sigma.worldskitchenserver.controller;

import com.sigma.worldskitchenserver.dto.User.UserProfileDto;
import com.sigma.worldskitchenserver.model.User;
import com.sigma.worldskitchenserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getPrincipleDetails() {
        User user = userService.getCurrentUser();

        return ResponseEntity.ok(userService.mapUserToUserProfileDto(user));
    }

    @PatchMapping("/me/updatePhoto")
    public ResponseEntity<?> updateProfilePicture(@RequestBody Map<String, String> requestBody) {
        String imageURI = requestBody.get("imageURI");
        userService.updateUserPhoto(imageURI);
        return ResponseEntity.ok().build();
    }
}

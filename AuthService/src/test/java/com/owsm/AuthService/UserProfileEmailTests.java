package com.owsm.AuthService;

import com.owsm.AuthService.dto.UserProfileResponse;
import com.owsm.AuthService.model.User;
import com.owsm.AuthService.model.UserProfile;
import com.owsm.AuthService.service.handler.UserProfileServiceHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserProfileEmailTests {

    @Test
    void includesLinkedUserEmailInProfileResponse() {
        User user = new User();
        user.setId(7L);
        user.setEmail("person@example.com");

        UserProfile profile = new UserProfile();
        profile.setUser(user);

        UserProfileResponse response = new UserProfileServiceHandler().convertToResponse(profile);

        assertEquals(7L, response.getUser().getId());
        assertEquals("person@example.com", response.getUser().getEmail());
    }
}

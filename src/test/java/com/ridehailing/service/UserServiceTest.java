package com.ridehailing.service;

import com.ridehailing.dto.request.RegisterUserRequest;
import com.ridehailing.dto.response.UserResponse;
import com.ridehailing.exception.DuplicateResourceException;
import com.ridehailing.exception.ResourceNotFoundException;
import com.ridehailing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new UserRepository());
    }

    @Test
    void register_persistsTrimmedFieldsAndAssignsId() {
        UserResponse response = userService.register(new RegisterUserRequest("  Ada  ", " 999 "));

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Ada");
        assertThat(response.phone()).isEqualTo("999");
    }

    @Test
    void register_rejectsDuplicatePhone() {
        userService.register(new RegisterUserRequest("Ada", "999"));

        assertThatThrownBy(() -> userService.register(new RegisterUserRequest("Bob", "999")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("phone");
    }

    @Test
    void getRequired_throwsWhenMissing() {
        assertThatThrownBy(() -> userService.getRequired(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    void getRequired_returnsPersistedUser() {
        UserResponse created = userService.register(new RegisterUserRequest("Ada", "999"));

        assertThat(userService.getRequired(created.userId()).getName()).isEqualTo("Ada");
    }
}

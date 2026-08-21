package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @Test
    void getUserById_returnsUser_whenFound() {
        User user = new User(1L, "Alice", "alice@example.com");
        when(repository.findById(1L)).thenReturn(Optional.of(user));

        User result = service.getUserById(1L);

        assertThat(result.getName()).isEqualTo("Alice");
    }

    @Test
    void getUserById_throwsNotFound_whenMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createUser_delegatesToRepository() {
        User input = new User(null, "Bob", "bob@example.com");
        User saved = new User(1L, "Bob", "bob@example.com");
        when(repository.save(input)).thenReturn(saved);

        User result = service.createUser(input);

        assertThat(result.getId()).isEqualTo(1L);
        verify(repository).save(input);
    }

    @Test
    void deleteUser_delegatesToRepository() {
        service.deleteUser(1L);

        verify(repository).deleteById(1L);
    }
}
package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserRepositoryTest {

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }

    @Test
    void save_assignsIdAndStoresUser() {
        User user = new User(null, "Alice", "alice@example.com");

        User saved = repository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).contains(saved);
    }

    @Test
    void findById_returnsEmpty_whenUserDoesNotExist() {
        Optional<User> result = repository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsAllSavedUsers() {
        repository.save(new User(null, "Alice", "alice@example.com"));
        repository.save(new User(null, "Bob", "bob@example.com"));

        List<User> all = repository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void deleteById_removesUser() {
        User saved = repository.save(new User(null, "Temp", "temp@example.com"));

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
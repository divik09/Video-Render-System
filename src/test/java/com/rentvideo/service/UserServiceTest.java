package com.rentvideo.service;

import com.rentvideo.dto.RegisterRequest;
import com.rentvideo.dto.UserResponse;
import com.rentvideo.entity.Role;
import com.rentvideo.entity.User;
import com.rentvideo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest request;
    private User savedUser;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setRole(Role.CUSTOMER);

        savedUser = User.builder()
                .id(1L).email("john@example.com").password("hashed")
                .firstName("John").lastName("Doe").role(Role.CUSTOMER)
                .build();
    }

    @Test
    void register_ValidRequest_ReturnsUserResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = userService.register(request);

        assertEquals("john@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals(Role.CUSTOMER, result.getRole());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_DuplicateEmail_ThrowsIllegalArgumentException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_NullRole_DefaultsToCustomer() {
        request.setRole(null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponse result = userService.register(request);

        assertEquals(Role.CUSTOMER, result.getRole());
    }

    @Test
    void getCurrentUser_WhenExists_ReturnsUserResponse() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(savedUser));

        UserResponse result = userService.getCurrentUser("john@example.com");

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
    }
}

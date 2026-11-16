package com.rentvideo.service;

import com.rentvideo.dto.RentalResponse;
import com.rentvideo.entity.Rental;
import com.rentvideo.entity.Role;
import com.rentvideo.entity.User;
import com.rentvideo.entity.Video;
import com.rentvideo.exception.ConflictException;
import com.rentvideo.exception.ResourceNotFoundException;
import com.rentvideo.repository.RentalRepository;
import com.rentvideo.repository.UserRepository;
import com.rentvideo.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock private RentalRepository rentalRepository;
    @Mock private VideoRepository videoRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RentalService rentalService;

    private User user;
    private Video video;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("a@x.com").role(Role.CUSTOMER).build();
        video = Video.builder().id(10L).title("Matrix").director("W").genre("Sci-Fi")
                .availabilityStatus(true).build();
    }

    @Test
    void rentVideo_Success_MarksVideoUnavailable() {
        when(userRepository.findByEmailForUpdate("a@x.com")).thenReturn(Optional.of(user));
        when(videoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(video));
        when(rentalRepository.countByUserIdAndReturnedAtIsNull(1L)).thenReturn(1L);
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0));

        RentalResponse result = rentalService.rentVideo("a@x.com", 10L);

        assertEquals(10L, result.getVideoId());
        assertNull(result.getReturnedAt());
        assertFalse(video.isAvailabilityStatus());
    }

    @Test
    void rentVideo_WhenTwoActiveRentals_ThrowsConflict() {
        when(userRepository.findByEmailForUpdate("a@x.com")).thenReturn(Optional.of(user));
        when(videoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(video));
        when(rentalRepository.countByUserIdAndReturnedAtIsNull(1L)).thenReturn(2L);

        assertThrows(ConflictException.class, () -> rentalService.rentVideo("a@x.com", 10L));
        verify(rentalRepository, never()).save(any());
        assertTrue(video.isAvailabilityStatus());
    }

    @Test
    void rentVideo_WhenUnavailable_ThrowsConflict() {
        video.setAvailabilityStatus(false);
        when(userRepository.findByEmailForUpdate("a@x.com")).thenReturn(Optional.of(user));
        when(videoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(video));

        assertThrows(ConflictException.class, () -> rentalService.rentVideo("a@x.com", 10L));
    }

    @Test
    void rentVideo_WhenVideoMissing_ThrowsNotFound() {
        when(userRepository.findByEmailForUpdate("a@x.com")).thenReturn(Optional.of(user));
        when(videoRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rentalService.rentVideo("a@x.com", 99L));
    }

    @Test
    void returnVideo_Success_MakesVideoAvailable() {
        video.setAvailabilityStatus(false);
        Rental rental = Rental.builder().id(5L).user(user).video(video)
                .rentedAt(LocalDateTime.now()).build();
        when(userRepository.findByEmail("a@x.com")).thenReturn(Optional.of(user));
        when(videoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(video));
        when(rentalRepository.findByUserIdAndVideoIdAndReturnedAtIsNull(1L, 10L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0));

        RentalResponse result = rentalService.returnVideo("a@x.com", 10L);

        assertNotNull(result.getReturnedAt());
        assertTrue(video.isAvailabilityStatus());
    }

    @Test
    void returnVideo_WhenNotRentedByUser_ThrowsNotFound() {
        when(userRepository.findByEmail("a@x.com")).thenReturn(Optional.of(user));
        when(videoRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(video));
        when(rentalRepository.findByUserIdAndVideoIdAndReturnedAtIsNull(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> rentalService.returnVideo("a@x.com", 10L));
    }
}

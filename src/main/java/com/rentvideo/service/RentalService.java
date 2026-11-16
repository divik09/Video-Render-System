package com.rentvideo.service;

import com.rentvideo.dto.RentalResponse;
import com.rentvideo.entity.Rental;
import com.rentvideo.entity.User;
import com.rentvideo.entity.Video;
import com.rentvideo.exception.ConflictException;
import com.rentvideo.exception.ResourceNotFoundException;
import com.rentvideo.repository.RentalRepository;
import com.rentvideo.repository.UserRepository;
import com.rentvideo.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalService {

    public static final int MAX_ACTIVE_RENTALS = 2;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public RentalResponse rentVideo(String email, Long videoId) {
        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Video video = videoRepository.findByIdForUpdate(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));

        if (!video.isAvailabilityStatus()) {
            throw new ConflictException("Video is not available for rent: " + videoId);
        }
        if (rentalRepository.countByUserIdAndReturnedAtIsNull(user.getId()) >= MAX_ACTIVE_RENTALS) {
            throw new ConflictException("Rental limit reached: a user can have at most "
                    + MAX_ACTIVE_RENTALS + " active rentals");
        }

        video.setAvailabilityStatus(false);
        videoRepository.save(video);

        Rental rental = rentalRepository.save(Rental.builder()
                .user(user)
                .video(video)
                .rentedAt(LocalDateTime.now())
                .build());
        return toResponse(rental);
    }

    @Transactional
    public RentalResponse returnVideo(String email, Long videoId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        Video video = videoRepository.findByIdForUpdate(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));

        Rental rental = rentalRepository.findByUserIdAndVideoIdAndReturnedAtIsNull(user.getId(), videoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active rental of video " + videoId + " for this user"));

        rental.setReturnedAt(LocalDateTime.now());
        video.setAvailabilityStatus(true);
        videoRepository.save(video);
        return toResponse(rentalRepository.save(rental));
    }

    @Transactional(readOnly = true)
    public List<RentalResponse> getActiveRentals(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return rentalRepository.findByUserIdAndReturnedAtIsNull(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private RentalResponse toResponse(Rental rental) {
        return RentalResponse.builder()
                .id(rental.getId())
                .videoId(rental.getVideo().getId())
                .videoTitle(rental.getVideo().getTitle())
                .rentedAt(rental.getRentedAt())
                .returnedAt(rental.getReturnedAt())
                .build();
    }
}

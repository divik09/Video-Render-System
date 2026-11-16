package com.rentvideo.service;

import com.rentvideo.dto.VideoRequest;
import com.rentvideo.dto.VideoResponse;
import com.rentvideo.entity.Video;
import com.rentvideo.exception.ResourceNotFoundException;
import com.rentvideo.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoService videoService;

    private Video video;
    private VideoRequest videoRequest;

    @BeforeEach
    void setUp() {
        video = Video.builder()
                .id(1L)
                .title("The Matrix")
                .director("Wachowski Sisters")
                .genre("Sci-Fi")
                .availabilityStatus(true)
                .build();

        videoRequest = new VideoRequest();
        videoRequest.setTitle("The Matrix");
        videoRequest.setDirector("Wachowski Sisters");
        videoRequest.setGenre("Sci-Fi");
        videoRequest.setAvailabilityStatus(true);
    }

    @Test
    void getAllVideos_ShouldReturnAllVideos() {
        Video video2 = Video.builder()
                .id(2L).title("Inception").director("Christopher Nolan")
                .genre("Sci-Fi").availabilityStatus(true).build();

        when(videoRepository.findAll()).thenReturn(Arrays.asList(video, video2));

        List<VideoResponse> result = videoService.getAllVideos();

        assertEquals(2, result.size());
        assertEquals("The Matrix", result.get(0).getTitle());
        assertEquals("Inception", result.get(1).getTitle());
        verify(videoRepository).findAll();
    }

    @Test
    void getVideoById_WhenFound_ShouldReturnVideo() {
        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));

        VideoResponse result = videoService.getVideoById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("The Matrix", result.getTitle());
    }

    @Test
    void getVideoById_WhenNotFound_ShouldThrowException() {
        when(videoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> videoService.getVideoById(99L));
    }

    @Test
    void createVideo_ShouldReturnCreatedVideo() {
        when(videoRepository.save(any(Video.class))).thenReturn(video);

        VideoResponse result = videoService.createVideo(videoRequest);

        assertNotNull(result);
        assertEquals("The Matrix", result.getTitle());
        assertEquals("Wachowski Sisters", result.getDirector());
        assertTrue(result.isAvailabilityStatus());
        verify(videoRepository).save(any(Video.class));
    }

    @Test
    void updateVideo_WhenFound_ShouldReturnUpdatedVideo() {
        VideoRequest updateReq = new VideoRequest();
        updateReq.setTitle("The Matrix Reloaded");
        updateReq.setDirector("Wachowski Sisters");
        updateReq.setGenre("Sci-Fi");
        updateReq.setAvailabilityStatus(true);

        Video updated = Video.builder().id(1L).title("The Matrix Reloaded")
                .director("Wachowski Sisters").genre("Sci-Fi").availabilityStatus(true).build();

        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));
        when(videoRepository.save(any(Video.class))).thenReturn(updated);

        VideoResponse result = videoService.updateVideo(1L, updateReq);

        assertEquals("The Matrix Reloaded", result.getTitle());
    }

    @Test
    void updateVideo_WhenNotFound_ShouldThrowException() {
        when(videoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> videoService.updateVideo(99L, videoRequest));
    }

    @Test
    void deleteVideo_WhenFound_ShouldDeleteSuccessfully() {
        when(videoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(videoRepository).deleteById(1L);

        assertDoesNotThrow(() -> videoService.deleteVideo(1L));
        verify(videoRepository).deleteById(1L);
    }

    @Test
    void deleteVideo_WhenNotFound_ShouldThrowException() {
        when(videoRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> videoService.deleteVideo(99L));
        verify(videoRepository, never()).deleteById(anyLong());
    }
}

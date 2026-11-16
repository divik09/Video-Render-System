package com.rentvideo.service;

import com.rentvideo.dto.VideoRequest;
import com.rentvideo.dto.VideoResponse;
import com.rentvideo.entity.Video;
import com.rentvideo.exception.ResourceNotFoundException;
import com.rentvideo.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    public List<VideoResponse> getAllVideos() {
        return videoRepository.findByAvailabilityStatusTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public VideoResponse getVideoById(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
        return toResponse(video);
    }

    public VideoResponse createVideo(VideoRequest request) {
        Video video = Video.builder()
                .title(request.getTitle())
                .director(request.getDirector())
                .genre(request.getGenre())
                .availabilityStatus(request.isAvailabilityStatus())
                .build();
        return toResponse(videoRepository.save(video));
    }

    public VideoResponse updateVideo(Long id, VideoRequest request) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));

        video.setTitle(request.getTitle());
        video.setDirector(request.getDirector());
        video.setGenre(request.getGenre());
        video.setAvailabilityStatus(request.isAvailabilityStatus());

        return toResponse(videoRepository.save(video));
    }

    public void deleteVideo(Long id) {
        if (!videoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Video not found with id: " + id);
        }
        videoRepository.deleteById(id);
    }

    private VideoResponse toResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .director(video.getDirector())
                .genre(video.getGenre())
                .availabilityStatus(video.isAvailabilityStatus())
                .build();
    }
}

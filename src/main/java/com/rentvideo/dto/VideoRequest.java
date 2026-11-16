package com.rentvideo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VideoRequest {
    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "director is required")
    private String director;

    @NotBlank(message = "genre is required")
    private String genre;

    private boolean availabilityStatus = true;
}

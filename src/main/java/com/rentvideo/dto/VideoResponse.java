package com.rentvideo.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoResponse {
    private Long id;
    private String title;
    private String director;
    private String genre;
    private boolean availabilityStatus;
}

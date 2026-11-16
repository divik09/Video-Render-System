package com.rentvideo.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RentalResponse {
    private Long id;
    private Long videoId;
    private String videoTitle;
    private LocalDateTime rentedAt;
    private LocalDateTime returnedAt;
}

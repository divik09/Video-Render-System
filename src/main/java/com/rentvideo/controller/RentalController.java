package com.rentvideo.controller;

import com.rentvideo.dto.RentalResponse;
import com.rentvideo.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Any authenticated user (CUSTOMER or ADMIN) may rent and return videos. */
@RestController
public class RentalController {

    @Autowired
    private RentalService rentalService;

    /** POST /videos/{videoId}/rent */
    @PostMapping("/videos/{videoId}/rent")
    public ResponseEntity<RentalResponse> rent(@PathVariable Long videoId, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rentalService.rentVideo(auth.getName(), videoId));
    }

    /** POST /videos/{videoId}/return */
    @PostMapping("/videos/{videoId}/return")
    public ResponseEntity<RentalResponse> returnVideo(@PathVariable Long videoId, Authentication auth) {
        return ResponseEntity.ok(rentalService.returnVideo(auth.getName(), videoId));
    }

    /** GET /rentals — caller's active rentals */
    @GetMapping("/rentals")
    public ResponseEntity<List<RentalResponse>> myRentals(Authentication auth) {
        return ResponseEntity.ok(rentalService.getActiveRentals(auth.getName()));
    }
}

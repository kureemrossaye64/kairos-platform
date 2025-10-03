package com.kairos.sports_atlas.controllers;


import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kairos.sports_atlas.facility.dto.PendingReviewItem;
import com.kairos.sports_atlas.services.ReviewService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/pending")
    public ResponseEntity<List<PendingReviewItem>> getPendingItems() {
    	return ResponseEntity.ok(reviewService.getPendingItems());
    }
    
    @PostMapping("/approve/{type}/{id}")
    public ResponseEntity<Void> approveItem(@PathVariable String type, @PathVariable UUID id) {
    	reviewService.approveItem(type, id);
         return ResponseEntity.ok().build();
    }
}
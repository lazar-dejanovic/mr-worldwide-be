package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.user.UserTripPreferenceDto;
import com.raf.mrworldwide.domain.dto.user.UserTripPreferenceRequest;
import com.raf.mrworldwide.services.ums.UserTripPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/preferences")
@RequiredArgsConstructor
public class UserTripPreferenceController {

    private final UserTripPreferenceService userTripPreferenceService;

    @GetMapping
    public ResponseEntity<UserTripPreferenceDto> get() {
        return ResponseEntity.ok(userTripPreferenceService.get());
    }

    @PostMapping
    public ResponseEntity<UserTripPreferenceDto> create(@RequestBody UserTripPreferenceRequest request) {
        return ResponseEntity.ok(userTripPreferenceService.createOrUpdate(request));
    }

    @PutMapping
    public ResponseEntity<UserTripPreferenceDto> update(@RequestBody UserTripPreferenceRequest request) {
        return ResponseEntity.ok(userTripPreferenceService.createOrUpdate(request));
    }
}


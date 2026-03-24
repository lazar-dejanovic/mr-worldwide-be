package com.raf.mrworldwide.web.controllers.user;

import com.raf.mrworldwide.domain.dto.ai.AIInteractionDto;
import com.raf.mrworldwide.domain.dto.ai.AIMessageRequest;
import com.raf.mrworldwide.domain.dto.ai.AIMessageResponse;
import com.raf.mrworldwide.services.ai.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips/{tripId}/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping("/chat")
    public ResponseEntity<AIMessageResponse> chat(@PathVariable UUID tripId,
                                                   @RequestBody AIMessageRequest request) {
        return ResponseEntity.ok(aiService.chat(tripId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AIInteractionDto>> history(@PathVariable UUID tripId) {
        return ResponseEntity.ok(aiService.getHistory(tripId));
    }
}


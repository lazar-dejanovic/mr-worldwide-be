package com.raf.mrworldwide.services.ai;

import com.raf.mrworldwide.dao.repositories.AIInteractionRepository;
import com.raf.mrworldwide.domain.dto.ai.AIInteractionDto;
import com.raf.mrworldwide.domain.dto.ai.AIMessageRequest;
import com.raf.mrworldwide.domain.dto.ai.AIMessageResponse;
import com.raf.mrworldwide.domain.entities.ai.AIInteraction;
import com.raf.mrworldwide.domain.entities.ai.SenderType;
import com.raf.mrworldwide.domain.entities.trip.DailyItinerary;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.trip.TripSegment;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.mappers.AIInteractionMapper;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.services.trip.TripPlanService;
import com.raf.mrworldwide.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIService {

    private final ChatClient chatClient;
    private final AIInteractionRepository aiInteractionRepository;
    private final TripPlanService tripPlanService;

    @Transactional
    public AIMessageResponse chat(UUID tripId, AIMessageRequest request) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);

        if (!user.getEmail().equals(tripPlan.getCreatedBy())) {
            throw new ForbiddenException("You are not authorized to chat on this trip plan");
        }

        List<AIInteraction> history = aiInteractionRepository.findByTripPlanOrderByTimestampAsc(tripPlan);

        // Build messages list: system prompt + conversation history + new user message
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(buildSystemPrompt(user, tripPlan)));
        for (AIInteraction interaction : history) {
            if (interaction.getSenderType() == SenderType.USER) {
                messages.add(new UserMessage(interaction.getMessage()));
            } else {
                messages.add(new AssistantMessage(interaction.getMessage()));
            }
        }
        messages.add(new UserMessage(request.message()));

        String reply = chatClient.prompt(new Prompt(messages)).call().content();
        ZonedDateTime now = ZonedDateTime.now();

        // Persist user message
        aiInteractionRepository.save(AIInteraction.builder()
                .tripPlan(tripPlan)
                .user(user)
                .message(request.message())
                .senderType(SenderType.USER)
                .timestamp(now)
                .build());

        // Persist AI reply
        aiInteractionRepository.save(AIInteraction.builder()
                .tripPlan(tripPlan)
                .user(user)
                .message(reply)
                .senderType(SenderType.AI)
                .timestamp(now.plusNanos(1))
                .build());

        return new AIMessageResponse(reply, now);
    }

    public List<AIInteractionDto> getHistory(UUID tripId) {
        User user = AuthUtils.getLoggedUser();
        TripPlan tripPlan = tripPlanService.getEntityById(tripId);

        if (!user.getEmail().equals(tripPlan.getCreatedBy())) {
            throw new ForbiddenException("You are not authorized to view this chat history");
        }

        return aiInteractionRepository.findByTripPlanOrderByTimestampAsc(tripPlan)
                .stream()
                .map(AIInteractionMapper.INSTANCE::toDto)
                .toList();
    }

    private String buildSystemPrompt(User user, TripPlan tripPlan) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a travel assistant for MR Worldwide.\n\n");
        sb.append("User: ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n");

        if (user.getUserTripPreference() != null) {
            var pref = user.getUserTripPreference();
            if (pref.getInterests() != null) sb.append("Interests: ").append(pref.getInterests()).append("\n");
            if (pref.getHobbies() != null) sb.append("Hobbies: ").append(pref.getHobbies()).append("\n");
            if (pref.getFavouriteDestinations() != null)
                sb.append("Favourite destinations: ").append(pref.getFavouriteDestinations()).append("\n");
        }

        sb.append("\nTrip Plan: ").append(tripPlan.getName()).append("\n");
        sb.append("Status: ").append(tripPlan.getStatus()).append("\n");
        sb.append("Dates: ").append(tripPlan.getStartDate()).append(" → ").append(tripPlan.getEndDate()).append("\n");
        if (tripPlan.getDestinations() != null) sb.append("Destinations: ").append(tripPlan.getDestinations()).append("\n");
        if (tripPlan.getInterests() != null) sb.append("Trip interests: ").append(tripPlan.getInterests()).append("\n");

        if (tripPlan.getTripSegments() != null && !tripPlan.getTripSegments().isEmpty()) {
            sb.append("\nSegments:\n");
            for (TripSegment seg : tripPlan.getTripSegments()) {
                sb.append("  [").append(seg.getOrderIndex()).append("] ")
                        .append(seg.getDeparture()).append(" → ").append(seg.getDestination())
                        .append(" (").append(seg.getArrivalDate()).append(" to ").append(seg.getDepartureDate()).append(")\n");
                if (seg.getTransport() != null) {
                    sb.append("    Transport: ").append(seg.getTransport().getTransportType()).append("\n");
                }
                if (seg.getAccommodation() != null) {
                    sb.append("    Accommodation: ").append(seg.getAccommodation().getName()).append("\n");
                }
                if (!seg.getDailyItineraries().isEmpty()) {
                    sb.append("    Activities:\n");
                    for (DailyItinerary it : seg.getDailyItineraries()) {
                        sb.append("      - ").append(it.getDay())
                                .append(" ").append(it.getStartTime()).append("-").append(it.getEndTime())
                                .append(": ").append(it.getName())
                                .append(" (").append(it.getCategory()).append(")\n");
                    }
                }
            }
        }

        sb.append("\nProvide helpful, accurate travel advice. Warn about schedule conflicts when detected.");
        return sb.toString();
    }
}


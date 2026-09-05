package com.hcmcyu.event.controller;

import com.hcmcyu.event.dto.EventRequest;
import com.hcmcyu.event.dto.EventParticipationRequest;
import com.hcmcyu.event.dto.EventParticipationResponse;
import com.hcmcyu.event.dto.EventParticipationSummaryResponse;
import com.hcmcyu.event.dto.EventResponse;
import com.hcmcyu.event.entity.EventStatus;
import com.hcmcyu.event.entity.EventType;
import com.hcmcyu.event.security.CurrentUser;
import com.hcmcyu.event.service.EventParticipationService;
import com.hcmcyu.event.service.EventService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final EventParticipationService participationService;

    public EventController(EventService eventService, EventParticipationService participationService) {
        this.eventService = eventService;
        this.participationService = participationService;
    }

    @GetMapping
    public Page<EventResponse> findAll(
            @RequestParam(required = false, name = "type") EventType type,
            @RequestParam(required = false, name = "organization") String organizationId,
            @RequestParam(required = false, name = "status") EventStatus status,
            @RequestParam(required = false, name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, name = "upcoming") Boolean upcoming,
            Pageable pageable,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return eventService.findAll(type, organizationId, status, date, upcoming, pageable, currentUser);
    }

    @GetMapping("/{id}")
    public EventResponse findById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return eventService.findById(id, currentUser);
    }

    @GetMapping("/me")
    public List<EventParticipationResponse> findMyEvents(@AuthenticationPrincipal CurrentUser currentUser) {
        return participationService.findCurrentMemberEvents(currentUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return eventService.create(request, currentUser);
    }

    @PutMapping("/{eventId}/participation")
    public EventParticipationResponse updateParticipation(
            @PathVariable("eventId") String eventId,
            @Valid @RequestBody EventParticipationRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return participationService.updateParticipation(eventId, request, currentUser);
    }

    @GetMapping("/{eventId}/participants")
    public List<EventParticipationResponse> findParticipants(
            @PathVariable("eventId") String eventId,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return participationService.findParticipants(eventId, currentUser);
    }

    @GetMapping("/{eventId}/participation-summary")
    public EventParticipationSummaryResponse participationSummary(
            @PathVariable("eventId") String eventId,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return participationService.summarize(eventId, currentUser);
    }

    @PutMapping("/{id}")
    public EventResponse update(
            @PathVariable("id") String id,
            @Valid @RequestBody EventRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return eventService.update(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        eventService.delete(id, currentUser);
    }
}

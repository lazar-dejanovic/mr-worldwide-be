package com.raf.mrworldwide.services;

import com.raf.mrworldwide.BaseServiceTest;
import com.raf.mrworldwide.dao.repositories.TripPlanRepository;
import com.raf.mrworldwide.domain.dto.trip.TripPlanDetailDto;
import com.raf.mrworldwide.domain.dto.trip.TripPlanDto;
import com.raf.mrworldwide.domain.dto.trip.TripPlanRequest;
import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.trip.TripPlanStatus;
import com.raf.mrworldwide.exceptions.BadRequestException;
import com.raf.mrworldwide.exceptions.ForbiddenException;
import com.raf.mrworldwide.exceptions.NotFoundException;
import com.raf.mrworldwide.services.trip.TripPlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripPlanServiceTest extends BaseServiceTest {

    @Mock
    private TripPlanRepository tripPlanRepository;

    @InjectMocks
    private TripPlanService tripPlanService;

    // -----------------------------------------------------------------------
    // create
    // -----------------------------------------------------------------------

    @Test
    void create_shouldSaveTripPlanWithDraftStatusAndReturnDto() {
        TripPlanRequest request = new TripPlanRequest(
                "European Adventure",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                List.of("MUSEUM", "FOOD")
        );
        TripPlan saved = TripPlan.builder()
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .interests(request.interests())
                .status(TripPlanStatus.DRAFT)
                .user(loggedUser)
                .build();
        when(tripPlanRepository.save(any(TripPlan.class))).thenReturn(saved);

        TripPlanDto result = tripPlanService.create(request);

        assertThat(result.name()).isEqualTo("European Adventure");
        assertThat(result.status()).isEqualTo(TripPlanStatus.DRAFT);

        ArgumentCaptor<TripPlan> captor = ArgumentCaptor.forClass(TripPlan.class);
        verify(tripPlanRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TripPlanStatus.DRAFT);
        assertThat(captor.getValue().getUser()).isEqualTo(loggedUser);
    }

    // -----------------------------------------------------------------------
    // getById
    // -----------------------------------------------------------------------

    @Test
    void getById_shouldReturnDetailDto_whenUserIsOwner() {
        TripPlan plan = buildOwnedTripPlan(PLAN_ID, TripPlanStatus.DRAFT);
        when(tripPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        TripPlanDetailDto result = tripPlanService.getById(PLAN_ID);

        assertThat(result.name()).isEqualTo("Test Trip");
    }

    @Test
    void getById_shouldThrowForbiddenException_whenUserIsNotOwner() {
        TripPlan plan = buildOwnedTripPlan(PLAN_ID, TripPlanStatus.DRAFT);
        plan.setCreatedBy("someone-else@example.com"); // different owner
        when(tripPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> tripPlanService.getById(PLAN_ID))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void getById_shouldThrowNotFoundException_whenPlanDoesNotExist() {
        when(tripPlanRepository.findById(PLAN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripPlanService.getById(PLAN_ID))
                .isInstanceOf(NotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // update
    // -----------------------------------------------------------------------

    @Test
    void update_shouldApplyChangesAndReturnDto_whenUserIsOwner() {
        TripPlan plan = buildOwnedTripPlan(PLAN_ID, TripPlanStatus.DRAFT);
        when(tripPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
        when(tripPlanRepository.save(any(TripPlan.class))).thenReturn(plan);

        TripPlanRequest request = new TripPlanRequest(
                "Updated Name", LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31), List.of("PARK"));

        TripPlanDto result = tripPlanService.update(PLAN_ID, request);

        assertThat(result.name()).isEqualTo("Updated Name");
        verify(tripPlanRepository).save(plan);
    }

    @Test
    void update_shouldThrowForbiddenException_whenUserIsNotOwner() {
        TripPlan plan = buildOwnedTripPlan(PLAN_ID, TripPlanStatus.DRAFT);
        plan.setCreatedBy("other@example.com");
        when(tripPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        TripPlanRequest request = new TripPlanRequest("New Name", null, null, List.of());

        assertThatThrownBy(() -> tripPlanService.update(PLAN_ID, request))
                .isInstanceOf(ForbiddenException.class);

        verify(tripPlanRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // delete
    // -----------------------------------------------------------------------

    @Test
    void delete_shouldRemovePlan_whenUserIsOwner() {
        TripPlan plan = buildOwnedTripPlan(PLAN_ID, TripPlanStatus.DRAFT);
        when(tripPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        tripPlanService.delete(PLAN_ID);

        verify(tripPlanRepository).delete(plan);
    }

    @Test
    void delete_shouldThrowForbiddenException_whenUserIsNotOwner() {
        TripPlan plan = buildOwnedTripPlan(PLAN_ID, TripPlanStatus.DRAFT);
        plan.setCreatedBy("other@example.com");
        when(tripPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> tripPlanService.delete(PLAN_ID))
                .isInstanceOf(ForbiddenException.class);

        verify(tripPlanRepository, never()).delete(any());
    }

    // -----------------------------------------------------------------------
    // updateStatus — valid transitions
    // -----------------------------------------------------------------------

    @Test
    void updateStatus_DRAFT_to_PLANNED_shouldSucceed() {
        assertValidTransition(TripPlanStatus.DRAFT, TripPlanStatus.PLANNED);
    }

    @Test
    void updateStatus_PLANNED_to_BOOKED_shouldSucceed() {
        assertValidTransition(TripPlanStatus.PLANNED, TripPlanStatus.BOOKED);
    }

    @Test
    void updateStatus_BOOKED_to_COMPLETED_shouldSucceed() {
        assertValidTransition(TripPlanStatus.BOOKED, TripPlanStatus.COMPLETED);
    }

    // -----------------------------------------------------------------------
    // updateStatus — invalid transitions
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "{0} -> {1} should throw BadRequestException")
    @CsvSource({
            "DRAFT,    BOOKED",
            "DRAFT,    COMPLETED",
            "PLANNED,  COMPLETED",
            "PLANNED,  DRAFT",
            "COMPLETED,PLANNED",
            "COMPLETED,BOOKED"
    })
    void updateStatus_invalidTransition_shouldThrowBadRequestException(
            TripPlanStatus current, TripPlanStatus next) {

        TripPlan plan = buildOwnedTripPlan(PLAN_ID, current);
        when(tripPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));

        assertThatThrownBy(() -> tripPlanService.updateStatus(PLAN_ID, next))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status transition");

        verify(tripPlanRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void assertValidTransition(TripPlanStatus from, TripPlanStatus to) {
        UUID planId = UUID.randomUUID();
        TripPlan plan = buildOwnedTripPlan(planId, from);
        when(tripPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(tripPlanRepository.save(any(TripPlan.class))).thenReturn(plan);

        TripPlanDto result = tripPlanService.updateStatus(planId, to);

        assertThat(result).isNotNull();
        verify(tripPlanRepository).save(plan);
    }
}


package com.raf.mrworldwide;

import com.raf.mrworldwide.domain.entities.trip.TripPlan;
import com.raf.mrworldwide.domain.entities.trip.TripPlanStatus;
import com.raf.mrworldwide.domain.entities.user.Role;
import com.raf.mrworldwide.domain.entities.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Base class for unit tests that exercise service methods relying on
 * {@code AuthUtils.getLoggedUser()}.  Each test starts with a stubbed
 * {@link User} stored in the {@link SecurityContextHolder}; the context
 * is cleared afterwards so tests cannot bleed into each other.
 */
public abstract class BaseServiceTest {

    protected static final UUID USER_ID   = UUID.randomUUID();
    protected static final UUID PLAN_ID   = UUID.randomUUID();
    protected static final String USER_EMAIL = "test@example.com";

    protected User loggedUser;

    @BeforeEach
    void setUpSecurityContext() {
        loggedUser = buildUser(USER_ID, USER_EMAIL, Role.REGULAR_USER);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loggedUser, null,
                        loggedUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // -----------------------------------------------------------------------
    // Entity builder helpers
    // -----------------------------------------------------------------------

    protected User buildUser(UUID id, String email, Role role) {
        User user = User.builder()
                .email(email)
                .firstName("Test")
                .lastName("User")
                .password("encodedPassword")
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    protected TripPlan buildOwnedTripPlan(UUID id, TripPlanStatus status) {
        TripPlan plan = TripPlan.builder()
                .name("Test Trip")
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .interests(List.of("MUSEUM"))
                .status(status)
                .user(loggedUser)
                .build();
        ReflectionTestUtils.setField(plan, "id", id);
        plan.setCreatedBy(USER_EMAIL);
        return plan;
    }
}


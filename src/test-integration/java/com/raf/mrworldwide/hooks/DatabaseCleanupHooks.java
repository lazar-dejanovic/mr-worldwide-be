package com.raf.mrworldwide.hooks;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Truncates all data tables before each scenario so every test starts with a clean DB.
 * Order respects FK dependencies; CASCADE handles any remaining references.
 */
public class DatabaseCleanupHooks {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void cleanDatabase() {
        jdbcTemplate.execute(
                "TRUNCATE TABLE " +
                "ai_interaction, " +
                "plan_share, " +
                "daily_itinerary, " +
                "trip_segment, " +
                "airplane_transport, " +
                "vehicle_transport, " +
                "transport, " +
                "accommodation, " +
                "trip_plan, " +
                "user_trip_preference, " +
                "\"user\" " +
                "RESTART IDENTITY CASCADE"
        );
    }
}


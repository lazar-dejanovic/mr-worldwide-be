package com.raf.mrworldwide.config;

import com.raf.mrworldwide.dao.repositories.*;
import com.raf.mrworldwide.domain.entities.accomodation.Accommodation;
import com.raf.mrworldwide.domain.entities.transport.AirplaneTransport;
import com.raf.mrworldwide.domain.entities.transport.Transport;
import com.raf.mrworldwide.domain.entities.transport.TransportType;
import com.raf.mrworldwide.domain.entities.transport.VehicleTransport;
import com.raf.mrworldwide.domain.entities.trip.*;
import com.raf.mrworldwide.domain.entities.user.Role;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.domain.entities.user.UserTripPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserTripPreferenceRepository userTripPreferenceRepository;
    private final TripPlanRepository tripPlanRepository;
    private final TripSegmentRepository tripSegmentRepository;
    private final AccommodationRepository accommodationRepository;
    private final TransportRepository transportRepository;
    private final AirplaneTransportRepository airplaneTransportRepository;
    private final VehicleTransportRepository vehicleTransportRepository;
    private final DailyItineraryRepository dailyItineraryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data — skipping seed.");
            return;
        }

        log.info("Seeding database with demo data...");

        User user1 = createUser("lazzar99@gmail.com", "Lazar", "Dejanovic",
                List.of("Culture", "History", "Food"),
                List.of("Photography", "Hiking"),
                List.of("Paris", "Rome", "Tokyo"));

        User user2 = createUser("ooggii05@gmail.com", "Ognjen", "Dejanovic",
                List.of("Adventure", "Nature", "Sports"),
                List.of("Skiing", "Surfing"),
                List.of("Oslo", "Bangkok", "New York"));

        seedUser1Trips(user1);
        seedUser2Trips(user2);

        log.info("Seed data loaded successfully.");
    }

    // ── User creation ──────────────────────────────────────────────────────────

    private User createUser(String email, String firstName, String lastName,
                            List<String> interests, List<String> hobbies,
                            List<String> favouriteDestinations) {
        UserTripPreference pref = UserTripPreference.builder()
                .name(firstName + "'s Preferences")
                .interests(interests)
                .hobbies(hobbies)
                .favouriteDestinations(favouriteDestinations)
                .build();
        userTripPreferenceRepository.save(pref);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("Password1!"))
                .firstName(firstName)
                .lastName(lastName)
                .role(Role.REGULAR_USER)
                .userTripPreference(pref)
                .build();
        return userRepository.save(user);
    }

    // ── Authenticate as a given user so JPA auditing sets createdBy correctly ──

    private void authenticateAs(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    private void seedUser1Trips(User user) {
        authenticateAs(user);
        try {
            seedEuropeanHighlights(user);
            seedBalkanRoadTrip(user);
            seedJapanExplorer(user);
        } finally {
            clearAuth();
        }
    }

    /** Trip 1 — European Highlights: NYC → Paris → Rome → Barcelona */
    private void seedEuropeanHighlights(User user) {
        TripPlan plan = tripPlanRepository.save(TripPlan.builder()
                .name("European Highlights")
                .destinations(List.of("Paris", "Rome", "Barcelona"))
                .interests(List.of("Culture", "History", "Food"))
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 22))
                .status(TripPlanStatus.PLANNED)
                .user(user)
                .build());

        // ── Segment 1: NYC → Paris ──────────────────────────────────────────
        AirplaneTransport flight1 = airplaneTransportRepository.save(AirplaneTransport.builder()
                .flightNumber("AA100")
                .departureTime(LocalDateTime.of(2026, 6, 1, 9, 0))
                .arrivalTime(LocalDateTime.of(2026, 6, 1, 21, 30))
                .duration("7h 30m")
                .price(620.0)
                .currency("USD")
                .build());

        Transport transport1 = transportRepository.save(Transport.builder()
                .transportType(TransportType.AIRPLANE)
                .airplaneTransport(flight1)
                .build());

        Accommodation hotel1 = accommodationRepository.save(Accommodation.builder()
                .name("Hotel Paris Central")
                .address("12 Rue de la Paix, Paris")
                .starRating(4.0)
                .reviewScore(8.7)
                .checkIn(LocalDate.of(2026, 6, 1))
                .checkOut(LocalDate.of(2026, 6, 8))
                .priceTotal(980.0)
                .currency("EUR")
                .build());

        TripSegment seg1 = tripSegmentRepository.save(TripSegment.builder()
                .departure("New York")
                .destination("Paris")
                .arrivalDate(LocalDate.of(2026, 6, 1))
                .departureDate(LocalDate.of(2026, 6, 8))
                .orderIndex(1)
                .destinationLatitude(48.8566)
                .destinationLongitude(2.3522)
                .transport(transport1)
                .accommodation(hotel1)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg1, "Eiffel Tower Visit", "Landmark", "13 Champ de Mars, Paris",
                        48.8584, 2.2945, LocalDate.of(2026, 6, 2),
                        LocalTime.of(10, 0), LocalTime.of(12, 30)),
                itinerary(seg1, "Louvre Museum", "Museum", "Rue de Rivoli, Paris",
                        48.8606, 2.3376, LocalDate.of(2026, 6, 3),
                        LocalTime.of(9, 0), LocalTime.of(13, 0)),
                itinerary(seg1, "Seine River Cruise", "Activity", "Port de la Bourdonnais, Paris",
                        48.8600, 2.3000, LocalDate.of(2026, 6, 4),
                        LocalTime.of(15, 0), LocalTime.of(16, 30))
        ));

        // ── Segment 2: Paris → Rome ─────────────────────────────────────────
        AirplaneTransport flight2 = airplaneTransportRepository.save(AirplaneTransport.builder()
                .flightNumber("AF1234")
                .departureTime(LocalDateTime.of(2026, 6, 8, 11, 0))
                .arrivalTime(LocalDateTime.of(2026, 6, 8, 13, 10))
                .duration("2h 10m")
                .price(180.0)
                .currency("EUR")
                .build());

        Transport transport2 = transportRepository.save(Transport.builder()
                .transportType(TransportType.AIRPLANE)
                .airplaneTransport(flight2)
                .build());

        Accommodation hotel2 = accommodationRepository.save(Accommodation.builder()
                .name("Hotel Roma Palace")
                .address("Via Nazionale 7, Rome")
                .starRating(4.5)
                .reviewScore(9.0)
                .checkIn(LocalDate.of(2026, 6, 8))
                .checkOut(LocalDate.of(2026, 6, 15))
                .priceTotal(1120.0)
                .currency("EUR")
                .build());

        TripSegment seg2 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Paris")
                .destination("Rome")
                .arrivalDate(LocalDate.of(2026, 6, 8))
                .departureDate(LocalDate.of(2026, 6, 15))
                .orderIndex(2)
                .destinationLatitude(41.9028)
                .destinationLongitude(12.4964)
                .transport(transport2)
                .accommodation(hotel2)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg2, "Colosseum Tour", "Landmark", "Piazza del Colosseo 1, Rome",
                        41.8902, 12.4922, LocalDate.of(2026, 6, 9),
                        LocalTime.of(9, 0), LocalTime.of(11, 30)),
                itinerary(seg2, "Vatican Museums", "Museum", "Viale Vaticano, Vatican City",
                        41.9065, 12.4536, LocalDate.of(2026, 6, 10),
                        LocalTime.of(8, 0), LocalTime.of(13, 0)),
                itinerary(seg2, "Trevi Fountain", "Landmark", "Piazza di Trevi, Rome",
                        41.9009, 12.4833, LocalDate.of(2026, 6, 11),
                        LocalTime.of(18, 0), LocalTime.of(19, 30))
        ));

        // ── Segment 3: Rome → Barcelona ─────────────────────────────────────
        AirplaneTransport flight3 = airplaneTransportRepository.save(AirplaneTransport.builder()
                .flightNumber("VY6203")
                .departureTime(LocalDateTime.of(2026, 6, 15, 14, 0))
                .arrivalTime(LocalDateTime.of(2026, 6, 15, 16, 45))
                .duration("2h 45m")
                .price(135.0)
                .currency("EUR")
                .build());

        Transport transport3 = transportRepository.save(Transport.builder()
                .transportType(TransportType.AIRPLANE)
                .airplaneTransport(flight3)
                .build());

        Accommodation hotel3 = accommodationRepository.save(Accommodation.builder()
                .name("Barcelona Suites")
                .address("Passeig de Gràcia 55, Barcelona")
                .starRating(4.0)
                .reviewScore(8.5)
                .checkIn(LocalDate.of(2026, 6, 15))
                .checkOut(LocalDate.of(2026, 6, 22))
                .priceTotal(860.0)
                .currency("EUR")
                .build());

        TripSegment seg3 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Rome")
                .destination("Barcelona")
                .arrivalDate(LocalDate.of(2026, 6, 15))
                .departureDate(LocalDate.of(2026, 6, 22))
                .orderIndex(3)
                .destinationLatitude(41.3851)
                .destinationLongitude(2.1734)
                .transport(transport3)
                .accommodation(hotel3)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg3, "Sagrada Família", "Landmark", "Carrer de Mallorca 401, Barcelona",
                        41.4036, 2.1744, LocalDate.of(2026, 6, 16),
                        LocalTime.of(9, 30), LocalTime.of(12, 0)),
                itinerary(seg3, "Park Güell", "Park", "Carrer d'Olot, Barcelona",
                        41.4145, 2.1527, LocalDate.of(2026, 6, 17),
                        LocalTime.of(10, 0), LocalTime.of(12, 30)),
                itinerary(seg3, "Las Ramblas Stroll", "Leisure", "La Rambla, Barcelona",
                        41.3808, 2.1734, LocalDate.of(2026, 6, 18),
                        LocalTime.of(17, 0), LocalTime.of(19, 0))
        ));
    }

    /** Trip 2 — Balkan Road Trip: Belgrade → Dubrovnik → Kotor */
    private void seedBalkanRoadTrip(User user) {
        TripPlan plan = tripPlanRepository.save(TripPlan.builder()
                .name("Balkan Road Trip")
                .destinations(List.of("Dubrovnik", "Kotor", "Mostar"))
                .interests(List.of("History", "Nature", "Coast"))
                .startDate(LocalDate.of(2026, 8, 5))
                .endDate(LocalDate.of(2026, 8, 20))
                .status(TripPlanStatus.DRAFT)
                .user(user)
                .build());

        // ── Segment 1: Belgrade → Dubrovnik ────────────────────────────────
        VehicleTransport car1 = vehicleTransportRepository.save(VehicleTransport.builder()
                .distanceKm(615.0)
                .estimatedFuelCost(72.0)
                .tollCost(18.0)
                .build());

        Transport transport1 = transportRepository.save(Transport.builder()
                .transportType(TransportType.VEHICLE)
                .vehicleTransport(car1)
                .build());

        Accommodation hotel1 = accommodationRepository.save(Accommodation.builder()
                .name("Hotel Stari Grad Dubrovnik")
                .address("Od Sigurate 4, Dubrovnik")
                .starRating(4.0)
                .reviewScore(8.9)
                .checkIn(LocalDate.of(2026, 8, 5))
                .checkOut(LocalDate.of(2026, 8, 10))
                .priceTotal(750.0)
                .currency("EUR")
                .build());

        TripSegment seg1 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Belgrade")
                .destination("Dubrovnik")
                .arrivalDate(LocalDate.of(2026, 8, 5))
                .departureDate(LocalDate.of(2026, 8, 10))
                .orderIndex(1)
                .destinationLatitude(42.6507)
                .destinationLongitude(18.0944)
                .transport(transport1)
                .accommodation(hotel1)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg1, "Old Town Walls Walk", "Landmark", "Dubrovnik City Walls",
                        42.6414, 18.1107, LocalDate.of(2026, 8, 6),
                        LocalTime.of(8, 0), LocalTime.of(10, 30)),
                itinerary(seg1, "Cable Car to Mount Srđ", "Activity", "Ul. Petra Krešimira IV, Dubrovnik",
                        42.6586, 18.1056, LocalDate.of(2026, 8, 7),
                        LocalTime.of(16, 0), LocalTime.of(18, 0)),
                itinerary(seg1, "Lokrum Island Day Trip", "Nature", "Lokrum Island, Dubrovnik",
                        42.6278, 18.1233, LocalDate.of(2026, 8, 8),
                        LocalTime.of(10, 0), LocalTime.of(15, 0))
        ));

        // ── Segment 2: Dubrovnik → Kotor ────────────────────────────────────
        VehicleTransport car2 = vehicleTransportRepository.save(VehicleTransport.builder()
                .distanceKm(95.0)
                .estimatedFuelCost(11.0)
                .tollCost(5.0)
                .build());

        Transport transport2 = transportRepository.save(Transport.builder()
                .transportType(TransportType.VEHICLE)
                .vehicleTransport(car2)
                .build());

        Accommodation hotel2 = accommodationRepository.save(Accommodation.builder()
                .name("Palazzo Drusko Kotor")
                .address("Trg od Oružja 1, Kotor")
                .starRating(4.5)
                .reviewScore(9.1)
                .checkIn(LocalDate.of(2026, 8, 10))
                .checkOut(LocalDate.of(2026, 8, 15))
                .priceTotal(630.0)
                .currency("EUR")
                .build());

        TripSegment seg2 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Dubrovnik")
                .destination("Kotor")
                .arrivalDate(LocalDate.of(2026, 8, 10))
                .departureDate(LocalDate.of(2026, 8, 15))
                .orderIndex(2)
                .destinationLatitude(42.4247)
                .destinationLongitude(18.7712)
                .transport(transport2)
                .accommodation(hotel2)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg2, "Kotor Old Town", "Landmark", "Old Town, Kotor",
                        42.4247, 18.7718, LocalDate.of(2026, 8, 11),
                        LocalTime.of(9, 0), LocalTime.of(12, 0)),
                itinerary(seg2, "Fortress of San Giovanni", "Hiking", "Kotor Fortifications",
                        42.4279, 18.7711, LocalDate.of(2026, 8, 12),
                        LocalTime.of(7, 0), LocalTime.of(10, 0)),
                itinerary(seg2, "Bay of Kotor Boat Tour", "Activity", "Kotor Harbour",
                        42.4241, 18.7746, LocalDate.of(2026, 8, 13),
                        LocalTime.of(14, 0), LocalTime.of(17, 0))
        ));

        // ── Segment 3: Kotor → Mostar ────────────────────────────────────────
        VehicleTransport car3 = vehicleTransportRepository.save(VehicleTransport.builder()
                .distanceKm(250.0)
                .estimatedFuelCost(29.0)
                .tollCost(8.0)
                .build());

        Transport transport3 = transportRepository.save(Transport.builder()
                .transportType(TransportType.VEHICLE)
                .vehicleTransport(car3)
                .build());

        Accommodation hotel3 = accommodationRepository.save(Accommodation.builder()
                .name("Hotel Kriva Cuprija Mostar")
                .address("Onešćukova 1, Mostar")
                .starRating(4.0)
                .reviewScore(8.6)
                .checkIn(LocalDate.of(2026, 8, 15))
                .checkOut(LocalDate.of(2026, 8, 20))
                .priceTotal(400.0)
                .currency("EUR")
                .build());

        TripSegment seg3 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Kotor")
                .destination("Mostar")
                .arrivalDate(LocalDate.of(2026, 8, 15))
                .departureDate(LocalDate.of(2026, 8, 20))
                .orderIndex(3)
                .destinationLatitude(43.3438)
                .destinationLongitude(17.8078)
                .transport(transport3)
                .accommodation(hotel3)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg3, "Stari Most Bridge", "Landmark", "Stari Most, Mostar",
                        43.3384, 17.8156, LocalDate.of(2026, 8, 16),
                        LocalTime.of(9, 0), LocalTime.of(10, 30)),
                itinerary(seg3, "Kujundžiluk Bazaar", "Shopping", "Bazaar Street, Mostar",
                        43.3387, 17.8161, LocalDate.of(2026, 8, 17),
                        LocalTime.of(11, 0), LocalTime.of(13, 0)),
                itinerary(seg3, "Blagaj Tekija", "Culture", "Blagaj, Mostar",
                        43.2570, 17.8893, LocalDate.of(2026, 8, 18),
                        LocalTime.of(10, 0), LocalTime.of(13, 0))
        ));
    }

    /** Trip 3 — Japan Explorer: Tokyo → Kyoto → Osaka */
    private void seedJapanExplorer(User user) {
        TripPlan plan = tripPlanRepository.save(TripPlan.builder()
                .name("Japan Explorer")
                .destinations(List.of("Tokyo", "Kyoto", "Osaka"))
                .interests(List.of("Culture", "Food", "Technology"))
                .startDate(LocalDate.of(2026, 10, 10))
                .endDate(LocalDate.of(2026, 10, 31))
                .status(TripPlanStatus.PLANNED)
                .user(user)
                .build());

        // ── Segment 1: London → Tokyo (flight) ────────────────────────────
        AirplaneTransport flight1 = airplaneTransportRepository.save(AirplaneTransport.builder()
                .flightNumber("BA007")
                .departureTime(LocalDateTime.of(2026, 10, 10, 11, 0))
                .arrivalTime(LocalDateTime.of(2026, 10, 11, 8, 30))
                .duration("11h 30m")
                .price(950.0)
                .currency("GBP")
                .build());

        Transport transport1 = transportRepository.save(Transport.builder()
                .transportType(TransportType.AIRPLANE)
                .airplaneTransport(flight1)
                .build());

        Accommodation hotel1 = accommodationRepository.save(Accommodation.builder()
                .name("Shinjuku Grand Hotel")
                .address("2-14-5 Kabukicho, Shinjuku, Tokyo")
                .starRating(4.0)
                .reviewScore(8.4)
                .checkIn(LocalDate.of(2026, 10, 11))
                .checkOut(LocalDate.of(2026, 10, 18))
                .priceTotal(1300.0)
                .currency("JPY")
                .build());

        TripSegment seg1 = tripSegmentRepository.save(TripSegment.builder()
                .departure("London")
                .destination("Tokyo")
                .arrivalDate(LocalDate.of(2026, 10, 11))
                .departureDate(LocalDate.of(2026, 10, 18))
                .orderIndex(1)
                .destinationLatitude(35.6762)
                .destinationLongitude(139.6503)
                .transport(transport1)
                .accommodation(hotel1)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg1, "Senso-ji Temple", "Temple", "2 Chome-3-1 Asakusa, Tokyo",
                        35.7148, 139.7967, LocalDate.of(2026, 10, 12),
                        LocalTime.of(8, 0), LocalTime.of(10, 30)),
                itinerary(seg1, "Shibuya Crossing & Harajuku", "Leisure", "Shibuya, Tokyo",
                        35.6598, 139.7004, LocalDate.of(2026, 10, 13),
                        LocalTime.of(13, 0), LocalTime.of(17, 0)),
                itinerary(seg1, "TeamLab Borderless", "Art & Technology", "1-3-8 Aomi, Koto, Tokyo",
                        35.6263, 139.7755, LocalDate.of(2026, 10, 14),
                        LocalTime.of(10, 0), LocalTime.of(14, 0))
        ));

        // ── Segment 2: Tokyo → Kyoto (Shinkansen / train) ──────────────────
        Transport transport2 = transportRepository.save(Transport.builder()
                .transportType(TransportType.TRAIN)
                .build());

        Accommodation hotel2 = accommodationRepository.save(Accommodation.builder()
                .name("Kyoto Machiya Inn")
                .address("Nishikikoji-dori, Nakagyo-ku, Kyoto")
                .starRating(4.5)
                .reviewScore(9.2)
                .checkIn(LocalDate.of(2026, 10, 18))
                .checkOut(LocalDate.of(2026, 10, 24))
                .priceTotal(1050.0)
                .currency("JPY")
                .build());

        TripSegment seg2 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Tokyo")
                .destination("Kyoto")
                .arrivalDate(LocalDate.of(2026, 10, 18))
                .departureDate(LocalDate.of(2026, 10, 24))
                .orderIndex(2)
                .destinationLatitude(35.0116)
                .destinationLongitude(135.7681)
                .transport(transport2)
                .accommodation(hotel2)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg2, "Fushimi Inari Taisha", "Temple", "68 Fukakusa Yabunouchicho, Fushimi, Kyoto",
                        34.9671, 135.7727, LocalDate.of(2026, 10, 19),
                        LocalTime.of(7, 0), LocalTime.of(10, 0)),
                itinerary(seg2, "Arashiyama Bamboo Grove", "Nature", "Sagaogurayama Tabuchiyamacho, Ukyo, Kyoto",
                        35.0174, 135.6724, LocalDate.of(2026, 10, 20),
                        LocalTime.of(9, 0), LocalTime.of(11, 30)),
                itinerary(seg2, "Kinkaku-ji Golden Pavilion", "Temple", "1 Kinkakujicho, Kita, Kyoto",
                        35.0394, 135.7292, LocalDate.of(2026, 10, 21),
                        LocalTime.of(9, 0), LocalTime.of(11, 0))
        ));

        // ── Segment 3: Kyoto → Osaka (train) ──────────────────────────────
        Transport transport3 = transportRepository.save(Transport.builder()
                .transportType(TransportType.TRAIN)
                .build());

        Accommodation hotel3 = accommodationRepository.save(Accommodation.builder()
                .name("Dotonbori Hotel Osaka")
                .address("1-6-14 Dotonbori, Chuo-ku, Osaka")
                .starRating(4.0)
                .reviewScore(8.8)
                .checkIn(LocalDate.of(2026, 10, 24))
                .checkOut(LocalDate.of(2026, 10, 31))
                .priceTotal(980.0)
                .currency("JPY")
                .build());

        TripSegment seg3 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Kyoto")
                .destination("Osaka")
                .arrivalDate(LocalDate.of(2026, 10, 24))
                .departureDate(LocalDate.of(2026, 10, 31))
                .orderIndex(3)
                .destinationLatitude(34.6937)
                .destinationLongitude(135.5023)
                .transport(transport3)
                .accommodation(hotel3)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg3, "Dotonbori Food Tour", "Food", "Dotonbori, Namba, Osaka",
                        34.6687, 135.5014, LocalDate.of(2026, 10, 25),
                        LocalTime.of(18, 0), LocalTime.of(21, 0)),
                itinerary(seg3, "Osaka Castle", "Landmark", "1-1 Osakajo, Chuo-ku, Osaka",
                        34.6873, 135.5262, LocalDate.of(2026, 10, 26),
                        LocalTime.of(9, 0), LocalTime.of(11, 30)),
                itinerary(seg3, "Universal Studios Japan", "Theme Park", "2-1-33 Sakurajima, Konohana, Osaka",
                        34.6654, 135.4323, LocalDate.of(2026, 10, 27),
                        LocalTime.of(9, 0), LocalTime.of(18, 0))
        ));
    }

    private void seedUser2Trips(User user) {
        authenticateAs(user);
        try {
            seedNordicAdventure(user);
            seedSoutheastAsia(user);
            seedAmericanRoadTrip(user);
        } finally {
            clearAuth();
        }
    }

    /** Trip 4 — Nordic Adventure: Oslo → Bergen → Stockholm */
    private void seedNordicAdventure(User user) {
        TripPlan plan = tripPlanRepository.save(TripPlan.builder()
                .name("Nordic Adventure")
                .destinations(List.of("Oslo", "Bergen", "Stockholm"))
                .interests(List.of("Nature", "Fjords", "Architecture"))
                .startDate(LocalDate.of(2026, 7, 5))
                .endDate(LocalDate.of(2026, 7, 26))
                .status(TripPlanStatus.PLANNED)
                .user(user)
                .build());

        // ── Segment 1: London → Oslo (flight) ──────────────────────────────
        AirplaneTransport flight1 = airplaneTransportRepository.save(AirplaneTransport.builder()
                .flightNumber("SK433")
                .departureTime(LocalDateTime.of(2026, 7, 5, 7, 30))
                .arrivalTime(LocalDateTime.of(2026, 7, 5, 10, 45))
                .duration("2h 15m")
                .price(210.0)
                .currency("GBP")
                .build());

        Transport transport1 = transportRepository.save(Transport.builder()
                .transportType(TransportType.AIRPLANE)
                .airplaneTransport(flight1)
                .build());

        Accommodation hotel1 = accommodationRepository.save(Accommodation.builder()
                .name("Oslo Grand Hotel")
                .address("Karl Johans gate 31, Oslo")
                .starRating(5.0)
                .reviewScore(9.3)
                .checkIn(LocalDate.of(2026, 7, 5))
                .checkOut(LocalDate.of(2026, 7, 12))
                .priceTotal(1400.0)
                .currency("NOK")
                .build());

        TripSegment seg1 = tripSegmentRepository.save(TripSegment.builder()
                .departure("London")
                .destination("Oslo")
                .arrivalDate(LocalDate.of(2026, 7, 5))
                .departureDate(LocalDate.of(2026, 7, 12))
                .orderIndex(1)
                .destinationLatitude(59.9139)
                .destinationLongitude(10.7522)
                .transport(transport1)
                .accommodation(hotel1)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg1, "Vigeland Sculpture Park", "Park", "Nobels gate 32, Oslo",
                        59.9270, 10.6996, LocalDate.of(2026, 7, 6),
                        LocalTime.of(10, 0), LocalTime.of(12, 0)),
                itinerary(seg1, "Viking Ship Museum", "Museum", "Huk Aveny 35, Oslo",
                        59.9046, 10.6844, LocalDate.of(2026, 7, 7),
                        LocalTime.of(9, 0), LocalTime.of(11, 30)),
                itinerary(seg1, "Aker Brygge Waterfront", "Leisure", "Aker Brygge, Oslo",
                        59.9107, 10.7279, LocalDate.of(2026, 7, 8),
                        LocalTime.of(15, 0), LocalTime.of(19, 0))
        ));

        // ── Segment 2: Oslo → Bergen (train) ───────────────────────────────
        Transport transport2 = transportRepository.save(Transport.builder()
                .transportType(TransportType.TRAIN)
                .build());

        Accommodation hotel2 = accommodationRepository.save(Accommodation.builder()
                .name("Bryggen Hotel Bergen")
                .address("Bryggen 2, Bergen")
                .starRating(4.0)
                .reviewScore(8.8)
                .checkIn(LocalDate.of(2026, 7, 12))
                .checkOut(LocalDate.of(2026, 7, 19))
                .priceTotal(1100.0)
                .currency("NOK")
                .build());

        TripSegment seg2 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Oslo")
                .destination("Bergen")
                .arrivalDate(LocalDate.of(2026, 7, 12))
                .departureDate(LocalDate.of(2026, 7, 19))
                .orderIndex(2)
                .destinationLatitude(60.3913)
                .destinationLongitude(5.3221)
                .transport(transport2)
                .accommodation(hotel2)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg2, "Bryggen Wharf", "Landmark", "Bryggen, Bergen",
                        60.3977, 5.3242, LocalDate.of(2026, 7, 13),
                        LocalTime.of(10, 0), LocalTime.of(12, 0)),
                itinerary(seg2, "Fløibanen Funicular", "Activity", "Vetrlidsallmenningen 23A, Bergen",
                        60.3935, 5.3320, LocalDate.of(2026, 7, 14),
                        LocalTime.of(9, 0), LocalTime.of(11, 0)),
                itinerary(seg2, "Sognefjord Day Trip", "Nature", "Sognefjord, Norway",
                        61.2059, 7.1006, LocalDate.of(2026, 7, 15),
                        LocalTime.of(8, 0), LocalTime.of(18, 0))
        ));

        // ── Segment 3: Bergen → Stockholm (flight) ─────────────────────────
        AirplaneTransport flight3 = airplaneTransportRepository.save(AirplaneTransport.builder()
                .flightNumber("SK1452")
                .departureTime(LocalDateTime.of(2026, 7, 19, 13, 0))
                .arrivalTime(LocalDateTime.of(2026, 7, 19, 15, 30))
                .duration("1h 30m")
                .price(175.0)
                .currency("EUR")
                .build());

        Transport transport3 = transportRepository.save(Transport.builder()
                .transportType(TransportType.AIRPLANE)
                .airplaneTransport(flight3)
                .build());

        Accommodation hotel3 = accommodationRepository.save(Accommodation.builder()
                .name("Stockholm City Hotel")
                .address("Vasagatan 1, Stockholm")
                .starRating(4.5)
                .reviewScore(8.9)
                .checkIn(LocalDate.of(2026, 7, 19))
                .checkOut(LocalDate.of(2026, 7, 26))
                .priceTotal(1250.0)
                .currency("SEK")
                .build());

        TripSegment seg3 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Bergen")
                .destination("Stockholm")
                .arrivalDate(LocalDate.of(2026, 7, 19))
                .departureDate(LocalDate.of(2026, 7, 26))
                .orderIndex(3)
                .destinationLatitude(59.3293)
                .destinationLongitude(18.0686)
                .transport(transport3)
                .accommodation(hotel3)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg3, "Gamla Stan (Old Town)", "Landmark", "Gamla Stan, Stockholm",
                        59.3252, 18.0714, LocalDate.of(2026, 7, 20),
                        LocalTime.of(10, 0), LocalTime.of(13, 0)),
                itinerary(seg3, "Vasa Museum", "Museum", "Galärvarvsvägen 14, Stockholm",
                        59.3280, 18.0914, LocalDate.of(2026, 7, 21),
                        LocalTime.of(9, 0), LocalTime.of(11, 30)),
                itinerary(seg3, "ABBA Museum", "Museum", "Djurgårdsvägen 68, Stockholm",
                        59.3275, 18.0965, LocalDate.of(2026, 7, 22),
                        LocalTime.of(12, 0), LocalTime.of(14, 0))
        ));
    }

    /** Trip 5 — Southeast Asia: Bangkok → Bali → Singapore */
    private void seedSoutheastAsia(User user) {
        TripPlan plan = tripPlanRepository.save(TripPlan.builder()
                .name("Southeast Asia Escape")
                .destinations(List.of("Bangkok", "Bali", "Singapore"))
                .interests(List.of("Beaches", "Culture", "Food"))
                .startDate(LocalDate.of(2026, 11, 1))
                .endDate(LocalDate.of(2026, 11, 22))
                .status(TripPlanStatus.BOOKED)
                .user(user)
                .build());

        // ── Segment 1: London → Bangkok (flight) ───────────────────────────
        AirplaneTransport flight1 = airplaneTransportRepository.save(AirplaneTransport.builder()
                .flightNumber("TG911")
                .departureTime(LocalDateTime.of(2026, 11, 1, 21, 30))
                .arrivalTime(LocalDateTime.of(2026, 11, 2, 14, 0))
                .duration("11h 30m")
                .price(780.0)
                .currency("GBP")
                .build());

        Transport transport1 = transportRepository.save(Transport.builder()
                .transportType(TransportType.AIRPLANE)
                .airplaneTransport(flight1)
                .build());

        Accommodation hotel1 = accommodationRepository.save(Accommodation.builder()
                .name("Mandarin Oriental Bangkok")
                .address("48 Oriental Ave, Bang Rak, Bangkok")
                .starRating(5.0)
                .reviewScore(9.5)
                .checkIn(LocalDate.of(2026, 11, 2))
                .checkOut(LocalDate.of(2026, 11, 9))
                .priceTotal(1800.0)
                .currency("THB")
                .build());

        TripSegment seg1 = tripSegmentRepository.save(TripSegment.builder()
                .departure("London")
                .destination("Bangkok")
                .arrivalDate(LocalDate.of(2026, 11, 2))
                .departureDate(LocalDate.of(2026, 11, 9))
                .orderIndex(1)
                .destinationLatitude(13.7563)
                .destinationLongitude(100.5018)
                .transport(transport1)
                .accommodation(hotel1)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg1, "Grand Palace", "Landmark", "Na Phra Lan Rd, Phra Nakhon, Bangkok",
                        13.7500, 100.4914, LocalDate.of(2026, 11, 3),
                        LocalTime.of(9, 0), LocalTime.of(12, 0)),
                itinerary(seg1, "Wat Pho Temple", "Temple", "2 Sanam Chai Rd, Bangkok",
                        13.7469, 100.4927, LocalDate.of(2026, 11, 4),
                        LocalTime.of(8, 0), LocalTime.of(10, 30)),
                itinerary(seg1, "Chatuchak Weekend Market", "Shopping", "Kamphaeng Phet 2 Rd, Bangkok",
                        13.7999, 100.5503, LocalDate.of(2026, 11, 5),
                        LocalTime.of(9, 0), LocalTime.of(14, 0))
        ));

        // ── Segment 2: Bangkok → Bali (flight) ─────────────────────────────
        AirplaneTransport flight2 = airplaneTransportRepository.save(AirplaneTransport.builder()
                .flightNumber("GA868")
                .departureTime(LocalDateTime.of(2026, 11, 9, 8, 0))
                .arrivalTime(LocalDateTime.of(2026, 11, 9, 12, 30))
                .duration("3h 30m")
                .price(220.0)
                .currency("USD")
                .build());

        Transport transport2 = transportRepository.save(Transport.builder()
                .transportType(TransportType.AIRPLANE)
                .airplaneTransport(flight2)
                .build());

        Accommodation hotel2 = accommodationRepository.save(Accommodation.builder()
                .name("Seminyak Beach Resort Bali")
                .address("Jl. Kayu Aya, Seminyak, Bali")
                .starRating(5.0)
                .reviewScore(9.4)
                .checkIn(LocalDate.of(2026, 11, 9))
                .checkOut(LocalDate.of(2026, 11, 16))
                .priceTotal(1500.0)
                .currency("USD")
                .build());

        TripSegment seg2 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Bangkok")
                .destination("Bali")
                .arrivalDate(LocalDate.of(2026, 11, 9))
                .departureDate(LocalDate.of(2026, 11, 16))
                .orderIndex(2)
                .destinationLatitude(-8.4095)
                .destinationLongitude(115.1889)
                .transport(transport2)
                .accommodation(hotel2)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg2, "Ubud Monkey Forest", "Nature", "Jl. Monkey Forest, Ubud, Bali",
                        -8.5186, 115.2589, LocalDate.of(2026, 11, 10),
                        LocalTime.of(9, 0), LocalTime.of(11, 0)),
                itinerary(seg2, "Tanah Lot Temple", "Temple", "Beraban, Kediri, Bali",
                        -8.6215, 115.0867, LocalDate.of(2026, 11, 11),
                        LocalTime.of(16, 0), LocalTime.of(19, 0)),
                itinerary(seg2, "Mount Batur Sunrise Trek", "Hiking", "Kintamani, Bali",
                        -8.2424, 115.3756, LocalDate.of(2026, 11, 12),
                        LocalTime.of(2, 0), LocalTime.of(8, 0))
        ));

        // ── Segment 3: Bali → Singapore (flight) ───────────────────────────
        AirplaneTransport flight3 = airplaneTransportRepository.save(AirplaneTransport.builder()
                .flightNumber("SQ938")
                .departureTime(LocalDateTime.of(2026, 11, 16, 11, 0))
                .arrivalTime(LocalDateTime.of(2026, 11, 16, 14, 10))
                .duration("2h 10m")
                .price(160.0)
                .currency("USD")
                .build());

        Transport transport3 = transportRepository.save(Transport.builder()
                .transportType(TransportType.AIRPLANE)
                .airplaneTransport(flight3)
                .build());

        Accommodation hotel3 = accommodationRepository.save(Accommodation.builder()
                .name("Marina Bay Sands")
                .address("10 Bayfront Ave, Singapore")
                .starRating(5.0)
                .reviewScore(9.6)
                .checkIn(LocalDate.of(2026, 11, 16))
                .checkOut(LocalDate.of(2026, 11, 22))
                .priceTotal(3200.0)
                .currency("SGD")
                .build());

        TripSegment seg3 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Bali")
                .destination("Singapore")
                .arrivalDate(LocalDate.of(2026, 11, 16))
                .departureDate(LocalDate.of(2026, 11, 22))
                .orderIndex(3)
                .destinationLatitude(1.3521)
                .destinationLongitude(103.8198)
                .transport(transport3)
                .accommodation(hotel3)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg3, "Gardens by the Bay", "Park", "18 Marina Gardens Dr, Singapore",
                        1.2816, 103.8636, LocalDate.of(2026, 11, 17),
                        LocalTime.of(10, 0), LocalTime.of(13, 0)),
                itinerary(seg3, "Hawker Food Tour — Maxwell", "Food", "Maxwell Rd Hawker Centre, Singapore",
                        1.2800, 103.8447, LocalDate.of(2026, 11, 18),
                        LocalTime.of(12, 0), LocalTime.of(14, 0)),
                itinerary(seg3, "Sentosa Island", "Beach & Leisure", "Sentosa Island, Singapore",
                        1.2494, 103.8303, LocalDate.of(2026, 11, 19),
                        LocalTime.of(10, 0), LocalTime.of(18, 0))
        ));
    }

    /** Trip 6 — American Road Trip: New York → Chicago → Los Angeles */
    private void seedAmericanRoadTrip(User user) {
        TripPlan plan = tripPlanRepository.save(TripPlan.builder()
                .name("American Road Trip")
                .destinations(List.of("New York", "Chicago", "Los Angeles"))
                .interests(List.of("Road Trip", "Music", "Food"))
                .startDate(LocalDate.of(2027, 4, 10))
                .endDate(LocalDate.of(2027, 5, 1))
                .status(TripPlanStatus.DRAFT)
                .user(user)
                .build());

        // ── Segment 1: Fly to New York, explore ───────────────────────────
        AirplaneTransport flight1 = airplaneTransportRepository.save(AirplaneTransport.builder()
                .flightNumber("BA177")
                .departureTime(LocalDateTime.of(2027, 4, 10, 10, 0))
                .arrivalTime(LocalDateTime.of(2027, 4, 10, 13, 0))
                .duration("8h 0m")
                .price(650.0)
                .currency("GBP")
                .build());

        Transport transport1 = transportRepository.save(Transport.builder()
                .transportType(TransportType.AIRPLANE)
                .airplaneTransport(flight1)
                .build());

        Accommodation hotel1 = accommodationRepository.save(Accommodation.builder()
                .name("The Manhattan Hotel")
                .address("226 W 52nd St, New York, NY")
                .starRating(4.0)
                .reviewScore(8.3)
                .checkIn(LocalDate.of(2027, 4, 10))
                .checkOut(LocalDate.of(2027, 4, 17))
                .priceTotal(2100.0)
                .currency("USD")
                .build());

        TripSegment seg1 = tripSegmentRepository.save(TripSegment.builder()
                .departure("London")
                .destination("New York")
                .arrivalDate(LocalDate.of(2027, 4, 10))
                .departureDate(LocalDate.of(2027, 4, 17))
                .orderIndex(1)
                .destinationLatitude(40.7128)
                .destinationLongitude(-74.0060)
                .transport(transport1)
                .accommodation(hotel1)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg1, "Central Park", "Park", "Central Park, New York",
                        40.7851, -73.9683, LocalDate.of(2027, 4, 11),
                        LocalTime.of(9, 0), LocalTime.of(11, 0)),
                itinerary(seg1, "Metropolitan Museum of Art", "Museum", "1000 5th Ave, New York",
                        40.7794, -73.9632, LocalDate.of(2027, 4, 12),
                        LocalTime.of(10, 0), LocalTime.of(14, 0)),
                itinerary(seg1, "Brooklyn Bridge Walk", "Landmark", "Brooklyn Bridge, New York",
                        40.7061, -73.9969, LocalDate.of(2027, 4, 13),
                        LocalTime.of(8, 0), LocalTime.of(10, 0))
        ));

        // ── Segment 2: New York → Chicago (road) ───────────────────────────
        VehicleTransport car2 = vehicleTransportRepository.save(VehicleTransport.builder()
                .distanceKm(1270.0)
                .estimatedFuelCost(140.0)
                .tollCost(35.0)
                .build());

        Transport transport2 = transportRepository.save(Transport.builder()
                .transportType(TransportType.VEHICLE)
                .vehicleTransport(car2)
                .build());

        Accommodation hotel2 = accommodationRepository.save(Accommodation.builder()
                .name("Kimpton Gray Hotel Chicago")
                .address("122 W Monroe St, Chicago, IL")
                .starRating(4.5)
                .reviewScore(9.0)
                .checkIn(LocalDate.of(2027, 4, 17))
                .checkOut(LocalDate.of(2027, 4, 24))
                .priceTotal(1600.0)
                .currency("USD")
                .build());

        TripSegment seg2 = tripSegmentRepository.save(TripSegment.builder()
                .departure("New York")
                .destination("Chicago")
                .arrivalDate(LocalDate.of(2027, 4, 17))
                .departureDate(LocalDate.of(2027, 4, 24))
                .orderIndex(2)
                .destinationLatitude(41.8781)
                .destinationLongitude(-87.6298)
                .transport(transport2)
                .accommodation(hotel2)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg2, "Millennium Park & Cloud Gate", "Landmark", "201 E Randolph St, Chicago",
                        41.8826, -87.6233, LocalDate.of(2027, 4, 18),
                        LocalTime.of(9, 0), LocalTime.of(11, 0)),
                itinerary(seg2, "Chicago Architecture Boat Tour", "Activity", "Navy Pier, Chicago",
                        41.8920, -87.6120, LocalDate.of(2027, 4, 19),
                        LocalTime.of(11, 0), LocalTime.of(13, 0)),
                itinerary(seg2, "Chicago Blues Bar Crawl", "Music & Nightlife", "Buddy Guy's Legends, Chicago",
                        41.8731, -87.6270, LocalDate.of(2027, 4, 20),
                        LocalTime.of(20, 0), LocalTime.of(23, 0))
        ));

        // ── Segment 3: Chicago → Los Angeles (road) ────────────────────────
        VehicleTransport car3 = vehicleTransportRepository.save(VehicleTransport.builder()
                .distanceKm(3230.0)
                .estimatedFuelCost(360.0)
                .tollCost(20.0)
                .build());

        Transport transport3 = transportRepository.save(Transport.builder()
                .transportType(TransportType.VEHICLE)
                .vehicleTransport(car3)
                .build());

        Accommodation hotel3 = accommodationRepository.save(Accommodation.builder()
                .name("Hotel Figueroa Los Angeles")
                .address("939 S Figueroa St, Los Angeles, CA")
                .starRating(4.0)
                .reviewScore(8.6)
                .checkIn(LocalDate.of(2027, 4, 24))
                .checkOut(LocalDate.of(2027, 5, 1))
                .priceTotal(1750.0)
                .currency("USD")
                .build());

        TripSegment seg3 = tripSegmentRepository.save(TripSegment.builder()
                .departure("Chicago")
                .destination("Los Angeles")
                .arrivalDate(LocalDate.of(2027, 4, 24))
                .departureDate(LocalDate.of(2027, 5, 1))
                .orderIndex(3)
                .destinationLatitude(34.0522)
                .destinationLongitude(-118.2437)
                .transport(transport3)
                .accommodation(hotel3)
                .tripPlan(plan)
                .build());

        dailyItineraryRepository.saveAll(List.of(
                itinerary(seg3, "Griffith Observatory", "Landmark", "2800 E Observatory Rd, Los Angeles",
                        34.1184, -118.3004, LocalDate.of(2027, 4, 25),
                        LocalTime.of(12, 0), LocalTime.of(15, 0)),
                itinerary(seg3, "Santa Monica Beach & Pier", "Beach", "200 Santa Monica Pier, CA",
                        34.0095, -118.4975, LocalDate.of(2027, 4, 26),
                        LocalTime.of(10, 0), LocalTime.of(15, 0)),
                itinerary(seg3, "Hollywood Walk of Fame", "Landmark", "Hollywood Blvd, Los Angeles",
                        34.1016, -118.3267, LocalDate.of(2027, 4, 27),
                        LocalTime.of(11, 0), LocalTime.of(13, 0))
        ));
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private DailyItinerary itinerary(TripSegment segment, String name, String category,
                                     String address, Double lat, Double lon,
                                     LocalDate day, LocalTime start, LocalTime end) {
        return DailyItinerary.builder()
                .name(name)
                .category(category)
                .address(address)
                .latitude(lat)
                .longitude(lon)
                .day(day)
                .startTime(start)
                .endTime(end)
                .tripSegment(segment)
                .build();
    }
}


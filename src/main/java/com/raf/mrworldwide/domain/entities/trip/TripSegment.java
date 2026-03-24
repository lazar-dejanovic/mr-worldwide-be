package com.raf.mrworldwide.domain.entities.trip;

import com.raf.mrworldwide.domain.entities.BaseEntityUUID;
import com.raf.mrworldwide.domain.entities.accomodation.Accommodation;
import com.raf.mrworldwide.domain.entities.transport.Transport;
import com.raf.mrworldwide.domain.entities.transport.TransportType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "trip_segment")
public class TripSegment extends BaseEntityUUID {

    private String departure; // from
    private String destination; // to

    private LocalDate arrivalDate;
    private LocalDate departureDate;

    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;

    @OneToMany(mappedBy = "tripSegment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DailyItinerary> dailyItineraries = new ArrayList<>();

    @OneToOne
    private Transport transport;
    @OneToOne
    private Accommodation accommodation;

    private Double destinationLatitude;
    private Double destinationLongitude;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        TripSegment that = (TripSegment) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

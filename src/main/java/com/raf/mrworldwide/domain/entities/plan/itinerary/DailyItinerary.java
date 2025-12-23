package com.raf.mrworldwide.domain.entities.plan.itinerary;

import com.raf.mrworldwide.domain.entities.BaseEntityUUID;
import com.raf.mrworldwide.domain.entities.plan.Destination;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "\"daily_itineraries\"")
public class DailyItinerary extends BaseEntityUUID {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    private String fsqPlaceId;
    private String name;
    private String categoryName;
    private String categoryIconUrl;

    private String formattedAddress;
    private Double latitude;
    private Double longitude;

    private LocalTime startTime;
    private LocalTime endTime;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        DailyItinerary that = (DailyItinerary) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

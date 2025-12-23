package com.raf.mrworldwide.domain.entities.plan.transportation;

import com.raf.mrworldwide.domain.entities.BaseEntityUUID;
import com.raf.mrworldwide.domain.entities.plan.Destination;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "\"flight_bookings\"")
public class FlightBooking extends BaseEntityUUID {

    @OneToOne
    @JoinColumn(name = "destination_id")
    private Destination destination;

    private String amadeusId;
    private String carrierCode;
    private String flightNumber;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private String duration;
    private Double price;
    private String currency;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Destination that = (Destination) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

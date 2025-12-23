package com.raf.mrworldwide.domain.entities.plan.stay;

import com.raf.mrworldwide.domain.entities.BaseEntityUUID;
import com.raf.mrworldwide.domain.entities.plan.Destination;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "\"accommodations\"")
public class Accommodation extends BaseEntityUUID {

    @OneToOne
    @JoinColumn(name = "destination_id")
    private Destination destination;

    private String hotelId;
    private String name;
    private String address;
    private String imageUrl;
    private String bookingUrl;

    private Double starRating;
    private Double reviewScore;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private Double priceTotal;
    private String currencyCode;

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
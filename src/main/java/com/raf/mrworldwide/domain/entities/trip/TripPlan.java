package com.raf.mrworldwide.domain.entities.trip;

import com.raf.mrworldwide.domain.converters.CsvConverter;
import com.raf.mrworldwide.domain.entities.BaseEntityUUID;
import com.raf.mrworldwide.domain.entities.user.User;
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
@Table(name = "trip_plan")
public class TripPlan extends BaseEntityUUID {

    private String name;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = CsvConverter.class)
    private List<String> destinations;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = CsvConverter.class)
    private List<String> interests;

    @Enumerated(EnumType.STRING)
    private TripPlanStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "tripPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<TripSegment> tripSegments = new ArrayList<>();


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        TripPlan that = (TripPlan) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "name = " + name + ", " +
                "startDate = " + startDate + ", " +
                "endDate = " + endDate + ", " +
                "destinations = " + destinations + ")";
    }

}

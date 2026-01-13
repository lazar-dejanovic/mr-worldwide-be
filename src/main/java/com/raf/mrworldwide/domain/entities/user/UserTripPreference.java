package com.raf.mrworldwide.domain.entities.user;

import com.raf.mrworldwide.domain.converters.CsvConverter;
import com.raf.mrworldwide.domain.entities.BaseEntityUUID;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user_trip_preference")
public class UserTripPreference extends BaseEntityUUID {

    private String name;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = CsvConverter.class)
    private List<String> interests;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = CsvConverter.class)
    private List<String> hobbies;

    @Column(columnDefinition = "TEXT")
    @Convert(converter = CsvConverter.class)
    private List<String> favouriteDestinations;

    @OneToOne(mappedBy = "userTripPreference")
    private User user;

}

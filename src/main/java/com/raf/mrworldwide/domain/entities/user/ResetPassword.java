package com.raf.mrworldwide.domain.entities.user;

import com.raf.mrworldwide.domain.entities.BaseEntityUUID;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "reset_password")
public class ResetPassword extends BaseEntityUUID {

    private String secretKey;
    private ZonedDateTime expirationTime;
    private String userEmail;
    private boolean used;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        ResetPassword resetPassword = (ResetPassword) o;
        return Objects.equals(getId(), resetPassword.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}

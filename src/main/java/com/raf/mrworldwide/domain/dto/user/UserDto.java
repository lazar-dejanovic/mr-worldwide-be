package com.raf.mrworldwide.domain.dto.user;

import com.raf.mrworldwide.domain.dto.BaseEntityDto;
import com.raf.mrworldwide.domain.entities.user.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserDto extends BaseEntityDto {

    private String email;
    private String firstName;
    private String lastName;
    private List<Role> roles;
    private boolean deleted;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto that = (UserDto) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hashCode(id) : 0;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "email='" + email + '\'' +
                ", roles=" + roles +
                ", id=" + id +
                ", deleted=" + deleted +
                '}';
    }

}

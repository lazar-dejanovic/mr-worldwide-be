package com.raf.mrworldwide.domain.entities.user;

import lombok.Getter;

import java.util.Set;

@Getter
public enum Role {

    SUPER_ADMIN(Set.of(Permission.SUPER_ADMIN)),
    SYSTEM_ADMIN(Set.of(Permission.SYSTEM_ADMIN)),
    ;

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions){
        this.permissions = permissions;
    }

}

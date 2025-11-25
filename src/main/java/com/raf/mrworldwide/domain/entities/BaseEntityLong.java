package com.raf.mrworldwide.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

@MappedSuperclass
@Getter
@ToString(of = {"id"})
public abstract class BaseEntityLong extends BaseEntityAudit {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Long version;

}

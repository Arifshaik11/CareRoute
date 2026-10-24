package com.innovation_matrix.careroute.location;

import com.innovation_matrix.careroute.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "facility")
public class Facility extends BaseEntity {

    public enum Type { PHC, CHC, DISTRICT_HOSPITAL }

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    @Column(nullable = false, length = 120)
    private String block;

    protected Facility() {
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getBlock() {
        return block;
    }
}

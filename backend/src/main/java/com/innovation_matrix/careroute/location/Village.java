package com.innovation_matrix.careroute.location;

import com.innovation_matrix.careroute.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "village")
public class Village extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 120)
    private String block;

    @Column(nullable = false, length = 120)
    private String district;

    /** The facility that receives referrals from this village. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "facility_id", nullable = false)
    private Facility facility;

    protected Village() {
    }

    public String getName() {
        return name;
    }

    public String getBlock() {
        return block;
    }

    public String getDistrict() {
        return district;
    }

    public Facility getFacility() {
        return facility;
    }
}

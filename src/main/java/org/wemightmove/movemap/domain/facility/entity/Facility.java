package org.wemightmove.movemap.domain.facility.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import org.wemightmove.movemap.global.enums.FacilityType;
import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "facility")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", length = 50, nullable = false)
    private FacilityType facilityType;

    @Column(name = "facility_subtype", length = 200)
    private String facilitySubtype;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.GEOMETRY)
    @Column(name = "location", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;

    @Column(name = "region_cd", length = 20, nullable = false)
    private String regionCode;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "hmpg_url", length = 200)
    private String hmpgUrl;

    @Column(name = "is_voucher_available")
    private boolean isVoucherAvailable;
}

package com.java.booking_system.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "stations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "station_code")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Station extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code", nullable = false, length = 10)
    private String stationCode;

    @Column(name = "station_name", nullable = false, length = 150)
    private String stationName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(name = "platform_count")
    private Integer platformCount;

    @Column(length = 100)
    private String zone;

    private Double latitude;

    private Double longitude;

}

package com.java.booking_system.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "cities",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "city_name")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class City extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_name", nullable = false, length = 100)
    private String cityName;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 100)
    private String country;

}
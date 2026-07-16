package com.java.booking_system.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "train_class_configs", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "train_id", "travel_class_id" })
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainClassConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id")
    private Train train;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "travel_class_id")
    private TravelClass travelClass;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "base_fare", nullable = false)
    private Double baseFare;

}

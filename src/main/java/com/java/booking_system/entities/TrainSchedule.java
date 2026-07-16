package com.java.booking_system.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "train_schedules", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "train_id", "departure_date" })
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "train_id")
    private Train train;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ScheduleStatus status;

}

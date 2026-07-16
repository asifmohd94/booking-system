package com.java.booking_system.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trains", uniqueConstraints = {
        @UniqueConstraint(columnNames = "train_number")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Train extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "train_number", nullable = false, length = 20)
    private String trainNumber;

    @Column(name = "train_name", nullable = false, length = 150)
    private String trainName;

    @Enumerated(EnumType.STRING)
    @Column(name = "train_type", nullable = false, length = 50)
    private TrainType trainType;

}

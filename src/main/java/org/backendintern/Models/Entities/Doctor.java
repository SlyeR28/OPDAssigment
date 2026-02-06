package org.backendintern.Models.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "doctors")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "doctor_name" , length = 100)
    private String name;

    @OneToMany(mappedBy = "doctor",cascade = CascadeType.ALL ,fetch = FetchType.LAZY)
    @Builder.Default
    private List<Slot> slotList = new ArrayList<>();
}



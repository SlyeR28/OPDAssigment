package org.backendintern.Models.Entities;


import jakarta.persistence.*;
import lombok.*;
import org.backendintern.Models.Enums.TokenStatus;
import org.backendintern.Models.Enums.TokenType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tokens")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_name" , nullable = false , length = 100)
    private String patientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false , length = 50)
    private TokenType tokenType;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_status", nullable = false , length = 50)
    private TokenStatus tokenStatus;

    @Column(nullable = false)
    private int score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="slot_id")
    private Slot timeSlot;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

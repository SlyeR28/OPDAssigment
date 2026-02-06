package org.backendintern.Models.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.backendintern.Models.Enums.TokenType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "slots")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @ToString.Exclude
    private Doctor doctor;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "current_capacity", nullable = false)
    private int currentCapacity;

    @OneToMany(mappedBy = "timeSlot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Token> tokens = new ArrayList<>();

    @Version
    private Long version;

    public boolean isFull() {
        return currentCapacity >= maxCapacity;
    }

    public void incrementCapacity() {
        if (isFull()) {
            throw new IllegalStateException("Slot is already full");
        }
        this.currentCapacity++;
    }

    public boolean canBook(TokenType type) {
        if (type == TokenType.EMERGENCY) {
            return true;
        }
        return !isFull();
    }
}

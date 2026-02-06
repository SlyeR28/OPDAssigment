package org.backendintern.Models.Enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
    ONLINE_BOOKING(40),
    WALK_IN(20),
    PAID_PRIORITY(80),
    FOLLOW_UP(60),
    EMERGENCY(100);

    private final int score;
}

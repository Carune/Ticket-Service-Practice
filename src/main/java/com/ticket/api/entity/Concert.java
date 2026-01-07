package com.ticket.api.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Concert extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private Integer runningTime;

    @Builder
    public Concert(String title, String description, String venue, Integer runningTime) {
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.runningTime = runningTime;
    }
}
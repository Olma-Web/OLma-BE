package com.olma.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "experience_levels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExperienceLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private Short minYears;

    @Column(nullable = false)
    private Short maxYears;

    @Column(nullable = false)
    private Short displayOrder;
}

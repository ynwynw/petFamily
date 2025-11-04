package com.example.petback.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsEntity {
    private Long animalCount;
    private Long availableCount;
    private Long userCount;
    private Long newUserCount;
    private Long adoptCount;
    private Long monthAdoptCount;
    private Long orderCount;
    private BigDecimal totalAmount;
    private Long fosterCount;
    private Long currentFosterCount;
} 
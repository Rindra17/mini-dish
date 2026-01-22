package com.school.hei.model;

import com.school.hei.type.MovementTypeEnum;

import java.time.Instant;

public class StockMovement {
    private Integer id;
    private StockValue value;
    private MovementTypeEnum type;
    private Instant creationDatetime;
}

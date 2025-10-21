package com.acsteknoloji.simulator.model;

import lombok.Data;

@Data
public class Unit {
    private int id;
    private String unitName;
    private String passwordHash;
    private String role;
}

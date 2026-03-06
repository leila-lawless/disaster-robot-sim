package com.example.demo.core.interfaces;

public interface Chargeable {
    void transferCharge(Object target, int amount);
    boolean canCharge(Object target);
    int getChargeCapacity();    
}

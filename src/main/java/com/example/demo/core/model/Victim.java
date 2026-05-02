package com.example.demo.core.model;

import com.example.demo.core.enums.VictimStatus;
import com.example.demo.core.enums.RobotType;
import java.util.concurrent.atomic.AtomicInteger;

public class Victim {
    private String id;
    private Position position;
    private VictimStatus status;
    private AtomicInteger survivalTimer;
    private RobotType requiresType;

    public Victim(String id, Position position, RobotType requiresType) {
        this.id = id;
        this.position = position;
        this.status = VictimStatus.UNDISCOVERED;
        this.survivalTimer = new AtomicInteger(100);
        this.requiresType = requiresType;
    }

    public void tick() { survivalTimer.decrementAndGet(); }
    public boolean isAlive() { return survivalTimer.get() > 0; }
    public void stabilize(int amount) { survivalTimer.addAndGet(amount); }

    public String getId()               { return id; }
    public Position getPosition()       { return position; }
    public VictimStatus getStatus()     { return status; }
    public int getSurvivalTimer()       { return survivalTimer.get(); }
    public RobotType getRequiresType()  { return requiresType; }
    public void setStatus(VictimStatus s) { this.status = s; }
}
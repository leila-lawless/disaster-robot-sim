package com.example.demo.core.model;

import com.example.demo.core.enums.RobotState;
import java.io.Serializable;

public class RobotStatus implements Serializable {
    private String robotId;
    private Position position;
    private RobotState state;
    private int battery;
    private long timestamp;

    public RobotStatus(String robotId, Position position, RobotState state, int battery) {
        this.robotId = robotId;
        this.position = position;
        this.state = state;
        this.battery = battery;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRobotId()    { return robotId; }
    public Position getPosition() { return position; }
    public RobotState getState()  { return state; }
    public int getBattery()       { return battery; }
    public long getTimestamp()    { return timestamp; }

    @Override
    public String toString() {
        return "RobotStatus[" + robotId + " " + state + " battery=" + battery + "]";
    }
}
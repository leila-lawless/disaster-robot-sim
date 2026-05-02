package com.example.demo.core.robots;

import com.example.demo.core.enums.RobotType;
import com.example.demo.core.model.Position;
import com.example.demo.core.model.Task;
import com.example.demo.core.state.LowBatteryState;

public class ScoutRobot extends RobotCore {
    private int scanRadius = 5;

    public ScoutRobot(String id, Position position) {
        super(id, position);
    }

    @Override
    public void performTask(Task task) {
        System.out.println("[" + id + "] Scout scanning area around " + task.getTargetPos());
    }

    @Override public RobotType getRobotType() { return RobotType.SCOUT; }

    @Override
    protected void onLowBattery() {
        System.out.println("[" + id + "] Scout battery low!");
        transitionTo(new LowBatteryState());
    }

    public int getScanRadius() { return scanRadius; }
}
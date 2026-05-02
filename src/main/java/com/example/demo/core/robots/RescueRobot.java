package com.example.demo.core.robots;

import com.example.demo.core.enums.RobotType;
import com.example.demo.core.model.Position;
import com.example.demo.core.model.Task;
import com.example.demo.core.state.LowBatteryState;

public class RescueRobot extends RobotCore {
    private int strengthLevel = 8;

    public RescueRobot(String id, Position position) {
        super(id, position);
    }

    @Override
    public void performTask(Task task) {
        System.out.println("[" + id + "] Rescue robot extracting victim at " + task.getTargetPos());
    }

    @Override public RobotType getRobotType() { return RobotType.RESCUE; }

    @Override
    protected void onLowBattery() {
        System.out.println("[" + id + "] Rescue battery low!");
        transitionTo(new LowBatteryState());
    }

    public int getStrengthLevel() { return strengthLevel; }
}
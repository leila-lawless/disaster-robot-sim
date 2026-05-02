package com.example.demo.core.robots;

import com.example.demo.core.enums.RobotType;
import com.example.demo.core.model.Position;
import com.example.demo.core.model.Task;
import com.example.demo.core.state.LowBatteryState;

public class SupplyRobot extends RobotCore {
    private int chargeCapacity = 50;

    public SupplyRobot(String id, Position position) {
        super(id, position);
    }

    @Override
    public void performTask(Task task) {
        System.out.println("[" + id + "] Supply robot delivering charge at " + task.getTargetPos());
    }

    @Override public RobotType getRobotType() { return RobotType.SUPPLY; }

    @Override
    protected void onLowBattery() {
        System.out.println("[" + id + "] Supply battery low!");
        transitionTo(new LowBatteryState());
    }

    public int getChargeCapacity() { return chargeCapacity; }
}
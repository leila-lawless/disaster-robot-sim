package com.example.demo.core.robots;

import com.example.demo.core.enums.RobotType;
import com.example.demo.core.model.Position;
import com.example.demo.core.model.Task;
import com.example.demo.core.state.LowBatteryState;

public class MedicalRobot extends RobotCore {
    private int medKitCount = 5;

    public MedicalRobot(String id, Position position) {
        super(id, position);
    }

    @Override
    public void performTask(Task task) {
        System.out.println("[" + id + "] Medical robot treating victim at " + task.getTargetPos());
    }

    @Override public RobotType getRobotType() { return RobotType.MEDICAL; }

    @Override
    protected void onLowBattery() {
        System.out.println("[" + id + "] Medical battery low!");
        transitionTo(new LowBatteryState());
    }

    public int getMedKitCount() { return medKitCount; }
}
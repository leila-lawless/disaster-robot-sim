package com.example.demo.core.state;

import com.example.demo.core.enums.RobotState;

public class IdleState implements RobotStateHandler {
    @Override
    public void handle(Object robot) {
        System.out.println("[IDLE] Waiting for task...");
    }
    @Override public void onEnter(Object robot) { System.out.println("-> Entered IDLE"); }
    @Override public void onExit(Object robot)  { System.out.println("<- Exited IDLE"); }
    @Override public RobotState getStateName()  { return RobotState.IDLE; }
}
package com.example.demo.core.state;

import com.example.demo.core.enums.RobotState;

public class EmergencyState implements RobotStateHandler {
    @Override public void handle(Object robot)  { System.out.println("[EMERGENCY] Attempting recovery..."); }
    @Override public void onEnter(Object robot) { System.out.println("-> Entered EMERGENCY"); }
    @Override public void onExit(Object robot)  { System.out.println("<- Exited EMERGENCY"); }
    @Override public RobotState getStateName()  { return RobotState.EMERGENCY; }
}
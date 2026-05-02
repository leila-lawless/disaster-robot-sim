package com.example.demo.core.state;

import com.example.demo.core.enums.RobotState;

public class OfflineState implements RobotStateHandler {
    @Override public void handle(Object robot)  { System.out.println("[OFFLINE] Robot is offline."); }
    @Override public void onEnter(Object robot) { System.out.println("-> Entered OFFLINE"); }
    @Override public void onExit(Object robot)  { System.out.println("<- Exited OFFLINE"); }
    @Override public RobotState getStateName()  { return RobotState.OFFLINE; }
}
package com.example.demo.core.state;

import com.example.demo.core.enums.RobotState;

public class LowBatteryState implements RobotStateHandler {
    @Override public void handle(Object robot)  { System.out.println("[LOW BATTERY] Seeking charge..."); }
    @Override public void onEnter(Object robot) { System.out.println("-> Entered LOW_BATTERY"); }
    @Override public void onExit(Object robot)  { System.out.println("<- Exited LOW_BATTERY"); }
    @Override public RobotState getStateName()  { return RobotState.LOW_BATTERY; }
}
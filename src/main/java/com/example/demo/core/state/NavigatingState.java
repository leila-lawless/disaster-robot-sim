package com.example.demo.core.state;

import com.example.demo.core.enums.RobotState;

public class NavigatingState implements RobotStateHandler {
    @Override public void handle(Object robot)  { System.out.println("[NAVIGATING] Moving to target..."); }
    @Override public void onEnter(Object robot) { System.out.println("-> Entered NAVIGATING"); }
    @Override public void onExit(Object robot)  { System.out.println("<- Exited NAVIGATING"); }
    @Override public RobotState getStateName()  { return RobotState.NAVIGATING; }
}
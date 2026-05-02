package com.example.demo.core.state;

import com.example.demo.core.enums.RobotState;

public class ExecutingState implements RobotStateHandler {
    @Override public void handle(Object robot)  { System.out.println("[EXECUTING] Performing task..."); }
    @Override public void onEnter(Object robot) { System.out.println("-> Entered EXECUTING"); }
    @Override public void onExit(Object robot)  { System.out.println("<- Exited EXECUTING"); }
    @Override public RobotState getStateName()  { return RobotState.EXECUTING; }
}
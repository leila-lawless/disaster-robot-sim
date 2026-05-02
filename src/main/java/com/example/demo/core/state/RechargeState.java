package com.example.demo.core.state;

import com.example.demo.core.enums.RobotState;

public class RechargeState implements RobotStateHandler {
    @Override public void handle(Object robot)  { System.out.println("[RECHARGING] Charging battery..."); }
    @Override public void onEnter(Object robot) { System.out.println("-> Entered RECHARGING"); }
    @Override public void onExit(Object robot)  { System.out.println("<- Exited RECHARGING"); }
    @Override public RobotState getStateName()  { return RobotState.RECHARGING; }
}
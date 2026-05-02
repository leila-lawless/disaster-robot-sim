package com.example.demo.core.state;

import com.example.demo.core.enums.RobotState;

public interface RobotStateHandler {
    void handle(Object robot);
    void onEnter(Object robot);
    void onExit(Object robot);
    RobotState getStateName();
}
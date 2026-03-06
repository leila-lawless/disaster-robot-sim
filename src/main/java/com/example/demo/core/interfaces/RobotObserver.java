package com.example.demo.core.interfaces;

import com.example.demo.core.model.RobotException;

public interface RobotObserver {
    void onStateChanged(Object robot, Object fromState, Object toState);
    void onTaskCompleted(Object robot, Object task);
    void onEmergency(Object robot, RobotException e);
    void onMetricRecorded(Object robot, Object metric);
}

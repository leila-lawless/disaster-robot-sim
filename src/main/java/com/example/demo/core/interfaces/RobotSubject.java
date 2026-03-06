package com.example.demo.core.interfaces;

public interface RobotSubject {
    void addObserver(RobotObserver observer);
    void removeObserver(RobotObserver observer);
    void notifyObserver();
}

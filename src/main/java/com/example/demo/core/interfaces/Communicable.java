package com.example.demo.core.interfaces;

import com.example.demo.core.model.RobotStatus;
import com.example.demo.core.model.RobotMessage;

public interface Communicable {
    void connect(String host, int port);
    void sendStatus(RobotStatus status);
    RobotMessage recieveCommand();
    void disconnect();
    boolean isConnected();
}

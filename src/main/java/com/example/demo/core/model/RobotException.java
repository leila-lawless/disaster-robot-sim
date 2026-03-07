package com.example.demo.core.model;

public abstract class RobotException extends RuntimeException{
    private final String robotId;

    public RobotException(String robotId, String message){
        super(message);
        this.robotId = robotId;
    }

    public RobotException(String robotId, String message, Throwable cause){
        super(message, cause);
        this.robotId = robotId;
    }

    public String getRobotId(){
        return robotId;
    }
}

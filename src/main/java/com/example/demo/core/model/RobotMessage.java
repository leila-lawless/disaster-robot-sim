package com.example.demo.core.model;

import com.example.demo.core.enums.MessageType;
import java.io.Serializable;

public class RobotMessage implements Serializable {
    private MessageType type;
    private String senderId;
    private Object payload;
    private long timestamp;

    public RobotMessage(MessageType type, String senderId, Object payload) {
        this.type = type;
        this.senderId = senderId;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public MessageType getType()  { return type; }
    public String getSenderId()   { return senderId; }
    public Object getPayload()    { return payload; }
    public long getTimestamp()    { return timestamp; }

    @Override
    public String toString() {
        return "RobotMessage[" + type + " from=" + senderId + "]";
    }
}
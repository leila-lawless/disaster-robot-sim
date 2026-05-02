package com.example.demo.core.model;

import com.example.demo.core.enums.TaskPriority;
import java.io.Serializable;

public abstract class Task implements Serializable {
    protected String id;
    protected TaskPriority priority;
    protected Position targetPos;
    protected String assignedTo;

    public Task(String id, TaskPriority priority, Position targetPos) {
        this.id = id;
        this.priority = priority;
        this.targetPos = targetPos;
        this.assignedTo = null;
    }

    public abstract void execute(Object robot);

    public String getId()             { return id; }
    public TaskPriority getPriority() { return priority; }
    public Position getTargetPos()    { return targetPos; }
    public String getAssignedTo()     { return assignedTo; }
    public void setAssignedTo(String robotId) { this.assignedTo = robotId; }

    @Override
    public String toString() {
        return "Task[" + id + " priority=" + priority + " target=" + targetPos + "]";
    }
}
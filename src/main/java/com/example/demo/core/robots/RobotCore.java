package com.example.demo.core.robots;

import com.example.demo.core.enums.RobotState;
import com.example.demo.core.enums.RobotType;
import com.example.demo.core.model.Position;
import com.example.demo.core.model.RobotStatus;
import com.example.demo.core.model.Task;
import com.example.demo.core.state.RobotStateHandler;
import com.example.demo.core.state.IdleState;
import com.example.demo.core.interfaces.RobotSubject;
import com.example.demo.core.interfaces.RobotObserver;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RobotCore implements Runnable, RobotSubject {
    protected String id;
    protected Position position;
    protected AtomicInteger battery;
    protected RobotStateHandler currentState;
    protected BlockingQueue<Task> taskQueue;
    protected List<RobotObserver> observers;
    protected AtomicBoolean running;

    public RobotCore(String id, Position position) {
        this.id = id;
        this.position = position;
        this.battery = new AtomicInteger(100);
        this.taskQueue = new LinkedBlockingQueue<>();
        this.observers = new CopyOnWriteArrayList<>();
        this.running = new AtomicBoolean(false);
        this.currentState = new IdleState();
    }

    public abstract void performTask(Task task);
    public abstract RobotType getRobotType();
    protected abstract void onLowBattery();

    @Override
    public void run() {
        running.set(true);
        System.out.println("[" + id + "] Robot started - Type: " + getRobotType());
        currentState.onEnter(this);
        while (running.get()) {
            currentState.handle(this);
            drainBattery(1);
            if (battery.get() <= 20) onLowBattery();
            try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
        }
        System.out.println("[" + id + "] Robot stopped.");
    }

    public void transitionTo(RobotStateHandler newState) {
        currentState.onExit(this);
        currentState = newState;
        currentState.onEnter(this);
        notifyObservers();
    }

    public void drainBattery(int amount) { battery.addAndGet(-amount); }
    public void rechargeBattery(int amount) { battery.addAndGet(amount); }
    public void shutdown() { running.set(false); }

    public RobotStatus getStatus() {
        return new RobotStatus(id, position, currentState.getStateName(), battery.get());
    }

    @Override public void addObserver(RobotObserver o)    { observers.add(o); }
    @Override public void removeObserver(RobotObserver o) { observers.remove(o); }
    @Override public void notifyObservers() {
        for (RobotObserver o : observers) o.onStateChanged(this, null, currentState.getStateName());
    }

    public String getId()       { return id; }
    public Position getPosition() { return position; }
    public int getBattery()     { return battery.get(); }
    public void setPosition(Position p) { this.position = p; }
}
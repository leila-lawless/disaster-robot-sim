package com.example.demo.core.model;

import java.util.LinkedList;

public class Path {
    private LinkedList<Position> waypoints;
    private double totalCost;

    public Path(){
        this.waypoints = new LinkedList<>();
        this.totalCost = 0.0;
    }

    public void addWaypoint(Position p){
        this.waypoints.add(p);
    }

    public Position next(){
        return waypoints.poll();
    }

    public boolean hasNext(){
        return !waypoints.isEmpty();
    }

    public int length(){
        return waypoints.size();
    }

    public boolean isEmpty(){
        return waypoints.isEmpty();
    }

    public void setTotalCost(double cost){
        this.totalCost = cost;
    }

    public double getTotalCost(){
        return this.totalCost;
    }

    @Override
    public String toString(){
        return "Path[" + length() + " steps, cost = " + totalCost + "]";
    }



    
}

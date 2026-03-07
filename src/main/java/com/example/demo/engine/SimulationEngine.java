package com.example.demo.engine;

import core.example.demo.model.Cell;
import com.example.demo.core.enums.SimulationState;
import com.example.demo.core.enums.ZoneType;

public class SimulationEngine{
    private Grid<Cell> grid;
    private SimulationState simState;
    private long tickCount;

    public SimulationEngine(int width, int height){
        this.grid = new Grid<>(width, height);
        this.simState = SimulationState.IDLE;
        this.tickCount = 0;
        initializeGrid(width, height);
    }

    private void initializeGrid(int width, int height){
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                grid.set(x,y, new Cell(new com.example.demo.core.model.Position(x,y), ZoneType.SAFE));
            }
        }
        System.out.println("Grid initialized: " + grid);
    }

    public void start(){
        simState = SimulationState.RUNNING;
        System.out.println("Simulation started! :)");
        for (int i = 1; i <= 5; i++){
            tick();
        }

        simState = SimulationState.COMPLETED;
        System.out.println("Simulation completed! Total ticks: " + tickCount);
    }

    private void tick(){
        tickCount++;
        System.out.println("Tick " + tickCount);
    }

    public void pause(){
        simState = SimulationState.PAUSED;
        System.out.println("Simulation paused! :(");
    }

    public void reset(){
        tickCount = 0;
        simState = SimulationState.IDLE;
        System.out.println("Simulation reset! :|");
    }

    // well... time for the getters
    public Grid<Cell> getGrid(){
        return grid;
    }

    public SimulationState getSimState(){
        return simState;
    }

    public long getTickcount(){ return tickCount; }


}
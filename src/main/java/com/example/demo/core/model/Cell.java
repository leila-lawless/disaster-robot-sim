package com.example.demo.core.model;

import com.example.demo.core.enums.ZoneType;
import java.util.Optional;

public class Cell {
    private Position position;
    private ZoneType zoneType;
    private Optional<Victim> victim;
    private boolean explored;
    private double traversalCost;
    
    
    public Cell(Position position, ZoneType zoneType){
        this.position = position;
        this.zoneType = zoneType;
        this.victim = Optional.empty();
        this.explored = false;
        this.traversalCost = calculateCost(zoneType);
    }

    private double calculateCost(ZoneType type){
        switch(type){
            case SAFE: return 1.0;
            case RUBBLE: return 3.0;
            case DANGER: return 10.0;
            case WATER: return 5.0;
            case FIRE: return 15.0;
            default: return 1.0;
        }
    }

    public boolean isPassable(){
        return zoneType != ZoneType.DANGER && zoneType != ZoneType.FIRE;
    }

    public boolean hasVictim(){
        return victim.isPresent();
    }

    // the getters now
    public Position getPosition() {return position;}
    public ZoneType getZoneType() {return zoneType;}
    public Optional<Victim> getVictim() {return victim;}
    public boolean isExplored() {return explored;}
    public double getTraversalCost() {return traversalCost;}

    // then the setters
    public void setZoneType(ZoneType z) {this.zoneType = z; this.traversalCost = calculateCost(z);}
    public void setVictim(Victim v) {this.victim = Optional.ofNullable(v);}
    public void setExplored(boolean e) {this.explored = e;}

    @Override
    public String toString(){
        return "Cell at[" + position + " " + zoneType + (hasVictim() ? "VICTIM" : "") + "]";
    }

}

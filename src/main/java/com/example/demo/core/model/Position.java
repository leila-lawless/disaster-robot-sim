package com.example.demo.core.model;

import java.io.Serializable;

public final class Position {
    public final int x;
    public final int y;
    
    public Position(int x, int y){
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Position other){
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dy + dy * dy);
    }

    public int manhattanTo(Position other){
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    @Override
    public boolean equals(Object o){
        if( !(o instanceof Position p)) return false;
        return x == p.x && y == p.y;
    }

    @Override
    public int hashCode(){
        return 31 * x + y;
    }

    @Override
    public String toString(){
        return "(" + x + "," + y + ")";
    }



}

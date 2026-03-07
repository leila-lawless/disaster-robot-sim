package com.example.demo.engine;

import com.example.demo.core.model.Position;
import java.util.ArrayList;
import java.util.List;

public class Grid<T> {
    private final Object[][] cells;
    private final int width;
    private final int height;
    
    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Object[width][height];
    }

    @SuppressWarnings("unchecked")
    public T get(int x, int y){
        if(!inBounds(x,y)) throw new IndexOutOfBoundsException("(" + x + "," + y + ")");
        return (T) cells [x][y];
    }

    public boolean inBounds(int x, int y){
        return x>= 0 && x < width && y >= 0 && y < height;
    }

    public void set(int x, int y, T value){
        if(!inBounds(x,y)) throw new IndexOutOfBoundsException("(" + x + "," + y + ")");
        cells[x][y] = value;
    }

    public List<T> getNeighbors(Position pos){
        int[][] directions = {{0,1},{0,-1},{1,0},{-1,0}};
        List<T> result = new ArrayList<>();
        for(int[] d : directions){
            int nx = pos.x + d[0];
            int ny = pos.y + d[1];
            
            if(inBounds(nx,ny) && cells[nx][ny] != null)
                result.add(get(nx,ny));
            }
            return result;
    }

    public int getWidth() { return width; }
    public int getHeight(){ return height; }

    @Override
    public string toString(){
        retrun "Grid[" + width + "x" + height + "]";
    }


}

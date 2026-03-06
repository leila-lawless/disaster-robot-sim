package com.example.demo.core.interfaces;

import com.example.demo.core.model.Cell;
import com.example.demo.core.model.Victim;
import java.util.List;

public interface Scannable {
    List <Cell> scan(int radius);
    int getScanRadius();
    List<Victim> detectVictims(List<Cell> cells);
 }

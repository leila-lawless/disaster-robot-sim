package com.example.demo.core.interfaces;

import com.example.demo.core.model.Cell;
import com.example.demo.core.model.Victim;


public interface Rescuable {
    boolean extractVictim(Victim victim);
    void clearRubble(Cell cell);
    int getStrengthLevel();    
}

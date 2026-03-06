package com.example.demo.core.interfaces;

import com.example.demo.core.model.Position;
import com.example.demo.engine.Grid;
import com.example.demo.core.model.Cell;
import com.example.demo.core.model.Path;
import java.util.Optional;



public interface Navigable {
    Path navigate(Grid <Cell> grid, Position target);
    void setStrategy(Object strategy);
    Optional<Path> getCurrentPath();
        
}

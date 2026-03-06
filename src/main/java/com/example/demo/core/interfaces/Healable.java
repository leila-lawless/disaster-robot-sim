package com.example.demo.core.interfaces;

import com.example.demo.core.model..Victim;
import com.example.demo.core.enums.VictimStatus;

public interface Healable {
    void stabilizeVictim(Victim victim);
    boolean deployMedKit();
    int getMedKitCount();
    VictimStatus assessVictim(Victim victim);    
}

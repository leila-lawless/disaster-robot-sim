import com.example.demo.core.model.Position;
import com.example.demo.core.robots.*;
import com.example.demo.core.model.Victim;
import com.example.demo.core.enums.RobotType;
import com.example.demo.core.enums.ZoneType;
import com.example.demo.core.model.Cell;
import com.example.demo.engine.SimulationEngine;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== DISASTER ROBOT SIMULATOR ===");
        System.out.println("Initializing simulation...\n");

        // Create the simulation engine with a 10x10 grid
        SimulationEngine engine = new SimulationEngine(10, 10);

        // Create robots
        ScoutRobot scout = new ScoutRobot("SCOUT-1", new Position(0, 0));
        RescueRobot rescue = new RescueRobot("RESCUE-1", new Position(1, 0));
        MedicalRobot medical = new MedicalRobot("MEDICAL-1", new Position(2, 0));
        SupplyRobot supply = new SupplyRobot("SUPPLY-1", new Position(3, 0));

        System.out.println("Robots created:");
        System.out.println("  " + scout.getId() + " at " + scout.getPosition());
        System.out.println("  " + rescue.getId() + " at " + rescue.getPosition());
        System.out.println("  " + medical.getId() + " at " + medical.getPosition());
        System.out.println("  " + supply.getId() + " at " + supply.getPosition());
        System.out.println();

        // Place a victim on the grid
        Victim victim = new Victim("V-1", new Position(5, 5), RobotType.RESCUE);
        Cell victimCell = engine.getGrid().get(5, 5);
        victimCell.setVictim(victim);
        victimCell.setZoneType(ZoneType.VICTIM);
        System.out.println("Victim placed at " + victim.getPosition());
        System.out.println();

        // Start simulation engine
        engine.start();
        System.out.println();

        // Show robot statuses
        System.out.println("=== ROBOT STATUS REPORT ===");
        System.out.println(scout.getStatus());
        System.out.println(rescue.getStatus());
        System.out.println(medical.getStatus());
        System.out.println(supply.getStatus());

        System.out.println("\n=== SIMULATION COMPLETE ===");
    }
}
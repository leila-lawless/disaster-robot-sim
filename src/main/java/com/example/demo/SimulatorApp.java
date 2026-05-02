package com.example.demo;

import com.example.demo.core.enums.RobotType;
import com.example.demo.core.enums.VictimStatus;
import com.example.demo.core.enums.ZoneType;
import com.example.demo.core.model.Cell;
import com.example.demo.core.model.Position;
import com.example.demo.core.model.Victim;
import com.example.demo.core.robots.*;
import com.example.demo.engine.SimulationEngine;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class SimulatorApp extends Application {

    // ── grid constants ────────────────────────────────────────────
    private static final int CELL  = 56;
    private static final int COLS  = 14;
    private static final int ROWS  = 12;

    // ── simulation state ──────────────────────────────────────────
    private SimulationEngine engine;
    private List<RobotCore>  robots  = new ArrayList<>();
    private List<Victim>     victims = new ArrayList<>();
    private List<String>     log     = new ArrayList<>();

    private int     tick    = 0;
    private int     rescued = 0;
    private boolean paused  = false;
    private Timeline timeline;

    // ── robot states ──────────────────────────────────────────────
    private enum RobotTask { SCOUTING, NAVIGATING_TO_VICTIM, STABILIZING,
                             RESCUING, RETURNING_TO_BASE, RECHARGING, CHARGING_OTHER }
    private Map<String, RobotTask>   robotTasks    = new HashMap<>();
    private Map<String, Victim>      robotTargets  = new HashMap<>();
    private Map<String, Integer>     rechargeTimer = new HashMap<>();

    // ── UI ────────────────────────────────────────────────────────
    private Canvas    canvas;
    private Label     tickLbl, rescuedLbl, eventLbl;
    private Button    pauseBtn;
    private VBox      logBox;
    private List<Label> statusLabels = new ArrayList<>();

    // ── colors ────────────────────────────────────────────────────
    private static final Color BG         = Color.rgb(6, 10, 20);
    private static final Color PANEL      = Color.rgb(12, 18, 35);
    private static final Color ACCENT     = Color.rgb(0, 210, 255);
    private static final Color C_SAFE     = Color.rgb(16, 30, 65);
    private static final Color C_DANGER   = Color.rgb(90, 12, 12);
    private static final Color C_FIRE     = Color.rgb(140, 45, 0);
    private static final Color C_RUBBLE   = Color.rgb(60, 48, 30);
    private static final Color C_WATER    = Color.rgb(8, 45, 110);
    private static final Color C_VICTIM   = Color.rgb(90, 70, 0);
    private static final Color C_BASE     = Color.rgb(15, 65, 25);
    private static final Color C_GRID     = Color.rgb(25, 42, 80);

    private static final Color[] RCOL = {
        Color.rgb(0,  210, 255),   // scout  – cyan
        Color.rgb(255, 70,  70),   // rescue – red
        Color.rgb(60, 255, 120),   // med    – green
        Color.rgb(255, 210,  0)    // supply – gold
    };
    private static final String[] RLET = {"S","R","M","P"};

    // ── BASE positions ────────────────────────────────────────────
    private static final Position BASE_LEFT  = new Position(0,  ROWS/2);
    private static final Position BASE_RIGHT = new Position(COLS-1, ROWS/2);

    // ─────────────────────────────────────────────────────────────
    @Override
    public void start(Stage stage) {
        engine = new SimulationEngine(COLS, ROWS);
        buildMap();
        buildRobots();
        buildVictims();
        buildUI(stage);

        drawGrid();

        timeline = new Timeline(new KeyFrame(Duration.millis(600), e -> {
            if (!paused) { tick++; simulate(); drawGrid(); refreshUI(); }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    // ══════════════════════════════════════════════════════════════
    //  MAP
    // ══════════════════════════════════════════════════════════════
    private void buildMap() {
        // Bases
        setZ(0, 5,  ZoneType.BASE); setZ(0, 6,  ZoneType.BASE);
        setZ(13, 5, ZoneType.BASE); setZ(13, 6, ZoneType.BASE);

        // Rubble walls — force robots to go around
        int[][] rubble = {
            {3,1},{3,2},{3,3},{3,4},
            {5,7},{5,8},{5,9},{5,10},
            {8,1},{8,2},{8,3},
            {10,6},{10,7},{10,8},{10,9},
            {6,4},{7,4},{7,5},
            {2,8},{2,9}
        };
        for (int[] r : rubble) setZ(r[0], r[1], ZoneType.RUBBLE);

        // Danger zones
        int[][] danger = {
            {4,5},{4,6},{5,5},{5,6},
            {9,4},{9,5},
            {11,2},{11,3}
        };
        for (int[] d : danger) setZ(d[0], d[1], ZoneType.DANGER);

        // Fire
        int[][] fire = {{6,1},{6,2},{7,1},{12,8},{12,9}};
        for (int[] f : fire) setZ(f[0], f[1], ZoneType.FIRE);

        // Water
        int[][] water = {{1,3},{2,3},{1,4},{8,9},{9,9},{9,10},{10,10}};
        for (int[] w : water) setZ(w[0], w[1], ZoneType.WATER);
    }

    // ══════════════════════════════════════════════════════════════
    //  ROBOTS
    // ══════════════════════════════════════════════════════════════
    private void buildRobots() {
        robots.add(new ScoutRobot ("SCOUT-1",  new Position(0, 5)));
        robots.add(new RescueRobot("RESCUE-1", new Position(0, 6)));
        robots.add(new MedicalRobot("MED-1",   new Position(13, 5)));
        robots.add(new SupplyRobot ("SUPPLY-1",new Position(13, 6)));

        for (RobotCore r : robots) {
            robotTasks.put(r.getId(), RobotTask.SCOUTING);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  VICTIMS
    // ══════════════════════════════════════════════════════════════
    private void buildVictims() {
        addVictim(4,  2,  RobotType.RESCUE);
        addVictim(11, 5,  RobotType.MEDICAL);
        addVictim(2,  10, RobotType.RESCUE);
        addVictim(9,  7,  RobotType.MEDICAL);
        addVictim(12, 2,  RobotType.RESCUE);
        addVictim(6,  10, RobotType.MEDICAL);
    }

    // ══════════════════════════════════════════════════════════════
    //  MAIN SIMULATION TICK
    // ══════════════════════════════════════════════════════════════
    private void simulate() {
        // Tick victim timers
        for (Victim v : victims) {
            if (v.getStatus() == VictimStatus.DISCOVERED ||
                v.getStatus() == VictimStatus.UNDISCOVERED) {
                v.tick();
                if (!v.isAlive() && v.getStatus() != VictimStatus.RESCUED
                    && v.getStatus() != VictimStatus.LOST) {
                    v.setStatus(VictimStatus.LOST);
                    setZ(v.getPosition().x, v.getPosition().y, ZoneType.SAFE);
                    addLog("LOST: " + v.getId() + " did not survive.");
                }
            }
        }

        for (int i = 0; i < robots.size(); i++) {
            RobotCore robot = robots.get(i);
            String id = robot.getId();
            RobotTask task = robotTasks.get(id);

            // ── RECHARGING at base ────────────────────────────────
            if (task == RobotTask.RECHARGING) {
                int t = rechargeTimer.getOrDefault(id, 0) + 1;
                rechargeTimer.put(id, t);
                robot.rechargeBattery(5);
                if (robot.getBattery() >= 95) {
                    robot.rechargeBattery(100 - robot.getBattery());
                    robotTasks.put(id, RobotTask.SCOUTING);
                    rechargeTimer.put(id, 0);
                    addLog(id + " fully recharged. Redeploying.");
                }
                continue;
            }

            // ── LOW BATTERY → return to base ─────────────────────
            if (robot.getBattery() < 20 && task != RobotTask.RETURNING_TO_BASE) {
                robotTasks.put(id, RobotTask.RETURNING_TO_BASE);
                robotTargets.remove(id);
                addLog(id + " battery critical! Returning to base.");
            }

            // ── RETURNING TO BASE ─────────────────────────────────
            if (task == RobotTask.RETURNING_TO_BASE) {
                Position base = (robot.getPosition().x < COLS / 2) ? BASE_LEFT : BASE_RIGHT;
                if (robot.getPosition().equals(base)) {
                    robotTasks.put(id, RobotTask.RECHARGING);
                    addLog(id + " reached base. Recharging...");
                } else {
                    moveToward(robot, base, true);
                }
                robot.drainBattery(1);
                continue;
            }

            // ── SUPPLY ROBOT: charge nearby low robots ────────────
            if (robot instanceof SupplyRobot) {
                RobotCore lowBot = findLowBatteryRobot(robot);
                if (lowBot != null && task != RobotTask.CHARGING_OTHER) {
                    robotTasks.put(id, RobotTask.CHARGING_OTHER);
                    robotTargets.put(id, null);
                    addLog(id + " moving to charge " + lowBot.getId());
                }
                if (task == RobotTask.CHARGING_OTHER) {
                    RobotCore target = findLowBatteryRobot(robot);
                    if (target == null) {
                        robotTasks.put(id, RobotTask.SCOUTING);
                    } else if (adjacent(robot.getPosition(), target.getPosition())) {
                        target.rechargeBattery(8);
                        robot.drainBattery(4);
                        addLog(id + " charging " + target.getId() + " [+" + 8 + "%]");
                        if (target.getBattery() >= 80) robotTasks.put(id, RobotTask.SCOUTING);
                    } else {
                        moveToward(robot, target.getPosition(), true);
                        robot.drainBattery(1);
                    }
                    continue;
                }
            }

            // ── SCOUT: discover victims ───────────────────────────
            if (robot instanceof ScoutRobot && task == RobotTask.SCOUTING) {
                // Reveal undiscovered victims nearby
                for (Victim v : victims) {
                    if (v.getStatus() == VictimStatus.UNDISCOVERED) {
                        if (robot.getPosition().distanceTo(v.getPosition()) <= 4) {
                            v.setStatus(VictimStatus.DISCOVERED);
                            setZ(v.getPosition().x, v.getPosition().y, ZoneType.VICTIM);
                            addLog("SCOUT found " + v.getId() + " at " + v.getPosition() + "!");
                        }
                    }
                }
                // Move toward unexplored area
                Position unexplored = findUnexplored(robot);
                if (unexplored != null) moveToward(robot, unexplored, true);
                robot.drainBattery(1);
                continue;
            }

            // ── MEDICAL ROBOT: stabilize discovered victims ───────
            if (robot instanceof MedicalRobot) {
                Victim target = robotTargets.get(id) != null ?
                    getVictimById(robotTargets.get(id).getId()) : null;

                if (target == null || target.getStatus() == VictimStatus.STABLE
                    || target.getStatus() == VictimStatus.RESCUED) {
                    target = findVictimForMedical();
                    if (target != null) {
                        robotTargets.put(id, target);
                        robotTasks.put(id, RobotTask.NAVIGATING_TO_VICTIM);
                        addLog(id + " heading to stabilize " + target.getId());
                    } else {
                        // Wander toward base area
                        moveToward(robot, BASE_RIGHT, true);
                        robot.drainBattery(1);
                        continue;
                    }
                }

                if (robot.getPosition().equals(target.getPosition())) {
                    // Stabilize
                    target.stabilize(15);
                    target.setStatus(VictimStatus.STABLE);
                    robotTasks.put(id, RobotTask.STABILIZING);
                    addLog(id + " STABILIZED " + target.getId() + "!");
                    robotTargets.put(id, null);
                } else {
                    moveToward(robot, target.getPosition(), true);
                }
                robot.drainBattery(1);
                continue;
            }

            // ── RESCUE ROBOT: rescue stable/discovered victims ────
            if (robot instanceof RescueRobot) {
                Victim target = robotTargets.get(id) != null ?
                    getVictimById(robotTargets.get(id).getId()) : null;

                if (target == null || target.getStatus() == VictimStatus.RESCUED
                    || target.getStatus() == VictimStatus.LOST) {
                    target = findVictimForRescue();
                    if (target != null) {
                        robotTargets.put(id, target);
                        robotTasks.put(id, RobotTask.NAVIGATING_TO_VICTIM);
                        addLog(id + " heading to rescue " + target.getId());
                    } else {
                        moveToward(robot, BASE_LEFT, true);
                        robot.drainBattery(1);
                        continue;
                    }
                }

                if (robot.getPosition().equals(target.getPosition())) {
                    if (target.getStatus() == VictimStatus.STABLE ||
                        target.getStatus() == VictimStatus.DISCOVERED) {
                        target.setStatus(VictimStatus.RESCUED);
                        setZ(target.getPosition().x, target.getPosition().y, ZoneType.SAFE);
                        rescued++;
                        rescuedLbl.setText("RESCUED: " + rescued + " / " + victims.size());
                        robotTasks.put(id, RobotTask.RETURNING_TO_BASE);
                        robotTargets.put(id, null);
                        addLog("★ " + id + " RESCUED " + target.getId() + "! (" + rescued + "/" + victims.size() + ")");
                    }
                } else {
                    moveToward(robot, target.getPosition(), true);
                }
                robot.drainBattery(1);
                continue;
            }

            // ── DEFAULT: drain battery ────────────────────────────
            robot.drainBattery(1);
        }

        tickLbl.setText("TICK  " + String.format("%03d", tick));

        // Check win
        long active = victims.stream().filter(v ->
            v.getStatus() != VictimStatus.RESCUED && v.getStatus() != VictimStatus.LOST).count();
        if (active == 0 && tick > 5) {
            eventLbl.setText("MISSION COMPLETE — " + rescued + " rescued, " +
                (victims.size() - rescued) + " lost. (" + tick + " ticks)");
            eventLbl.setTextFill(rescued == victims.size() ? Color.GOLD : Color.ORANGE);
            timeline.stop();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  PATHFINDING — BFS (avoids rubble, danger, fire, water)
    // ══════════════════════════════════════════════════════════════
    private void moveToward(RobotCore robot, Position target, boolean avoidHazards) {
        Position cur = robot.getPosition();
        if (cur.equals(target)) return;

        List<Position> path = bfs(cur, target, avoidHazards);
        if (path != null && path.size() > 1) {
            robot.setPosition(path.get(1));
        } else if (path == null) {
            // If no path found, try ignoring some hazards
            path = bfs(cur, target, false);
            if (path != null && path.size() > 1) robot.setPosition(path.get(1));
        }
    }

    private List<Position> bfs(Position start, Position goal, boolean avoidHazards) {
        Queue<Position> queue = new LinkedList<>();
        Map<Position, Position> came = new HashMap<>();
        queue.add(start);
        came.put(start, null);

        while (!queue.isEmpty()) {
            Position cur = queue.poll();
            if (cur.equals(goal)) {
                // Reconstruct path
                List<Position> path = new ArrayList<>();
                Position p = goal;
                while (p != null) { path.add(0, p); p = came.get(p); }
                return path;
            }
            for (Position nb : neighbors(cur)) {
                if (!came.containsKey(nb)) {
                    if (avoidHazards && isHazard(nb) && !nb.equals(goal)) continue;
                    came.put(nb, cur);
                    queue.add(nb);
                }
            }
        }
        return null;
    }

    private List<Position> neighbors(Position p) {
        List<Position> nb = new ArrayList<>();
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        for (int[] d : dirs) {
            int nx = p.x + d[0], ny = p.y + d[1];
            if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS) nb.add(new Position(nx, ny));
        }
        return nb;
    }

    private boolean isHazard(Position p) {
        ZoneType z = engine.getGrid().get(p.x, p.y).getZoneType();
        return z == ZoneType.RUBBLE || z == ZoneType.DANGER ||
               z == ZoneType.FIRE   || z == ZoneType.WATER;
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════
    private Victim findVictimForRescue() {
        Victim best = null; double minD = Double.MAX_VALUE;
        RobotCore rescue = robots.get(1);
        for (Victim v : victims) {
            if ((v.getStatus() == VictimStatus.STABLE || v.getStatus() == VictimStatus.DISCOVERED)
                && !isAssigned(v)) {
                double d = rescue.getPosition().distanceTo(v.getPosition());
                if (d < minD) { minD = d; best = v; }
            }
        }
        return best;
    }

    private Victim findVictimForMedical() {
        Victim best = null; double minD = Double.MAX_VALUE;
        RobotCore med = robots.get(2);
        for (Victim v : victims) {
            if (v.getStatus() == VictimStatus.DISCOVERED && !isAssigned(v)) {
                double d = med.getPosition().distanceTo(v.getPosition());
                if (d < minD) { minD = d; best = v; }
            }
        }
        return best;
    }

    private boolean isAssigned(Victim v) {
        for (Victim t : robotTargets.values()) {
            if (t != null && t.getId().equals(v.getId())) return true;
        }
        return false;
    }

    private RobotCore findLowBatteryRobot(RobotCore supply) {
        for (RobotCore r : robots) {
            if (r == supply) continue;
            RobotTask t = robotTasks.get(r.getId());
            if (r.getBattery() < 40 && t != RobotTask.RECHARGING && t != RobotTask.RETURNING_TO_BASE) {
                return r;
            }
        }
        return null;
    }

    private Position findUnexplored(RobotCore scout) {
        // Move in a sweeping pattern based on tick
        int tx = (tick * 3) % COLS;
        int ty = (tick * 2) % ROWS;
        return new Position(tx, ty);
    }

    private boolean adjacent(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y) <= 1;
    }

    private Victim getVictimById(String id) {
        return victims.stream().filter(v -> v.getId().equals(id)).findFirst().orElse(null);
    }

    private void addLog(String msg) {
        log.add(0, "T" + String.format("%03d", tick) + "  " + msg);
        if (log.size() > 12) log.remove(log.size() - 1);
        eventLbl.setText(msg);
        eventLbl.setTextFill(msg.startsWith("★") ? Color.GOLD :
                             msg.startsWith("LOST") ? Color.RED :
                             Color.rgb(180, 210, 255));
        refreshLog();
    }

    private void refreshLog() {
        logBox.getChildren().clear();
        for (String entry : log) {
            Label l = new Label(entry);
            l.setFont(Font.font("Courier New", 10));
            l.setTextFill(entry.contains("★") ? Color.GOLD :
                          entry.contains("LOST") ? Color.rgb(255,100,100) :
                          entry.contains("STABILIZED") ? Color.LIMEGREEN :
                          entry.contains("recharg") ? Color.rgb(255,210,0) :
                          Color.rgb(160, 190, 240));
            l.setWrapText(true);
            logBox.getChildren().add(l);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  DRAWING
    // ══════════════════════════════════════════════════════════════
    private void drawGrid() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(BG);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Cells
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                ZoneType z = engine.getGrid().get(x, y).getZoneType();
                double px = x * CELL, py = y * CELL;

                Color bg = switch (z) {
                    case DANGER -> C_DANGER;
                    case FIRE   -> C_FIRE;
                    case RUBBLE -> C_RUBBLE;
                    case WATER  -> C_WATER;
                    case VICTIM -> C_VICTIM;
                    case BASE   -> C_BASE;
                    default     -> C_SAFE;
                };
                gc.setFill(bg);
                gc.fillRoundRect(px+2, py+2, CELL-4, CELL-4, 6, 6);
                gc.setStroke(C_GRID);
                gc.setLineWidth(1);
                gc.strokeRoundRect(px+2, py+2, CELL-4, CELL-4, 6, 6);

                drawSymbol(gc, z, px, py);
            }
        }

        // Victims
        for (Victim v : victims) {
            if (v.getStatus() == VictimStatus.RESCUED || v.getStatus() == VictimStatus.LOST) continue;
            double px = v.getPosition().x * CELL;
            double py = v.getPosition().y * CELL;
            double pulse = 7 + 3 * Math.sin(tick * 0.5);
            double cx = px + CELL / 2.0, cy = py + CELL / 2.0;

            // Outer ring
            Color ringCol = v.getStatus() == VictimStatus.STABLE ? Color.LIMEGREEN : Color.YELLOW;
            gc.setStroke(ringCol);
            gc.setLineWidth(2);
            gc.strokeOval(cx - pulse - 3, cy - pulse - 3, (pulse+3)*2, (pulse+3)*2);

            // Inner dot
            gc.setFill(ringCol);
            gc.fillOval(cx - pulse, cy - pulse, pulse*2, pulse*2);

            // Label
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 9));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(v.getStatus() == VictimStatus.STABLE ? "OK" : "SOS", cx, cy + 4);

            // Survival bar
            double pct = Math.max(0, v.getSurvivalTimer() / 100.0);
            gc.setFill(Color.rgb(20,20,40));
            gc.fillRect(px+4, py+4, CELL-8, 5);
            gc.setFill(pct > 0.5 ? Color.LIMEGREEN : pct > 0.25 ? Color.ORANGE : Color.RED);
            gc.fillRect(px+4, py+4, (CELL-8)*pct, 5);
        }

        // Robots
        for (int i = 0; i < robots.size(); i++) {
            RobotCore r = robots.get(i);
            double px = r.getPosition().x * CELL;
            double py = r.getPosition().y * CELL;
            Color rc = RCOL[i];
            double cx = px + CELL/2.0, cy = py + CELL/2.0;

            // Glow
            gc.setFill(Color.color(rc.getRed(), rc.getGreen(), rc.getBlue(), 0.18));
            gc.fillOval(px+4, py+4, CELL-8, CELL-8);

            // Body
            gc.setFill(rc);
            gc.fillOval(px+11, py+9, CELL-22, CELL-20);

            // Letter
            gc.setFill(Color.rgb(6, 10, 20));
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(RLET[i], cx, cy + 5);

            // Task badge
            RobotTask task = robotTasks.get(r.getId());
            String badge = switch(task) {
                case RECHARGING         -> "CHG";
                case RETURNING_TO_BASE  -> "RTB";
                case STABILIZING        -> "MED";
                case RESCUING           -> "RES";
                case CHARGING_OTHER     -> "PWR";
                case NAVIGATING_TO_VICTIM -> "NAV";
                default                 -> "SCT";
            };
            gc.setFill(Color.rgb(20,20,50,0.85));
            gc.fillRoundRect(px+2, py+CELL-16, CELL-4, 13, 3, 3);
            gc.setFill(rc);
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 8));
            gc.fillText(badge, cx, py+CELL-5);

            // Battery bar
            double batt = r.getBattery() / 100.0;
            gc.setFill(Color.rgb(10,10,30));
            gc.fillRect(px+4, py+3, CELL-8, 4);
            gc.setFill(batt > 0.5 ? Color.LIMEGREEN : batt > 0.2 ? Color.ORANGE : Color.RED);
            gc.fillRect(px+4, py+3, (CELL-8)*batt, 4);
        }
    }

    private void drawSymbol(GraphicsContext gc, ZoneType z, double px, double py) {
        double cx = px + CELL/2.0, cy = py + CELL/2.0;
        gc.setTextAlign(TextAlignment.CENTER);
        switch (z) {
            case DANGER -> {
                gc.setStroke(Color.rgb(255,200,0)); gc.setLineWidth(2);
                gc.strokePolygon(new double[]{cx,cx-13,cx+13}, new double[]{cy-12,cy+9,cy+9}, 3);
                gc.setFill(Color.rgb(255,200,0));
                gc.setFont(Font.font("Courier New", FontWeight.BOLD, 13));
                gc.fillText("!", cx, cy+7);
            }
            case FIRE -> {
                gc.setFill(Color.rgb(255,130,0));
                gc.fillOval(cx-7, cy-4, 14, 18);
                gc.setFill(Color.rgb(255,220,0));
                gc.fillOval(cx-4, cy+2, 8, 12);
                gc.setFill(Color.WHITE);
                gc.fillOval(cx-2, cy+6, 4, 6);
            }
            case RUBBLE -> {
                gc.setFill(Color.rgb(110,90,60));
                gc.fillRect(cx-11,cy-7,9,7); gc.fillRect(cx+2,cy-9,9,9); gc.fillRect(cx-5,cy+2,12,6);
            }
            case WATER -> {
                gc.setStroke(Color.rgb(80,150,255)); gc.setLineWidth(2.5);
                gc.beginPath();
                gc.moveTo(cx-14, cy); gc.bezierCurveTo(cx-7,cy-7,cx,cy+7,cx+7,cy);
                gc.bezierCurveTo(cx+10,cy-4,cx+14,cy-7,cx+14,cy); gc.stroke();
            }
            case BASE -> {
                gc.setFill(Color.rgb(50,200,80));
                gc.fillRect(cx-2,cy-11,4,22); gc.fillRect(cx-11,cy-2,22,4);
            }
            default -> {}
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  UI
    // ══════════════════════════════════════════════════════════════
    private void buildUI(Stage stage) {
        canvas = new Canvas(COLS * CELL, ROWS * CELL);

        // Top bar
        tickLbl = new Label("TICK  000");
        tickLbl.setFont(Font.font("Courier New", FontWeight.BOLD, 20));
        tickLbl.setTextFill(ACCENT);

        rescuedLbl = new Label("RESCUED: 0 / " + victims.size());
        rescuedLbl.setFont(Font.font("Courier New", FontWeight.BOLD, 15));
        rescuedLbl.setTextFill(Color.LIMEGREEN);

        eventLbl = new Label("Simulation starting — robots deploying...");
        eventLbl.setFont(Font.font("Courier New", 12));
        eventLbl.setTextFill(Color.rgb(160, 200, 255));

        pauseBtn = new Button("  PAUSE  ");
        styleBtn(pauseBtn, ACCENT);
        pauseBtn.setOnAction(e -> {
            paused = !paused;
            pauseBtn.setText(paused ? " RESUME  " : "  PAUSE  ");
        });

        Button resetBtn = new Button("  RESET  ");
        styleBtn(resetBtn, Color.rgb(255,100,100));
        resetBtn.setOnAction(e -> reset());

        HBox top = new HBox(20, tickLbl, rescuedLbl, eventLbl);
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(top, Priority.ALWAYS);
        HBox topBar = new HBox(20, top, pauseBtn, resetBtn);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10, 16, 10, 16));
        topBar.setStyle("-fx-background-color: rgb(8,12,28); -fx-border-color: rgb(0,210,255); -fx-border-width: 0 0 1 0;");

        // Right panel
        VBox right = new VBox(12);
        right.setPrefWidth(200);
        right.setPadding(new Insets(14, 10, 14, 10));
        right.setStyle("-fx-background-color: rgb(12,18,35); -fx-border-color: rgb(0,210,255); -fx-border-width: 0 0 0 1;");

        Label rtitle = sectionLabel("ROBOT STATUS");
        right.getChildren().add(rtitle);

        String[] rnames = {"SCOUT-1","RESCUE-1","MED-1","SUPPLY-1"};
        for (int i = 0; i < 4; i++) {
            Label l = new Label(rnames[i] + "\n  BATT: 100% | SCOUTING");
            l.setFont(Font.font("Courier New", 10));
            l.setTextFill(RCOL[i]);
            l.setWrapText(true);
            l.setStyle("-fx-background-color: rgba(0,0,0,0.35); -fx-padding: 5 7 5 7; -fx-background-radius: 4;");
            statusLabels.add(l);
            right.getChildren().add(l);
        }

        Label ltitle = sectionLabel("LEGEND");
        ltitle.setPadding(new Insets(10,0,0,0));
        right.getChildren().add(ltitle);

        addLeg(right,"SAFE",    Color.rgb(40,80,160));
        addLeg(right,"DANGER",  Color.rgb(200,40,40));
        addLeg(right,"FIRE",    Color.rgb(255,120,0));
        addLeg(right,"RUBBLE",  Color.rgb(130,100,60));
        addLeg(right,"WATER",   Color.rgb(40,100,220));
        addLeg(right,"BASE",    Color.rgb(50,180,70));
        addLeg(right,"VICTIM",  Color.YELLOW);

        Label logtitle = sectionLabel("EVENT LOG");
        logtitle.setPadding(new Insets(10,0,0,0));
        right.getChildren().add(logtitle);
        logBox = new VBox(3);
        right.getChildren().add(logBox);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(canvas);
        root.setRight(right);
        root.setStyle("-fx-background-color: rgb(6,10,20);");

        Scene scene = new Scene(root, COLS * CELL + 206, ROWS * CELL + 52);
        stage.setTitle("Disaster Robot Simulator");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void refreshUI() {
        String[] rnames = {"SCOUT-1","RESCUE-1","MED-1","SUPPLY-1"};
        for (int i = 0; i < robots.size(); i++) {
            RobotCore r = robots.get(i);
            RobotTask t = robotTasks.get(r.getId());
            statusLabels.get(i).setText(rnames[i] + "\n  " + r.getBattery() + "% | " + t.toString());
        }
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        l.setTextFill(ACCENT);
        return l;
    }

    private void addLeg(VBox box, String label, Color color) {
        HBox row = new HBox(7);
        row.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.shape.Rectangle r = new javafx.scene.shape.Rectangle(11,11,color);
        r.setArcWidth(3); r.setArcHeight(3);
        Label l = new Label(label);
        l.setFont(Font.font("Courier New", 10));
        l.setTextFill(Color.rgb(170,195,230));
        row.getChildren().addAll(r, l);
        box.getChildren().add(row);
    }

    private void styleBtn(Button btn, Color c) {
        String hex = String.format("#%02x%02x%02x",
            (int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255));
        String base = "-fx-background-color:transparent;-fx-border-color:" + hex +
            ";-fx-border-width:1;-fx-text-fill:" + hex +
            ";-fx-font-family:'Courier New';-fx-font-size:12;-fx-cursor:hand;-fx-padding:5 12 5 12;";
        String hover = "-fx-background-color:" + hex + "22;-fx-border-color:" + hex +
            ";-fx-border-width:1;-fx-text-fill:" + hex +
            ";-fx-font-family:'Courier New';-fx-font-size:12;-fx-cursor:hand;-fx-padding:5 12 5 12;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }

    private void reset() {
        timeline.stop();
        tick = 0; rescued = 0; paused = false;
        pauseBtn.setText("  PAUSE  ");
        tickLbl.setText("TICK  000");
        rescuedLbl.setText("RESCUED: 0 / " + victims.size());
        eventLbl.setText("Simulation reset — redeploying robots...");
        eventLbl.setTextFill(Color.rgb(160,200,255));
        log.clear(); logBox.getChildren().clear();
        robots.get(0).setPosition(new Position(0,5));
        robots.get(1).setPosition(new Position(0,6));
        robots.get(2).setPosition(new Position(13,5));
        robots.get(3).setPosition(new Position(13,6));
        for (RobotCore r : robots) {
            r.rechargeBattery(100 - r.getBattery());
            robotTasks.put(r.getId(), RobotTask.SCOUTING);
            robotTargets.remove(r.getId());
        }
        for (Victim v : victims) {
            v.setStatus(VictimStatus.UNDISCOVERED);
            // reset survival timer via re-add
        }
        buildVictimsReset();
        timeline.play();
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════
    private void addVictim(int x, int y, RobotType type) {
        Victim v = new Victim("V-" + (victims.size()+1), new Position(x,y), type);
        victims.add(v);
        // Don't show on map until scout discovers
    }

    private void buildVictimsReset() {
        victims.clear();
        engine.getGrid().get(5,5).setZoneType(ZoneType.SAFE);
        engine.getGrid().get(3,7).setZoneType(ZoneType.SAFE);
        engine.getGrid().get(7,2).setZoneType(ZoneType.SAFE);
        engine.getGrid().get(11,5).setZoneType(ZoneType.SAFE);
        engine.getGrid().get(2,10).setZoneType(ZoneType.SAFE);
        engine.getGrid().get(6,10).setZoneType(ZoneType.SAFE);
        buildVictims();
    }

    private void setZ(int x, int y, ZoneType z) {
        engine.getGrid().get(x,y).setZoneType(z);
    }

    public static void main(String[] args) { launch(args); }
}
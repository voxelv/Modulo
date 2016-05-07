package com.derelictech.macromachine.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.derelictech.macromachine.e_net.EConsumer;
import com.derelictech.macromachine.e_net.EConsumerProducerStorageUnit;
import com.derelictech.macromachine.e_net.EProducer;
import com.derelictech.macromachine.e_net.EStorage;
import com.derelictech.macromachine.screens.GameScreen;
import com.derelictech.macromachine.tiles.Material;
import com.derelictech.macromachine.tiles.Tile;
import com.derelictech.macromachine.tiles.materials.BasicMaterial;
import com.derelictech.macromachine.tiles.materials.MetalicMaterial;
import com.derelictech.macromachine.tiles.units.Cell;
import com.derelictech.macromachine.tiles.units.ControlUnit;
import com.derelictech.macromachine.tiles.units.Unit;
import com.derelictech.macromachine.tiles.units.Wire;

/**
 * Contains all the information for a Power Level
 * TODO: WIP
 */
public class Level extends Group {

    private GameScreen gameScreen;
    private int powerLevel;
    private TileGrid gameGrid;
    private Cell cell;
    private IntMap<Grid<Unit>> lowerCells;

    public Level(GameScreen gameScreen, int power) {
        this.gameScreen = gameScreen;
        this.powerLevel = power;
        this.lowerCells = new IntMap<Grid<Unit>>();
        this.init();
    }

    public void init() {
        gameGrid = new TileGrid(25, 25, 5 / Const.TEXTURE_RESOLUTION, 3 / Const.TEXTURE_RESOLUTION, false, "game_grid_edge5_pad3");
        addActor(gameGrid);

        cell = new Cell(gameGrid, 10, 10, 5, 5);
        addActor(cell);

        this.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug("LEVEL", "LEVEL GOT CLICK TOO. ");
                if (event.getRelatedActor() != null) Gdx.app.debug("LEVEL", " ITEM: " + event.getRelatedActor());
                else Gdx.app.debug("--", "\n");

                Slot s;
                if (event.getRelatedActor() instanceof Slot) {
                    s = ((Slot) event.getRelatedActor());

                    if (cell.containsUnitAt(s.getGridX(), s.getGridY())) {
                        if (!cell.isClosed()) {
                            switch (button) {
                                case Input.Buttons.LEFT:
                                    cell.addUnitAt(new ControlUnit(cell), s.getGridX(), s.getGridY());
                                    break;
                                case Input.Buttons.MIDDLE:
                                    cell.addUnitAt(new Wire(cell), s.getGridX(), s.getGridY());
                                    break;
                                case Input.Buttons.RIGHT:
                                    cell.removeUnitAt(s.getGridX(), s.getGridY());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    else {
                        Material m;
                        switch(button) {
                            case Input.Buttons.LEFT:
                                m = new BasicMaterial();
                                if(gameGrid.addTileAt(m, s.getGridX(), s.getGridY())) {
                                    gameGrid.addActor(m);
                                }
                                break;
                            case Input.Buttons.MIDDLE:
                                m = new MetalicMaterial();
                                if(gameGrid.addTileAt(m, s.getGridX(), s.getGridY())) {
                                    gameGrid.addActor(m);
                                }
                                break;
                            case Input.Buttons.RIGHT:
                                gameGrid.removeTileAt(s.getGridX(), s.getGridY());
                                break;
                            default:
                                break;
                        }
                    }
                }

                return true;
            }
        });
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public void upLevel() {
        if(!gameGrid.isEmpty()) return;
        if(!cellAtCenter()) return;

        cell.closeCell();
        lowerCells.put(powerLevel, cell.getUnits()); // Save the old cell contents into an IntMap

        Task finishUpLevelTask = new Task() {
            @Override
            public void run() {
                removeActor(gameGrid);
                gameGrid = new TileGrid(25, 25, 5 / Const.TEXTURE_RESOLUTION, 3 / Const.TEXTURE_RESOLUTION, true, "game_grid_edge5_pad3");
                addActor(gameGrid);

                removeActor(cell);
                cell = new Cell(gameGrid, 10, 10, 5, 5);
                addActor(cell);

                computeLowerCells(cell);
            }
        };

        Timer.schedule(finishUpLevelTask, cell.timeTillClosed());

        System.gc(); // Collect that garbage

        powerLevel++;

        Gdx.app.debug("LEVEL", lowerCells.toString());
    }

    //TODO Optimize, this is brute force O(N^3), horrible yes, but N is small...
    private void computeLowerCells(Cell applyToThisCell) {
        long consumeAmount = 0;
        long consumeBuffer = 0;
        long produceAmount = 0;
        long energyStored = 0;
        long energyStorageCapacity =  0;

        for(Grid g : lowerCells.values()) {
            for(int x = 0; x < g.getCols(); x++) {
                for(int y = 0; y < g.getRows(); y++) {
                    Object o = g.getItemAt(x, y);
                    if(!(o instanceof ControlUnit)) // Don't count control units because we have them here.
                        if(o instanceof EProducer) {
                            produceAmount += ((EProducer) o).getProduceAmount();
                        }
                        if(o instanceof EConsumer) {
                            consumeAmount += ((EConsumer) o).getConsumeAmount();
                            consumeBuffer += ((EConsumer) o).getConsumeBuffer();
                        }
                        if(o instanceof EStorage) {
                            energyStored += ((EStorage) o).amountStored();
                            energyStorageCapacity += ((EStorage) o).getCapacity();
                        }
                }
            }
        }
        cell.getControlUnit().setStats(consumeAmount, consumeBuffer, produceAmount, energyStored, energyStorageCapacity);
    }

    private boolean cellAtCenter() {
        return (cell.getGridX() == 10 && cell.getGridY() == 10);
    }

    public boolean moveCell(GridDirection dir) {
        return cell.move(dir);
    }
}

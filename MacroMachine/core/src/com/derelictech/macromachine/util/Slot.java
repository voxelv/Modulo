package com.derelictech.macromachine.util;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.derelictech.macromachine.tiles.Tile;
import com.derelictech.macromachine.tiles.units.Wire;

import javax.management.RuntimeErrorException;

/**
 * A Slot contains a {@link Tile}
 */
public class Slot extends Actor{
    private int gridX, gridY;
    private float width, height;
    private SlotGrid grid;

    private Tile tile;

    public Slot(SlotGrid grid, final int gridX, final int gridY, float width, float height) {
        this.grid = grid;
        this.gridX = gridX;
        this.gridY = gridY;
        this.width = width;
        this.height = height;

        setBounds(0, 0, 1, 1);
        setTouchable(Touchable.enabled);

        addListener(new ClickListener() {
            int counter = 0;
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug("SLOT", "["+ counter++ +"] Touched Slot: x"+gridX+" y"+gridY+" Tile"+ ((tile == null) ? " NULL" : " bounds: " + tile.getX() + " " + tile.getY() + " " + tile.getWidth() + " " + tile.getHeight()
                        + " Contains: " + tile.toString() +" | hash: "+ tile.hashCode()));
                event.setRelatedActor(Slot.this);
                return true;
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if(tile != null) {
                    event.setRelatedActor(Slot.this);
                    tile.setColor(Color.RED);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if(tile != null) {
                    event.setRelatedActor(Slot.this);
                    tile.setColor(Color.WHITE);
                }
            }
        });
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public Tile removeTile() {
        Tile t = this.tile;
        this.tile = null;
        return t;
    }

    public void setGridPos(int x, int y) {
        this.gridX = x;
        this.gridY = y;
        tile.setGridPos(x, y);
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        if(tile != null) {
            tile.setPosition(x, y);
        }
    }
}

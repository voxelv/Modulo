package com.derelictech.macromachine.tiles;

import com.derelictech.macromachine.util.Assets;

/**
 * Created by Tim on 4/19/2016.
 */
public abstract class Material extends Tile {
    public Material(String texture_name) {
        super(texture_name);
        sprite.setRegion(Assets.inst.getRegion(texture_name));
    }
}

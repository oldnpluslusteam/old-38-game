package com.github.oldnpluslusteam.old_38_game.model.impl;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.oldnpluslusteam.old_38_game.model.ISize;
import com.github.oldnpluslusteam.old_38_game.model.Positionable;

/**
 *
 */
public class PlayerPlanet implements Positionable, ISize {
    private final Vector2 position;
    private final float size;

    public PlayerPlanet(Vector2 position, float size) {
        this.position = position;
        this.size = size;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public float getSize() {
        return size;
    }

    public Vector2 startPoint() {
        Vector2 pt = new Vector2(0, getSize() * 0.125f);
        pt.rotate(MathUtils.random(360));
        pt.add(getPosition());
        return pt;
    }
}

package com.github.oldnpluslusteam.old_38_game.model.impl;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.oldnpluslusteam.old_38_game.model.Collidable;
import com.github.oldnpluslusteam.old_38_game.model.CollidableAction;
import com.github.oldnpluslusteam.old_38_game.model.ISize;
import com.github.oldnpluslusteam.old_38_game.model.Positionable;

/**
 *
 */
public class PlayerPlanet implements Positionable, ISize, Collidable {
    private final Vector2 position;
    private final float size;
    private final CollidableAction collidableAction;

    public PlayerPlanet(Vector2 position, float size, CollidableAction collidableAction) {
        this.position = position;
        this.size = size;
        this.collidableAction = collidableAction;
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

    @Override
    public boolean isCollide(Collidable collidable) {
        if (!(collidable instanceof EnemyPlanet)) return false;
        float distance = collidable.getPosition().dst(position);
        return distance < (getSize() + collidable.getSize()) / 2;
    }

    @Override
    public CollidableAction getCollidableAction() {
        return collidableAction;
    }
}

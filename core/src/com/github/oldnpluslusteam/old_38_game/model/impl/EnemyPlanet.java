package com.github.oldnpluslusteam.old_38_game.model.impl;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.github.oldnpluslusteam.old_38_game.model.*;

public class EnemyPlanet implements Collidable, Positionable, ISize, Velocity, Updatable, Disposable {
    private final float size;
    private final CollidableAction collidableAction;
    private final Vector2 velocity;
    private final Vector2 position;
    private final Texture texture;
    private final DisposableAction disposableAction;

    public EnemyPlanet(float size, CollidableAction collidableAction, Vector2 velocity, Vector2 position, Texture texture, DisposableAction disposableAction) {
        this.size = size;
        this.collidableAction = collidableAction;
        this.velocity = velocity;
        this.position = position;
        this.texture = texture;
        this.disposableAction = disposableAction;
    }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public boolean isCollide(Collidable collidable) {
        if (this == collidable) return false;
        if (collidable instanceof EnemyPlanet) return false;
        float distance = collidable.getPosition().dst(position);
        return distance < (getSize() + collidable.getSize()) / 2;
    }

    @Override
    public CollidableAction getCollidableAction() {
        return collidableAction;
    }

    @Override
    public Vector2 getVelocity() {
        return velocity;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public boolean update(float dt) {
        return true;
    }

    public Texture getTexture() {
        return texture;
    }

    @Override
    public void dispose() {
        if (null != disposableAction) disposableAction.dispose();
    }
}

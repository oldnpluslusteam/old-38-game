package com.github.oldnpluslusteam.old_38_game.model.impl;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.github.oldnpluslusteam.old_38_game.model.*;

public class EnemyPlanet implements Collidable, Positionable, ISize, Velocity, Updatable, Disposable {
    private final float size;
    private final CollidableAction collidableAction;
    private final Vector2 velocity;
    private final Vector2 position;
    private final Vector2 tmp;
    private final Texture texture;
    private final DisposableAction disposableAction;
    private Positionable target;
    private final float velocityMod = MathUtils.random(40, 120);

    public EnemyPlanet(float size, CollidableAction collidableAction, Vector2 velocity, Vector2 position, Texture texture, DisposableAction disposableAction, PlayerPlanet target) {
        this.size = size;
        this.collidableAction = collidableAction;
        this.velocity = velocity;
        this.position = position;
        this.texture = texture;
        this.target = target;
        tmp = new Vector2();
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
        // update velocity
        Vector2 direction = target.getPosition().cpy()
                .sub(getPosition())
                .nor()
                .scl(velocityMod);
        velocity.set(direction);
        // apply velocity
        position.add(tmp.set(velocity).scl(dt));
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

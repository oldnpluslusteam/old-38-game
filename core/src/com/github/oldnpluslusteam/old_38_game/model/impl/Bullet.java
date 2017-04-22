package com.github.oldnpluslusteam.old_38_game.model.impl;

import com.badlogic.gdx.math.Vector2;
import com.github.oldnpluslusteam.old_38_game.model.Collidable;
import com.github.oldnpluslusteam.old_38_game.model.Updatable;
import com.github.oldnpluslusteam.old_38_game.model.Velocity;

public class Bullet implements Collidable, Velocity, Updatable {
	private static final Vector2 tmp = new Vector2();
	private Vector2 position;
	private Vector2 velocity;
	private float size;

	public Bullet(Vector2 position, Vector2 velocity, float size) {
		this.position = position;
		this.velocity = velocity;
		this.size = size;
	}

	@Override
	public boolean isCollide(Collidable collidable) {
		float distance = collidable.getPosition().dst(position);
		return distance < getSize() + collidable.getSize();
	}

	@Override
	public Vector2 getPosition() {
		return position;
	}

	@Override
	public Vector2 getVelocity() {
		return velocity;
	}

	@Override
	public void update(float dt) {
		position.add(tmp.set(velocity).scl(dt));
	}

	@Override
	public float getSize() {
		return size;
	}
}

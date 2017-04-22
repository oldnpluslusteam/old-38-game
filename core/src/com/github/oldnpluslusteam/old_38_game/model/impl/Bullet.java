package com.github.oldnpluslusteam.old_38_game.model.impl;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.github.oldnpluslusteam.old_38_game.model.Collidable;
import com.github.oldnpluslusteam.old_38_game.model.CollidableAction;
import com.github.oldnpluslusteam.old_38_game.model.Updatable;
import com.github.oldnpluslusteam.old_38_game.model.Velocity;

public class Bullet implements Collidable, Velocity, Updatable, Disposable {
	private static final Vector2 tmp = new Vector2();
	private Vector2 position;
	private Vector2 velocity;
	private float size;
	private DisposableAction disposableAction;
	private CollidableAction collidableAction;

	public Bullet(Vector2 position, Vector2 velocity, float size) {
		this.position = position;
		this.velocity = velocity;
		this.size = size;
		collidableAction = new CollidableAction() {
			@Override
			public void act() {
				dispose();
			}
		};
	}

	@Override
	public boolean isCollide(Collidable collidable) {
		if (this == collidable) return false;
		float distance = collidable.getPosition().dst(position);
		return distance < getSize() + collidable.getSize();
	}

	@Override
	public CollidableAction getCollidableAction() {
		return collidableAction;
	}

	public void setCollidableAction(CollidableAction collidableAction) {
		this.collidableAction = collidableAction;
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
		updateVelocity();
		position.add(tmp.set(velocity).scl(dt));
	}

	public void updateVelocity() {
	}

	@Override
	public float getSize() {
		return size;
	}

	@Override
	public void dispose() {
		if (disposableAction != null) {
			disposableAction.dispose();
		}
	}

	public DisposableAction getDisposableAction() {
		return disposableAction;
	}

	public void setDisposableAction(DisposableAction disposableAction) {
		this.disposableAction = disposableAction;
	}
}

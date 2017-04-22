package com.github.oldnpluslusteam.old_38_game.model.impl;

import com.badlogic.gdx.math.Vector2;
import com.github.oldnpluslusteam.old_38_game.model.Positionable;

public class SelftargetingBullet extends Bullet {
	private Positionable target;

	public SelftargetingBullet(Vector2 position, Vector2 velocity, float size, Positionable target) {
		super(position, velocity, size);
		this.target = target;
	}

	@Override
	public void updateVelocity() {
		Vector2 direction = target.getPosition().cpy()
				.sub(getPosition())
				.nor()
				.scl(300);
		getVelocity().set(direction);
	}
}

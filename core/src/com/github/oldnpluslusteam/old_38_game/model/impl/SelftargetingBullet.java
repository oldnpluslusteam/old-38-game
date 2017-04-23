package com.github.oldnpluslusteam.old_38_game.model.impl;

import com.badlogic.gdx.math.Vector2;
import com.github.oldnpluslusteam.old_38_game.model.Positionable;

public class SelftargetingBullet extends Bullet {
	private Positionable target;

	private static final Vector2 tmp1 = new Vector2();

	public SelftargetingBullet(Vector2 position, Vector2 velocity, float size, Positionable target) {
		super(position, velocity, size);
		this.target = target;
	}

	@Override
	public void updateVelocity(float dt) {
		if (null == target) return;
		float k = Math.min(1f, dt * 0.25f);
		float vMod = getVelocity().len();
		tmp1.set(target.getPosition());
		tmp1.sub(getPosition());
		// angle current velocity
		float acv = getVelocity().angle();
		// angle target velocity
		float atv = tmp1.angle();
		// delta angle initial
		float dai = Math.abs(acv - atv);
		if (Math.abs((360 + acv) - atv) < dai) {
			acv = 360 + acv;
		} else if (Math.abs(acv - (atv + 360)) < dai) {
			atv = 360 + atv;
		}
		getVelocity().setAngle(k * atv + (1f - k) * acv);

//		tmp1.set(target.getPosition());
//		tmp1.sub(getPosition());
//		tmp1.nor().scl(vMod);
//		getVelocity().lerp(tmp1, dt * 0.25f);
	}
}

package com.github.oldnpluslusteam.old_38_game.model;

public interface Collidable extends Positionable, ISize {
	boolean isCollide(Collidable collidable);

	CollidableAction getCollidableAction();
}

package com.github.oldnpluslusteam.old_38_game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.github.oldnpluslusteam.old_38_game.TheGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.fullscreen = true;
		config.width = 1366;
		config.height = 768;
		new LwjglApplication(new TheGame(), config);
	}
}

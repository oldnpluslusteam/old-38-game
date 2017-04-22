package com.github.oldnpluslusteam.old_38_game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.oldnpluslusteam.old_38_game.model.Collidable;
import com.github.oldnpluslusteam.old_38_game.model.Updatable;
import com.github.oldnpluslusteam.old_38_game.model.impl.Bullet;

import java.util.ArrayList;
import java.util.Collection;

import static com.badlogic.gdx.graphics.GL20.GL_ONE;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;
import static com.badlogic.gdx.graphics.glutils.HdpiUtils.glViewport;

public class TheGame extends ApplicationAdapter {
    static final int VP_WIDTH = 1366, VP_HEIGHT = 768;

    SpriteBatch batch;
    Texture img;

    Viewport screenViewport = new FitViewport(VP_WIDTH, VP_HEIGHT);
    Camera camera;

    FrameBuffer frameBuffer_00;
    FrameBuffer frameBuffer_01;
    FrameBuffer frameBuffer_02;

    ShaderProgram shaderMBlurPrev;
    ShaderProgram shaderPPMain;

    InputListener inputListener;
	Collection<Bullet> bullets;
	Collection<Collidable> collidables;
    Collection<Updatable> updatables;

    @Override
    public void create() {
		bullets = new ArrayList<Bullet>();
		collidables = new ArrayList<Collidable>();
		updatables = new ArrayList<Updatable>();

		Gdx.input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT){
                    Bullet bullet = new Bullet(new Vector2(screenX, Gdx.graphics.getHeight()-screenY),
                            new Vector2(0, 200),
                            10);
                    bullets.add(bullet);
                    collidables.add(bullet);
                    updatables.add(bullet);
                }
                return true;
            }
        });

        batch = new SpriteBatch();
        img = new Texture(Gdx.files.internal("img/bullet-1.png"));
//        img = new Texture(Gdx.files.internal("badlogic.jpg"));

        camera = new OrthographicCamera(VP_WIDTH, VP_HEIGHT);
        camera.position.set(VP_WIDTH/2, VP_HEIGHT/2, 0);

        frameBuffer_00 = new FrameBuffer(Pixmap.Format.RGBA8888,
                (int) screenViewport.getWorldWidth(), (int) screenViewport.getWorldHeight(), false);
        frameBuffer_01 = new FrameBuffer(Pixmap.Format.RGBA8888,
                (int) screenViewport.getWorldWidth(), (int) screenViewport.getWorldHeight(), false);
        frameBuffer_02 = new FrameBuffer(Pixmap.Format.RGBA8888,
                (int) screenViewport.getWorldWidth(), (int) screenViewport.getWorldHeight(), false);

        shaderMBlurPrev = new ShaderProgram(
                Gdx.files.internal("shaders/postfx_vertex_00.glsl"),
                Gdx.files.internal("shaders/postfx_fragment_mblurPrev.glsl")
        );
        shaderPPMain = new ShaderProgram(
                Gdx.files.internal("shaders/postfx_vertex_00.glsl"),
                Gdx.files.internal("shaders/postfx_fragment_mainPP.glsl")
        );
    }

    void setupMainPPUniforms() {
        shaderPPMain.setUniformf("u_grayscalePower",
                (float) Gdx.input.getX(0) / (float) Gdx.graphics.getWidth());
    }

    void drawBG() {
        batch.draw(img, 0, 0);
    }

    void drawCurrentBullets() {
	    for (Bullet bullet : bullets) {
		    batch.draw(img, bullet.getPosition().x, bullet.getPosition().y, bullet.getSize(), bullet.getSize());
	    }
        batch.draw(img, 0, (TimeUtils.millis() % 10000) / 10.f, 64, 64);
    }

    void drawPlanets() {

    }

    void drawAll() {
        camera.update();
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);

        {
            FrameBuffer prevBulletBuf = frameBuffer_00;
            FrameBuffer curBulletBuf = frameBuffer_01;

            curBulletBuf.begin();
            glViewport(0, 0, curBulletBuf.getWidth(), curBulletBuf.getHeight());
            Gdx.gl.glClearColor(0,0,0,0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            batch.setBlendFunction(GL_SRC_ALPHA, GL_ONE);
            float bf = 0.9f;
            batch.setColor(bf, bf, bf, 1);
            batch.setShader(shaderMBlurPrev);
            batch.draw(prevBulletBuf.getColorBufferTexture(), 0, 0);
            batch.setColor(Color.WHITE);
            batch.setShader(null);
            drawCurrentBullets();
            batch.setBlendFunction(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            batch.end();
            curBulletBuf.end();

            frameBuffer_00 = curBulletBuf;
            frameBuffer_01 = prevBulletBuf;
        }

        {
            frameBuffer_02.begin();
            glViewport(0, 0, frameBuffer_02.getWidth(), frameBuffer_02.getHeight());
            Gdx.gl.glClearColor(0,0,.2f,0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            drawBG();
            batch.setBlendFunction(GL_SRC_ALPHA, GL_ONE);
            batch.draw(frameBuffer_00.getColorBufferTexture(), 0, 0);
            batch.setBlendFunction(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            drawPlanets();
            batch.end();
            frameBuffer_02.end();
        }

        {
            screenViewport.apply(false);
            Gdx.gl.glClearColor(0,0,0,0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            batch.setShader(shaderPPMain);
            setupMainPPUniforms();
            batch.draw(frameBuffer_02.getColorBufferTexture(), 0, 0);
            batch.end();
            batch.setShader(null);
        }
    }

    @Override
    public void resize(int width, int height) {
        screenViewport.update(width, height);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(Gdx.graphics.getDeltaTime());
        drawAll();
    }

    private void update(float dt) {
        for (Updatable updatable : updatables) {
            updatable.update(dt);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}

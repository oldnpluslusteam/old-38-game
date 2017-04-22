package com.github.oldnpluslusteam.old_38_game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.oldnpluslusteam.old_38_game.model.Collidable;
import com.github.oldnpluslusteam.old_38_game.model.CollidableAction;
import com.github.oldnpluslusteam.old_38_game.model.Updatable;
import com.github.oldnpluslusteam.old_38_game.model.impl.Bullet;
import com.github.oldnpluslusteam.old_38_game.model.impl.DisposableAction;
import com.github.oldnpluslusteam.old_38_game.model.impl.SelftargetingBullet;

import java.util.*;

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
    List<Bullet> bullets;
    Collection<Collidable> collidables;
    Collection<Updatable> updatables;

    static final int BG_PADDING = 10;
    float[] bgInfo;
    Texture bgStarImg;

    ParticleEffect bloodEffect;
    List<ParticleEffect> bloodEffects = new LinkedList<ParticleEffect>();

    @Override
    public void create() {
        bullets = new ArrayList<Bullet>();
        collidables = new ArrayList<Collidable>();
        updatables = new ArrayList<Updatable>();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.B) {
                    addBlood(new Vector2(Gdx.input.getX(), Gdx.input.getY()), new Vector2(1f, 1f));
                }

                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.LEFT) {
                    final Bullet bullet = new Bullet(new Vector2(screenX, screenY),
                            new Vector2(0, 150),
                            10);
                    bullet.setDisposableAction(new DisposableAction() {
                        @Override
                        public void dispose() {
                            bullets.remove(bullet);
                            collidables.remove(bullet);
                            updatables.remove(bullet);
                        }
                    });
                    bullets.add(bullet);
                    collidables.add(bullet);
                    updatables.add(bullet);
                }
                if (button == Input.Buttons.RIGHT) {
                    final SelftargetingBullet bullet = new SelftargetingBullet(new Vector2(screenX, screenY),
                            new Vector2(0, 150),
                            10,
                            bullets.get(bullets.size() - 1)
                    );
                    bullet.setDisposableAction(new DisposableAction() {
                        @Override
                        public void dispose() {
                            bullets.remove(bullet);
                            collidables.remove(bullet);
                            updatables.remove(bullet);
                        }
                    });
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

        bgStarImg = new Texture(Gdx.files.internal("img/star-1.png"));

        camera = new OrthographicCamera(VP_WIDTH, VP_HEIGHT);
        camera.position.set(VP_WIDTH / 2, VP_HEIGHT / 2, 0);

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
//        ShaderProgram.pedantic = false;

        initBG();

        bloodEffect = new ParticleEffect();
        bloodEffect.load(Gdx.files.internal("particles/blood.p"), Gdx.files.internal("img"));
    }

    void setupMainPPUniforms() {
        shaderPPMain.setUniformf("u_grayscalePower",
                (float) Gdx.input.getX(0) / (float) Gdx.graphics.getWidth());
    }

    void initBG() {
        int bgItems = VP_WIDTH / 2;

        // [X, Y, Z(v,s)]
        bgInfo = new float[bgItems * 3];

        for (int i = 0; i < bgInfo.length; i += 3) {
            bgInfo[i + 0] = MathUtils.random(-BG_PADDING, VP_WIDTH + BG_PADDING);
            bgInfo[i + 1] = MathUtils.random(-BG_PADDING, VP_HEIGHT + BG_PADDING);
            bgInfo[i + 2] = MathUtils.random(1, 10);
        }
    }

    void addBlood(Vector2 pos, Vector2 dir) {
        float a = dir.angle();
        ParticleEffect particleEffect = new ParticleEffect(bloodEffect);
        particleEffect.setPosition(pos.x, pos.y);
        particleEffect.getEmitters().get(0).getAngle().setHigh(a - 45, a + 45);
        particleEffect.start();
        bloodEffects.add(particleEffect);
    }

    void drawBG() {
        float dt = Gdx.graphics.getDeltaTime();

        batch.setColor(Color.toFloatBits(180, 180, 180, 255));

        for (int i = 0; i < bgInfo.length; i += 3) {
            float size_2 = (bgInfo[i + 2] * 1.1f) * .8f;
            batch.draw(bgStarImg,
                    bgInfo[i] - size_2, bgInfo[i + 1] - size_2,
                    size_2 * 2, size_2 * 2);

            float yy = dt * -(100.f + bgInfo[i + 2] * 20f) * .3f + bgInfo[i + 1];

            if (yy < -BG_PADDING) {
                bgInfo[i + 0] = MathUtils.random(-BG_PADDING, VP_WIDTH + BG_PADDING);
                bgInfo[i + 1] = VP_HEIGHT + BG_PADDING;
                bgInfo[i + 2] = MathUtils.random(1, 10);
            } else {
                bgInfo[i + 1] = yy;
            }
        }

        batch.setColor(Color.WHITE);
    }

    void drawCurrentBullets() {
        for (Bullet bullet : bullets) {
            batch.draw(img, bullet.getPosition().x, bullet.getPosition().y, bullet.getSize(), bullet.getSize());
        }

        Iterator<ParticleEffect> pei = bloodEffects.iterator();
        float dt = Gdx.graphics.getDeltaTime();
        while (pei.hasNext()) {
            ParticleEffect effect = pei.next();
            if (effect.isComplete()) {
                pei.remove();
            } else {
                effect.draw(batch, dt);
            }
        }
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
            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            batch.setBlendFunction(GL_ONE, GL_ONE);
            float bf = .98f;
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
            Gdx.gl.glClearColor(0, 0, 0, 0);
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
            Gdx.gl.glClearColor(0, 0, 0, 0);
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
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(Gdx.graphics.getDeltaTime());
        drawAll();
    }

    private void update(float dt) {
        for (Updatable updatable : updatables) {
            updatable.update(dt);
        }
        Collection<CollidableAction> actions = new ArrayList<CollidableAction>();
        for (Collidable collidableFirst : collidables) {
            for (Collidable collidableSecond : collidables) {
                if (collidableFirst.isCollide(collidableSecond)) {
                    actions.add(collidableFirst.getCollidableAction());
                }
            }
        }
        for (CollidableAction action : actions) {
            action.act();
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}

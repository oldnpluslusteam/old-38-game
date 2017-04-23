package com.github.oldnpluslusteam.old_38_game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.oldnpluslusteam.old_38_game.model.Collidable;
import com.github.oldnpluslusteam.old_38_game.model.CollidableAction;
import com.github.oldnpluslusteam.old_38_game.model.Positionable;
import com.github.oldnpluslusteam.old_38_game.model.Updatable;
import com.github.oldnpluslusteam.old_38_game.model.impl.*;

import java.lang.ref.Reference;
import java.util.*;

import static com.badlogic.gdx.graphics.GL20.*;
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

    List<Bullet> bullets;
    Collection<Collidable> collidables;
    Collection<Updatable> updatables;

    PlayerPlanet playerPlanet;
    Texture playerImg;

    List<EnemyPlanet> enemyPlanets;
    List<Texture> enemyTextures;

    static final int BG_PADDING = 10;
    float[] bgInfo;
    Texture bgStarImg;

    ParticleEffect bloodEffect;
    List<ParticleEffect> bloodEffects = new LinkedList<ParticleEffect>();

    float fireTimeout0 = 0;
    float fireTimeout1 = 0;
    float fireTime0 = 0;
    float fireTime1 = 0;

    @Override
    public void create() {
        bullets = new ArrayList<Bullet>();
        collidables = new ArrayList<Collidable>();
        updatables = new ArrayList<Updatable>();
        enemyPlanets = new LinkedList<EnemyPlanet>();
        enemyTextures = new ArrayList<Texture>();

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

                }
                if (button == Input.Buttons.RIGHT) {

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
        screenViewport.setCamera(camera);

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

        playerPlanet = new PlayerPlanet(new Vector2(VP_WIDTH / 2, 64), 128);
        playerImg = new Texture(Gdx.files.internal("img/planet-1.png"), true);

        enemyTextures.add(new Texture(Gdx.files.internal("img/planet-2.png")));
        enemyTextures.add(new Texture(Gdx.files.internal("img/planet-3.png")));
        enemyTextures.add(new Texture(Gdx.files.internal("img/planet-4.png")));

        spawnEnemy();
    }

    void setupMainPPUniforms() {
        shaderPPMain.setUniformf("u_grayscalePower",
                (float) Gdx.input.getX(0) / (float) Gdx.graphics.getWidth());
    }

    void initBG() {
        int bgItems = VP_WIDTH / 2;

        // [X, Y, Z(v,s), Blink]
        bgInfo = new float[bgItems * 4];

        for (int i = 0; i < bgInfo.length; i += 4) {
            bgInfo[i + 0] = MathUtils.random(-BG_PADDING, VP_WIDTH + BG_PADDING);
            bgInfo[i + 1] = MathUtils.random(-BG_PADDING, VP_HEIGHT + BG_PADDING);
            bgInfo[i + 2] = MathUtils.random(1, 10);
            bgInfo[i + 3] = 0;
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

    static final Vector2 tmp1 = new Vector2();

    void spawnEnemy() {
        final float size = MathUtils.random(64, 128);
        float posX = MathUtils.random(BG_PADDING + size / 2, VP_WIDTH - BG_PADDING + size / 2);
        float posY = VP_HEIGHT - size;

        Texture texture = enemyTextures.get(MathUtils.random(enemyTextures.size() - 1));

        final EnemyPlanet[] planetA = new EnemyPlanet[1];

        EnemyPlanet planet = new EnemyPlanet(
                size,
                new CollidableAction() {
                    @Override
                    public void act(Collidable other) {
                        tmp1.set(other.getPosition());
                        tmp1.sub(planetA[0].getPosition());
                        tmp1.setLength(size / 2);

                        addBlood(
                                planetA[0].getPosition(),
                                tmp1.cpy()
                        );
                    }
                },
                new Vector2(),
                new Vector2(posX, posY),
                texture);

        planetA[0] = planet;

        updatables.add(planet);
        collidables.add(planet);
        enemyPlanets.add(planet);
    }

    void drawBG() {
        float dt = Gdx.graphics.getDeltaTime();
        float blinkChance = 500 * dt / (float) bgInfo.length;
        float blinkFade = (float) Math.pow(0.1f, dt);

        batch.setColor(Color.toFloatBits(180, 180, 180, 255));

        for (int i = 0; i < bgInfo.length; i += 4) {
            float size_2 = ((bgInfo[i + 2] + bgInfo[i + 3]) * 1.1f) * .8f;
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

            bgInfo[i + 3] = Math.max(0, bgInfo[i + 3] * blinkFade);

            if (MathUtils.randomBoolean(blinkChance)) {
                bgInfo[i + 3] = MathUtils.random(2, 10);
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
        for (EnemyPlanet planet : enemyPlanets) {
            float size = planet.getSize();
            Vector2 pos = planet.getPosition();

            batch.draw(planet.getTexture(),
                    pos.x - size / 2, pos.y - size / 2, size, size);
        }

        batch.draw(playerImg,
                playerPlanet.getPosition().x - playerPlanet.getSize() / 2,
                playerPlanet.getPosition().y - playerPlanet.getSize() / 2,
                playerPlanet.getSize(), playerPlanet.getSize());
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
            batch.setBlendFunction(GL_ONE, GL_ONE_MINUS_SRC_COLOR);
            batch.draw(frameBuffer_00.getColorBufferTexture(), 0, VP_HEIGHT, VP_WIDTH, -VP_HEIGHT);
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

    void updateFire(float dt) {
        fireTimeout0 -= dt;
        fireTimeout1 -= dt;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            fireTime0 += dt;
            if (fireTimeout0 <= 0) {
                fireTimeout0 = 0.1f;

                Vector2 v = screenViewport.unproject(
                        new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                v.sub(playerPlanet.getPosition()).nor();
                float a = v.angle();
                a += 10f * Math.sin(fireTime0 * Math.PI * 2);
                v.setAngle(a).scl(200);
                final Bullet bullet = new Bullet(playerPlanet.getPosition().cpy(), v, 10);
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
        }

        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            float ftd = dt;
            float fct = 0.04f;

            if (fireTimeout1 < fct * -10) {
                fireTimeout1 = fct;
            }

            while (fireTimeout1 <= 0) {
                fireTimeout1 += fct;
                fireTime1 += fct;
                ftd -= fct;

                Vector2 v = screenViewport.unproject(
                        new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                v.sub(playerPlanet.getPosition()).nor();
                float a = v.angle();
                a += 90f * Math.sin(fireTime1 * Math.PI * 2);
                v.setAngle(a).scl(150);
                Positionable target = null;

                if (enemyPlanets.size() > 0) {
                    target = enemyPlanets.get(MathUtils.random(enemyPlanets.size() - 1));
                }

                final SelftargetingBullet bullet = new SelftargetingBullet(
                        playerPlanet.getPosition().cpy(), v, 5, target);
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

            fireTime1 += ftd;
        }
    }

    Collection<Runnable> actions = new ArrayList<Runnable>();

    private void update(float dt) {
        for (final Updatable updatable : updatables) {
            if (!updatable.update(dt)) {
                actions.add(new Runnable() {
                    @Override
                    public void run() {
                        if (updatable instanceof Disposable) {
                            ((Disposable) updatable).dispose();
                        }
                    }
                });
            }
        }
        for (Runnable action : actions) {
            action.run();
        }
        actions.clear();
        for (final Collidable collidableFirst : collidables) {
            for (final Collidable collidableSecond : collidables) {
                if (collidableFirst.isCollide(collidableSecond)) {
                    actions.add(new Runnable() {
                        @Override
                        public void run() {
                            collidableFirst.getCollidableAction().act(collidableSecond);
                        }
                    });
                }
            }
        }
        for (Runnable action : actions) {
            action.run();
        }
        actions.clear();
        updateFire(dt);
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}

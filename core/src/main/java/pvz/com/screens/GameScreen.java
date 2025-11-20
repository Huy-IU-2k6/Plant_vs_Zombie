package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import pvz.com.managers.FontManager;
import pvz.com.managers.BackgroundManager;

// ECS imports
import pvz.com.entities.Entity;
import pvz.com.entities.plants.Plant;
import pvz.com.entities.components.PlantDamageType;
import pvz.com.entities.projectiles.PeaProjectile;
import pvz.com.factories.PlantFactory;
import pvz.com.systems.IGameSpawner;
import pvz.com.systems.RenderSystem;
import pvz.com.systems.SunProductionSystem;
import pvz.com.systems.PlantAttackSystem;

import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen, IGameSpawner {

    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 600f;

    private final Game game;
    private final SpriteBatch batch;
    private final Stage hudStage;

    private final OrthographicCamera camera;
    private final Viewport viewport;

    // Background + Countdown (HEAD branch)
    private final BackgroundManager backgroundManager;
    private CountdownActor countdown;

    private enum State {
        COUNTDOWN,
        PLAYING
    }
    private State state = State.COUNTDOWN;
    private float countdownTime = 6f;

    // ECS
    private Texture bgTex;
    private List<Entity> entities;
    private List<Plant> plants;
    private RenderSystem renderSystem;
    private SunProductionSystem sunSystem;
    private PlantAttackSystem attackSystem;

    public GameScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
        this.hudStage = new Stage(new ScreenViewport());

        // Camera + Viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        // Background Manager
        backgroundManager = new BackgroundManager();

        // Countdown Actor
        countdown = new CountdownActor(countdownTime, FontManager.getPvzFont());
        countdown.setPosition(400f, 500f);
        hudStage.addActor(countdown);

        // ECS init
        entities = new ArrayList<>();
        plants = new ArrayList<>();

        try {
            bgTex = new Texture(Gdx.files.internal("assets/images/backgrounds/Lawn.jpeg"));
            bgTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Lỗi load BG", e);
        }

        renderSystem = new RenderSystem(batch);
        sunSystem = new SunProductionSystem(this);
        attackSystem = new PlantAttackSystem(this);

        // Test plants
        spawnPlant(PlantFactory.createSunflower(100, 200));
        spawnPlant(PlantFactory.createPeashooter(200, 200));
        spawnPlant(PlantFactory.createWallnut(300, 300));
    }

    private void spawnPlant(Plant plant) {
        entities.add(plant);
        plants.add(plant);
    }

    @Override
    public void show() {
        // Click để tạo cây
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));

                if (button == Input.Buttons.LEFT)
                    spawnPlant(PlantFactory.createPeashooter(world.x, world.y));
                else if (button == Input.Buttons.RIGHT)
                    spawnPlant(PlantFactory.createSunflower(world.x, world.y));

                return true;
            }
        });
    }

    @Override
    public void render(float delta) {

        updateState(delta);

        // Clear screen
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        if (state == State.COUNTDOWN) {
            backgroundManager.renderCount(batch, w, h);
        } else {
            backgroundManager.renderMain(batch, w, h);
        }
        batch.end();

        // Only update ECS when playing
        if (state == State.PLAYING) {
            sunSystem.update(plants, delta);
            attackSystem.update(plants, delta);

            // Render entities
            batch.setProjectionMatrix(camera.combined);
            renderSystem.update(entities);
        }

        // HUD
        hudStage.act(delta);
        hudStage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            // TODO: mở Resume Screen
        }
    }

    private void updateState(float delta) {
        if (state == State.COUNTDOWN) {
            countdownTime -= delta;
            if (countdownTime <= 0f) {
                state = State.PLAYING;
                if (countdown != null) {
                    countdown.remove();
                    countdown = null;
                }
            }
        }
    }

    @Override
    public void spawnSun(float x, float y, int amount) {
        Gdx.app.log("GameEvent", "Sun Spawn: " + x + "," + y);
    }

    @Override
    public void spawnProjectile(float x, float y, int damage, PlantDamageType type, Class<?> projectileClass) {
        if (projectileClass == PeaProjectile.class) {
            Entity pea = new PeaProjectile(x, y, damage);
            entities.add(pea);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudStage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudStage.dispose();
        backgroundManager.dispose();
        if (bgTex != null) bgTex.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}

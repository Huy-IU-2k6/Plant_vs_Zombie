package pvz.com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;

import pvz.com.Zombies.NormalZombie;
import pvz.com.managers.FontManager;
import pvz.com.managers.BackgroundManager;
import pvz.com.ui.CountdownActor;
import pvz.com.ui.ItemType;
import pvz.com.ui.PlantCard;
import pvz.com.ui.SeedBank;

public class GameScreen implements Screen {

    // ===== World & layout =====
    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 600f;

    // ===== Game config =====
    private static final float COUNTDOWN_DURATION = 6f;
    private static final int INITIAL_SUN = 150;

    // ===== Zombie wave config (tạm) =====
    private static final int ZOMBIE_LANE_COUNT = 5;
    private static final float ZOMBIE_START_OFFSET_X = 50f;
    private static final float ZOMBIE_FIRST_LANE_Y = 100f;
    private static final float ZOMBIE_LANE_GAP_Y = 80f;

    // ===== SeedBank layout =====
    private static final float SEED_BANK_HEIGHT = 110f; // tăng cao hơn chút
    private static final float SEED_BANK_MARGIN_TOP = 10f;
    private static final float SEED_BANK_MARGIN_LEFT = 5f; // gần như sát cạnh trái

    private enum State {
        COUNTDOWN,
        PLAYING
    }

    // ===== Core refs =====
    private final Game game;
    private final SpriteBatch batch;
    private final Stage hudStage;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final BackgroundManager backgroundManager;

    // ===== UI & state =====
    private CountdownActor countdown;
    private final Array<PlantCard> plantCards = new Array<>();
    private final SeedBank seedBank;
    private final BitmapFont hudFont;
    private State state = State.COUNTDOWN;

    // ===== Sun HUD =====
    private int sunPoints = INITIAL_SUN;

    // ===== Entities =====
    private final Array<NormalZombie> zombies = new Array<>();

    public GameScreen(Game game) {
        this.game = game;

        batch = new SpriteBatch();

        // HUD stage (UI)
        hudStage = new Stage(new ScreenViewport());
        hudStage.getRoot().setUserObject(this); // cho PlantCard callback ngược

        // Camera + viewport world 800x600
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f);
        camera.update();

        // Background
        backgroundManager = new BackgroundManager();

        // SeedBank
        seedBank = new SeedBank();
        layoutSeedBank();
        seedBank.setVisible(false); // chỉ hiện sau countdown
        hudStage.addActor(seedBank);

        // Countdown
        countdown = new CountdownActor(COUNTDOWN_DURATION, FontManager.getPvzFont());
        countdown.setPosition(400f, 500f);
        hudStage.addActor(countdown);

        // Plant cards
        createPlantCards();

        // Sun HUD
        hudFont = FontManager.getPvzFont();
    }

    // ================== UI setup ==================

    private void layoutSeedBank() {
        float hudW = hudStage.getViewport().getWorldWidth();
        float hudH = hudStage.getViewport().getWorldHeight();

        float originalW = seedBank.getWidth();
        float originalH = seedBank.getHeight();

        float scale = SEED_BANK_HEIGHT / originalH;
        float trayW = originalW * scale;
        float trayH = originalH * scale;

        seedBank.setSize(trayW, trayH);

        // dính mép trái, chỉ chừa margin rất nhỏ
        seedBank.setPosition(
                SEED_BANK_MARGIN_LEFT,
                hudH - trayH - SEED_BANK_MARGIN_TOP);
    }

    private void createPlantCards() {
        for (ItemType type : ItemType.values()) {
            PlantCard card = new PlantCard(type);
            plantCards.add(card);
            seedBank.addCard(card);
        }
    }

    // PlantCard gọi hàm này khi được click
    public void onPlantCardClicked(PlantCard card) {
        if (!card.canUse(sunPoints))
            return;
        if (!spendSun(card.type.cost))
            return;

        card.triggerUse();
        // TODO: chuyển sang mode đặt plant lên grid
    }

    // ================== Sun helpers ==================

    public void addSun(int amount) {
        sunPoints += amount;
    }

    public boolean spendSun(int cost) {
        if (sunPoints < cost)
            return false;
        sunPoints -= cost;
        return true;
    }

    private void drawSunHud(SpriteBatch batch) {
        float sbX = seedBank.getX();
        float sbY = seedBank.getY();

        // Toạ độ ước lượng, tuỳ texture mà chỉnh tiếp
        float textX = sbX + 55f;
        float textY = sbY + 42f;

        hudFont.draw(batch, String.valueOf(sunPoints), textX, textY);
    }

    // ================== Game logic ==================

    private void spawnInitialZombies() {
        float startX = WORLD_WIDTH + ZOMBIE_START_OFFSET_X;

        for (int i = 0; i < ZOMBIE_LANE_COUNT; i++) {
            NormalZombie z = new NormalZombie();
            float laneY = ZOMBIE_FIRST_LANE_Y + i * ZOMBIE_LANE_GAP_Y;
            z.setPosition(startX, laneY);
            zombies.add(z);
        }
    }

    private void unlockPlantCards() {
        for (PlantCard card : plantCards) {
            card.setLockedByGame(false);
        }
    }

    private void updateState(float delta) {
        if (state != State.COUNTDOWN || countdown == null)
            return;

        if (countdown.isFinished()) {
            state = State.PLAYING;

            countdown.remove();
            countdown = null;

            unlockPlantCards();
            seedBank.setVisible(true);
            spawnInitialZombies();
        }
    }

    private void updateGame(float delta) {
        if (state != State.PLAYING)
            return;

        for (NormalZombie z : zombies) {
            z.act(delta);
        }
        // TODO: remove zombie, check va chạm...
    }

    // ================== Render helpers ==================

    private void renderWorld(SpriteBatch batch) {
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        if (state == State.COUNTDOWN) {
            backgroundManager.renderCount(batch, w, h);
            return;
        }

        backgroundManager.renderMain(batch, w, h);

        for (NormalZombie z : zombies) {
            z.draw(batch, 1f);
        }

        // TODO: vẽ plant, bullet...

        // Vẽ HUD mặt trời chỉ khi khay đã hiện
        if (state == State.PLAYING) {
            drawSunHud(batch);
        }
    }

    private void handleEscape() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new ResumeScreen(game, this));
        }
    }

    // ================== Screen lifecycle ==================

    @Override
    public void show() {
        Gdx.input.setInputProcessor(hudStage);
    }

    @Override
    public void render(float delta) {
        updateState(delta);
        updateGame(delta);
        handleEscape();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        renderWorld(batch);
        batch.end();

        hudStage.act(delta);
        hudStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudStage.getViewport().update(width, height, true);
        layoutSeedBank();
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudStage.dispose();
        backgroundManager.dispose();
        seedBank.dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}

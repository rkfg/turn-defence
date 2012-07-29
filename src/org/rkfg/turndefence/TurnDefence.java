package org.rkfg.turndefence;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;

public class TurnDefence implements ApplicationListener {
    public static float BFWIDTH = 2048.0f, BFHEIGHT = 720.0f, GAMESTEP = 0.02f, // game
                                                                                // logic
                                                                                // step
                                                                                // in
                                                                                // seconds
            STEP = 32.0f, // path points step
            SCANLINEPERIOD = 4000.0f, SCANLINESLOWNESS = 4.0f;
    public static int PLWIDTH = 14, PLHEIGHT = 8, PPS = (int) (1 / GAMESTEP),
            TURNSWITCH = PPS; // turn switch transition in steps

    private InputMultiplexer mInputMultiplexer;
    static Random myRandom;
    static float screenWidth, screenHeight;
    static Stage Stage, UI;
    private static Group StaticGroup, PlatformsGroup;
    static Group BuildingsGroup;
    static Group UnitsGroup[];
    private static Label mScoreLabel;
    private static Label mUpkeepLabel;
    private Label mPlayerLabel;
    private TextButton mDoTurn, mBackInTime, mForwardInTime;
    static Skin MainSkin;
    static Building Selected;
    private Platform player1Platform, player2Platform;
    static BuildingMenu BuildMenu;
    static boolean GameOver;

    static int Score[], PlayerUpkeep;
    static int MeteorTime[];
    static int Turn, TurnProcess, PlayTime, PresentPlayTime, TurnSwitchProcess,
            MeteorLife, SpawnDelay;
    static float Runtime;
    private float mDeltaIteration, mDeltaIterationRemainder;

    static AssetManager myAssetManager;
    private Pixmap mHealthPixmap;
    static Texture HealthTexture;
    static Array<BuildingParams> BuildingParamsList;
    private List<String> mTextures = Arrays.asList("cannon_1.png",
            "cannon_2.png", "edge.png", "explosion.png", "floor2.png",
            "ship_1.png", "meteor_1.png", "meteor_2.png", "meteor_3.png",
            "stars_1.jpg", "button.png", "beacon_1.png", "menuitembg.png",
            "smoke.png", "range_active.png", "range_passive.png",
            "building_ph.png", "path.png", "recycle.png");
    static BitmapFont PriceFont;

    public static void changeScore(int delta) {
        Score[Turn] += delta;
        mScoreLabel.setText("Score: " + Score[Turn]);
        if (Score[Turn] < 0) {
            GameOver = true;
            Label gameOverLabel = new Label("Game over. Player " + (2 - Turn)
                    + " won.", MainSkin.getStyle("gameover", LabelStyle.class));
            gameOverLabel.x = (screenWidth - gameOverLabel.width) / 2;
            gameOverLabel.y = screenHeight / 2;
            UI.addActor(gameOverLabel);
        }
    }

    public static void changeUpkeep(int delta) {
        PlayerUpkeep += delta;
        mUpkeepLabel.setText("Upkeep: " + PlayerUpkeep);
    }

    public static void init() {
        MeteorTime[0] = MeteorTime[1] = 0;
        BuildingsGroup.clear();
        UnitsGroup[0].clear();
        UnitsGroup[1].clear();
        Score[0] = Score[1] = 5000;
        TurnProcess = 0;
        TurnSwitchProcess = 0;
        Turn = 0;
        PlayTime = 0;
        MeteorLife = 50;
        SpawnDelay = 1 * PPS;
        Selected = null;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        Score = new int[2];
        MeteorTime = new int[2];
        GameOver = false;
        myRandom = new Random();
        myAssetManager = new AssetManager();
        mHealthPixmap = new Pixmap(64, 4, Format.RGB565);
        mHealthPixmap.setColor(Color.WHITE);
        mHealthPixmap.fill();
        HealthTexture = new Texture(mHealthPixmap);

        for (String asset : mTextures)
            myAssetManager.load("gfx/" + asset, Texture.class);

        myAssetManager.load("skins/main.skin", Skin.class,
                new SkinLoader.SkinParameter("gfx/button.png"));
        myAssetManager.finishLoading();
        UI = new Stage(0, 0, true);
        Stage = new Stage(0, 0, true);
        mInputMultiplexer = new InputMultiplexer(UI, Stage);
        Gdx.input.setInputProcessor(mInputMultiplexer);
        StaticGroup = new Group("static");
        PlatformsGroup = new Group("platforms");
        UnitsGroup = new Group[2];
        UnitsGroup[0] = new Group("units1");
        UnitsGroup[1] = new Group("units2");
        BuildingsGroup = new Group("cannons");
        init();
        Stage.addActor(StaticGroup);
        Stage.addActor(PlatformsGroup);
        Stage.addActor(BuildingsGroup);
        Stage.addActor(UnitsGroup[0]);
        Stage.addActor(UnitsGroup[1]);
        StaticGroup.addActor(new Background());
        MainSkin = myAssetManager.get("skins/main.skin", Skin.class);
        PriceFont = MainSkin.getFont("dejavu");
        BuildingParamsList = new Array<BuildingParams>(true, 10);
        BuildingParamsList
                .add(new BuildingParams(HeavyCannon.class, myAssetManager.get(
                        "gfx/cannon_1.png", Texture.class), 1000, 50));
        BuildingParamsList
                .add(new BuildingParams(MachineGun.class, myAssetManager.get(
                        "gfx/cannon_2.png", Texture.class), 700, 70));
        BuildingParamsList.add(new BuildingParams(Beacon.class, myAssetManager
                .get("gfx/beacon_1.png", Texture.class), 1500, 100));

        mScoreLabel = new Label("Score: " + Score[0], MainSkin);
        mScoreLabel.x = 10;
        mScoreLabel.y = screenHeight - mScoreLabel.getPrefHeight();
        UI.addActor(mScoreLabel);
        mUpkeepLabel = new Label("Upkeep: " + PlayerUpkeep, MainSkin);
        mUpkeepLabel.x = (screenWidth - mUpkeepLabel.getPrefWidth()) / 2;
        mUpkeepLabel.y = screenHeight - mScoreLabel.getPrefHeight();
        UI.addActor(mUpkeepLabel);
        TurnProcess = 3 * PPS;
        mPlayerLabel = new Label("Player " + (Turn + 1) + " turn", MainSkin);
        mPlayerLabel.x = screenWidth - mPlayerLabel.getTextBounds().width - 10;
        mPlayerLabel.y = screenHeight - mPlayerLabel.getPrefHeight();
        UI.addActor(mPlayerLabel);
        mDoTurn = new TextButton("End turn", MainSkin);
        mDoTurn.x = (screenWidth - mDoTurn.width) / 2;
        mDoTurn.y = 10;
        mDoTurn.setClickListener(new ClickListener() {

            @Override
            public void click(Actor actor, float x, float y) {
                if (TurnProcess == 0 && !GameOver) {
                    if (PlayTime < PresentPlayTime)
                        TimeMachine.timeTravel(PresentPlayTime);
                    BuildingMenu.hideAll();
                    if (Selected != null && !Selected.isActive())
                        Selected.sell(1.0f);

                    PlayerUpkeep = 0;
                    Selected = null;
                    TurnProcess = 0;
                    TurnSwitchProcess = TURNSWITCH;
                    Turn = 1 - Turn;
                    TimeMachine.storeEvent(PlayTime, new GameEvent(
                            EventType.TURN, Turn));
                    mPlayerLabel.setText("Player " + (Turn + 1) + " turn");
                    changeScore(0);
                }
            }
        });
        UI.addActor(mDoTurn);
        mBackInTime = new TextButton("<<", MainSkin);
        mBackInTime.x = 10;
        mBackInTime.y = 10;
        mBackInTime.setClickListener(new ClickListener() {

            @Override
            public void click(Actor actor, float x, float y) {
                if (TurnProcess == 0 && !GameOver && PlayTime > 6 * PPS)
                    TimeMachine.timeTravel(PlayTime - 6 * PPS);
            }
        });
        UI.addActor(mBackInTime);
        mForwardInTime = new TextButton(">>", MainSkin);
        mForwardInTime.x = screenWidth - mBackInTime.width - 10;
        mForwardInTime.y = 10;
        mForwardInTime.setClickListener(new ClickListener() {

            @Override
            public void click(Actor actor, float x, float y) {
                if (TurnProcess == 0 && !GameOver
                        && PlayTime <= PresentPlayTime - 6 * PPS)
                    TimeMachine.timeTravel(PlayTime + 6 * PPS);
            }
        });
        UI.addActor(mForwardInTime);
        BuildMenu = new BuildingMenu(UI, BuildingParamsList);
        player1Platform = new Platform(0.0f, 64.0f, PLWIDTH, PLHEIGHT, 0);
        player2Platform = new Platform(BFWIDTH - 64 * PLWIDTH, 64.0f, PLWIDTH,
                PLHEIGHT, 1);
        PlatformsGroup.addActor(player1Platform);
        PlatformsGroup.addActor(player2Platform);

        MoveMap.initMoveMap("maps/map_1.txt");
    }

    @Override
    public void dispose() {
        Stage.dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        mDeltaIteration = Gdx.graphics.getDeltaTime()
                + mDeltaIterationRemainder;
        while (mDeltaIteration > GAMESTEP) {
            Stage.act(GAMESTEP);
            UI.act(GAMESTEP);
            Runtime += GAMESTEP;
            mDeltaIteration -= GAMESTEP;
            if (TurnSwitchProcess == 0 && TurnProcess > 0) {
                TurnProcess -= 1;
                PlayTime += 1;
                if (PlayTime > PresentPlayTime)
                    PresentPlayTime = PlayTime; // sync the present time
                Gdx.app.debug(
                        "Render",
                        String.format(
                                "Runtime: %f, TurnProcess: %d, PlayTime: %d, PresentPlayTime: %d",
                                Runtime, TurnProcess, PlayTime, PresentPlayTime));
            }
        }
        mDeltaIterationRemainder = mDeltaIteration; // put the remains to the
                                                    // next iteration
        Stage.draw();
        UI.draw();
    }

    @Override
    public void resize(int width, int height) {
        Stage.setViewport(width, height, true);
        UI.setViewport(width, height, true);
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void resume() {
        HealthTexture.dispose();
        HealthTexture = new Texture(mHealthPixmap);
    }

}

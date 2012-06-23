package org.rkfg.turndefence;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
	private static float BFWIDTH = 2048.0f, BFHEIGHT = 720.0f,
			TURNSWITCH = 1.0f;
	private static int PLWIDTH = 14, PLHEIGHT = 8;
	private static float STEP = 64.0f, SCANLINEPERIOD = 4000.0f,
			SCANLINESLOWNESS = 4.0f;

	private Texture mExplosionTexture, mCellBg;
	private float screenWidth, screenHeight;
	private Platform player1Platform, player2Platform;
	private int mTurn;
	private float mTime = 0.0f, mRuntime = 0.0f;
	private float mSpawnDelay = 1.0f;
	private Stage mStage, mUI;
	private BuildMenu mBuildMenu;
	private InputMultiplexer mInputMultiplexer;
	private Group mStaticGroup, mPlatformsGroup, mBuildingsGroup,
			mUnitsGroup[];
	private Label mScoreLabel, mUpkeepLabel, mPlayerLabel;
	private TextButton mDoTurn;
	private Skin mMainSkin;
	private Building mSelected;
	private int mScore[], mPlayerUpkeep;
	private float mTurnProcess, mTurnSwitchProcess;
	private boolean mGameOver;
	private Pixmap mHealthPixmap;
	private Texture mHealthTexture;
	private AssetManager mAssetManager;
	private Random mRandom;
	private BitmapFont mPriceFont;
	private float mDeltaIteration;
	private Array<BuildingParams> mBuildingParamsList;
	private MoveMap mMap;
	private List<String> mTextures = Arrays.asList("cannon2.png", "edge.png",
			"explosion.png", "floor2.png", "road.png", "ship_1.png",
			"meteor_1.png", "stars_1.jpg", "button.png", "beacon_1.png",
			"menuitembg.png", "smoke.png", "range_active.png",
			"range_passive.png", "building_ph.png", "path.png");

	@Override
	public void create() {
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		mGameOver = false;
		mRandom = new Random();
		mAssetManager = new AssetManager();
		mHealthPixmap = new Pixmap(64, 4, Format.RGB565);
		mHealthPixmap.setColor(Color.WHITE);
		mHealthPixmap.fill();
		mHealthTexture = new Texture(mHealthPixmap);

		for (String asset : mTextures)
			mAssetManager.load("gfx/" + asset, Texture.class);

		mAssetManager.load("skins/main.skin", Skin.class,
				new SkinLoader.SkinParameter("gfx/button.png"));
		mAssetManager.finishLoading();
		mUI = new Stage(0, 0, true);
		mStage = new Stage(0, 0, true);
		mInputMultiplexer = new InputMultiplexer(mUI, mStage);
		Gdx.input.setInputProcessor(mInputMultiplexer);
		mStaticGroup = new Group("static");
		mPlatformsGroup = new Group("platforms");
		mUnitsGroup = new Group[2];
		mUnitsGroup[0] = new Group("units1");
		mUnitsGroup[1] = new Group("units2");
		mBuildingsGroup = new Group("cannons");
		mStage.addActor(mStaticGroup);
		mStage.addActor(mPlatformsGroup);
		mStage.addActor(mBuildingsGroup);
		mStage.addActor(mUnitsGroup[0]);
		mStage.addActor(mUnitsGroup[1]);
		mCellBg = mAssetManager.get("gfx/menuitembg.png", Texture.class);
		mStaticGroup.addActor(new Background());
		mMainSkin = mAssetManager.get("skins/main.skin", Skin.class);
		mPriceFont = mMainSkin.getFont("dejavu");
		mBuildingParamsList = new Array<TurnDefence.BuildingParams>(true, 10);
		mBuildingParamsList.add(new BuildingParams(BasicCannon.class,
				mAssetManager.get("gfx/cannon2.png", Texture.class), 1000, 50));
		mBuildingParamsList.add(new BuildingParams(Beacon.class, mAssetManager
				.get("gfx/beacon_1.png", Texture.class), 1500, 100));

		mScore = new int[2];
		mScore[0] = mScore[1] = 5000;
		mScoreLabel = new Label("Score: " + mScore[0], mMainSkin);
		mScoreLabel.x = 10;
		mScoreLabel.y = screenHeight - mScoreLabel.getPrefHeight();
		mUI.addActor(mScoreLabel);
		mUpkeepLabel = new Label("Upkeep: " + mPlayerUpkeep, mMainSkin);
		mUpkeepLabel.x = (screenWidth - mUpkeepLabel.getPrefWidth()) / 2;
		mUpkeepLabel.y = screenHeight - mScoreLabel.getPrefHeight();
		mUI.addActor(mUpkeepLabel);
		mTurn = 0;
		mTurnProcess = 3.0f;
		mPlayerLabel = new Label("Player " + (mTurn + 1) + " turn", mMainSkin);
		mPlayerLabel.x = screenWidth - mPlayerLabel.getTextBounds().width - 10;
		mPlayerLabel.y = screenHeight - mPlayerLabel.getPrefHeight();
		mUI.addActor(mPlayerLabel);
		mDoTurn = new TextButton("End turn", mMainSkin);
		mDoTurn.x = (screenWidth - mDoTurn.width) / 2;
		mDoTurn.y = 10;
		mDoTurn.setClickListener(new ClickListener() {

			@Override
			public void click(Actor actor, float x, float y) {
				if (mTurnProcess == 0.0f && !mGameOver) {
					mBuildMenu.hide();
					if (mSelected != null && !mSelected.isActive())
						mSelected.sell(1.0f);

					mPlayerUpkeep = 0;
					mSelected = null;
					mTurnProcess = 3.0f;
					mTurnSwitchProcess = TURNSWITCH;
					mTurn = 1 - mTurn;
					mPlayerLabel.setText("Player " + (mTurn + 1) + " turn");
					changeScore(0);
				}
			}
		});
		mUI.addActor(mDoTurn);
		mBuildMenu = new BuildMenu(mUI);
		player1Platform = new Platform(0.0f, 64.0f, PLWIDTH, PLHEIGHT, 0);
		player2Platform = new Platform(BFWIDTH - 64 * PLWIDTH, 64.0f, PLWIDTH,
				PLHEIGHT, 1);
		mPlatformsGroup.addActor(player1Platform);
		mPlatformsGroup.addActor(player2Platform);

		mExplosionTexture = mAssetManager.get("gfx/explosion.png",
				Texture.class);
		mMap = new MoveMap("maps/map_1.txt");
	}

	@Override
	public void dispose() {
		mStage.dispose();
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		mDeltaIteration = Gdx.graphics.getDeltaTime();
		while (mDeltaIteration > 0.1f) {
			mStage.act(0.1f);
			mUI.act(0.1f);
			mRuntime += 0.1f;
			mDeltaIteration -= 0.1f;
		}
		mStage.act(mDeltaIteration);
		mUI.act(mDeltaIteration);
		mStage.draw();
		mUI.draw();
		mRuntime += mDeltaIteration;
	}

	@Override
	public void resize(int width, int height) {
		mStage.setViewport(width, height, true);
		mUI.setViewport(width, height, true);
		screenWidth = width;
		screenHeight = height;
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
		mHealthTexture.dispose();
		mHealthTexture = new Texture(mHealthPixmap);
	}

	public void changeScore(int delta) {
		mScore[mTurn] += delta;
		mScoreLabel.setText("Score: " + mScore[mTurn]);
		if (mScore[mTurn] < 0) {
			mGameOver = true;
			Label gameOverLabel = new Label("Game over. Player " + (2 - mTurn)
					+ " won.", mMainSkin.getStyle("gameover", LabelStyle.class));
			gameOverLabel.x = (screenWidth - gameOverLabel.width) / 2;
			gameOverLabel.y = screenHeight / 2;
			mUI.addActor(gameOverLabel);
		}
	}

	public void changeUpkeep(int delta) {
		mPlayerUpkeep += delta;
		mUpkeepLabel.setText("Upkeep: " + mPlayerUpkeep);
	}

	public class BuildingParams {
		public int mPrice, mUpkeep;
		public Texture mBuildingTexture;
		public Class<? extends Building> mClass;

		public BuildingParams(Class<? extends Building> class_,
				Texture texture, int price, int upkeep) {
			this.mClass = class_;
			this.mBuildingTexture = texture;
			this.mPrice = price;
			this.mUpkeep = upkeep;
		}
	}

	private class Building extends Actor {
		protected Texture mBuildingTexture, mSelectedTexturePassive,
				mSelectedTextureActive, mPHTexture;
		protected int playerNumber;
		private BuildingParams buildingParams;
		protected boolean mUpkeep, mActive = false;

		public Building(int playerNumber) {
			buildingParams = getParamsByClass(this.getClass());
			mBuildingTexture = buildingParams.mBuildingTexture;
			this.playerNumber = playerNumber;
			this.mSelectedTextureActive = mAssetManager.get(
					"gfx/range_active.png", Texture.class);
			this.mSelectedTexturePassive = mAssetManager.get(
					"gfx/range_passive.png", Texture.class);
			this.mPHTexture = mAssetManager.get("gfx/building_ph.png",
					Texture.class);
			changeScore(-buildingParams.mPrice);
			changeUpkeep(buildingParams.mUpkeep);
		}

		protected BuildingParams getParamsByClass(
				Class<? extends Building> class_) {
			for (BuildingParams buildingParams : mBuildingParamsList) {
				if (buildingParams.mClass == class_) {
					return buildingParams;
				}
			}
			return new BuildingParams(Building.class, mPHTexture, 0, 0);
			// throw new Exception("Invalid class supplied.");
		}

		public void init(float x, float y) {
			this.x = x;
			this.y = y;
			this.width = this.height = 64;
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.draw(mPHTexture, x, y);
			mPriceFont.draw(batch, this.getClass().getName(), x, y + 20);
		}

		@Override
		public void act(float delta) {
			super.act(delta);
			if (mTurn != playerNumber) {
				mUpkeep = false;
				return;
			}
			if (!mUpkeep && mTurnProcess > 0.0f) {
				mUpkeep = true;
				for (BuildingParams buildingParams : mBuildingParamsList) {
					if (buildingParams.mClass == this.getClass()) {
						changeScore(-buildingParams.mUpkeep);
						changeUpkeep(buildingParams.mUpkeep);
					}
				}
			}
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		@Override
		public boolean touchDown(float x, float y, int pointer) {
			if (mGameOver)
				return false;

			if (!mActive)
				mActive = true;
			else {
				if (mSelected == this)
					mSelected = null;
				else
					mSelected = this;
				mBuildMenu.hide();
			}
			return true;
		}

		public boolean isActive() {
			return mActive;
		}

		public void sell(float coeff) {
			remove();
			changeScore((int) (getParamsByClass(this.getClass()).mPrice * coeff));
			changeUpkeep(-getParamsByClass(this.getClass()).mUpkeep);
		}
	}

	private class BasicCannon extends Building {
		private float reload;
		private Actor damagedActor;
		private double distance;
		private float prevDist;
		public static final float range = 200.0f;
		private float mRangeScale;

		public BasicCannon(float x, float y, int playerNumber) {
			super(playerNumber);
			init(x, y);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			if (mSelected == this) {
				batch.setColor(0.5f, 0.7f, 1.0f, 0.7f);
				mRangeScale = range / mSelectedTexturePassive.getWidth() * 2;
				batch.draw(mSelectedTexturePassive, x, y, width / 2.0f,
						height / 2.0f, width, height, mRangeScale, mRangeScale,
						0, 0, 0, mSelectedTexturePassive.getWidth(),
						mSelectedTexturePassive.getHeight(), false, false);
				batch.setColor(0.5f, 0.7f, 1.0f,
						1.0f - (float) (mRuntime - Math.floor(mRuntime)) * 0.9f);
				batch.draw(
						mSelectedTextureActive,
						x,
						y,
						width / 2.0f,
						height / 2.0f,
						width,
						height,
						mRangeScale * (float) (mRuntime - Math.floor(mRuntime)),
						mRangeScale * (float) (mRuntime - Math.floor(mRuntime)),
						0, 0, 0, mSelectedTexturePassive.getWidth(),
						mSelectedTexturePassive.getHeight(), false, false);
				batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			}
			if (!mActive)
				batch.setColor(1.0f, 1.0f, 1.0f, 0.5f);
			batch.draw(mBuildingTexture, x, y);
			batch.setColor(0.0f, 0.0f, 1.0f, 0.5f);
			batch.draw(mHealthTexture, x, y + 67.0f, 64.0f * (1.0f - reload),
					4.0f);
			batch.setColor(Color.WHITE);
			// mMainSkin.getFont("dejavu").draw(batch, String.valueOf(x) + ", "
			// + String.valueOf(y), x + 32.0f, y + 32.0f);
		}

		@Override
		public void act(float delta) {
			if (!mActive)
				return;

			super.act(delta);
			if (mGameOver || mTurnProcess == 0.0f || playerNumber != mTurn)
				return;

			reload -= delta;
			if (reload < 0.0f)
				reload = 0.0f;

			if (reload == 0.0f) {
				prevDist = 0.0f;
				damagedActor = null;
				for (Actor actor : mUnitsGroup[1 - mTurn].getActors()) {
					if (((Unit) actor).isEnemyFor(playerNumber)) {
						distance = Math.sqrt((x + width / 2 - actor.x)
								* (x + width / 2 - actor.x)
								+ (y + height / 2 - actor.y)
								* (y + height / 2 - actor.y));
						if (distance < range
								&& ((Unit) actor).getTotalDistance() > prevDist) {
							prevDist = ((Unit) actor).getTotalDistance();
							damagedActor = actor;
							reload = 1.0f;
						}
					}
				}
				if (damagedActor != null)
					((Unit) damagedActor).doDamage(30);
			}
		}
	}

	private class Beacon extends Building {

		private float mGenTime;

		public Beacon(float x, float y, int playerNumber) {
			super(playerNumber);
			init(x, y);
			this.mGenTime = 0.0f;
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			if (!mActive)
				batch.setColor(1.0f, 1.0f, 1.0f, 0.5f);
			batch.draw(mBuildingTexture, x, y + 5, width, height);
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		@Override
		public void act(float delta) {
			// Acts at enemy's turn
			if (!mActive)
				return;

			super.act(delta);
			if (playerNumber == mTurn || mTurnProcess == 0.0f)
				return;

			mGenTime += delta;
			if (mGenTime > 3.0f) {
				mGenTime -= 3.0f;
				Ship newShip = new Ship(1 - mTurn);
				mUnitsGroup[1 - mTurn].addActor(newShip);
			}
		}
	}

	private class Platform extends Actor {
		private Texture mEdgeTexture, mFloorTexture;
		private int playerNumber;
		private Building mBuilding = null;
		private boolean mBuildingProcess;
		private Vector2 buildVector = new Vector2();
		private Vector3 menuVector = new Vector3();
		private float mHighlightPhase;
		private BuildingParams mBuildingParams;
		private Constructor<?> ctor;

		public Platform(float x, float y, int width, int height,
				int playerNumber) {
			this.x = x;
			this.y = y + 64;
			this.width = width * 64.0f;
			this.height = height * 64.0f;
			this.playerNumber = playerNumber;
			mEdgeTexture = mAssetManager.get("gfx/edge.png", Texture.class);
			mEdgeTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

			mFloorTexture = mAssetManager.get("gfx/floor2.png", Texture.class);
			mFloorTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.draw(mEdgeTexture, x, y - 64, width, 64.0f, 0, 1, width / 64,
					0);
			batch.draw(mFloorTexture, x, y, width, height, 0, height / 64.0f,
					width / 64.0f, 0);

			if (mBuildingProcess) {
				mHighlightPhase = (float) Math.sin(mRuntime * 2);
				batch.setColor(0.3f, 0.3f + (mHighlightPhase + 1) * 0.35f,
						0.3f, (mHighlightPhase + 1) / 3);
				batch.draw(mCellBg, buildVector.x, buildVector.y);
				batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			}
			mMap.draw(batch, playerNumber);
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		@Override
		public boolean touchDown(float x, float y, int pointer) {
			if (mGameOver || mTurn != playerNumber)
				return true;

			if (mSelected != null) {
				if (!mSelected.isActive()) {
					mSelected.sell(1.0f);
					mSelected = null;
					return true;
				}

				mSelected = null;
			}

			if (mTurnProcess != 0.0f)
				return false;

			if (!mBuildingProcess) {
				buildVector.set((float) Math.floor(x / 64) * 64,
						(float) Math.floor(y / 64) * 64);
				buildVector.add(this.x, this.y);
				menuVector.set(buildVector.x, buildVector.y, 0);
				getStage().getCamera().project(menuVector);
				mBuildMenu.show(menuVector.x, menuVector.y - 70, this);
				mBuildingProcess = true;
			} else
				mBuildMenu.hide();
			return true;
		}

		public void stopBuilding() {
			mBuildingProcess = false;
		}

		public void build(int type) {
			mBuildingParams = mBuildingParamsList.get(type);
			if (mScore[mTurn] >= mBuildingParams.mPrice) {
				try {
					ctor = mBuildingParams.mClass.getConstructor(
							TurnDefence.class, float.class, float.class,
							int.class);
					mBuilding = (Building) ctor.newInstance(TurnDefence.this,
							buildVector.x, buildVector.y, playerNumber);
					mBuildingsGroup.addActor(mBuilding);
					mSelected = mBuilding;
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			mBuildingProcess = false;
		}
	}

	private class BuildMenu extends Actor {
		private float menuX;
		private Stage mStage;
		private Platform callback;

		public BuildMenu(Stage stage) {
			this.mStage = stage;
			this.width = 70.0f * mBuildingParamsList.size;
			this.height = 64.0f;
		}

		public void show(float x, float y, Platform callback) {
			this.callback = callback;
			this.x = x - this.width / 2 + 32;
			if (this.x < 0.0f)
				this.x = 0.0f;
			if (this.x + this.width > Gdx.graphics.getWidth())
				this.x = Gdx.graphics.getWidth() - this.width;
			if (y > 0.0f)
				this.y = y;
			else
				this.y = 0;
			this.mStage.addActor(this);
		}

		public void hide() {
			if (this.callback != null)
				this.callback.stopBuilding();
			this.mStage.removeActor(this);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			this.menuX = x;
			for (BuildingParams buildParams : mBuildingParamsList) {
				if (buildParams.mPrice > mScore[mTurn])
					batch.setColor(1.0f, 0.3f, 0.3f, 0.7f);
				else
					batch.setColor(0.3f, 1.0f, 0.3f, 0.7f);
				batch.draw(mCellBg, menuX, y);
				batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
				batch.draw(buildParams.mBuildingTexture, menuX, y);
				mPriceFont.setColor(mMainSkin.getColor("yellow"));
				mPriceFont.draw(batch, String.valueOf(buildParams.mPrice),
						menuX + 5, y + 64);
				mPriceFont.setColor(mMainSkin.getColor("upkeep_color"));
				mPriceFont.draw(batch, String.valueOf(buildParams.mUpkeep),
						menuX + 5, y + 20);
				menuX += 70.0f;
			}
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		@Override
		public boolean touchDown(float x, float y, int pointer) {
			if (pointer > 0)
				return false;

			callback.build((int) (x - 10) / 70);
			hide();
			callback = null;
			return true;
		}
	}

	private class Unit extends Actor {
		private float speed;
		protected Color mColor;
		protected int life, originLife;
		protected Texture mUnitTexture;
		protected int playerNumber;
		private float originY, totalDistance, moveDelta;
		private int mReward;
		private Vector2 nextXY, deltaXY, dist;

		public Unit(Texture texture, int playerNumber) {
			mUnitTexture = texture;
			width = texture.getWidth();
			height = texture.getHeight();
			this.playerNumber = playerNumber;
			this.mColor = new Color(mRandom.nextFloat(), mRandom.nextFloat(),
					mRandom.nextFloat(), 1.0f);
		}

		protected void init(float x, float y, float speed, int life, int reward) {
			this.x = x;
			this.y = y;
			this.originY = y;
			this.totalDistance = 0.0f;
			this.speed = speed;
			this.life = life;
			this.originLife = life;
			this.mReward = reward;
			this.deltaXY = new Vector2();
			this.nextXY = new Vector2();
			this.dist = new Vector2();
			nextPath();
		}

		protected void nextPath() {
			nextXY = mMap.getNextXY(this.x, this.y, playerNumber == 0);
			this.deltaXY.set((nextXY.x - x), (nextXY.y - y));
			this.dist.set(deltaXY);
			this.deltaXY.nor();
		}

		protected void preDraw(SpriteBatch batch) {
			mColor.a = mSelected != null ? 0.1f : 1.0f;
			batch.setColor(mColor);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			if (life > originLife / 2) {
				batch.setColor((originLife - life) / originLife * 2, 1.0f,
						0.0f, 1.0f);
			} else {
				batch.setColor(1.0f, life / originLife * 2, 0.0f, 1.0f);
			}
			batch.draw(mHealthTexture, x - width / 2, y + height / 2 + 6, width
					* life / originLife, 4.0f);
			batch.setColor(Color.WHITE);
		}

		@Override
		public void act(float delta) {
			// Acts at enemy's turn
			super.act(delta);
			y = (float) (originY + Math.sin(mRuntime + originY) * 3.0f);
			if (playerNumber == mTurn)
				return;

			if (mGameOver || mTurnProcess == 0.0f)
				return;

			while (true) {
				if (delta * speed < dist.len()) {
					moveDelta = speed * delta;
					x += deltaXY.x * moveDelta;
					originY += deltaXY.y * moveDelta;
					dist.x -= deltaXY.x * moveDelta;
					dist.y -= deltaXY.y * moveDelta;
					totalDistance += moveDelta;
					break;
				}
				x += deltaXY.x * dist.len();
				originY += deltaXY.y * dist.len();
				x = Math.round(x);
				originY = Math.round(originY);
				y = originY;
				totalDistance += dist.len();
				nextPath();
			}
			y = originY;
			if (x < -128.0f && playerNumber == 1 || x > BFWIDTH + 128
					&& playerNumber == 0) {
				remove();
				changeScore(-200);
			}
		}

		public void doDamage(int damage) {
			life -= damage;
			// mInvulnerable += 0.1f;
			getStage()
					.addActor(new Explosion(x - width / 2, y - height / 2, 1));
			if (life <= 0) {
				changeScore(mReward);
				remove();
			}
		}

		public float getTotalDistance() {
			return totalDistance;
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		public boolean isEnemyFor(int player) {
			return this.playerNumber != player;
		}
	}

	private class Meteor extends Unit {

		public Meteor(int playerNumber, int life) {
			super(mAssetManager.get("gfx/meteor_1.png", Texture.class),
					playerNumber);
			init(BFWIDTH / 2, mMap.getYbyX(BFWIDTH / 2),
					mRandom.nextFloat() * 30 + 100, life, 150);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			preDraw(batch);
			batch.draw(mUnitTexture, x - width / 2, y - height / 2, width / 2,
					height / 2, width, height, 1.0f, 1.0f, mRuntime * 180, 0,
					0, (int) width, (int) height, false, false);
			// mPriceFont.setColor(mMainSkin.getColor("yellow"));
			// mPriceFont.draw(batch, String.valueOf(life), x, y);
			super.draw(batch, parentAlpha);
		}

	}

	private class Ship extends Unit {

		private float mInitX;

		public Ship(int playerNumber) {
			super(mAssetManager.get("gfx/ship_1.png", Texture.class),
					playerNumber);
			switch (playerNumber) {
			case 0:
				mInitX = -128;
				break;
			case 1:
				mInitX = BFWIDTH + 128;
				break;
			}
			init(mInitX, mMap.getYbyX(mInitX),
					(mRandom.nextFloat() * 50 + 100), 200, 250);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			preDraw(batch);
			if (playerNumber == 0)
				batch.draw(mUnitTexture, x - width / 2, y - height / 2, width,
						height, 0, 0, 128, 64, true, false);
			else
				batch.draw(mUnitTexture, x - width / 2, y - height / 2);
			super.draw(batch, parentAlpha);
		}
	}

	private class Explosion extends Actor {

		private float lifetime, starttime;

		public Explosion(float x, float y, float lifetime) {
			this.lifetime = lifetime;
			this.starttime = lifetime;
			this.x = x;
			this.y = y;
			this.width = this.height = 64;
			this.touchable = false;
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.setColor(1.0f, 1.0f, 1.0f, lifetime / starttime);
			float size = ((starttime - lifetime) / starttime * 2 + 1);
			batch.draw(mExplosionTexture, x, y, 32.0f, 32.0f, width, height,
					size, size, 0.0f, 0, 0, 64, 64, false, false);
			batch.setColor(Color.WHITE);
		}

		@Override
		public void act(float delta) {
			super.act(delta);
			lifetime -= delta;
			if (lifetime < 0)
				remove();
		}

		@Override
		public Actor hit(float x, float y) {
			return null;
		}
	}

	private class Background extends Actor {
		private Texture mBackgroundTexture, mSmokeTexture;
		private float deltaX, deltaY, dragX, dragY, camTargetX, camTargetY,
				camDeltaX, camDeltaY, interpolatedDelta;
		private Camera camera;
		private Vector3 dragVector = new Vector3();
		private int i, mMeteorLife = 50;
		private Vector3[] mSmokeParticles;

		public Background() {
			super();
			mBackgroundTexture = mAssetManager.get("gfx/stars_1.jpg",
					Texture.class);
			mSmokeTexture = mAssetManager.get("gfx/smoke.png", Texture.class);
			mBackgroundTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			x = 0;
			y = Gdx.graphics.getHeight() - BFHEIGHT;
			width = BFWIDTH;
			height = BFHEIGHT;
			mSmokeParticles = new Vector3[(int) BFHEIGHT / 30];
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.draw(mBackgroundTexture, x, y, width, height);
			for (Vector3 particle : mSmokeParticles) {
				batch.setColor(1.0f, 1.0f, 1.0f, particle.z);
				batch.draw(mSmokeTexture, particle.x, particle.y, 32.0f, 32.0f,
						64.0f, 64.0f, 2.0f - particle.z, 2.0f - particle.z,
						particle.z * 180 + particle.y, 0, 0, 64, 64, false,
						false);
				batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			}
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		@Override
		public void act(float delta) {
			super.act(delta);
			for (i = 0; i < (int) BFHEIGHT / 30; i++) {
				if (mSmokeParticles[i] == null)
					mSmokeParticles[i] = new Vector3();

				if (mSmokeParticles[i].z == 0.0f) {
					mSmokeParticles[i].x = BFWIDTH / 2 + mRandom.nextFloat()
							* 20.0f - 10.0f - 32.0f;
					mSmokeParticles[i].y = BFHEIGHT - mRandom.nextFloat()
							* 20.0f - i * 30 - 40;
					mSmokeParticles[i].z = mRandom.nextFloat();
				}
				mSmokeParticles[i].y += delta * 2;
				mSmokeParticles[i].z -= delta / 3;
				if (mSmokeParticles[i].z < 0.0f)
					mSmokeParticles[i].z = 0.0f;
			}

			if (mTurnSwitchProcess > 0.0f) { // move
				// camera
				if (mTurnSwitchProcess == TURNSWITCH) { // init movement
					if (mTurn == 0)
						camTargetX = screenWidth / 2;
					else
						camTargetX = BFWIDTH - screenWidth / 2;

					camTargetY = screenHeight / 2;
					camera = getStage().getCamera();
					camDeltaX = camTargetX - camera.position.x;
					camDeltaY = camTargetY - camera.position.y;
				}
				// POEHALI
				if (mTurnSwitchProcess > TURNSWITCH / 2.0f)
					interpolatedDelta = delta
							* (TURNSWITCH - mTurnSwitchProcess) / TURNSWITCH
							* 4.0f;
				else
					interpolatedDelta = delta * mTurnSwitchProcess / TURNSWITCH
							* 4.0f;
				camera.translate(camDeltaX * interpolatedDelta, camDeltaY
						* interpolatedDelta, 0);
				mTurnSwitchProcess -= delta;
				if (mTurnSwitchProcess < 0.0f) {
					mTurnSwitchProcess = 0.0f;
					camera.position.x = camTargetX;
					camera.position.y = camTargetY;
				}
			}

			if (mTurnProcess > 0.0f && mTurnSwitchProcess == 0.0f) { // move
																		// units
				mTime += delta;
				while (mTime > mSpawnDelay
						|| mUnitsGroup[1 - mTurn].getActors().size() < 5) {
					mTime -= mSpawnDelay;
					mUnitsGroup[1 - mTurn].addActor(new Meteor(1 - mTurn,
							mMeteorLife));
					if (mMeteorLife < 121)
						mMeteorLife += 1;
					if (mSpawnDelay > 1.0f)
						mSpawnDelay -= 0.01f;
				}
				mTurnProcess -= delta;
				if (mTurnProcess < 0.0f)
					mTurnProcess = 0.0f;
			}

		}

		@Override
		public boolean touchDown(float x, float y, int pointer) {
			if (pointer > 0 || mTurnSwitchProcess > 0.0f)
				return false;

			if (camera == null)
				camera = getStage().getCamera();

			dragVector.set(x, y, 0);
			camera.project(dragVector);
			dragX = dragVector.x;
			dragY = dragVector.y;
			return true;
		}

		@Override
		public void touchDragged(float x, float y, int pointer) {
			if (pointer > 0 || mTurnSwitchProcess > 0.0f)
				return;

			if (camera == null)
				camera = getStage().getCamera();

			dragVector.set(x, y, 0);
			camera.project(dragVector);
			deltaX = (dragX - dragVector.x);
			deltaY = (dragY - dragVector.y);
			if (camera.position.x + deltaX < screenWidth / 2)
				deltaX = screenWidth / 2 - camera.position.x;
			if (camera.position.x + deltaX > BFWIDTH - screenWidth / 2)
				deltaX = BFWIDTH - screenWidth / 2 - camera.position.x;
			if (camera.position.y + deltaY < screenHeight / 2
					+ (screenHeight - BFHEIGHT))
				deltaY = screenHeight / 2 + (screenHeight - BFHEIGHT)
						- camera.position.y;
			if (camera.position.y + deltaY > screenHeight / 2)
				deltaY = screenHeight / 2 - camera.position.y;

			camera.translate(deltaX, deltaY, 0);

			dragX = dragVector.x;
			dragY = dragVector.y;
		}

		@Override
		public void touchUp(float x, float y, int pointer) {
			deltaX = 0.0f;
			deltaY = 0.0f;
		}
	}

	private class MoveMap {
		private String mXYString[];
		private Vector2 mInternalMap[][];
		private int x, y, x2, y2, cnt;
		private boolean mMirror;
		private Array<Vector2> mNextResult;
		private Array<Float> mYbyXResult;
		private Vector2 mSelectedResult;
		private float mElemLen, mPathAlpha;
		private Vector2 mNormalizedElem, mCurElem, mCurElemMeasure;
		private Texture mPathTexture;

		public MoveMap(String mapname) {
			FileHandle mFileHandle = Gdx.files.internal(mapname);
			String mString = mFileHandle.readString();
			String mLines[] = mString.split("\n");
			mInternalMap = new Vector2[mLines.length][];
			cnt = 0;
			for (String line : mLines) {
				mXYString = line.split(" ");
				x = Integer.parseInt(mXYString[0]);
				y = Integer.parseInt(mXYString[1]);
				x2 = Integer.parseInt(mXYString[2]);
				y2 = Integer.parseInt(mXYString[3]);
				mInternalMap[cnt] = new Vector2[] { new Vector2(x, y),
						new Vector2(x2, y2) };
				cnt++;
			}
			mNextResult = new Array<Vector2>(5); // path forks
			mYbyXResult = new Array<Float>(5);
			mNormalizedElem = new Vector2();
			mCurElem = new Vector2();
			mCurElemMeasure = new Vector2();
			mPathTexture = mAssetManager.get("gfx/path.png", Texture.class);
		}

		public Vector2 getNextXY(float curX, float curY, boolean toRight) {
			mMirror = curX >= BFWIDTH / 2 && toRight || curX > BFWIDTH / 2
					&& !toRight; // strong magic
			if (mMirror)
				curX = BFWIDTH - curX;

			mNextResult.clear();
			for (Vector2[] XY : mInternalMap) { // very strong magic
				if (toRight != mMirror && XY[0].x == curX && XY[0].y == curY)
					mNextResult.add(new Vector2(XY[1]));
				if (toRight == mMirror && XY[1].x == curX && XY[1].y == curY)
					mNextResult.add(new Vector2(XY[0]));
			}
			if (mNextResult.size == 0)
				return new Vector2(toRight ? BFWIDTH * 2 : -BFWIDTH * 2,
						-BFHEIGHT);

			mSelectedResult = mNextResult.random();

			if (mMirror)
				mSelectedResult.x = BFWIDTH - mSelectedResult.x;

			return mSelectedResult;
		}

		public float getYbyX(float x) {
			mMirror = x > BFWIDTH / 2;
			if (mMirror)
				x = BFWIDTH - x;
			mYbyXResult.clear();
			for (Vector2[] XY : mInternalMap) { // very strong magic
				if (XY[0].x == x)
					mYbyXResult.add(Float.valueOf(XY[0].y));
				if (XY[1].x == x)
					mYbyXResult.add(Float.valueOf(XY[1].y));
			}
			if (mYbyXResult.size == 0)
				return 360;

			return mYbyXResult.random();
		}

		public void draw(SpriteBatch batch, int playerNumber) {
			for (Vector2[] pathElement : mInternalMap) {
				mCurElem.set(pathElement[1]).sub(pathElement[0]);
				mNormalizedElem.set(mCurElem).nor().mul(STEP);
				mElemLen = mCurElem.len();
				mCurElem.set(pathElement[0]);
				mCurElemMeasure.set(0.0f, 0.0f);
				while (mCurElemMeasure.len() + STEP <= mElemLen) {
					mPathAlpha = Math.abs(mCurElem.x - mRuntime
							* SCANLINEPERIOD / SCANLINESLOWNESS
							% SCANLINEPERIOD / SCANLINEPERIOD * BFWIDTH / 2);
					if (mPathAlpha > 100)
						mPathAlpha = 0.3f;
					else
						mPathAlpha = (100.0f - mPathAlpha) / 145.0f + 0.3f;
					batch.setColor(1.0f, 1.0f, 1.0f, mPathAlpha);
					if (playerNumber == 0)
						batch.draw(mPathTexture,
								mCurElem.x - mPathTexture.getWidth() / 2,
								mCurElem.y - mPathTexture.getHeight() / 2);
					else
						batch.draw(mPathTexture, BFWIDTH - mCurElem.x
								- mPathTexture.getWidth() / 2, mCurElem.y
								- mPathTexture.getHeight() / 2);
					mCurElem.add(mNormalizedElem);
					mCurElemMeasure.add(mNormalizedElem);
				}
			}
			batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}
}

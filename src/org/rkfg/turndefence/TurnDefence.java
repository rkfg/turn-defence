package org.rkfg.turndefence;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
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

public class TurnDefence implements ApplicationListener {
	private static float BFWIDTH = 2048.0f, BFHEIGHT = 2048.0f,
			TURNSWITCH = 1.0f;

	private Texture mExplosionTexture, mCellBg;
	private float screenWidth;
	private float screenHeight;
	private Platform player1Platform, player2Platform;
	private int mTurn;
	private float mTime = 0.0f, mRuntime = 0.0f;
	private float mSpawnDelay = 1.0f;
	private Stage mStage, mUI;
	private BuildMenu mBuildMenu;
	private InputMultiplexer mInputMultiplexer;
	private Group mStaticGroup, mPlatformsGroup, mBuildingsGroup, mMonstersGroup[];
	private Label mScoreLabel, mPlayerLabel;
	private TextButton mDoTurn;
	private Skin mMainSkin;
	private int[] mScore;
	private float mTurnProcess, mTurnSwitchProcess;
	private boolean mGameOver;
	private Pixmap mHealthPixmap;
	private Texture mHealthTexture;
	private AssetManager mAssetManager;
	private Random mRandom;
	private List<String> mTextures = Arrays.asList("cannon2.png", "edge.png",
			"explosion.png", "floor2.png", "road.png", "ship_1.png",
			"meteor_1.png", "stars_1.jpg", "button.png", "beacon_1.png",
			"menuitembg.png", "smoke.png");

	@Override
	public void create() {
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		mGameOver = false;
		mRandom = new Random();
		mAssetManager = new AssetManager();
		for (String asset : mTextures)
			mAssetManager.load("gfx/" + asset, Texture.class);

		mAssetManager.load("skins/main.skin", Skin.class,
				new SkinLoader.SkinParameter("gfx/button.png"));
		mAssetManager.finishLoading();
		mStage = new Stage(0, 0, true);
		mUI = new Stage(0, 0, true);
		mInputMultiplexer = new InputMultiplexer(mUI, mStage);
		Gdx.input.setInputProcessor(mInputMultiplexer);
		mStaticGroup = new Group("static");
		mPlatformsGroup = new Group("platforms");
		mMonstersGroup = new Group[2];
		mMonstersGroup[0] = new Group("monsters1");
		mMonstersGroup[1] = new Group("monsters2");
		mBuildingsGroup = new Group("cannons");
		mStage.addActor(mStaticGroup);
		mStage.addActor(mPlatformsGroup);
		mStage.addActor(mBuildingsGroup);
		mStage.addActor(mMonstersGroup[0]);
		mStage.addActor(mMonstersGroup[1]);
		mCellBg = mAssetManager.get("gfx/menuitembg.png", Texture.class);
		mStaticGroup.addActor(new Background());
		mMainSkin = mAssetManager.get("skins/main.skin", Skin.class);
		mScore = new int[2];
		mScore[0] = mScore[1] = 5000;
		mScoreLabel = new Label("Score: " + mScore[0], mMainSkin);
		mScoreLabel.x = 10;
		mScoreLabel.y = screenHeight - mScoreLabel.getPrefHeight();
		mUI.addActor(mScoreLabel);
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
					mTurnProcess = 3.0f;
					mTurnSwitchProcess = TURNSWITCH;
					mTurn = 1 - mTurn;
					mPlayerLabel.setText("Player " + (mTurn + 1) + " turn");
					mScoreLabel.setText("Score: " + mScore[mTurn]);
				}
			}
		});
		mUI.addActor(mDoTurn);
		mBuildMenu = new BuildMenu(mUI, new Texture[] {
				mAssetManager.get("gfx/cannon2.png", Texture.class),
				mAssetManager.get("gfx/beacon_1.png", Texture.class) },
				new int[] { 1000, 2000 });
		player1Platform = new Platform(0, 100, 3, 8, 0);
		player2Platform = new Platform(BFWIDTH - 64 * 3, 100, 3, 8, 1);
		mPlatformsGroup.addActor(player1Platform);
		mPlatformsGroup.addActor(player2Platform);

		mHealthPixmap = new Pixmap(64, 4, Format.RGB565);
		mHealthPixmap.setColor(Color.WHITE);
		mHealthPixmap.fill();
		mHealthTexture = new Texture(mHealthPixmap);

		mExplosionTexture = mAssetManager.get("gfx/explosion.png",
				Texture.class);
	}

	@Override
	public void dispose() {
		mStage.dispose();
	}

	@Override
	public void render() {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		mStage.act(Gdx.graphics.getDeltaTime());
		mUI.act(Gdx.graphics.getDeltaTime());
		mStage.draw();
		mUI.draw();
		mRuntime += Gdx.graphics.getDeltaTime();
	}

	@Override
	public void resize(int width, int height) {
		mStage.setViewport(width, height, true);
		mUI.setViewport(width, height, true);
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
	}

	private class Cannon extends Actor {
		private Texture mCannonTexture;
		private float reload;
		private Actor damagedActor;
		private double distance;
		private double prevDistance;
		private int playerNumber;
		private boolean active;

		public Cannon(float x, float y, int playerNumber) {
			mCannonTexture = mAssetManager
					.get("gfx/cannon2.png", Texture.class);
			this.x = x;
			this.y = y;
			this.width = this.height = 64;
			this.playerNumber = playerNumber;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			if (!active)
				batch.setColor(1.0f, 1.0f, 1.0f, 0.3f);

			batch.draw(mCannonTexture, x, y);
			batch.setColor(0.0f, 0.0f, 1.0f, 0.5f);
			batch.draw(mHealthTexture, x, y + 67.0f, 64.0f * (1.0f - reload),
					4.0f);
			batch.setColor(Color.WHITE);
			//mMainSkin.getFont("dejavu").draw(batch, String.valueOf(x) + ", " + String.valueOf(y), x + 32.0f, y + 32.0f);
		}

		@Override
		public void act(float delta) {
			super.act(delta);
			if (!active || mGameOver || mTurnProcess == 0.0f || playerNumber != mTurn)
				return;

			reload -= delta;
			if (reload < 0.0f)
				reload = 0.0f;

			if (reload == 0.0f) {
				prevDistance = BFWIDTH;
				damagedActor = null;
				for (Actor actor : mMonstersGroup[1 - mTurn].getActors()) {
					if (((Monster) actor).isEnemyFor(playerNumber)) {
						distance = Math.sqrt((x - actor.x) * (x - actor.x)
								+ (y - actor.y) * (y - actor.y));
						if (distance < 200.0f && actor.x < prevDistance) {
							prevDistance = actor.x;
							damagedActor = actor;
							reload = 1.0f;
						}
					}
				}
				if (damagedActor != null)
					((Monster) damagedActor).doDamage(30);
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

			if (!active && mScore[mTurn] >= 1000) {
				setActive(true);
				changeScore(-1000);
			}

			return true;
		}
	}

	private class Platform extends Actor {
		private Texture mEdgeTexture, mFloorTexture;
		private int playerNumber;
		private Actor mBuilding = null;
		private boolean mBuildingProcess;
		private Vector2 buildVector = new Vector2();
		private Vector3 menuVector = new Vector3();
		private float mHighlightPhase;

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
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		@Override
		public boolean touchDown(float x, float y, int pointer) {
			if (mGameOver || mTurn != playerNumber)
				return true;

			if (!mBuildingProcess) {
				buildVector.set((float) Math.floor(x / 64) * 64,
						(float) Math.floor(y / 64) * 64);
				buildVector.add(this.x, this.y);
				menuVector.set(buildVector.x, buildVector.y, 0);
				getStage().getCamera().project(menuVector);
				mBuildMenu.show(menuVector.x, menuVector.y - 70, this);
				mBuildingProcess = true;
			} else {
				mBuildMenu.hide();
				mBuildingProcess = false;
			}
			return true;
		}

		public void build(int type) {
			switch (type) {
			case 1: // cannon
				if (mScore[mTurn] >= 1000) {
					mBuilding = new Cannon(buildVector.x, buildVector.y,
							playerNumber);
					((Cannon) mBuilding).setActive(true);
					mBuildingsGroup.addActor(mBuilding);
					changeScore(-1000);
				}
				break;
			case 2: // beacon
				if (mScore[mTurn] >= 2000) {
					mBuilding = new Beacon(buildVector.x, buildVector.y,
							playerNumber);
					mBuildingsGroup.addActor(mBuilding);
					changeScore(-2000);
				}
				break;
			}
			mBuildingProcess = false;
		}
	}

	private class Monster extends Actor {
		private float speed;
		protected Color mColor;
		private int life, originLife;
		protected Texture mMonsterTexture;
		protected int playerNumber;
		private float originY;

		public Monster(Texture texture, int playerNumber) {
			mMonsterTexture = texture;
			width = texture.getWidth();
			height = texture.getHeight();
			this.playerNumber = playerNumber;
			this.mColor = new Color(mRandom.nextFloat(), mRandom.nextFloat(),
					mRandom.nextFloat(), 1.0f);
		}

		protected void init(float x, float y, float speed, int life) {
			this.x = x;
			this.y = y;
			this.originY = y;
			this.speed = speed;
			this.life = life;
			this.originLife = life;
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			if (life > originLife / 2) {
				batch.setColor((originLife - life) / originLife * 2, 1.0f,
						0.0f, 1.0f);
			} else {
				batch.setColor(1.0f, life / originLife * 2, 0.0f, 1.0f);
			}
			batch.draw(mHealthTexture, x, y + 70, width * life / originLife,
					4.0f);
			batch.setColor(Color.WHITE);
		}

		@Override
		public void act(float delta) {
			super.act(delta);
			if (playerNumber == mTurn)
				return;

			y = (float) (originY + Math.sin(mRuntime + originY) * 3.0f);
			if (mGameOver || mTurnProcess == 0.0f)
				return;

			x -= speed * delta;
			if (x < -128.0f && playerNumber == 1 || x > BFWIDTH + 128 && playerNumber == 0) {
				remove();
				changeScore(-200);
				if (mScore[mTurn] < 0) {
					mGameOver = true;
					Label gameOverLabel = new Label("Game Over. You've lost.",
							mMainSkin.getStyle("gameover", LabelStyle.class));
					gameOverLabel.x = (screenWidth - gameOverLabel.width) / 2;
					gameOverLabel.y = screenHeight / 2;
					mUI.addActor(gameOverLabel);
				}
			}
		}

		public void doDamage(int damage) {
			life -= damage;
			// mInvulnerable += 0.1f;
			getStage().addActor(new Explosion(x, y, 1));
			if (life <= 0) {
				changeScore(100);
				remove();
			}
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		public boolean isEnemyFor(int player) {
			return this.playerNumber != player;
		}
	}

	private class Meteor extends Monster {

		public Meteor(int playerNumber) {
			super(mAssetManager.get("gfx/meteor_1.png", Texture.class),
					playerNumber);
			init(screenWidth,
					mRandom.nextFloat() * (Gdx.graphics.getHeight() - height),
					(mRandom.nextFloat() * 30 + 50) * (playerNumber - 0.5f) * 2,
					50);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.setColor(mColor);
			batch.draw(mMonsterTexture, x, y, width / 2, height / 2, width,
					height, 1.0f, 1.0f, mRuntime * 180, 0, 0, (int) width,
					(int) height, false, false);
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
		private int i;
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
					mSmokeParticles[i].x = screenWidth + mRandom.nextFloat()
							* 20.0f - 10.0f - 32.0f;
					mSmokeParticles[i].y = screenHeight - mRandom.nextFloat()
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
						|| mMonstersGroup[1 - mTurn].getActors().size() < 5) {
					mTime -= mSpawnDelay;
					mMonstersGroup[1 - mTurn].addActor(new Meteor(1 - mTurn));
					if (mSpawnDelay > 0.4f)
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

	private class Beacon extends Actor {

		private Texture mTexture;
		private int playerNumber;

		public Beacon(float x, float y, int playerNumber) {
			super();
			this.x = x;
			this.y = y;
			this.playerNumber = playerNumber;
			this.mTexture = mAssetManager
					.get("gfx/beacon_1.png", Texture.class);
			this.width = 64;
			this.height = 64;
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.draw(mTexture, x, y + 5, width, height);
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

	}

	private class BuildMenu extends Actor {
		private float menuX;
		private Color mMenuColor;
		private Stage mStage;
		private Texture[] mItems;
		private int[] mPrices;
		private Platform callback;
		private int mPriceCnt;
		private BitmapFont mPriceFont;

		public BuildMenu(Stage stage, Texture[] items, int[] prices) {
			this.mMenuColor = new Color();
			this.mStage = stage;
			mItems = items;
			mPrices = prices;
			this.width = 70.0f * items.length;
			this.height = 64.0f;
			this.mPriceFont = mMainSkin.getFont("dejavu");
			this.mPriceFont.setColor(mMainSkin.getColor("yellow"));
		}

		public void show(float x, float y, Platform callback) {
			this.callback = callback;
			this.x = x - this.width / 2 + 32;
			if (this.x < 0.0f)
				this.x = 0.0f;
			if (y > 0.0f)
				this.y = y;
			else
				this.y = 0;
			this.mStage.addActor(this);
		}

		public void hide() {
			this.mStage.removeActor(this);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			this.menuX = x;
			this.mPriceCnt = 0;
			for (Texture item : mItems) {
				if (mPrices[mPriceCnt] > mScore[mTurn])
					batch.setColor(1.0f, 0.3f, 0.3f, 0.5f);
				else
					batch.setColor(0.3f, 1.0f, 0.3f, 0.5f);
				batch.draw(mCellBg, menuX, y);
				batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
				batch.draw(item, menuX, y);
				mPriceFont.draw(batch, String.valueOf(mPrices[mPriceCnt]),
						menuX + 5, y + 64);
				menuX += 70.0f;
				this.mPriceCnt++;
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

			callback.build((int) (x - 10) / 70 + 1);
			hide();
			callback = null;
			return true;
		}
	}
}

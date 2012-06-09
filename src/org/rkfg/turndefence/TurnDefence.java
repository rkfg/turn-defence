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
import com.badlogic.gdx.utils.Pool;

public class TurnDefence implements ApplicationListener {
	private static float BFWIDTH = 1024.0f;
	private static float BFHEIGHT = 1024.0f;
	private static int AMMOPERTURN = 7;

	private Texture mExplosionTexture;
	private float screenWidth;
	private float screenHeight;
	private Platform player1Platform, player2Platform;
	private MonsterPool mMonsterPool;
	private float mTime = 0.0f, mRuntime = 0.0f;
	private float mSpawnDelay = 1.0f;
	private Stage mStage, mUI;
	private InputMultiplexer mInputMultiplexer;
	private Group mStaticGroup, mPlatformsGroup, mMonstersGroup, mCannonsGroup;
	private Label mScoreLabel, mAmmoLabel;
	private TextButton mDoTurn;
	private Skin mMainSkin;
	private int mScore;
	private float mTurnProcess = 5.0f;
	private int mPersonalAmmo = AMMOPERTURN;
	private boolean mGameOver;
	private Pixmap mHealthPixmap;
	private Texture mHealthTexture;
	private AssetManager mAssetManager;
	private List<String> mTextures = Arrays.asList("cannon.png", "edge.png",
			"explosion.png", "floor.png", "road.png", "reaper.png",
			"stars.jpg", "button.png");

	@Override
	public void create() {
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		mGameOver = false;
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
		mMonstersGroup = new Group("monsters");
		mCannonsGroup = new Group("cannons");
		mStage.addActor(mStaticGroup);
		mStage.addActor(mPlatformsGroup);
		mStage.addActor(mCannonsGroup);
		mStage.addActor(mMonstersGroup);
		mStaticGroup.addActor(new Background());
		mMainSkin = mAssetManager.get("skins/main.skin", Skin.class);
		mScoreLabel = new Label("Score: " + mScore, mMainSkin);
		mScoreLabel.x = (screenWidth - mScoreLabel.getPrefWidth()) / 2;
		mScoreLabel.y = screenHeight - mScoreLabel.getPrefHeight() - 20;
		mUI.addActor(mScoreLabel);
		mAmmoLabel = new Label("Ammo: " + mPersonalAmmo, mMainSkin);
		mAmmoLabel.x = (screenWidth - mScoreLabel.getPrefWidth()) / 2;
		mAmmoLabel.y = screenHeight - mScoreLabel.getPrefHeight() - 50;
		mUI.addActor(mAmmoLabel);
		mDoTurn = new TextButton("End turn", mMainSkin);
		mDoTurn.x = (screenWidth - mDoTurn.width) / 2;
		mDoTurn.y = mDoTurn.height;
		mDoTurn.setClickListener(new ClickListener() {

			@Override
			public void click(Actor actor, float x, float y) {
				if (mTurnProcess == 0.0f && !mGameOver) {
					mTurnProcess = 3.0f;
					mPersonalAmmo = AMMOPERTURN;
					updateAmmo();
				}
			}
		});
		mUI.addActor(mDoTurn);
		player1Platform = new Platform(0, 100, 3, 8, 1);
		player2Platform = new Platform(screenWidth - 64 * 3, 100, 3, 8, 2);
		mPlatformsGroup.addActor(player1Platform);
		mPlatformsGroup.addActor(player2Platform);

		mHealthPixmap = new Pixmap(64, 4, Format.RGB565);
		mHealthPixmap.setColor(Color.WHITE);
		mHealthPixmap.fill();
		mHealthTexture = new Texture(mHealthPixmap);

		mExplosionTexture = mAssetManager.get("gfx/explosion.png",
				Texture.class);
		mMonsterPool = new MonsterPool(mAssetManager.get("gfx/reaper.png",
				Texture.class), 2);
		for (int i = 0; i < 5; i++) {
			mMonstersGroup.addActor(mMonsterPool.obtain());
		}
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
		mScore += delta;
		mScoreLabel.setText("Score: " + mScore);
	}

	public void updateAmmo() {
		mAmmoLabel.setText("Ammo: " + mPersonalAmmo);
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
			mCannonTexture = mAssetManager.get("gfx/cannon.png", Texture.class);
			this.x = x;
			this.y = y;
			this.width = this.height = 64;
			this.playerNumber = playerNumber;
		}

		public void setActive(boolean active) {
			this.active = active;
		}

		public boolean isActive() {
			return active;
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
		}

		@Override
		public void act(float delta) {
			super.act(delta);
			if (!active || mGameOver || mTurnProcess == 0.0f)
				return;

			reload -= delta;
			if (reload < 0.0f)
				reload = 0.0f;

			if (reload == 0.0f) {
				prevDistance = screenWidth;
				damagedActor = null;
				for (Actor actor : mMonstersGroup.getActors()) {
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

			if (!active && mScore >= 1000) {
				setActive(true);
				changeScore(-1000);
			}

			return true;
		}
	}

	private class Platform extends Actor {
		private Texture mEdgeTexture, mFloorTexture;
		private int playerNumber;
		private Cannon mBuildingCannon = null;
		private Vector2 buildVector = new Vector2();

		public Platform(float x, float y, int width, int height,
				int playerNumber) {
			this.x = x;
			this.y = y + 64;
			this.width = width * 64.0f;
			this.height = height * 64.0f;
			this.playerNumber = playerNumber;
			mEdgeTexture = mAssetManager.get("gfx/edge.png", Texture.class);
			mEdgeTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

			mFloorTexture = mAssetManager.get("gfx/floor.png", Texture.class);
			mFloorTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.draw(mEdgeTexture, x, y - 64, width, 64.0f, 0, 0, width / 64,
					1);
			batch.draw(mFloorTexture, x, y, width, height, 0, 0, width / 64.0f,
					height / 64.0f);
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		@Override
		public boolean touchDown(float x, float y, int pointer) {
			if (mGameOver || mScore < 1000)
				return false;

			if (mBuildingCannon == null || mBuildingCannon.isActive()) {
				buildVector.set((float) Math.floor(x / 64) * 64,
						(float) Math.floor(y / 64) * 64);
				buildVector.add(this.x, this.y);
				mBuildingCannon = new Cannon(buildVector.x, buildVector.y,
						playerNumber);
				mBuildingCannon.setActive(false);
				mCannonsGroup.addActor(mBuildingCannon);
			} else {
				mCannonsGroup.removeActor(mBuildingCannon);
				mBuildingCannon = null;
			}
			return true;
		}
	}

	private class Monster extends Actor {
		private float speed;
		private Color mColor;
		private int life;
		private float mInvulnerable;
		private Texture mMonsterTexture;
		private int playerNumber;
		private float originY;

		public Monster(Texture texture) {
			mMonsterTexture = texture;
			width = 128;
			height = 64;
		}

		public void init(float x, float y, float speed, int playerNumber) {
			this.x = x;
			this.y = y;
			this.originY = y;
			this.speed = speed;
			this.life = 100;
			this.playerNumber = playerNumber;
			Random random = new Random();
			this.mColor = new Color(random.nextFloat(), random.nextFloat(),
					random.nextFloat(), 1.0f);
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.setColor(mColor);
			batch.draw(mMonsterTexture, x, y);
			if (life > 50) {
				batch.setColor((100.0f - life) / 50.0f, 1.0f, 0.0f, 1.0f);
			} else {
				batch.setColor(1.0f, life / 50.0f, 0.0f, 1.0f);
			}
			batch.draw(mHealthTexture, x, y + 70, 64.0f * life / 100.0f, 4.0f);
			batch.setColor(Color.WHITE);
		}

		@Override
		public void act(float delta) {
			super.act(delta);
			y = (float) (originY + Math.sin(mRuntime + originY) * 3.0f);
			if (mGameOver || mTurnProcess == 0.0f)
				return;

			x -= speed * delta;
			if (x < -128.0f) {
				mMonsterPool.free(this);
				remove();
				changeScore(-200);
				if (mScore < 0) {
					mGameOver = true;
					Label gameOverLabel = new Label("Game Over. You've lost.",
							mMainSkin.getStyle("gameover", LabelStyle.class));
					gameOverLabel.x = (screenWidth - gameOverLabel.width) / 2;
					gameOverLabel.y = screenHeight / 2;
					mUI.addActor(gameOverLabel);
				}
			}
			if (mInvulnerable > 0)
				mInvulnerable -= delta;
			if (mInvulnerable < 0)
				mInvulnerable = 0;
		}

		public void doDamage(int damage) {
			if (mInvulnerable != 0.0f)
				return;

			life -= damage;
			// mInvulnerable += 0.1f;
			getStage().addActor(new Explosion(x, y, 1));
			if (life <= 0) {
				changeScore(100);
				mMonsterPool.free(this);
				remove();
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
			if (mPersonalAmmo == 0)
				return false;

			mPersonalAmmo -= 1;
			updateAmmo();
			doDamage(34);
			return true;
		}

		public boolean isEnemyFor(int player) {
			return this.playerNumber != player;
		}
	}

	private class MonsterPool extends Pool<Monster> {

		private Texture mMonsterTexture;
		private int playerNumber;
		Random mRandom;

		public MonsterPool(Texture texture, int playerNumber) {
			super();
			mMonsterTexture = texture;
			this.playerNumber = playerNumber;
			mRandom = new Random();
		}

		@Override
		protected Monster newObject() {
			Monster monster = new Monster(mMonsterTexture);
			return monster;
		}

		@Override
		public Monster obtain() {
			Monster monster = super.obtain();
			monster.init(Gdx.graphics.getWidth(),
					mRandom.nextInt(Gdx.graphics.getHeight() - 80),
					mRandom.nextFloat() * 50 + 30, playerNumber);
			return monster;
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
		private Texture mBackgroundTexture;
		private float deltaX, deltaY, dragX, dragY;
		private Camera camera;
		private Vector3 dragVector = new Vector3();

		public Background() {
			super();
			mBackgroundTexture = mAssetManager.get("gfx/stars.jpg",
					Texture.class);
			x = 0;
			y = Gdx.graphics.getHeight() - BFHEIGHT;
			width = BFWIDTH;
			height = BFHEIGHT;
		}

		@Override
		public void draw(SpriteBatch batch, float parentAlpha) {
			batch.draw(mBackgroundTexture, x, y, width, height);
		}

		@Override
		public Actor hit(float x, float y) {
			return x > 0 && x < width && y > 0 && y < height ? this : null;
		}

		@Override
		public void act(float delta) {
			super.act(delta);
			if (mTurnProcess > 0.0f)
				mTurnProcess -= delta;

			if (mTurnProcess < 0.0f)
				mTurnProcess = 0.0f;

			if (mTurnProcess == 0.0f)
				return;

			mTime += delta;
			while (mTime > mSpawnDelay || mMonstersGroup.getActors().size() < 5) {
				mTime -= mSpawnDelay;
				mMonstersGroup.addActor(mMonsterPool.obtain());
				if (mSpawnDelay > 0.4f)
					mSpawnDelay -= 0.01f;
			}

		}

		@Override
		public boolean touchDown(float x, float y, int pointer) {
			if (pointer > 0)
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
			if (pointer > 0)
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
}

package org.rkfg.turndefence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Background extends Actor {
    private Texture mBackgroundTexture, mSmokeTexture;
    private float deltaX, deltaY, dragX, dragY, camTargetX, camTargetY,
            camDeltaX, camDeltaY, interpolatedDelta;
    private Camera camera;
    private Vector3 dragVector = new Vector3();
    private int i;
    private Vector3[] mSmokeParticles;
    private int mMeteorSize;

    public Background() {
        super();
        mBackgroundTexture = TurnDefence.myAssetManager.get("gfx/stars_1.jpg",
                Texture.class);
        mSmokeTexture = TurnDefence.myAssetManager.get("gfx/smoke.png",
                Texture.class);
        mBackgroundTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        x = 0;
        y = Gdx.graphics.getHeight() - TurnDefence.BFHEIGHT;
        width = TurnDefence.BFWIDTH;
        height = TurnDefence.BFHEIGHT;
        mSmokeParticles = new Vector3[(int) TurnDefence.BFHEIGHT / 30];
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.draw(mBackgroundTexture, x, y, width, height);
        for (Vector3 particle : mSmokeParticles) {
            if (particle != null) {
                batch.setColor(1.0f, 1.0f, 1.0f, particle.z);
                batch.draw(mSmokeTexture, particle.x, particle.y, 32.0f, 32.0f,
                        64.0f, 64.0f, 2.0f - particle.z, 2.0f - particle.z,
                        particle.z * 180 + particle.y, 0, 0, 64, 64, false,
                        false);
                batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public Actor hit(float x, float y) {
        return x > 0 && x < width && y > 0 && y < height ? this : null;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        for (i = 0; i < (int) TurnDefence.BFHEIGHT / 30; i++) {
            if (mSmokeParticles[i] == null)
                mSmokeParticles[i] = new Vector3();

            if (mSmokeParticles[i].z == 0.0f) {
                mSmokeParticles[i].x = TurnDefence.BFWIDTH / 2
                        + TurnDefence.myRandom.nextFloat() * 20.0f - 10.0f
                        - 32.0f;
                mSmokeParticles[i].y = TurnDefence.BFHEIGHT
                        - TurnDefence.myRandom.nextFloat() * 20.0f - i * 30
                        - 40;
                mSmokeParticles[i].z = TurnDefence.myRandom.nextFloat();
            }
            mSmokeParticles[i].y += delta * 2;
            mSmokeParticles[i].z -= delta / 3;
            if (mSmokeParticles[i].z < 0.0f)
                mSmokeParticles[i].z = 0.0f;
        }

        if (TurnDefence.TurnSwitchProcess > 0) { // move
            // camera
            if (TurnDefence.TurnSwitchProcess == TurnDefence.TURNSWITCH) { // init
                                                                           // movement
                if (TurnDefence.Turn == 0)
                    camTargetX = TurnDefence.screenWidth / 2;
                else
                    camTargetX = TurnDefence.BFWIDTH - TurnDefence.screenWidth
                            / 2;

                camTargetY = TurnDefence.screenHeight / 2;
                camera = getStage().getCamera();
                camDeltaX = camTargetX - camera.position.x;
                camDeltaY = camTargetY - camera.position.y;
            }
            // POEHALI
            if (TurnDefence.TurnSwitchProcess > TurnDefence.TURNSWITCH / 2)
                interpolatedDelta = delta
                        * (TurnDefence.TURNSWITCH - TurnDefence.TurnSwitchProcess)
                        / TurnDefence.TURNSWITCH * 4.0f;
            else
                interpolatedDelta = delta * TurnDefence.TurnSwitchProcess
                        / TurnDefence.TURNSWITCH * 4.0f;
            camera.translate(camDeltaX * interpolatedDelta, camDeltaY
                    * interpolatedDelta, 0);
            TurnDefence.TurnSwitchProcess -= 1;
            if (TurnDefence.TurnSwitchProcess == 0) {
                camera.position.x = camTargetX;
                camera.position.y = camTargetY;
                TurnDefence.TurnProcess = 3 * TurnDefence.PPS; // start the game
                                                               // events flow
            }
        }

        // move units if the camera doesn't move
        if (TurnDefence.TurnProcess > 0 && TurnDefence.TurnSwitchProcess == 0) {
            TurnDefence.MeteorTime[TurnDefence.Turn] += 1;
            // Gdx.app.debug("MeteorTime",
            // String.valueOf(TurnDefence.MeteorTime[TurnDefence.Turn]));
            if (TurnDefence.MeteorTime[TurnDefence.Turn] >= TurnDefence.SpawnDelay) {
                TurnDefence.MeteorTime[TurnDefence.Turn] -= TurnDefence.SpawnDelay;
                // Gdx.app.debug("SpawningM", String
                // .valueOf(TurnDefence.MeteorTime[TurnDefence.Turn]));
                if (TurnDefence.PlayTime < TurnDefence.PresentPlayTime
                        && TimeMachine.getEvents(TurnDefence.PlayTime) != null) {
                    try {
                        Gdx.app.debug("Trying to respawn", String.format(
                                "PlayTime: %d, Turn: %d", TurnDefence.PlayTime,
                                TurnDefence.Turn));
                        TurnDefence.UnitsGroup[1 - TurnDefence.Turn]
                                .addActor(TimeMachine.respawn(
                                        TurnDefence.PlayTime, Meteor.class,
                                        1 - TurnDefence.Turn));
                    } catch (NotRespawnable e) {
                        Gdx.app.debug(
                                "Time conflict",
                                String.format(
                                        "Error respawning a meteor, PlayTime: %d, Turn: %d, PresentPlayTime: %d",
                                        TurnDefence.PlayTime, TurnDefence.Turn,
                                        TurnDefence.PresentPlayTime));
                    }
                } else {
                    mMeteorSize = TurnDefence.myRandom.nextInt(6);
                    Meteor meteor;
                    switch (mMeteorSize) {
                    case 0:
                        meteor = new Meteor(1 - TurnDefence.Turn, 1,
                                TurnDefence.MeteorLife * 2);
                        break;
                    case 1:
                    case 2:
                        meteor = new Meteor(1 - TurnDefence.Turn, 2,
                                TurnDefence.MeteorLife);
                        break;
                    default:
                        meteor = new Meteor(1 - TurnDefence.Turn, 3,
                                TurnDefence.MeteorLife / 2);
                        break;
                    }
                    Gdx.app.debug("Generated", String.format(
                            "New meteor, size: %d, basic life: %d, turn: %d",
                            mMeteorSize, TurnDefence.MeteorLife,
                            TurnDefence.Turn));
                    TurnDefence.UnitsGroup[1 - TurnDefence.Turn]
                            .addActor(meteor);
                }
                if (TurnDefence.MeteorLife < 121)
                    TurnDefence.MeteorLife += 1;
                // if (mSpawnDelay > 20.0f)
                // mSpawnDelay -= 1f;
            }
        }

    }

    @Override
    public boolean touchDown(float x, float y, int pointer) {
        if (pointer > 0 || TurnDefence.TurnSwitchProcess > 0.0f)
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
        if (pointer > 0 || TurnDefence.TurnSwitchProcess > 0.0f)
            return;

        if (camera == null)
            camera = getStage().getCamera();

        dragVector.set(x, y, 0);
        camera.project(dragVector);
        deltaX = (dragX - dragVector.x);
        deltaY = (dragY - dragVector.y);
        if (camera.position.x + deltaX < TurnDefence.screenWidth / 2)
            deltaX = TurnDefence.screenWidth / 2 - camera.position.x;
        if (camera.position.x + deltaX > TurnDefence.BFWIDTH
                - TurnDefence.screenWidth / 2)
            deltaX = TurnDefence.BFWIDTH - TurnDefence.screenWidth / 2
                    - camera.position.x;
        if (camera.position.y + deltaY < TurnDefence.screenHeight / 2
                + (TurnDefence.screenHeight - TurnDefence.BFHEIGHT))
            deltaY = TurnDefence.screenHeight / 2
                    + (TurnDefence.screenHeight - TurnDefence.BFHEIGHT)
                    - camera.position.y;
        if (camera.position.y + deltaY > TurnDefence.screenHeight / 2)
            deltaY = TurnDefence.screenHeight / 2 - camera.position.y;

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

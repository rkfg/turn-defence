package org.rkfg.turndefence;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;

class Platform extends Actor implements Callback {
    private Texture mEdgeTexture, mFloorTexture, mCellBg;
    private int playerNumber;
    private Building mBuilding = null;
    private boolean mBuildingProcess;
    private Vector2 buildVector = new Vector2();
    private Vector3 menuVector = new Vector3();
    private float mHighlightPhase;
    private Constructor<?> ctor;

    public Platform(float x, float y, int width, int height, int playerNumber) {
        this.x = x;
        this.y = y + 64;
        this.width = width * 64.0f;
        this.height = height * 64.0f;
        this.playerNumber = playerNumber;
        mEdgeTexture = TurnDefence.myAssetManager.get("gfx/edge.png",
                Texture.class);
        mEdgeTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

        mFloorTexture = TurnDefence.myAssetManager.get("gfx/floor2.png",
                Texture.class);
        mFloorTexture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

        mCellBg = TurnDefence.myAssetManager.get("gfx/menuitembg.png",
                Texture.class);
    }

    @Override
    public void cancel() {
        mBuildingProcess = false;
        
    }

    @Override
    public void done(BuildingParams params) {
        if (TurnDefence.Score[TurnDefence.Turn] - TurnDefence.PlayerUpkeep >= params.mPrice) {
            try {
                ctor = params.mClass.getConstructor(float.class,
                        float.class, int.class);
                mBuilding = (Building) ctor.newInstance(buildVector.x,
                        buildVector.y, playerNumber);
                TurnDefence.BuildingsGroup.addActor(mBuilding);
                TurnDefence.Selected = mBuilding;
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

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.draw(mEdgeTexture, x, y - 64, width, 64.0f, 0, 1, width / 64, 0);
        batch.draw(mFloorTexture, x, y, width, height, 0, height / 64.0f,
                width / 64.0f, 0);

        if (mBuildingProcess) {
            mHighlightPhase = (float) Math.sin(TurnDefence.Runtime * 2);
            batch.setColor(0.5f, 1.0f, 0.5f, (mHighlightPhase + 1) / 3 + 0.1f);
            batch.draw(mCellBg, buildVector.x, buildVector.y);
            batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        MoveMap.draw(batch, playerNumber);
    }

    @Override
    public Actor hit(float x, float y) {
        return x > 0 && x < width && y > 0 && y < height ? this : null;
    }

    public void stopBuilding() {
    }

    @Override
    public boolean touchDown(float x, float y, int pointer) {
        if (TurnDefence.GameOver || TurnDefence.Turn != playerNumber)
            return true;

        if (TurnDefence.Selected != null) {
            BuildingMenu.hideAll();
            if (!TurnDefence.Selected.isActive()) {
                TurnDefence.Selected.sell(1.0f);
                TurnDefence.Selected = null;
                return true;
            }

            TurnDefence.Selected = null;
        }

        if (TurnDefence.TurnProcess != 0)
            return false;

        if (!mBuildingProcess) {
            buildVector.set((float) Math.floor(x / 64) * 64,
                    (float) Math.floor(y / 64) * 64);
            buildVector.add(this.x, this.y);
            menuVector.set(buildVector.x, buildVector.y, 0);
            TurnDefence.BuildMenu.show(menuVector.x, menuVector.y - 70, this);
            mBuildingProcess = true;
        } else
            BuildingMenu.hideAll();
        return true;
    }
}

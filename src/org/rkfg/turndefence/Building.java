package org.rkfg.turndefence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

class Building extends Actor implements Cloneable {
    protected Texture mBuildingTexture, mSelectedTexturePassive,
            mSelectedTextureActive, mPHTexture;
    protected int playerNumber;
    private BuildingParams buildingParams;
    protected boolean mUpkeep, mActive = false;

    public Building(int playerNumber) {
        buildingParams = getParamsByClass(this.getClass());
        mBuildingTexture = buildingParams.mBuildingTexture;
        this.playerNumber = playerNumber;
        this.mSelectedTextureActive = TurnDefence.myAssetManager.get(
                "gfx/range_active.png", Texture.class);
        this.mSelectedTexturePassive = TurnDefence.myAssetManager.get(
                "gfx/range_passive.png", Texture.class);
        this.mPHTexture = TurnDefence.myAssetManager.get("gfx/building_ph.png",
                Texture.class);
        TurnDefence.changeScore(-buildingParams.mPrice);
        TurnDefence.changeUpkeep(buildingParams.mUpkeep);
    }

    protected Object clone(boolean store) throws CloneNotSupportedException {
        if (!store) {
            buildingParams = getParamsByClass(this.getClass());
            TurnDefence.changeScore(-buildingParams.mPrice);
            TurnDefence.changeUpkeep(buildingParams.mUpkeep);
            for (Actor actor : TurnDefence.BuildingsGroup.getActors()) {
                if (actor.x == this.x && actor.y == this.y) {
                    Gdx.app.log("Time conflict", "Object @ " + x + ":" + y
                            + " already exists, can't place a clone!");
                    return null;
                }
            }
        }
        return super.clone();
    }

    protected BuildingParams getParamsByClass(
            Class<? extends Building> class_) {
        for (BuildingParams buildingParams : TurnDefence.BuildingParamsList) {
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
        try {
            TimeMachine.storeEvent(TurnDefence.PlayTime, new GameEvent(
                    EventType.BUILD, (Building) this.clone(true)), true);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.draw(mPHTexture, x, y);
        TurnDefence.PriceFont.draw(batch, this.getClass().getName(), x, y + 20);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (TurnDefence.Turn != playerNumber) {
            mUpkeep = false;
            return;
        }
        if (!mUpkeep && TurnDefence.TurnProcess > 0) {
            mUpkeep = true;
            for (BuildingParams buildingParams : TurnDefence.BuildingParamsList) {
                if (buildingParams.mClass == this.getClass()) {
                    TurnDefence.changeScore(-buildingParams.mUpkeep);
                    TurnDefence.changeUpkeep(buildingParams.mUpkeep);
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
        if (TurnDefence.GameOver)
            return false;

        if (!mActive)
            mActive = true;
        else {
            if (TurnDefence.Selected == this)
                TurnDefence.Selected = null;
            else
                TurnDefence.Selected = this;
            TurnDefence.BuildMenu.hide();
        }
        return true;
    }

    public boolean isActive() {
        return mActive;
    }

    public void sell(float coeff) {
        remove();
        TurnDefence.changeScore((int) (getParamsByClass(this.getClass()).mPrice * coeff));
        TurnDefence.changeUpkeep(-getParamsByClass(this.getClass()).mUpkeep);
    }
}

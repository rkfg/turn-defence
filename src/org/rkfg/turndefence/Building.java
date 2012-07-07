package org.rkfg.turndefence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

class Building extends Actor implements Cloneable, Callback {
    protected Texture mBuildingTexture, mSelectedTexturePassive,
            mSelectedTextureActive, mPHTexture;
    protected int playerNumber;
    private BuildingParams mBuildingParams;
    protected boolean mUpkeep, mActive = false;
    protected BuildingMenu mBuildingMenu;
    protected Array<BuildingParams> mBuildingMenuParams;

    public Building(int playerNumber, Array<BuildingParams> buildingParams) {
        mBuildingParams = getParamsByClass(this.getClass());
        mBuildingTexture = mBuildingParams.mBuildingTexture;
        this.playerNumber = playerNumber;
        this.mSelectedTextureActive = TurnDefence.myAssetManager.get(
                "gfx/range_active.png", Texture.class);
        this.mSelectedTexturePassive = TurnDefence.myAssetManager.get(
                "gfx/range_passive.png", Texture.class);
        this.mPHTexture = TurnDefence.myAssetManager.get("gfx/building_ph.png",
                Texture.class);
        this.mBuildingMenuParams = buildingParams;
        this.mBuildingMenuParams.insert(
                0,
                new BuildingParams(Recycle.class, TurnDefence.myAssetManager
                        .get("gfx/recycle.png", Texture.class), 0,
                        mBuildingParams.mPrice));
        Gdx.app.debug("Items",
                String.format("Params: %d", mBuildingMenuParams.size));
        this.mBuildingMenu = new BuildingMenu(TurnDefence.UI,
                mBuildingMenuParams);
        TurnDefence.changeScore(-mBuildingParams.mPrice);
        TurnDefence.changeUpkeep(mBuildingParams.mUpkeep);
    }

    protected Object clone(boolean store) throws CloneNotSupportedException {
        if (!store) {
            mBuildingParams = getParamsByClass(this.getClass());
            TurnDefence.changeScore(-mBuildingParams.mPrice);
            TurnDefence.changeUpkeep(mBuildingParams.mUpkeep);
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

    protected BuildingParams getParamsByClass(Class<? extends Building> class_) {
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

        if (!mActive) {
            mActive = true;
            try {
                TimeMachine.storeEvent(TurnDefence.PlayTime, new GameEvent(
                        EventType.BUILD, (Building) this.clone(true)));
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else {
            if (TurnDefence.Selected == this) {
                TurnDefence.Selected = null;
                BuildingMenu.hideAll();
            } else {
                TurnDefence.Selected = this;
                mBuildingMenu.show(this.x, this.y - 70, this);
            }
            TurnDefence.BuildMenu.hide();
        }
        return true;
    }

    public boolean isActive() {
        return mActive;
    }

    /**
     * Sells the building with the specified coefficient.
     * 
     * @param coeff
     *            price coefficient
     */
    public int sell(float coeff) {
        remove();
        int sellPrice = (int) (getParamsByClass(this.getClass()).mPrice * coeff);
        TurnDefence
                .changeScore(sellPrice);
        TurnDefence.changeUpkeep(-getParamsByClass(this.getClass()).mUpkeep);
        return sellPrice;
    }

    @Override
    public void cancel() {
    }

    /**
     * Confirms selection and does appropriate things.
     */
    @Override
    public void done(BuildingParams param) {
        if (param.mClass == Recycle.class) {
            try {
                GameEvent sellEvent = new GameEvent(EventType.SELL,
                        (Building) this.clone(true));
                sellEvent.number = sell(1.0f); 
                Gdx.app.debug("Sell", String.format("Selling the building @ %f, %f for %d", sellEvent.building.x, sellEvent.building.y, sellEvent.number));
                TimeMachine.storeEvent(TurnDefence.PlayTime, sellEvent);
            } catch (CloneNotSupportedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

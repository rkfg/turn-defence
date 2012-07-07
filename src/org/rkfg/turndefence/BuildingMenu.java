package org.rkfg.turndefence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

class BuildingMenu extends Actor {
    private float menuX;
    private Stage mStage;
    private Callback callback;
    private Array<BuildingParams> mLocalBuildingParamsList;
    private Texture mCellBg;
    private Vector3 menuVector;
    static Array<BuildingMenu> visibleMenus = new Array<BuildingMenu>();

    public BuildingMenu(Stage stage, Array<BuildingParams> buildingParams) {
        this.mStage = stage;
        mLocalBuildingParamsList = buildingParams;
        this.width = 70.0f * mLocalBuildingParamsList.size;
        this.height = 64.0f;
        this.menuVector = new Vector3();
        mCellBg = TurnDefence.myAssetManager.get("gfx/menuitembg.png",
                Texture.class);
    }

    public void show(float x, float y, Callback callback) {
        this.callback = callback;
        menuVector.set(x, y, 0);
        TurnDefence.Stage.getCamera().project(menuVector);
        this.x = menuVector.x - this.width / 2 + 32;
        this.y = menuVector.y;
        if (this.x < 0.0f)
            this.x = 0.0f;
        if (this.x + this.width > Gdx.graphics.getWidth())
            this.x = Gdx.graphics.getWidth() - this.width;
        if (this.y < 0.0f)
            this.y = 0;
        visibleMenus.add(this);
        this.mStage.addActor(this);
    }

    public void hide() {
        if (this.callback != null)
            this.callback.cancel();
        visibleMenus.removeValue(this, true);
        this.mStage.removeActor(this);
    }

    public static void hideAll() {
        for (BuildingMenu menu : visibleMenus){
            menu.hide();
        }
    }
    
    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        this.menuX = x;
        for (BuildingParams buildParams : mLocalBuildingParamsList) {
            if (buildParams.mClass != Recycle.class && buildParams.mPrice > TurnDefence.Score[TurnDefence.Turn]
                    - TurnDefence.PlayerUpkeep)
                batch.setColor(1.0f, 0.3f, 0.3f, 0.7f);
            else
                batch.setColor(0.3f, 1.0f, 0.3f, 0.7f);
            batch.draw(mCellBg, menuX, y);
            batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            batch.draw(buildParams.mBuildingTexture, menuX, y);
            if (buildParams.mPrice > 0) {
                TurnDefence.PriceFont.setColor(TurnDefence.MainSkin
                        .getColor("yellow"));
                TurnDefence.PriceFont.draw(batch,
                        String.valueOf(buildParams.mPrice), menuX + 5, y + 64);
            }
            if (buildParams.mUpkeep > 0) {
                TurnDefence.PriceFont.setColor(TurnDefence.MainSkin
                        .getColor("upkeep_color"));
                TurnDefence.PriceFont.draw(batch,
                        String.valueOf(buildParams.mUpkeep), menuX + 5, y + 20);
            }
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

        callback.done(mLocalBuildingParamsList.get((int) (x - 10) / 70));
        hide();
        callback = null;
        return true;
    }
}

package org.rkfg.turndefence;

import com.badlogic.gdx.graphics.Texture;

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

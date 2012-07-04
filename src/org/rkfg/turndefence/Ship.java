package org.rkfg.turndefence;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

class Ship extends Unit {

    private float mInitX;

    public Ship(int playerNumber) {
        super(TurnDefence.myAssetManager.get("gfx/ship_1.png", Texture.class),
                playerNumber);
        switch (playerNumber) {
        case 0:
            mInitX = -128;
            break;
        case 1:
            mInitX = TurnDefence.BFWIDTH + 128;
            break;
        }
        init(mInitX, MoveMap.getYbyX(mInitX),
                (TurnDefence.myRandom.nextFloat() * 50 + 100), 200, 250);
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

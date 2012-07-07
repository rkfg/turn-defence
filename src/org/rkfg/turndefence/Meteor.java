package org.rkfg.turndefence;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Meteor extends Unit {

    public Meteor(int playerNumber, int meteorType, int life) {
        super(TurnDefence.myAssetManager.get("gfx/meteor_" + meteorType + ".png",
                Texture.class), playerNumber);
        init(TurnDefence.BFWIDTH / 2, MoveMap.getYbyX(TurnDefence.BFWIDTH / 2),
                TurnDefence.myRandom.nextInt(30) + 100, life,
                150 + 50 * (3 - meteorType));
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        preDraw(batch);
        batch.draw(mUnitTexture, x - width / 2, y - height / 2, width / 2,
                height / 2, width, height, 1.0f, 1.0f, TurnDefence.Runtime * 180 * speed
                        / 130 * (playerNumber * 2 - 1), 0, 0, (int) width,
                (int) height, false, false);
        // mPriceFont.setColor(mMainSkin.getColor("yellow"));
        // mPriceFont.draw(batch, String.valueOf(life), x, y);
        super.draw(batch, parentAlpha);
    }

}

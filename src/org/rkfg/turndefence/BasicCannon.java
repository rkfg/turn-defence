package org.rkfg.turndefence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

class BasicCannon extends Building {
    private float reload;
    private Actor damagedActor;
    private double distance;
    private float prevDist;
    public static final float range = 100.0f;
    private float mRangeScale;

    public BasicCannon(float x, float y, int playerNumber) {
        super(playerNumber);
        init(x, y);
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        if (TurnDefence.Selected == this) {
            batch.setColor(0.5f, 0.7f, 1.0f, 0.7f);
            mRangeScale = range / mSelectedTexturePassive.getWidth() * 2;
            batch.draw(mSelectedTexturePassive, x, y, width / 2.0f,
                    height / 2.0f, width, height, mRangeScale, mRangeScale,
                    0, 0, 0, mSelectedTexturePassive.getWidth(),
                    mSelectedTexturePassive.getHeight(), false, false);
            batch.setColor(0.5f, 0.7f, 1.0f,
                    1.0f - (float) (TurnDefence.Runtime - Math.floor(TurnDefence.Runtime)) * 0.9f);
            batch.draw(
                    mSelectedTextureActive,
                    x,
                    y,
                    width / 2.0f,
                    height / 2.0f,
                    width,
                    height,
                    mRangeScale * (float) (TurnDefence.Runtime - Math.floor(TurnDefence.Runtime)),
                    mRangeScale * (float) (TurnDefence.Runtime - Math.floor(TurnDefence.Runtime)),
                    0, 0, 0, mSelectedTexturePassive.getWidth(),
                    mSelectedTexturePassive.getHeight(), false, false);
            batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        if (!mActive)
            batch.setColor(1.0f, 1.0f, 1.0f, 0.5f);
        batch.draw(mBuildingTexture, x, y);
        batch.setColor(0.0f, 0.0f, 1.0f, 0.5f);
        batch.draw(TurnDefence.HealthTexture, x, y + 67.0f, 64.0f * (1.0f - reload),
                4.0f);
        batch.setColor(Color.WHITE);
        // mMainSkin.getFont("dejavu").draw(batch, String.valueOf(x) + ", "
        // + String.valueOf(y), x + 32.0f, y + 32.0f);
    }

    @Override
    public void act(float delta) {
        if (!mActive)
            return;

        super.act(delta);
        if (TurnDefence.GameOver || TurnDefence.TurnProcess == 0 || playerNumber != TurnDefence.Turn)
            return;

        reload -= delta;
        if (reload < 0.0f)
            reload = 0.0f;

        if (reload == 0.0f) {
            prevDist = 0.0f;
            damagedActor = null;
            for (Actor actor : TurnDefence.UnitsGroup[1 - TurnDefence.Turn].getActors()) {
                if (((Unit) actor).isEnemyFor(playerNumber)) {
                    distance = Math.sqrt((x + width / 2 - actor.x)
                            * (x + width / 2 - actor.x)
                            + (y + height / 2 - actor.y)
                            * (y + height / 2 - actor.y));
                    if (distance < range
                            && ((Unit) actor).getTotalDistance() > prevDist) {
                        prevDist = ((Unit) actor).getTotalDistance();
                        damagedActor = actor;
                        reload = 1.0f;
                    }
                }
            }
            if (damagedActor != null)
                ((Unit) damagedActor).doDamage(30);
        }
    }
}

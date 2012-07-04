package org.rkfg.turndefence;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

class Beacon extends Building {

    private int mGenTime;

    public Beacon(float x, float y, int playerNumber) {
        super(playerNumber);
        init(x, y);
        this.mGenTime = 0;
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        if (!mActive)
            batch.setColor(1.0f, 1.0f, 1.0f, 0.5f);
        batch.draw(mBuildingTexture, x, y + 5, width, height);
    }

    @Override
    public Actor hit(float x, float y) {
        return x > 0 && x < width && y > 0 && y < height ? this : null;
    }

    @Override
    public void act(float delta) {
        // Acts at enemy's turn
        if (!mActive)
            return;

        super.act(delta);
        if (playerNumber == TurnDefence.Turn || TurnDefence.TurnProcess == 0)
            return;

        mGenTime += 1;
        if (mGenTime > 3 * TurnDefence.PPS) {
            mGenTime -= 3 * TurnDefence.PPS;
            if (TurnDefence.PlayTime < TurnDefence.PresentPlayTime
                    && TimeMachine.getEvents(TurnDefence.PlayTime) != null) {
                try {
                    TurnDefence.UnitsGroup[1 - TurnDefence.Turn].addActor(TimeMachine.respawn(
                            TurnDefence.PlayTime, Ship.class, 1 - TurnDefence.Turn));
                    return;
                } catch (NotRespawnable e) {
                }
            }
            Ship newShip = new Ship(1 - TurnDefence.Turn);
            TurnDefence.UnitsGroup[1 - TurnDefence.Turn].addActor(newShip);
        }
    }
}

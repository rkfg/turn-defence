package org.rkfg.turndefence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Unit extends Actor implements Cloneable {
    protected int speed;
    protected Color mColor;
    protected int life, originLife;
    protected Texture mUnitTexture;
    protected int playerNumber;
    protected float originY, totalDistance, moveDelta;
    protected int mReward;
    protected Vector2 nextXY, deltaXY, dist;

    public Unit(Texture texture, int playerNumber) {
        mUnitTexture = texture;
        width = texture.getWidth();
        height = texture.getHeight();
        this.playerNumber = playerNumber;
        if (playerNumber == 0)
            this.mColor = new Color(0.2f, 0.8f, 0.4f, 1.0f);
        else
            this.mColor = new Color(0.8f, 0.4f, 0.2f, 1.0f);
    }

    @Override
    public void act(float delta) {
        // Acts at enemy's turn
        super.act(delta);
        y = (float) (originY + Math.sin(TurnDefence.Runtime + originY * x) * 3.0f);
        if (playerNumber == TurnDefence.Turn)
            return;

        if (TurnDefence.GameOver || TurnDefence.TurnProcess == 0)
            return;

        while (true) {
            if (delta * speed < dist.len()) {
                moveDelta = speed * delta;
                x += deltaXY.x * moveDelta;
                originY += deltaXY.y * moveDelta;
                dist.x -= deltaXY.x * moveDelta;
                dist.y -= deltaXY.y * moveDelta;
                totalDistance += moveDelta;
                break;
            }
            x += deltaXY.x * dist.len();
            originY += deltaXY.y * dist.len();
            x = Math.round(x);
            originY = Math.round(originY);
            y = originY;
            totalDistance += dist.len();
            nextPath();
        }
        y = originY;
        if (x < -128.0f && playerNumber == 1 || x > TurnDefence.BFWIDTH + 128
                && playerNumber == 0) {
            remove();
            TurnDefence.changeScore(-200);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Unit clonedUnit = (Unit) super.clone();
        clonedUnit.deltaXY = new Vector2();
        clonedUnit.nextXY = new Vector2();
        clonedUnit.dist = new Vector2();
        return clonedUnit;
    }

    public void doDamage(int damage) {
        life -= damage;
        // mInvulnerable += 0.1f;
        if (TurnDefence.PlayTime == TurnDefence.PresentPlayTime)
            getStage().addActor(new Explosion(x - width / 2, y - height / 2, 1));
        
        if (life <= 0) {
            TurnDefence.changeScore(mReward);
            remove();
        }
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        if (life > originLife / 2) {
            batch.setColor((originLife - life) * 2.0f / originLife, 1.0f, 0.0f,
                    TurnDefence.Selected != null ? 0.1f : 1.0f);
        } else {
            batch.setColor(1.0f, life * 2.0f / originLife, 0.0f,
                    TurnDefence.Selected != null ? 0.1f : 1.0f);
        }
        batch.draw(TurnDefence.HealthTexture, x - width / 2.0f, y + height / 2
                + 6, width * life / originLife, 4.0f);
        batch.setColor(Color.WHITE);
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    @Override
    public Actor hit(float x, float y) {
        return x > 0 && x < width && y > 0 && y < height ? this : null;
    }

    protected void init(float x, float y, int speed, int life, int reward) {
        this.x = x;
        this.y = y;
        this.originY = y;
        this.totalDistance = 0.0f;
        this.speed = speed;
        this.life = life;
        this.originLife = life;
        this.mReward = reward;
        this.deltaXY = new Vector2();
        this.nextXY = new Vector2();
        this.dist = new Vector2();
        try {
            Gdx.app.debug(
                    "Unit init",
                    String.format(
                            "life: %d, speed: %d, playerNumber: %d, playtime: %s, classname: %s",
                            life, speed, playerNumber, TurnDefence.PlayTime,
                            getClass().getSimpleName()));
            TimeMachine.storeEvent(TurnDefence.PlayTime, new GameEvent(
                    EventType.SPAWN, (Unit) this.clone()));
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        nextPath();
    }

    public boolean isEnemyFor(int player) {
        return this.playerNumber != player;
    }

    protected void nextPath() {
        nextXY = MoveMap.getNextXY(this.x, this.y, playerNumber == 0);
        this.deltaXY.set((nextXY.x - x), (nextXY.y - y));
        this.dist.set(deltaXY);
        this.deltaXY.nor();
    }

    protected void preDraw(SpriteBatch batch) {
        mColor.a = TurnDefence.Selected != null ? 0.1f : 1.0f;
        batch.setColor(mColor);
    }
}

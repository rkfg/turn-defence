package org.rkfg.turndefence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Explosion extends Actor {

    private float lifetime, starttime;
    private static Texture mExplosionTexture;

    public Explosion(float x, float y, float lifetime) {
        this.lifetime = lifetime;
        this.starttime = lifetime;
        this.x = x;
        this.y = y;
        this.width = this.height = 64;
        this.touchable = false;
        if (mExplosionTexture == null)
            mExplosionTexture = TurnDefence.myAssetManager.get(
                    "gfx/explosion.png", Texture.class);
    }

    @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
        batch.setColor(1.0f, 1.0f, 1.0f, lifetime / starttime);
        float size = ((starttime - lifetime) / starttime * 2 + 1);
        batch.draw(mExplosionTexture, x, y, 32.0f, 32.0f, width, height, size,
                size, 0.0f, 0, 0, 64, 64, false, false);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        lifetime -= delta;
        if (lifetime < 0)
            remove();
    }

    @Override
    public Actor hit(float x, float y) {
        return null;
    }
}

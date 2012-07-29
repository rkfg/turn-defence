package org.rkfg.turndefence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

class MoveMap {
    private static String mXYString[];
    private static Vector2 mInternalMap[][];
    private static int x, y, x2, y2, cnt;
    private static boolean mMirror;
    private static Array<Vector2> mNextResult;
    private static Array<Float> mYbyXResult;
    private static Vector2 mSelectedResult;
    private static float mElemLen, mPathAlpha;
    private static Vector2 mNormalizedElem, mCurElem, mCurElemMeasure;
    private static Texture mPathTexture;
    private static Array<Vector2> mPathPoints;

    public static void draw(SpriteBatch batch, int playerNumber) {
        if (mPathPoints.size == 0) {
            for (Vector2[] pathElement : mInternalMap) {
                mCurElem.set(pathElement[1]).sub(pathElement[0]);
                mNormalizedElem.set(mCurElem).nor().mul(TurnDefence.STEP);
                mElemLen = mCurElem.len();
                mCurElem.set(pathElement[0]);
                mCurElemMeasure.set(0.0f, 0.0f);
                while (mCurElemMeasure.len() + TurnDefence.STEP <= mElemLen) {
                    drawPoint(batch, playerNumber, mCurElem);
                    mPathPoints.add(mCurElem.cpy());
                    mCurElem.add(mNormalizedElem);
                    mCurElemMeasure.add(mNormalizedElem);
                }
            }
        } else {
            for (Vector2 pathElement : mPathPoints) {
                drawPoint(batch, playerNumber, pathElement);
            }
        }
        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawPoint(SpriteBatch batch, int playerNumber, Vector2 curElem) {
        mPathAlpha = Math.abs(curElem.x - TurnDefence.Runtime * TurnDefence.SCANLINEPERIOD
                / TurnDefence.SCANLINESLOWNESS % TurnDefence.SCANLINEPERIOD / TurnDefence.SCANLINEPERIOD
                * TurnDefence.BFWIDTH / 2);
        if (mPathAlpha > 100)
            mPathAlpha = 0.5f;
        else
            mPathAlpha = (100.0f - mPathAlpha) / 200.0f + 0.5f;
        if (playerNumber == 0) {
            batch.setColor(0.6f, 1.0f, 0.6f, mPathAlpha);
            batch.draw(mPathTexture, curElem.x - mPathTexture.getWidth() / 2,
                    curElem.y - mPathTexture.getHeight() / 2);
        } else {
            batch.setColor(1.0f, 0.6f, 0.6f, mPathAlpha);
            batch.draw(mPathTexture, TurnDefence.BFWIDTH - curElem.x
                    - mPathTexture.getWidth() / 2,
                    curElem.y - mPathTexture.getHeight() / 2);
        }
    }

    public static Vector2 getNextXY(float curX, float curY, boolean toRight) {
        mMirror = curX >= TurnDefence.BFWIDTH / 2 && toRight
                || curX > TurnDefence.BFWIDTH / 2 && !toRight; // strong magic
        if (mMirror)
            curX = TurnDefence.BFWIDTH - curX;

        mNextResult.clear();
        for (Vector2[] XY : mInternalMap) { // very strong magic
            if (toRight != mMirror && XY[0].x == curX && XY[0].y == curY)
                mNextResult.add(new Vector2(XY[1]));
            if (toRight == mMirror && XY[1].x == curX && XY[1].y == curY)
                mNextResult.add(new Vector2(XY[0]));
        }
        if (mNextResult.size == 0)
            return new Vector2(toRight ? TurnDefence.BFWIDTH * 2
                    : -TurnDefence.BFWIDTH * 2, -TurnDefence.BFHEIGHT);

        mSelectedResult = mNextResult.random();

        if (mMirror)
            mSelectedResult.x = TurnDefence.BFWIDTH - mSelectedResult.x;

        return mSelectedResult;
    }

    public static float getYbyX(float x) {
        mMirror = x > TurnDefence.BFWIDTH / 2;
        if (mMirror)
            x = TurnDefence.BFWIDTH - x;
        mYbyXResult.clear();
        for (Vector2[] XY : mInternalMap) { // very strong magic
            if (XY[0].x == x)
                mYbyXResult.add(Float.valueOf(XY[0].y));
            if (XY[1].x == x)
                mYbyXResult.add(Float.valueOf(XY[1].y));
        }
        if (mYbyXResult.size == 0)
            return 360;

        return mYbyXResult.random();
    }

    public static void initMoveMap(String mapname) {
        mPathPoints = new Array<Vector2>(100);
        FileHandle mFileHandle = Gdx.files.internal(mapname);
        String mString = mFileHandle.readString();
        String mLines[] = mString.split("\n");
        mInternalMap = new Vector2[mLines.length][];
        cnt = 0;
        for (String line : mLines) {
            mXYString = line.split(" ");
            x = Integer.parseInt(mXYString[0]);
            y = Integer.parseInt(mXYString[1]);
            x2 = Integer.parseInt(mXYString[2]);
            y2 = Integer.parseInt(mXYString[3]);
            mInternalMap[cnt] = new Vector2[] { new Vector2(x, y),
                    new Vector2(x2, y2) };
            cnt++;
        }
        mNextResult = new Array<Vector2>(5); // path forks
        mYbyXResult = new Array<Float>(5);
        mNormalizedElem = new Vector2();
        mCurElem = new Vector2();
        mCurElemMeasure = new Vector2();
        mPathTexture = TurnDefence.myAssetManager.get("gfx/path.png", Texture.class);
    }
}

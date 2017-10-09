package com.iwai.tomoki.chat.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.iwai.tomoki.chat.R;

/**
 * 三角形Viewクラス
 */
public class Triangle extends View {
    // 三角形の方向
    public final static int DIRECTION_RIGHT = 0;
    public final static int DIRECTION_LEFT = 1;
    public final static int DIRECTION_TOP = 2;
    public final static int DIRECTION_BOTTOM = 3;

    // 描画オブジェクト
    private Paint mPaint;

    // 三角形の色
    private int mColor;

    // 三角形の方向
    private int mDirection;

    /**
     * コンストラクタ
     *
     * @param context {@link Context}
     * @param attrs   {@link AttributeSet}
     */
    public Triangle(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 描画オブジェクトを初期化
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        // XMLで定義されたプロパティ値を取得
        TypedArray props = context.obtainStyledAttributes(attrs, R.styleable.Triangle);

        // 三角形の色を取得(デフォ:白)
        mColor = props.getColor(R.styleable.Triangle_color, 0xFFFFFFFF);

        // 三角形の方向を取得(デフォ:左)
        mDirection = props.getInt(R.styleable.Triangle_direction, DIRECTION_LEFT);

        props.recycle();
    }

    /**
     * 三角形の色を設定します。
     *
     * @param color 色
     */
    public void setColor(String color) {
        mColor = Color.parseColor(color);
        requestLayout();
        invalidate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 三角形の色を設定
        mPaint.setColor(mColor);

        // 三角形の方向に応じてパスを生成
        Path triangle = new Path();
        switch (mDirection) {
            case DIRECTION_RIGHT:
                // 左上から
                triangle.moveTo(0, 0);
                // まっすぐ左下へ
                triangle.lineTo(0, getMeasuredHeight());
                // んで、右端の高さは真ん中へ
                triangle.lineTo(getMeasuredWidth(), (getMeasuredHeight() / 2));
                break;

            case DIRECTION_LEFT:
                triangle.moveTo(getMeasuredWidth(), 0);
                triangle.lineTo(getMeasuredWidth(), getMeasuredHeight());
                triangle.lineTo(0, (getMeasuredHeight() / 2));
                break;

            case DIRECTION_TOP:
                triangle.moveTo(0, getMeasuredHeight());
                triangle.lineTo(getMeasuredWidth(), getMeasuredHeight());
                triangle.lineTo((getMeasuredWidth() / 2), 0);
                break;

            case DIRECTION_BOTTOM:
                triangle.moveTo(0, 0);
                triangle.lineTo(getMeasuredWidth(), 0);
                triangle.lineTo((getMeasuredWidth() / 2), getMeasuredHeight());
                break;
        }

        // パスを塗る
        canvas.drawPath(triangle, mPaint);
    }
}

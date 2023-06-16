package com.teamshiny.numberjudgement;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class MyCanvas extends View {

    private Path path;
    private Paint paint;

    private Bitmap bitmap;

    public MyCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        //１）コンストラクタ（≒必需品）
        path = new Path();//線を引いたり、図形を描いたり、要するにグラフィック

        paint = new Paint();//筆の種類
        paint.setColor(Color.BLACK);//色の指定：BLACK,RED,BLUE
        paint.setStyle(Paint.Style.STROKE);//線をひく
        paint.setStrokeWidth(50);//幅

    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    //２）onDraw（描画の準備/プロペラが回りだした状態）

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }


    //３）実際の操縦 (条件分岐：押したとき、動かしたとき、放した時）

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //(3-1)座標を取得（x座標、y座標）
        float x = event.getX();
        float y = event.getY();

        //(3-2)タッチの処理
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }

        //return super.onTouchEvent(event);
        return true;
    }


    //４）クリア処理
    public void clearCanvas() {
        path.reset();
        invalidate();
    }

}
package com.teamshiny.numberjudgement;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private com.teamshiny.numberjudgement.AI judgement;
    private com.teamshiny.numberjudgement.MyCanvas myCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//R.layout.activity_main
        //６）描画のid取得
        myCanvas = (com.teamshiny.numberjudgement.MyCanvas) findViewById(R.id.myCanvas);
    }

    //クリアボタン
    public void clearButton(View view) {
        myCanvas.clearCanvas();
    }

    //判定ボタン
    public void predictButton(View view) {
        //myCanvasを縮小する。
        int size = 28;//28*3,50*3
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(getViewCapture(myCanvas), size, size, false);
        List<Integer> targetList = setBitmapArray(scaledBitmap);//配列化する。
        judgement = new AI(this);
        int resultIndex = judgement.judgement(targetList);//予測確率インデックス
        List<Float> finalList = judgement.getFinalList();//予測確率リスト
        resultDisplay(finalList, resultIndex);
    }

    //予測結果を表示する
    public void resultDisplay(List<Float> targetList, int targetIndex) {
        String[] sNumber = {"【０】", "【１】", "【２】", "【３】", "【４】", "【５】", "【６】", "【７】", "【８】", "【９】"};
        //Log.i("baron", "resultIndex:" + targetIndex);
        //Log.i("baron", "最終確率リスト:" + targetList);
        ((TextView) findViewById(R.id.text1)).setText("予測：" + sNumber[targetIndex]);
        ((TextView) findViewById(R.id.text2)).setText("確率：" + (Math.round(targetList.get(targetIndex) * 10000) / 100.0) + "%");
        ((TextView) findViewById(R.id.pre0)).setText("【０】" + (Math.round(targetList.get(0) * 10000) / 100.0) + "%");//"【０】" + targetList.get(0)
        ((TextView) findViewById(R.id.pre1)).setText("【１】" + (Math.round(targetList.get(1) * 10000) / 100.0) + "%");
        ((TextView) findViewById(R.id.pre2)).setText("【２】" + (Math.round(targetList.get(2) * 10000) / 100.0) + "%");
        ((TextView) findViewById(R.id.pre3)).setText("【３】" + (Math.round(targetList.get(3) * 10000) / 100.0) + "%");
        ((TextView) findViewById(R.id.pre4)).setText("【４】" + (Math.round(targetList.get(4) * 10000) / 100.0) + "%");
        ((TextView) findViewById(R.id.pre5)).setText("【５】" + (Math.round(targetList.get(5) * 10000) / 100.0) + "%");
        ((TextView) findViewById(R.id.pre6)).setText("【６】" + (Math.round(targetList.get(6) * 10000) / 100.0) + "%");
        ((TextView) findViewById(R.id.pre7)).setText("【７】" + (Math.round(targetList.get(7) * 10000) / 100.0) + "%");
        ((TextView) findViewById(R.id.pre8)).setText("【８】" + (Math.round(targetList.get(8) * 10000) / 100.0) + "%");
        ((TextView) findViewById(R.id.pre9)).setText("【９】" + (Math.round(targetList.get(9) * 10000) / 100.0) + "%");
    }

    //bitmapを配列化
    public List<Integer> setBitmapArray(Bitmap b) {
        List<Integer> colorNumber = new ArrayList<>();
        int width = b.getWidth();
        int height = b.getHeight();
        //BitmapからPixelを取得
        //白は255,黒は0
        //白#FFFFFF(255,255,255)
        //黒#000000(0,0,0)
        //赤#FF0000(255,0,0)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = b.getPixel(x, y) & 0xff;//下位の8ビットだけ残し、残りを全て0にする。(Integer型)
                //白は255,黒は0
                if (color == 255) {
                    colorNumber.add(0);
                } else {
                    colorNumber.add(1);
                }
            }
        }
        //Log.i("baron", "List2828: " + colorNumber.toString());//len784
        return colorNumber;
    }

    //キャプチャを撮る部分
    public Bitmap getViewCapture(View view) {
        view.setDrawingCacheEnabled(true);
        // Viewのキャッシュを取得
        Bitmap cache = view.getDrawingCache();
        Bitmap screenShot = Bitmap.createBitmap(cache);
        view.setDrawingCacheEnabled(false);
        return screenShot;
    }

    //バックボタンが押された時の処理変更
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("数字判定を終了しますか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);//アプリ全体を終了する。
                        finish();//終了する。
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
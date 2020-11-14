package com.example.countdowntimer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private CountDownTimer wCdt;//カウントダウンタイマー
    private CountDownTimer bCdt;//カウントダウンタイマー
    static int whiteTime1;//int白時間
    static long whiteTime2;//long白時間
    static int blackTime1;//int黒時間
    static long blackTime2;//long黒時間
    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.US);//残り時間を分,秒で表示

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ここから
        this.whiteTime1 = 180000;//180秒
        this.blackTime1 = 180000;//180秒
        TextView whiteTimer = findViewById(R.id.whiteTimer);
        TextView blackTimer = findViewById(R.id.blackTimer);
        whiteTimer.setText(dateFormat.format(this.whiteTime1));
        blackTimer.setText(dateFormat.format(this.blackTime1));
        this.wCdt = new Timer(this.whiteTime1, 100, whiteTimer, blackTimer, "w");
        this.bCdt = new Timer(this.blackTime1, 100, whiteTimer, blackTimer, "b");
    }

    public void startTimer(View view) {
        int id = view.getId();
        TextView whiteTimer = findViewById(R.id.whiteTimer);
        TextView blackTimer = findViewById(R.id.blackTimer);
        if (id == R.id.whiteStart) {
            this.wCdt = new Timer(this.whiteTime1, 100, whiteTimer, blackTimer, "w");
            this.wCdt.start();//wタイマースタート
        } else if (id == R.id.blackStart) {
            this.bCdt = new Timer(this.blackTime1, 100, whiteTimer, blackTimer, "b");
            this.bCdt.start();//bタイマースタート
        }
    }

    public void stopTimer(View view) {
        int id = view.getId();
        if (id == R.id.whiteStop) {
            TextView whiteRemainingTime = findViewById(R.id.whiteRemainingTime);
            whiteRemainingTime.setText("残り時間：" + this.whiteTime1 / 1000 + "秒" + " (" + this.whiteTime2 + ")");
            this.wCdt.cancel();//タイマー一時停止
        } else if (id == R.id.blackStop) {
            TextView bRemainingTime = findViewById(R.id.blackRemainingTime);
            bRemainingTime.setText("残り時間：" + this.blackTime1 / 1000 + "秒" + " (" + this.blackTime2 + ")");
            this.bCdt.cancel();//タイマー一時停止
        }
    }
}
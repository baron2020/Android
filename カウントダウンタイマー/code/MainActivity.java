package com.example.countdowntimer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TimeLimitControl wCdt;//wカウントダウンタイマー
    private TimeLimitControl bCdt;//bカウントダウンタイマー
    static long whiteTime;//long白時間
    static long blackTime;//long黒時間
    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.US);//残り時間を分,秒で表示

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ここから
        this.whiteTime = 180000;//180秒
        this.blackTime = 180000;//180秒
        TextView whiteTimer = findViewById(R.id.whiteTimer);
        TextView blackTimer = findViewById(R.id.blackTimer);
        whiteTimer.setText(dateFormat.format(this.whiteTime));
        blackTimer.setText(dateFormat.format(this.blackTime));
        this.wCdt = new TimeLimitControl(this.whiteTime, 100, whiteTimer, "w");
        this.bCdt = new TimeLimitControl(this.blackTime, 100, blackTimer, "b");
    }

    public void startTimer(View view) {
        int id = view.getId();
        TextView whiteTimer = findViewById(R.id.whiteTimer);
        TextView blackTimer = findViewById(R.id.blackTimer);
        if (id == R.id.whiteStart) {
            this.wCdt = new TimeLimitControl(this.whiteTime, 100, whiteTimer, "w");
            this.wCdt.start();//wタイマースタート
        } else if (id == R.id.blackStart) {
            this.bCdt = new TimeLimitControl(this.blackTime, 100, blackTimer, "b");
            this.bCdt.start();//bタイマースタート
        }
    }

    public void stopTimer(View view) {
        int id = view.getId();
        if (id == R.id.whiteStop) {
            TextView wRemainingTime = findViewById(R.id.whiteRemainingTime);
            wRemainingTime.setText("残り時間：" + this.whiteTime / 1000 + "秒" + " (" + this.whiteTime + ")");
            this.wCdt.cancel();//タイマー一時停止
        } else if (id == R.id.blackStop) {
            TextView bRemainingTime = findViewById(R.id.blackRemainingTime);
            bRemainingTime.setText("残り時間：" + this.blackTime / 1000 + "秒" + " (" + this.blackTime + ")");
            this.bCdt.cancel();//タイマー一時停止
        }
    }
}

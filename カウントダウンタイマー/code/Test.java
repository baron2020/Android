package com.example.countdowntimer;

import android.os.CountDownTimer;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Test {
    private int startTime;
    Test(){
        this.startTime=MainActivity.remainingTime;
    }
    public void play(TextView textTimer){
        MainActivity.cdt = new CountDownTimer(this.startTime, 100) {
            //第一引数：10000(10秒),第二引数：100(0.1秒)おきに実行される
            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.US);//残り時間を分,秒で表示
            @Override
            public void onTick(long millisUntilFinished) {
                String diaplayDataTime=dateFormat.format(millisUntilFinished);
                textTimer.setText(diaplayDataTime);
                MainActivity.remainingTime=(int)millisUntilFinished;
            }
            @Override
            public void onFinish() {
                textTimer.setText("End");
            }
        };
    }
}

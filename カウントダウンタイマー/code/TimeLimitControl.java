package com.example.countdowntimer;

import android.os.CountDownTimer;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Timer extends CountDownTimer {
    private TextView wt;//白タイマー
    private TextView bt;//黒タイマー
    private String target;//白？黒？
    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.US);//残り時間を分,秒で表示

    public Timer(long millisInFuture, long countDownInterval, TextView wt, TextView bt, String target) {
        super(millisInFuture, countDownInterval);
        this.target = target;
        this.wt = wt;
        this.bt = bt;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        String diaplayDataTime = dateFormat.format(millisUntilFinished);
        if (this.target.equals("w")) {
            MainActivity.whiteTime1 = (int) millisUntilFinished;
            MainActivity.whiteTime2 = (long) millisUntilFinished;
            this.wt.setText(diaplayDataTime);
        } else if (this.target.equals("b")) {
            MainActivity.blackTime1 = (int) millisUntilFinished;
            MainActivity.blackTime2 = millisUntilFinished;
            this.bt.setText(diaplayDataTime);
        }
    }

    @Override
    public void onFinish() {
        if (this.target.equals("w")) {
            this.wt.setText("End");
        } else if (this.target.equals("b")) {
            this.bt.setText("End");
        }
    }
}


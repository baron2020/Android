package com.example.countdowntimer;

import android.os.CountDownTimer;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeLimitControl extends CountDownTimer {
    private TextView targetTimer;
    private String target;
    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.US);//残り時間を分,秒で表示

    public TimeLimitControl(long millisInFuture, long countDownInterval, TextView targetTimer, String target) {
        super(millisInFuture, countDownInterval);
        this.targetTimer = targetTimer;
        this.target = target;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        String diaplayTime = dateFormat.format(millisUntilFinished);
        if (this.target.equals("w")) {
            MainActivity.whiteTime = millisUntilFinished;
        } else if (this.target.equals("b")) {
            MainActivity.blackTime =millisUntilFinished;
        }
        this.targetTimer.setText(diaplayTime);
    }

    @Override
    public void onFinish() {
        this.targetTimer.setText("End");
    }
}


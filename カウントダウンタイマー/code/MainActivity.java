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

    static CountDownTimer cdt;//カウントダウンタイマー
    static int remainingTime;//残り時間

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.remainingTime=10100;//10秒
        TextView displayTimer = findViewById(R.id.timer);
        TextView diaplayRemainingTime = findViewById(R.id.diaplayRemainingTime);
        Test test = new Test();
        test.play(displayTimer);//タイマー,残り時間
    }

    public void startTimer(View view) {
        TextView displayTimer = findViewById(R.id.timer);
        Test reStart = new Test();
        reStart.play(displayTimer);
        cdt.start();
    }

    public void stopTimer(View view) {
        TextView diaplayRemainingTime = findViewById(R.id.diaplayRemainingTime);
        diaplayRemainingTime.setText("残り時間："+remainingTime/1000+"秒");
        cdt.cancel();
    }
}
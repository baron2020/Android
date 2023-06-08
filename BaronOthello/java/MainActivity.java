package com.baron.baronothello;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity<Final> extends AppCompatActivity {
    //メニューを追加する。
    //リファクタリング
    final int defaultChoice = -1;
    final String[] timeChoiceItem = {"１０分切れ負け", "５分切れ負け", "３分切れ負け"};
    final List<Integer> timeChoiceItemNumber = new ArrayList<>();
    final String[] blackWhiteChoiceItem = {"黒", "白", "ランダム"};
    final List<Integer> bwChoiceItemNumber = new ArrayList<>();
    MediaPlayer mpStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.test);
        this.mpStart = MediaPlayer.create(this, R.raw.tyakusyu);
    }

    public void startEvent(View view) {
        mpStart.start();
        timeChoiceDialog();
    }

    //持ち時間の選択
    public void timeChoiceDialog() {
        this.timeChoiceItemNumber.clear();
        this.timeChoiceItemNumber.add(defaultChoice);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//getActivity()
        builder.setTitle("持ち時間を選択してください。");
        //リスト表示する文字列配列,デフォルトで選択している位置,項目クリック時のクリックリスナー
        builder.setSingleChoiceItems(timeChoiceItem, defaultChoice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //ラジオボタン選択時の処理
                timeChoiceItemNumber.clear();
                timeChoiceItemNumber.add(which);
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //OKの処理
                if (timeChoiceItemNumber.get(0) == -1) {
                    Toast.makeText(MainActivity.this, "選択していません。", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    blackWhiteChoiceDialog();
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);//Cancel
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //黒,白の選択
    public void blackWhiteChoiceDialog() {
        this.bwChoiceItemNumber.clear();
        this.bwChoiceItemNumber.add(defaultChoice);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//getActivity()
        builder.setTitle("黒,白を選択してください。");
        //リスト表示する文字列配列,デフォルトで選択している位置,項目クリック時のクリックリスナー
        builder.setSingleChoiceItems(blackWhiteChoiceItem, defaultChoice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //ラジオボタン選択時の処理
                bwChoiceItemNumber.clear();
                bwChoiceItemNumber.add(which);
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //OKの処理
                if (bwChoiceItemNumber.get(0) == -1) {
                    Toast.makeText(MainActivity.this, "選択していません。", Toast.LENGTH_SHORT).show();
                    return;
                } else if (bwChoiceItemNumber.get(0) == 2) {
                    bwChoiceItemNumber.clear();
                    bwChoiceItemNumber.add(returnRandomNumber());
                    intentOthelloStart();
                } else {
                    intentOthelloStart();
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public int returnRandomNumber() {
        //ランダム処理
        Random rand = new Random();
        int randomNumber = rand.nextInt(2);
        return randomNumber;
    }

    public void intentOthelloStart() {
        Intent intent = new Intent(getApplication(), OthelloStart.class);
        intent.putExtra("timeKey", timeChoiceItem[timeChoiceItemNumber.get(0)]);//持ち時間をセット
        intent.putExtra("bwKey", blackWhiteChoiceItem[bwChoiceItemNumber.get(0)]);//黒,白をセット
        startActivity(intent);//画面推移
        finish();//activity_main画面を終了し、バックキーで戻らせない。
    }

    //バックボタンが押された時の処理変更
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("ばろんオセロを終了しますか？")
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
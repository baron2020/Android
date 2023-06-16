package com.teamshiny.shogiai;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity<Final> extends AppCompatActivity {
    final int defaultChoice = -1;
    final String[] tebanChoiceItem = {"先手", "後手"};
    final List<Integer> tebanChoiceItemNumber = new ArrayList<>();
    MediaPlayer mpStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initInstallTime();
    }

    private void initInstallTime() {
        try {
            Context context = createPackageContext(getPackageName(), 0);//com.teamshiny.shogiai
            AssetManager assetManager = context.getAssets();
            //Log.i("baron", "pname:" + getPackageName());
            //Log.i("baron", "assetManager :" + assetManager);
            //Log.i("baron", "assetManager :" + assetManager.getClass());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    //対局ボタン
    public void clickEvent1(View view) {
        tebanChoiceDialog();
    }

    //検討ボタン
    public void clickEvent2(View view) {
        Intent intent = new Intent(getApplication(), Consideration.class);
        startActivity(intent);//画面推移
        finish();//activity_main画面を終了し、バックキーで戻らせない。
    }

    public void tebanChoiceDialog() {
        tebanChoiceItemNumber.add(defaultChoice);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//getActivity()
        builder.setTitle("先手、後手を選択してください。");
        //リスト表示する文字列配列,デフォルトで選択している位置,項目クリック時のクリックリスナー
        builder.setSingleChoiceItems(tebanChoiceItem, defaultChoice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //ラジオボタン選択時の処理
                tebanChoiceItemNumber.clear();
                tebanChoiceItemNumber.add(which);
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //OKの処理
                if (tebanChoiceItemNumber.get(0) == -1) {
                    Toast.makeText(MainActivity.this, "選択していません。", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    intentBattle();
                }
            }
        });
        builder.setNegativeButton("キャンセル", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void intentBattle() {
        Intent intent = new Intent(getApplication(), Battle.class);
        intent.putExtra("tebanKey", tebanChoiceItem[tebanChoiceItemNumber.get(0)]);//先手,後手をセット
        startActivity(intent);//画面推移
        finish();//activity_main画面を終了し、バックキーで戻らせない。
    }

    //バックボタンが押された時の処理変更
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("チームシャイニー将棋AIを終了しますか？")
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
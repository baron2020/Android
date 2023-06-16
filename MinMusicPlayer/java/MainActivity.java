package com.baron.minmusicplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener{

    // drawableに画像を入れる、R.id.xxx はint型
    //かいこう,うたがたり,はいいろ,ゆうげん,セカシタガリ
    private static final int[] photos = {
            R.drawable.kaikou300,
            R.drawable.utagatari300,
            R.drawable.haiiro300,
            R.drawable.yuugen300,
            R.drawable.sekasitagari300,
            0
    };

    private String[] imageComments = {
            "test邂逅(かいこう)", "詩語(うたがたり)", "灰彩(はいいろ)", "遊幻(ゆうげん)", "セカシタガリ"
            ,""
    };
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ListViewのインスタンスを生成
        listView = (ListView) findViewById(R.id.listtest);
        // BaseAdapter を継承したadapterのインスタンスを生成
        // レイアウトファイル list_items.xml を
        // activity_main.xml に inflate するためにadapterに引数として渡す
        BaseAdapter adapter = new ListAdapter(
                this.getApplicationContext(), R.layout.list_items, imageComments, photos
        );
        // ListViewにadapterをセット
        listView.setAdapter(adapter);

        // クリックリスナーをセット
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        if(position==5){
            return;
        }
        Intent intent = new Intent(getApplication(), MusicPlayer.class);
        intent.putExtra("musicKey",String.valueOf(position) );//選曲をセット
        // SubActivityへ遷移
        startActivity(intent);
        finish();//activity_main画面を終了し、バックキーで戻らせない。
    }

    public void intentStart(String musicKey) {
        Intent intent = new Intent(getApplication(), MusicPlayer.class);
        intent.putExtra("musicKey",musicKey );//選曲をセット
        startActivity(intent);//画面推移
        finish();//activity_main画面を終了し、バックキーで戻らせない。
    }

    //バックボタンが押された時の処理変更
    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setMessage("minの音楽プレーヤーを終了しますか？")
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
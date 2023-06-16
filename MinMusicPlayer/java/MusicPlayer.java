package com.baron.minmusicplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayer extends AppCompatActivity {
    private int[] musicTime = {
            257000,241000,277000,247000,191000,0
    };
    private int musicNumber = 0;//選曲
    private int mpMaxTime = 0;

    private MediaPlayer mpMusic;//かいこう,うたがたり,はいいろ,ゆうげん,セカシタガリ
    private ImageButton playButton;//再生,停止ボタン
    private SeekBar seekBar;//シークバー
    private boolean buttonPushFlg=false;//ボタンを押したらtrue、曲が流れたらfalse
    private boolean stopFlg = false;//停止ボタンを押したらtrue

    private Timer timer = null;
    private MyTimerTask timerTask = null;

    private Resources res;//リソース
    private Drawable dPlay;
    private Drawable dStop;

    private boolean onPauseFlg = false;//中断されたらtrue
    private boolean onPausePlaying = false;//中断された時に再生されていたらtrue
    private int onPauseTime = 0;//中断された時の曲の位置

    private SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss", Locale.US);//残り時間を分,秒で表示("mm:ss")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player);
        getMusicKey();//インテントから選曲を取得
        mpAndTimerStop();//mp停止確認
        prepare1();
        mpCreate(this.musicNumber);//mp作成
        //読み込み完了リスナー登録
        this.mpMusic.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //読み込み完了で行いたい処理
                setUpSeekBar();//シークバーの準備
                mpStart();//タイマーを設定して再生
            }
        });
    }

    //データを受け取り、ルール(持ち時間,先後)を設定。
    //かいこう,うたがたり,はいいろ,ゆうげん,セカシタガリ
    public void getMusicKey() {
        Intent intent = getIntent();
        String musicKey = intent.getStringExtra("musicKey");
        setMusicNumber(musicKey);//選んだ曲を設定
        setMusicImageAndTitle(this.musicNumber);//選んだ曲にあわせた画面とタイトルを設定
    }

    //選んだ曲を設定
    public void setMusicNumber(String index) {
        if (index.equals("0")) {
            this.musicNumber = 0;
        } else if (index.equals("1")) {
            this.musicNumber = 1;
        } else if (index.equals("2")) {
            this.musicNumber = 2;
        } else if (index.equals("3")) {
            this.musicNumber = 3;
        } else if (index.equals("4")) {
            this.musicNumber = 4;
        }
    }

    //選んだ曲にあわせた画面とタイトルを設定
    public void setMusicImageAndTitle(int index) {
        if (index == 0) {
            ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.kaikou300);
            ((TextView) findViewById(R.id.textView_title)).setText("邂逅(かいこう)");
        } else if (index == 1) {
            ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.utagatari300);
            ((TextView) findViewById(R.id.textView_title)).setText("詩語(うたがたり)");
        } else if (index == 2) {
            ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.haiiro300);
            ((TextView) findViewById(R.id.textView_title)).setText("灰彩(はいいろ)");
        } else if (index == 3) {
            ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.yuugen300);
            ((TextView) findViewById(R.id.textView_title)).setText("遊幻(ゆうげん)");
        } else if (index == 4) {
            ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.sekasitagari300);
            ((TextView) findViewById(R.id.textView_title)).setText("セカシタガリ");
        }
    }

    //mpとタイマーのストップ
    public void mpAndTimerStop() {
        if (this.mpMusic != null) {
            if (this.mpMusic.isPlaying() == true) {
                this.mpMusic.stop();//停止
            }
            this.mpMusic.release();// リソースの解放
            this.mpMusic = null;
        }
        if (this.timer != null) {
            this.timer.cancel();//タスクを破棄して終了
            this.timer = null;
            this.timerTask = null;
        }
    }

    //準備1
    public void prepare1() {
        this.playButton = findViewById(R.id.button_play);
        this.res = getResources();//リソースを取得
        this.dPlay = res.getDrawable(R.drawable.icon_play, getTheme());
        this.dStop = res.getDrawable(R.drawable.icon_pause, getTheme());
    }

    //mp作成
    public void mpCreate(int selectNumber) {
        if (selectNumber == 0) {
            this.mpMusic = MediaPlayer.create(this, R.raw.kaikou417);
        } else if (selectNumber == 1) {
            this.mpMusic = MediaPlayer.create(this, R.raw.utagatari401);
        } else if (selectNumber == 2) {
            this.mpMusic = MediaPlayer.create(this, R.raw.haiiro438);
        } else if (selectNumber == 3) {
            this.mpMusic = MediaPlayer.create(this, R.raw.yuugen407);
        } else if (selectNumber == 4) {
            this.mpMusic = MediaPlayer.create(this, R.raw.sekasitagari);
        }
    }

    //シークバーの準備
    public void setUpSeekBar() {
        this.seekBar = findViewById(R.id.seekBar);//シークバーのインスタンスを生成
        this.mpMaxTime = this.mpMusic.getDuration(); //曲の長さを設定
        this.seekBar.setMax(this.mpMaxTime / 1000);// シークバーの最大値を曲の長さに設定
        //シークバーの最大値テキストを曲の長さに設定
        ((TextView) findViewById(R.id.textView_duration)).setText((String) this.dateFormat.format(this.mpMaxTime));
        //シークバーを監視するリスナーを設定
        this.seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // シークバーを動かしている最中に呼ばれます
                        int sp = seekBar.getProgress();
                        ((TextView) findViewById(R.id.textView_position)).setText((String) dateFormat.format(sp * 1000));
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // シークバーに触れていない状態からシークバーに触れて動かす間に呼ばれます
                        //時間を表示
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // シークバーを動かしてから、シークバーをはなした場合に呼び出されます。
                        //移動後の時間にシークバーを設定
                        int sp = seekBar.getProgress();
                        ((TextView) findViewById(R.id.textView_position)).setText((String) dateFormat.format(sp * 1000));
                        if (mpMusic != null) {
                            //曲を移動後の時間から再生
                            mpMusic.seekTo(sp * 1000);
                        }
                    }
                }
        );
    }

    //シークバーの準備
    public void resetSeekBar() {
        this.seekBar = findViewById(R.id.seekBar);//シークバーのインスタンスを生成
        this.seekBar.setMax(musicTime[this.musicNumber]/ 1000);// シークバーの最大値を曲の長さに設定
        //シークバーの最大値テキストを曲の長さに設定
        ((TextView) findViewById(R.id.textView_duration)).setText((String) dateFormat.format(musicTime[this.musicNumber]));
        //シークバーを監視するリスナーを設定
        this.seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // シークバーを動かしている最中に呼ばれます
                        int sp = seekBar.getProgress();
                        ((TextView) findViewById(R.id.textView_position)).setText((String) dateFormat.format(sp * 1000));
                    }
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // シークバーに触れていない状態からシークバーに触れて動かす間に呼ばれます
                        //時間を表示
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // シークバーを動かしてから、シークバーをはなした場合に呼び出されます。
                        //移動後の時間にシークバーを設定
                        int sp = seekBar.getProgress();
                        ((TextView) findViewById(R.id.textView_position)).setText((String) dateFormat.format(sp * 1000));
                        if (mpMusic != null) {
                            //曲を移動後の時間から再生
                            mpMusic.seekTo(sp * 1000);
                        }
                    }
                }
        );
    }

    //タイマータスクを作成してmp再生
    public void mpStart() {
        this.timer = new Timer();// タイマーインスタンスを作成
        this.timerTask = new MyTimerTask();// タイマータスクインスタンスを作成
        if (this.mpMusic != null) {
            this.mpMusic.start();
            // タイマースケジュールを設定
            this.timer.schedule(timerTask, 0, 1000);// (TimerTask,開始時間(long),間隔(1000=1秒,long))
        }
    }

    //this.stopFlg = false;停止ボタンを押していない：停止ボタンが表示されている
    //this.stopFlg = true;停止ボタンを押した：スタートボタンが表示されている
    //再生,停止ボタン
    public void playEvent(View view) {
        this.buttonPushFlg=true;
        if (this.stopFlg == false){
            //停止ボタンを押した
            mpAndTimerStop();//mp,タイマーインスタンスの解放
            this.playButton.setBackground(dPlay);//再生ボタンの表示
            this.stopFlg = true;
        }else if (this.stopFlg == true){
            //再生ボタンを押した
            mpAndTimerStop();//mp,タイマーインスタンスの解放
            this.playButton.setBackground(dStop);//再生ボタンの表示
            this.stopFlg = false;
            mpCreate(this.musicNumber);//mp作成
            //読み込み完了リスナー登録
            this.mpMusic.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    //読み込み完了で行いたい処理
                    if (seekBar.getMax() == seekBar.getProgress()) {
                        setUpSeekBar();
                        mpStart();
                    } else {
                        setUpSeekBar();
                        mpMusic.seekTo(seekBar.getProgress() * 1000);
                        mpStart();
                    }
                }
            });
        }
    }

    //進むボタン〇
    public void nextEvent(View view) {
        this.buttonPushFlg=true;
        this.seekBar.setProgress(seekBar.getMax());//最終に合わせる
        this.playButton.setBackground(dPlay);
        mpAndTimerStop();//リソースの解放
        this.stopFlg = true;
    }

    //次の曲を再生する。
    public void nextMusicPlay() {
        if (this.musicNumber == 4) {
            playButton.setBackground(dPlay);
            return;
        }
        this.musicNumber += 1;//選んだ曲を設定
        setMusicImageAndTitle(this.musicNumber);//選んだ曲にあわせた画面とタイトルを設定
        this.playButton.setBackground(dStop);//停止ボタンを設定
        mpCreate(this.musicNumber);//mp作成
        //読み込み完了リスナー登録
        this.mpMusic.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //読み込み完了で行いたい処理
                setUpSeekBar();//シークバーの準備
                mpStart();//タイマーを設定して再生
            }
        });
    }

    //前進ボタン〇
    public void forwardEvent(View view) {
        this.buttonPushFlg=true;
        if (this.musicNumber == 4) {
            //進むボタンと同じ
            this.seekBar.setProgress(seekBar.getMax());//最終に合わせる
            this.playButton.setBackground(dPlay);
            mpAndTimerStop();//リソースの解放
            this.stopFlg = true;
            return;
        } else {
            this.seekBar.setProgress(0);
            if ((this.mpMusic != null) && (this.mpMusic.isPlaying() == true)) {
                mpAndTimerStop();//mp停止確認
                nextMusicPlay();//次の曲を再生する。
            } else {
                mpAndTimerStop();//mp停止確認
                this.musicNumber += 1;//選んだ曲を設定
                setMusicImageAndTitle(this.musicNumber);//選んだ曲にあわせた画面とタイトルを設定
                this.playButton.setBackground(dPlay);
                resetSeekBar();//シークバーの再設定
            }
        }
    }

    //戻るボタン〇
    public void backEvent(View view) {
        this.buttonPushFlg=true;
        backMusic();
    }

    //巻き戻し。
    public void backMusic() {
        this.seekBar.setProgress(0);
        if ((this.mpMusic != null) && (this.mpMusic.isPlaying() == true)) {
            mpAndTimerStop();//リソースの解放
            this.playButton.setBackground(dStop);
            mpCreate(this.musicNumber);//mp作成
            //読み込み完了リスナー登録
            this.mpMusic.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    //読み込み完了で行いたい処理
                    mpStart();//mp再生スタート
                    playButton.setBackground(dStop);
                }
            });
        } else {
            this.playButton.setBackground(dPlay);
            mpAndTimerStop();//リソースの解放
        }
    }

    //巻き戻しボタン〇
    public void rewindEvent(View view) {
        this.buttonPushFlg=true;
        this.seekBar.setProgress(0);
        if (this.musicNumber == 0) {
            //戻るボタンと同じ
            backMusic();
            return;
        } else {
            if ((this.mpMusic != null) && (this.mpMusic.isPlaying() == true)) {
                mpAndTimerStop();//mp停止確認
                this.musicNumber -= 1;//選んだ曲を設定
                setMusicImageAndTitle(this.musicNumber);//選んだ曲にあわせた画面とタイトルを設定
                this.playButton.setBackground(dStop);
                mpCreate(this.musicNumber);//mp作成
                //読み込み完了リスナー登録
                this.mpMusic.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        //読み込み完了で行いたい処理
                        setUpSeekBar();//シークバーの準備
                        mpStart();//タイマーを設定して再生
                    }
                });
            } else {
                mpAndTimerStop();//mp停止確認
                this.musicNumber -= 1;//選んだ曲を設定
                setMusicImageAndTitle(this.musicNumber);//選んだ曲にあわせた画面とタイトルを設定
                this.playButton.setBackground(dPlay);
                resetSeekBar();//シークバーの再設定
            }
        }
    }

    //this.stopFlg = false;停止ボタンを押していない：停止ボタンが表示されている
    //this.stopFlg = true;停止ボタンを押した：スタートボタンが表示されている
    public class MyTimerTask extends TimerTask {
        private Handler handler = new Handler();
        public MyTimerTask() {
        }

        @Override
        public void run() {
            handler.post(new Runnable() {
                public void run() {
                    if (mpMusic == null) {
                        mpAndTimerStop();
                        return;
                    }
                    //停止ボタンを押さずに曲が最後まで再生され、止まっている。
                    if ((mpMusic.isPlaying() == false)&&(stopFlg == false)&&(buttonPushFlg==false)) {
                        seekBar.setProgress(seekBar.getMax());//最終に合わせる
                        mpAndTimerStop();
                        nextMusicPlay();
                    }else if (mpMusic.isPlaying() == true) {
                        buttonPushFlg=false;
                        //再生中なら１秒間隔で、曲の再生位置を取得し、シークバーの位置を合わせる。
                        int currentMpPosition = mpMusic.getCurrentPosition();//現在の再生ポジションを返します
                        seekBar.setProgress(currentMpPosition / 1000);
                    }
                }
            });
        }
    }

    //アプリがユーザーから見えなくなった時の処理
    @Override
    //onStop()
    protected void onPause() {
        super.onPause();
        if (this.mpMusic != null) {
            this.onPausePlaying = this.mpMusic.isPlaying();
            this.onPauseTime = this.mpMusic.getCurrentPosition();
        }
        mpAndTimerStop();//mp,タイマーインスタンスの解放
        this.onPauseFlg = true;
    }

    //アプリが再開された時の処理
    @Override
    protected void onResume() {
        super.onResume();
        //再開時の処理
        if ((this.onPauseFlg == true) && (this.onPausePlaying == true)) {
            mpCreate(this.musicNumber);//mp作成
            this.timer = new Timer();// タイマーインスタンスを作成
            this.timerTask = new MyTimerTask();// タイマータスクインスタンスを作成
            if (this.mpMusic != null) {
                mpMusic.seekTo(this.onPauseTime);//中断された位置から再生
                this.mpMusic.start();
                // タイマースケジュールを設定
                this.timer.schedule(timerTask, 0, 1000);// (TimerTask,開始時間(long),間隔(1000=1秒,long))
            }
        }
        this.onPauseFlg = false;
        this.onPausePlaying = false;
        this.onPauseTime = 0;
    }

    //バックボタンが押された時の処理変更
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("minの音楽プレーヤーを終了しますか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mpAndTimerStop();
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
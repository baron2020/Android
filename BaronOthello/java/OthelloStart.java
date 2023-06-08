package com.baron.baronothello;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;

public class OthelloStart extends AppCompatActivity {
    final List<Map> allGameRecord = new ArrayList<Map>();//棋譜保存用(データ型)
    final List<String> kihuArray = new ArrayList<String>();//棋譜保存用(文字列型)
    final Map<String, String> gameRecord = new HashMap<String, String>();//盤面情報
    private String[] gameRecordKeys;
    int[] masuIdArray;

    ImageView tebanImage;//手番画像
    private String playerBW;//playerは白か黒か？
    private String comBW;//comは白か黒か？
    private String teban;//手番
    private String tebanCP;//手番はcomか？playerか？

    private long comTime;//comの時間
    private long playerTime;//playerの時間
    private TimeLimitControl comCountDownTimer;//comカウントダウンタイマー
    private TimeLimitControl playerCountDownTimer;//playerカウントダウンタイマー
    TextView comTimerDisplay;//com時間の表示
    TextView playerTimerDisplay;//player時間の表示
    SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.US);//残り時間を分,秒で表示

    private int blackNum;//黒石
    private int whiteNum;//白石
    private String currentMasu;//クリックしたマス
    private List<String> gouhousyuArray;//合法手配列
    static boolean gameEndFlg;//決着フラグ
    private boolean passFlg;//連続パスフラグ

    MediaPlayer mpSekasitagari = null;//対局中BGMセカシタガリ
    MediaPlayer mpTyakusyu = null;//着手音

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.othello_start);
        mpCreate();//mpの再生準備＆セカシタガリスタート
        getRule();
        setTimer();
        start1();//初期化準備
        changeDesign();//デザイン変更
        startTebanTimer();//黒のタイマースタート
        if (this.teban.equals(this.playerBW)) {
            gouhousyuDisplay();//playerの合法手をセットする。
        } else if (this.teban.equals(this.comBW)) {
            comProcess(1);
        }
    }

    public class TimeLimitControl extends CountDownTimer {
        private Long limitTime;
        private Long interval;
        Context setConText;
        private String targetCP;
        private String playerBWKey;
        private TextView targetTimerDisplay;
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.US);//残り時間を分,秒で表示

        public TimeLimitControl(long millisInFuture, long countDownInterval, Context setConText, String playerBWKey, String targetCP, TextView targetTimerDisplay) {
            super(millisInFuture, countDownInterval);
            this.limitTime = millisInFuture;
            this.interval = countDownInterval;
            this.setConText = setConText;
            this.playerBWKey = playerBWKey;
            this.targetCP = targetCP;
            this.targetTimerDisplay = targetTimerDisplay;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            this.limitTime = this.limitTime - this.interval;
            String displayTime = dateFormat.format(this.limitTime);
            if (this.limitTime >= 1000) {
                this.targetTimerDisplay.setText(displayTime);
            }
        }

        @Override
        public void onFinish() {
            //時間切れテスト
            //時間が切れた場合の処理
            this.targetTimerDisplay.setText((String) dateFormat.format(0));
            OthelloStart.gameEndFlg = true;
            Toast.makeText(setConText, "時間切れです。", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplication(), OthelloEnd.class);
            Map<String, String> temp = new HashMap<>(gameRecord);
            if (this.targetCP.equals("c")) {
                if (this.playerBWKey.equals("black")) {
                    intent.putExtra("playerBWKey", "black");//プレイヤーの白黒をセット
                    kihuArray.add("白時間切れ");
                    allGameRecord.add(temp);
                } else if (this.playerBWKey.equals("white")) {
                    intent.putExtra("playerBWKey", "white");//プレイヤーの白黒をセット
                    kihuArray.add("黒時間切れ");
                    allGameRecord.add(temp);
                }
            } else if (this.targetCP.equals("p")) {
                if (this.playerBWKey.equals("black")) {
                    intent.putExtra("playerBWKey", "black");//プレイヤーの白黒をセット
                    kihuArray.add("黒時間切れ");
                    allGameRecord.add(temp);
                } else if (this.playerBWKey.equals("white")) {
                    intent.putExtra("playerBWKey", "white");//プレイヤーの白黒をセット
                    kihuArray.add("白時間切れ");
                    allGameRecord.add(temp);
                }
            }
            intent.putExtra("allGameRecordKey", (Serializable) allGameRecord);//棋譜データ型をセット
            intent.putExtra("kihuKey", (Serializable) kihuArray);//棋譜をセット
            startActivity(intent);//画面推移
            finish();
        }
    }

    //データを受け取り、ルール(持ち時間,先後)を設定。
    public void getRule() {
        Intent intent = getIntent();
        String key1 = intent.getStringExtra("timeKey");//"１０分切れ負け", "５分切れ負け", "３分切れ負け"
        String key2 = intent.getStringExtra("bwKey");//"黒(先手)", "白(後手)"
        if (key1.equals("１０分切れ負け")) {
            this.comTime = 600000;
            this.playerTime = 600000;
        } else if (key1.equals("５分切れ負け")) {
            this.comTime = 300000;
            this.playerTime = 300000;
        } else if (key1.equals("３分切れ負け")) {
            this.comTime = 180000;
            this.playerTime = 180000;
        }
        this.teban = "black";
        if (key2.equals("黒")) {
            this.playerBW = "black";
            this.comBW = "white";
            this.tebanCP = "player";
            ((ImageView) findViewById(R.id.player)).setImageResource(R.drawable.black);
            ((ImageView) findViewById(R.id.com)).setImageResource(R.drawable.white);
            Toast.makeText(this, "あなたが先手です。", Toast.LENGTH_LONG).show();
        } else if (key2.equals("白")) {
            this.playerBW = "white";
            this.comBW = "black";
            this.tebanCP = "com";
            ((ImageView) findViewById(R.id.player)).setImageResource(R.drawable.white);
            ((ImageView) findViewById(R.id.com)).setImageResource(R.drawable.black);
            Toast.makeText(this, "あなたが後手です。", Toast.LENGTH_LONG).show();
        }
        this.tebanImage = (ImageView) findViewById(R.id.tebanImage);
    }

    //タイマーの準備。
    public void setTimer() {
        this.comTimerDisplay = findViewById(R.id.comTimer);
        this.playerTimerDisplay = findViewById(R.id.playerTimer);
        this.comCountDownTimer = new TimeLimitControl(this.comTime, 100, this, this.playerBW, "c", this.comTimerDisplay);
        this.playerCountDownTimer = new TimeLimitControl(this.playerTime, 100, this, this.playerBW, "p", this.playerTimerDisplay);
        this.comTimerDisplay.setText(dateFormat.format(this.comTime));
        this.playerTimerDisplay.setText(dateFormat.format(this.comTime));
    }

    //手番のタイマーのスタート。
    public void startTebanTimer() {
        if (this.tebanCP.equals("com")) {
            this.comCountDownTimer.start();//comタイマー
        } else if (this.tebanCP.equals("player")) {
            this.playerCountDownTimer.start();//playerタイマー
        }
    }

    //手番のタイマーの一時停止。
    public void stopTebanTimer() {
        if (this.tebanCP.equals("com")) {
            this.comCountDownTimer.cancel();//comタイマーの停止
        } else if (this.tebanCP.equals("player")) {
            this.playerCountDownTimer.cancel();//playerタイマーの停止
        }
    }

    //デザイン変更
    public void changeDesign() {
        Point screenSize = getDisplaySize();//画面サイズの取得
        int[] targetArray = new int[]{
                R.id.d1s1, R.id.d1s2, R.id.d1s3, R.id.d1s4, R.id.d1s5, R.id.d1s6, R.id.d1s7, R.id.d1s8,
                R.id.d2s1, R.id.d2s2, R.id.d2s3, R.id.d2s4, R.id.d2s5, R.id.d2s6, R.id.d2s7, R.id.d2s8,
                R.id.d3s1, R.id.d3s2, R.id.d3s3, R.id.d3s4, R.id.d3s5, R.id.d3s6, R.id.d3s7, R.id.d3s8,
                R.id.d4s1, R.id.d4s2, R.id.d4s3, R.id.d4s4, R.id.d4s5, R.id.d4s6, R.id.d4s7, R.id.d4s8,
                R.id.d5s1, R.id.d5s2, R.id.d5s3, R.id.d5s4, R.id.d5s5, R.id.d5s6, R.id.d5s7, R.id.d5s8,
                R.id.d6s1, R.id.d6s2, R.id.d6s3, R.id.d6s4, R.id.d6s5, R.id.d6s6, R.id.d6s7, R.id.d6s8,
                R.id.d7s1, R.id.d7s2, R.id.d7s3, R.id.d7s4, R.id.d7s5, R.id.d7s6, R.id.d7s7, R.id.d7s8,
                R.id.d8s1, R.id.d8s2, R.id.d8s3, R.id.d8s4, R.id.d8s5, R.id.d8s6, R.id.d8s7, R.id.d8s8,
                R.id.com, R.id.player, R.id.tebanImage, R.id.changeBgmImage, R.id.passImage, R.id.surrenderImage
        };
        ImageView[] masuImageViewArray = new ImageView[targetArray.length];
        for (int i = 0; i < targetArray.length; i++) {
            masuImageViewArray[i] = (ImageView) findViewById(targetArray[i]);
        }
        LinearLayout mainArea = (LinearLayout) findViewById(R.id.mainArea);//盤周り
        TableLayout board = (TableLayout) findViewById(R.id.board);//盤
        TextView boardTop = (TextView) findViewById(R.id.boardTop);
        TextView boardBottom = (TextView) findViewById(R.id.boardBottom);
        //盤周り,タイマー関連のデザイン調整
        TextView[] textViewArray = {
                (TextView) findViewById(R.id.comStr),
                (TextView) findViewById(R.id.playerStr),
                (TextView) findViewById(R.id.comStoneNum),
                (TextView) findViewById(R.id.playerStoneNum),
                (TextView) findViewById(R.id.comTimer),
                (TextView) findViewById(R.id.playerTimer),
        };
        LinearLayout comArea = (LinearLayout) findViewById(R.id.comArea);//comArea
        LinearLayout playerArea = (LinearLayout) findViewById(R.id.playerArea);//playerArea
        //右上エリアのデザイン調整
        LinearLayout rightTopArea = (LinearLayout) findViewById(R.id.rightTopArea);//rightTopArea
        //デザイン変更
        ChangeStartDesign cd = new ChangeStartDesign(screenSize, masuImageViewArray, board, boardTop, boardBottom,
                textViewArray, playerArea);//デザイン変更クラスのインスタンス生成
    }

    //画面サイズの取得
    public Point getDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        return screenSize;
    }

    //スタート1
    public void start1() {
        initialize();//初期化
        setUpGameRecord();//盤面情報の初期化
        setUpStone();//初期石の配置
        checkStoneNum();//石の数の確認
        //棋譜保存
        this.kihuArray.add("開始局面");
        Map<String, String> temp = new HashMap<>(this.gameRecord);
        this.allGameRecord.add(temp);
    }

    //初期化
    public void initialize() {
        this.gameRecordKeys = new String[]{
                "d1s1", "d1s2", "d1s3", "d1s4", "d1s5", "d1s6", "d1s7", "d1s8",
                "d2s1", "d2s2", "d2s3", "d2s4", "d2s5", "d2s6", "d2s7", "d2s8",
                "d3s1", "d3s2", "d3s3", "d3s4", "d3s5", "d3s6", "d3s7", "d3s8",
                "d4s1", "d4s2", "d4s3", "d4s4", "d4s5", "d4s6", "d4s7", "d4s8",
                "d5s1", "d5s2", "d5s3", "d5s4", "d5s5", "d5s6", "d5s7", "d5s8",
                "d6s1", "d6s2", "d6s3", "d6s4", "d6s5", "d6s6", "d6s7", "d6s8",
                "d7s1", "d7s2", "d7s3", "d7s4", "d7s5", "d7s6", "d7s7", "d7s8",
                "d8s1", "d8s2", "d8s3", "d8s4", "d8s5", "d8s6", "d8s7", "d8s8"};
        this.masuIdArray = new int[]{
                R.id.d1s1, R.id.d1s2, R.id.d1s3, R.id.d1s4, R.id.d1s5, R.id.d1s6, R.id.d1s7, R.id.d1s8,
                R.id.d2s1, R.id.d2s2, R.id.d2s3, R.id.d2s4, R.id.d2s5, R.id.d2s6, R.id.d2s7, R.id.d2s8,
                R.id.d3s1, R.id.d3s2, R.id.d3s3, R.id.d3s4, R.id.d3s5, R.id.d3s6, R.id.d3s7, R.id.d3s8,
                R.id.d4s1, R.id.d4s2, R.id.d4s3, R.id.d4s4, R.id.d4s5, R.id.d4s6, R.id.d4s7, R.id.d4s8,
                R.id.d5s1, R.id.d5s2, R.id.d5s3, R.id.d5s4, R.id.d5s5, R.id.d5s6, R.id.d5s7, R.id.d5s8,
                R.id.d6s1, R.id.d6s2, R.id.d6s3, R.id.d6s4, R.id.d6s5, R.id.d6s6, R.id.d6s7, R.id.d6s8,
                R.id.d7s1, R.id.d7s2, R.id.d7s3, R.id.d7s4, R.id.d7s5, R.id.d7s6, R.id.d7s7, R.id.d7s8,
                R.id.d8s1, R.id.d8s2, R.id.d8s3, R.id.d8s4, R.id.d8s5, R.id.d8s6, R.id.d8s7, R.id.d8s8};
        this.blackNum = 0;
        this.whiteNum = 0;
        this.currentMasu = "";
        OthelloStart.gameEndFlg = false;
        this.passFlg = false;
    }

    public void setUpGameRecord() {
        //盤面情報の初期化
        for (int i = 0; i < this.gameRecordKeys.length; i++) {
            if ((this.gameRecordKeys[i].equals("d4s4")) || (this.gameRecordKeys[i].equals("d5s5"))) {
                this.gameRecord.put(this.gameRecordKeys[i], "white");
            } else if ((this.gameRecordKeys[i].equals("d4s5")) || (this.gameRecordKeys[i].equals("d5s4"))) {
                this.gameRecord.put(this.gameRecordKeys[i], "black");
            } else {
                this.gameRecord.put(this.gameRecordKeys[i], "None");
            }
        }
    }

    //初期石の配置
    public void setUpStone() {
        ((ImageView) findViewById(R.id.d4s4)).setImageResource(R.drawable.white);
        ((ImageView) findViewById(R.id.d4s5)).setImageResource(R.drawable.black);
        ((ImageView) findViewById(R.id.d5s4)).setImageResource(R.drawable.black);
        ((ImageView) findViewById(R.id.d5s5)).setImageResource(R.drawable.white);
    }

    public void changeTeban() {
        if (this.teban.equals("black")) {
            this.teban = "white";
            this.tebanImage.setImageResource(R.drawable.white);
        } else if (this.teban.equals("white")) {
            this.teban = "black";
            this.tebanImage.setImageResource(R.drawable.black);
        }
        if (this.tebanCP.equals("player")) {
            this.tebanCP = "com";
        } else if (this.tebanCP.equals("com")) {
            this.tebanCP = "player";
        }
    }

    public void checkStoneNum() {
        //石の数の確認し,石の数のテキストを更新する。
        int tempBlackNum = 0;
        int tempWhiteNum = 0;
        for (String val : this.gameRecord.values()) {
            if (val.equals("black")) {
                tempBlackNum++;
            } else if (val.equals("white")) {
                tempWhiteNum++;
            }
        }
        this.blackNum = tempBlackNum;
        this.whiteNum = tempWhiteNum;
        if (this.playerBW.equals("black")) {
            ((TextView) findViewById(R.id.playerStoneNum)).setText(String.valueOf(this.blackNum));//player：黒石
            ((TextView) findViewById(R.id.comStoneNum)).setText(String.valueOf(this.whiteNum));//com：白石
        } else if (this.playerBW.equals("white")) {
            ((TextView) findViewById(R.id.playerStoneNum)).setText(String.valueOf(this.whiteNum));//player：白石
            ((TextView) findViewById(R.id.comStoneNum)).setText(String.valueOf(this.blackNum));//com：黒石
        }
    }

    //ランダム時間を返す。
    public int returnRandomTime() {
        Random rand = new Random();
        int randomTime = (rand.nextInt(2000)) + 1000;//0以上x未満の乱数
        return randomTime;
    }

    //comの全処理
    public void comProcess(int start) {
        int time = 0;
        int intervalTime = 1500;
        if (start == 1) {
            time = 1000;
        } else {
            time = returnRandomTime();//約1~4秒
        }
        //comの着手を返す。
        BaronAI baron = new BaronAI(this.gameRecord, this.comBW);
        String comTyakusyu = baron.baronTyakusyu();
        //合法手がなければパスする
        if (comTyakusyu.equals("パス")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("パスしました。");
            AlertDialog dialog = builder.create();
            dialog.show();
            if (this.passFlg) {
                //連続パスにより終了
                winLoseJudgment(1);
            } else {
                this.kihuArray.add("パス");//棋譜配列に保存
                Map<String, String> temp = new HashMap<>(this.gameRecord);
                this.allGameRecord.add(temp);//棋譜保存２
                setPassFlg(true);
                stopTebanTimer();
                changeTeban();//手番の交代
                startTebanTimer();//playerのタイマーを開始する。
                gouhousyuDisplay();//playerの合法手をセットする。
            }
            return;
        }

        int indexNumber = 0;
        for (int i = 0; i < this.gameRecordKeys.length; i++) {
            if (this.gameRecordKeys[i].equals(comTyakusyu)) {
                indexNumber = i;
                break;
            }
        }
        int targetId = this.masuIdArray[indexNumber];

        //着手1.5秒前に着手ポイントを表示する。
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //ここに実行するコードを記載
                comTyakusyuPoint(targetId);
            }
        }, time);

        //1.5秒後に着手する。
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                com1(targetId);
                com2(comTyakusyu);
            }
        }, time + intervalTime);
    }

    //com着手ポイントの表示
    public void comTyakusyuPoint(int targetId) {
        if (this.comBW.equals("black")) {
            ((ImageView) findViewById(targetId)).setImageResource(R.drawable.black_point);
        } else if (this.comBW.equals("white")) {
            ((ImageView) findViewById(targetId)).setImageResource(R.drawable.white_point);
        }
    }

    //com着手処理1
    public void com1(int targetId) {
        if (this.comBW.equals("black")) {
            ((ImageView) findViewById(targetId)).setImageResource(R.drawable.black);
        } else if (this.comBW.equals("white")) {
            ((ImageView) findViewById(targetId)).setImageResource(R.drawable.white);
        }
        tyakusyuMP3();
    }

    //com着手処理2
    public void com2(String comTyakusyu) {
        setKihuArray(comTyakusyu);//棋譜保存１
        this.gameRecord.put(comTyakusyu, this.comBW);//盤面情報の更新
        turnOverStone(comTyakusyu);//石の反転
        //棋譜保存２
        Map<String, String> temp = new HashMap<>(this.gameRecord);
        this.allGameRecord.add(temp);
        setPassFlg(false);//連続パス判定フラグ
        checkStoneNum();//石の数の更新
        stopTebanTimer();//com着手時間の停止
        winLoseJudgment(0);//決着が着いているか？
        if (gameEndFlg) {
            return;
        } else if (!gameEndFlg) {
            changeTeban();//手番の交代
            startTebanTimer();//playerのタイマーを開始する。
            gouhousyuDisplay();//playerの合法手をセットする。
        }
    }

    public void masuClick(View view) {
        if (gameEndFlg) {
            //決着がついている。
            return;
        }
        if (!(this.teban.equals(this.playerBW))) {
            return;//プレイヤーの手番でなければ無効
        }
        int id = view.getId();//クリックしたマスのid
        ImageView clickImage = (ImageView) findViewById(id);//クリックしたマスのImageView
        setGouhousyuArray();//手番の合法手をセットする。
        if (this.gouhousyuArray.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("合法手がありません。パスしてください。");
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }
        setCurrentMasu(id);
        //合法手なら
        if (this.gouhousyuArray.contains(this.currentMasu)) {
            stopTebanTimer();
            setKihuArray(this.currentMasu);//棋譜保存１
            clearGouhousyuDisplay();//合法手表示をクリアする。
            if (this.teban.equals("black")) {
                clickImage.setImageResource(R.drawable.black);
            } else if (this.teban.equals("white")) {
                clickImage.setImageResource(R.drawable.white);
            }
            tyakusyuMP3();
            this.gameRecord.put(this.currentMasu, this.teban);//盤面情報の更新
            turnOverStone(this.currentMasu);//石の反転
            //棋譜保存２
            Map<String, String> tempP = new HashMap<>(this.gameRecord);
            this.allGameRecord.add(tempP);
            setPassFlg(false);//連続パス判定フラグ
            checkStoneNum();//石の数の更新
            changeTeban();//手番の交代
            winLoseJudgment(0);//決着が着いているか？
            if (gameEndFlg) {
                return;
            } else if (!gameEndFlg) {
                startTebanTimer();
                comProcess(0);//comの全処理
            }
        } else {
            //合法手ではない。
            return;
        }
    }

    //playerの合法手を表示する。
    public void gouhousyuDisplay() {
        setGouhousyuArray();
        if (this.gouhousyuArray.size() == 0) {
            return;
        } else {
            for (int i = 0; i < this.gouhousyuArray.size(); i++) {
                for (int j = 0; j < this.gameRecordKeys.length; j++) {
                    if (this.gouhousyuArray.get(i).equals(this.gameRecordKeys[j])) {
                        if (this.playerBW.equals("black")) {
                            ((ImageView) findViewById(this.masuIdArray[j])).setImageResource(R.drawable.black_point);
                        } else if (this.playerBW.equals("white")) {
                            ((ImageView) findViewById(this.masuIdArray[j])).setImageResource(R.drawable.white_point);
                        }
                    }
                }
            }
        }
    }

    //playerの合法手をクリアする。
    public void clearGouhousyuDisplay() {
        setGouhousyuArray();
        if (this.gouhousyuArray.size() == 0) {
            return;
        } else {
            for (int i = 0; i < this.gouhousyuArray.size(); i++) {
                for (int j = 0; j < this.gameRecordKeys.length; j++) {
                    if (this.gouhousyuArray.get(i).equals(this.gameRecordKeys[j])) {
                        ((ImageView) findViewById(this.masuIdArray[j])).setImageResource(R.drawable.green);
                    }
                }
            }
        }
    }

    public void setGouhousyuArray() {
        //手番の合法手をセットする。
        //8方向探索用配列
        int[][] allDirectionArray = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1},
                {-1, -1}};//8方向(上,右上,右,右下,下,左下,左,左上)
        List<String> tempGouhousyuArray = new ArrayList<String>();//合法手仮格納配列
        String[] switchArray = null;//手番により切り替え
        String[] useBlackArray = {"black", "white"};//手番黒用
        String[] useWhiteArray = {"white", "black"};//手番白用
        int targetDan, targetSuji, checkDan, checkSuji;
        String checkMasu;
        boolean existRivalStoneFlg;//ライバルの石が間に存在するか？
        if (this.gouhousyuArray != null) {
            this.gouhousyuArray.clear();//配列のリセット
        }
        if (this.teban.equals("black")) {
            switchArray = useBlackArray;
        } else if (this.teban.equals("white")) {
            switchArray = useWhiteArray;
        }
        for (int i = 0; i < gameRecordKeys.length; i++) {
            //gameRecordKeys[i]:合法手確認の対象のマス
            if (!(gameRecord.get(gameRecordKeys[i]).equals("None"))) {
                continue;//合法手確認の対象のマスに石があれば抜ける
            }
            targetDan = Integer.parseInt(this.gameRecordKeys[i].substring(1, 2));//二文字目の段の切り出し
            targetSuji = Integer.parseInt(this.gameRecordKeys[i].substring(3, 4));//四文字目の筋の切り出し
            for (int j = 0; j < allDirectionArray.length; j++) {
                existRivalStoneFlg = false;//ライバルの石が間に存在しないフラグをFalseにする
                checkDan = targetDan;//new Integer()//コピー
                checkSuji = targetSuji;
                while (true) {
                    checkDan += allDirectionArray[j][0];
                    checkSuji += allDirectionArray[j][1];
                    checkMasu = 'd' + String.valueOf(checkDan) + 's' + String.valueOf(checkSuji);
                    if ((checkDan == 0) || (checkSuji == 0) || (checkDan == 9) || (checkSuji == 9)) {
                        break;//盤外であれば抜ける
                    } else {
                        //盤内であれば
                        if (this.gameRecord.get(checkMasu).equals("None")) {
                            break;//一マス先に石がなければ抜ける
                        }
                        if ((existRivalStoneFlg == false) && (this.gameRecord.get(checkMasu).equals(switchArray[0]))) {
                            //[0]:自石
                            break;//#間にライバルの石がない＆一マス先が自石ならぬける
                        }
                        if (this.gameRecord.get(checkMasu).equals(switchArray[1])) {
                            //[1]:ライバルの石
                            existRivalStoneFlg = true;
                            continue;//マスの確認方向を一マス伸ばし処理を続ける
                        }
                        if ((existRivalStoneFlg) && (this.gameRecord.get(checkMasu).equals(switchArray[0]))) {
                            //[0]:自石
                            tempGouhousyuArray.add(this.gameRecordKeys[i]);//合法手を配列に格納
                            existRivalStoneFlg = false;//フラグをFalseに戻す
                            break;//ループを抜ける
                        }
                    }
                }
            }
        }
        this.gouhousyuArray = new ArrayList<String>(new HashSet<>(tempGouhousyuArray));//配列から重複した値を削除する
    }

    public void turnOverStone(String startingPoint) {
        //着手＆石を反転させる。
        //startingPoint:着手を起点にする。
        //8方向探索用配列
        int[][] allDirectionArray = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1},
                {-1, -1}};//8方向(上,右上,右,右下,下,左下,左,左上)
        List<String> turnOverStoneArray = new ArrayList<String>();//反転対象配列
        boolean turnOverFlg;//反転動作確認に使用
        String[] switchArray = null;//手番により切り替え
        String[] useBlackArray = {"black", "white"};//手番黒用
        String[] useWhiteArray = {"white", "black"};//手番白用
        int targetDan, targetSuji, checkDan, checkSuji, targetX, targetY;
        String checkMasu;
        if (this.teban.equals("black")) {
            switchArray = useBlackArray;
        } else if (this.teban.equals("white")) {
            switchArray = useWhiteArray;
        }
        //石の反転
        targetDan = Integer.parseInt(startingPoint.substring(1, 2));//二文字目の段の切り出し
        targetSuji = Integer.parseInt(startingPoint.substring(3, 4));//四文字目の筋の切り出し
        for (int j = 0; j < allDirectionArray.length; j++) {
            turnOverFlg = false;//反転動作確認に使用
            checkDan = targetDan;
            checkSuji = targetSuji;
            while (true) {
                checkDan += allDirectionArray[j][0];
                checkSuji += allDirectionArray[j][1];
                checkMasu = 'd' + String.valueOf(checkDan) + 's' + String.valueOf(checkSuji);
                if ((checkDan == 0) || (checkSuji == 0) || (checkDan == 9) || (checkSuji == 9)) {
                    turnOverStoneArray.clear();//配列のリセット
                    break;//盤外であれば抜ける
                }
                //盤内であれば
                if (this.gameRecord.get(checkMasu).equals("None")) {
                    turnOverStoneArray.clear();
                    break;//一マス先に石がなければ抜ける
                }
                if ((turnOverFlg == false) && (this.gameRecord.get(checkMasu).equals(switchArray[0]))) {
                    //[0]:自石
                    turnOverStoneArray.clear();
                    break;//間にライバルの石がない＆一マス先が自石ならぬける
                }
                if (this.gameRecord.get(checkMasu).equals(switchArray[1])) {
                    //[1]:ライバルの石
                    turnOverFlg = true;
                    turnOverStoneArray.add(checkMasu);//反転対象の石が置かれているマスを配列に格納する
                    continue;//マスの確認方向を一マス伸ばし処理を続ける
                }
                if ((turnOverFlg) && (this.gameRecord.get(checkMasu).equals(switchArray[0]))) {
                    //[0]:自石
                    //配列をもとに反転させる
                    //System.out.println("反転対象配列：" + turnOverStoneArray);
                    for (int i = 0; i < turnOverStoneArray.size(); i++) {
                        int targetMasuId = returnTargetId(turnOverStoneArray.get(i));//反転対象のマスのid
                        if (this.teban.equals("black")) {
                            ((ImageView) findViewById(targetMasuId)).setImageResource(R.drawable.black);
                        } else if (this.teban.equals("white")) {
                            ((ImageView) findViewById(targetMasuId)).setImageResource(R.drawable.white);
                        }
                        this.gameRecord.put(turnOverStoneArray.get(i), this.teban);//盤面情報の更新
                    }
                    turnOverFlg = false;//フラグをFalseに戻す
                    break;//ループを抜ける
                }
            }
        }
        return;
    }

    //this.currentMasuに着手をセット
    public void setCurrentMasu(int targetId) {
        int indexNumber = 0;
        for (int i = 0; i < this.masuIdArray.length; i++) {
            if (this.masuIdArray[i] == (targetId)) {
                indexNumber = i;
                break;
            }
        }
        this.currentMasu = this.gameRecordKeys[indexNumber];
        ;
    }

    public int returnTargetId(String targetMasu) {
        int indexNumber = 0;
        for (int i = 0; i < this.gameRecordKeys.length; i++) {
            if (this.gameRecordKeys[i].equals(targetMasu)) {
                indexNumber = i;
                break;
            }
        }
        return this.masuIdArray[indexNumber];
    }

    //棋譜変換
    public String returnKihu(String targetMasu) {
        String kihu = "";
        int indexNumber = 0;
        String[] playerBlackArray = {
                "A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1",
                "A2", "B2", "C2", "D2", "E2", "F2", "G2", "H2",
                "A3", "B3", "C3", "D3", "E3", "F3", "G3", "H3",
                "A4", "B4", "C4", "D4", "E4", "F4", "G4", "H4",
                "A5", "B5", "C5", "D5", "E5", "F5", "G5", "H5",
                "A6", "B6", "C6", "D6", "E6", "F6", "G6", "H6",
                "A7", "B7", "C7", "D7", "E7", "F7", "G7", "H7",
                "A8", "B8", "C8", "D8", "E8", "F8", "G8", "H8"
        };
        String[] playerWhiteArray = {
                "H8", "G8", "F8", "E8", "D8", "C8", "B8", "A8",
                "H7", "G7", "F7", "E7", "D7", "C7", "B7", "A7",
                "H6", "G6", "F6", "E6", "D6", "C6", "B6", "A6",
                "H5", "G5", "F5", "E5", "D5", "C5", "B5", "A5",
                "H4", "G4", "F4", "E4", "D4", "C4", "B4", "A4",
                "H3", "G3", "F3", "E3", "D3", "C3", "B3", "A3",
                "H2", "G2", "F2", "E2", "D2", "C2", "B2", "A2",
                "H1", "G1", "F1", "E1", "D1", "C1", "B1", "A1",
        };
        for (int i = 0; i < this.gameRecordKeys.length; i++) {
            if (this.gameRecordKeys[i].equals(targetMasu)) {
                indexNumber = i;
                break;
            }
        }
        if (this.playerBW.equals("black")) {
            kihu = playerBlackArray[indexNumber];
        } else if (this.playerBW.equals("white")) {
            kihu = playerWhiteArray[indexNumber];
        }
        return kihu;
    }

    //棋譜配列に保存する
    public void setKihuArray(String targetMasu) {
        String addkihu = returnKihu(targetMasu);
        this.kihuArray.add(addkihu);//棋譜配列に保存
    }

    public void winLoseJudgment(int endNumber) {
        //着手完了後に、勝敗が着いているか調べる。勝敗が着いている場合は、手番テキストを更新し、終了フラグを立てる。
        //endNumber==1:連続パスによる終了
        //endNumber==50:時間切れ
        //endNumber==100:投了
        //盤面に石の置ける場所がない。又は、石の数が0。であれば終了
        int tempNoneNum = 0;
        int tempBlackNum = 0;
        int tempWhiteNum = 0;
        for (int i = 0; i < this.gameRecordKeys.length; i++) {
            if (this.gameRecord.get(this.gameRecordKeys[i]).equals("None")) {
                tempNoneNum++;
            } else if (this.gameRecord.get(this.gameRecordKeys[i]).equals("black")) {
                tempBlackNum++;
            } else if (this.gameRecord.get(this.gameRecordKeys[i]).equals("white")) {
                tempWhiteNum++;
            }
        }
        if ((endNumber == 1) || (endNumber == 50) || (endNumber == 100) || (tempNoneNum == 0) || (tempBlackNum == 0) || (tempWhiteNum == 0)) {
            gameEndFlg = true;//決着フラグ
        } else {
            return;
        }
        if (gameEndFlg) {
            this.comCountDownTimer.cancel();//comタイマーの停止
            this.playerCountDownTimer.cancel();//playerタイマーの停止
            Map<String, String> temp = new HashMap<>(this.gameRecord);
            if (endNumber == 1) {
                //連続パスにより終了
                this.kihuArray.add("連続パス");//棋譜保存１
                this.allGameRecord.add(temp);//棋譜保存２
            }
            if (endNumber == 100) {
                //投了により終了
                this.kihuArray.add("投了");//棋譜保存１
                this.allGameRecord.add(temp);//棋譜保存２
                if (this.teban.equals("black")) {
                    this.kihuArray.add("まで白の勝ち");
                    Toast.makeText(this, "白の勝ちです。お疲れ様でした(*_ _)", Toast.LENGTH_LONG).show();
                } else if (this.teban.equals("white")) {
                    this.kihuArray.add("まで黒の勝ち");
                    Toast.makeText(this, "黒の勝ちです。お疲れ様でした(*_ _)", Toast.LENGTH_LONG).show();
                }
                this.allGameRecord.add(temp);//棋譜保存２
                intentOthelloEnd();
                return;
            }
            if ((tempWhiteNum == 0) || (tempBlackNum > tempWhiteNum)) {
                this.kihuArray.add("まで黒の勝ち");
                this.allGameRecord.add(temp);//棋譜保存２
                Toast.makeText(this, "黒の勝ちです。お疲れ様でした(*_ _)", Toast.LENGTH_LONG).show();
                intentOthelloEnd();
                return;
            }
            if ((tempBlackNum == 0) || (tempBlackNum < tempWhiteNum)) {
                this.kihuArray.add("まで白の勝ち");
                this.allGameRecord.add(temp);//棋譜保存２
                Toast.makeText(this, "白の勝ちです。お疲れ様でした(*_ _)", Toast.LENGTH_LONG).show();
                intentOthelloEnd();
                return;
            }
            if (tempBlackNum == tempWhiteNum) {
                this.kihuArray.add("まで引き分け");
                this.allGameRecord.add(temp);//棋譜保存２
                Toast.makeText(this, "引き分けです。お疲れ様でした(*_ _)", Toast.LENGTH_LONG).show();
                intentOthelloEnd();
                return;
            }
        }
    }

    //時間切れ処理
    public void timeOver() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("時間切れです。");
        AlertDialog dialog = builder.create();
        dialog.show();
        this.gameEndFlg = true;
    }

    //BGMのON,OFF
    boolean bgmFlg = true;

    public void bgmOnOff(View view) {
        int id = view.getId();//クリックしたマスのid
        ImageView bgmImage = (ImageView) findViewById(id);//bgmImageView
        if (bgmFlg == false) {
            //BGMなし
            mpSekasitagari.start();
            bgmImage.setImageResource(R.drawable.offbgm);
            bgmFlg = true;
        } else if (bgmFlg) {
            //BGMあり
            mpSekasitagari.pause();//mp.stop();
            bgmImage.setImageResource(R.drawable.onbgm);
            bgmFlg = false;
        }
    }

    //手番表示
    public void tebanShow(View view) {
        if (this.gameEndFlg) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (this.teban.equals("black")) {
            builder.setMessage("黒の手番です。");
        } else if (this.teban.equals("white")) {
            builder.setMessage("白の手番です。");
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void passButtonEvent(View view) {
        if (this.gameEndFlg) {
            return;
        }
        if (!(this.teban.equals(this.playerBW))) {
            return;//プレイヤーの手番でなければ無効
        }
        setGouhousyuArray();
        if (this.gouhousyuArray.size() != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("合法手があります。パス出来ません。");
            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        } else {
            //パスしました。
            if (this.passFlg) {
                //連続パスにより終了
                winLoseJudgment(1);
            } else {
                this.kihuArray.add("パス");//棋譜配列に保存
                Map<String, String> temp = new HashMap<>(this.gameRecord);
                this.allGameRecord.add(temp);//棋譜保存２
                setPassFlg(true);
                stopTebanTimer();
                changeTeban();
                startTebanTimer();
                comProcess(0);//comの全処理
            }
        }
    }

    public void surrenderButtonEvent(View view) {
        if (this.gameEndFlg) {
            return;
        }
        if (!(this.teban.equals(this.playerBW))) {
            return;//プレイヤーの手番でなければ無効
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("投了しますか？");
        AlertDialog.Builder standbyBuilder = new AlertDialog.Builder(this);
        standbyBuilder.setMessage("投了しました。");
        builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setGameEndFlg(true);
                AlertDialog standbyDialog = standbyBuilder.create();
                standbyDialog.show();
                winLoseJudgment(100);
                return;
            }
        });
        builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void intentOthelloEnd() {
        mpSekasitagari.pause();
        if (this.mpTyakusyu.isPlaying()) {
            this.mpTyakusyu.pause();
        }
        Intent intent = new Intent(getApplication(), OthelloEnd.class);
        if (this.playerBW.equals("black")) {
            intent.putExtra("playerBWKey", "black");//プレイヤーの白黒をセット
        } else if (this.playerBW.equals("white")) {
            intent.putExtra("playerBWKey", "white");//プレイヤーの白黒をセット
        }
        intent.putExtra("allGameRecordKey", (Serializable) this.allGameRecord);//棋譜データ型をセット
        intent.putExtra("kihuKey", (Serializable) this.kihuArray);//棋譜をセット
        startActivity(intent);//画面推移
        finish();//activity_main画面を終了し、バックキーで戻らせない。
    }

    //着手音
    public void tyakusyuMP3() {
        mpTyakusyu.start();
    }

    public boolean getPassFlg() {
        return this.passFlg;
    }

    public void setGameEndFlg(boolean gameEndFlg) {
        this.gameEndFlg = gameEndFlg;
    }

    public void setPassFlg(boolean passFlg) {
        this.passFlg = passFlg;
    }

    //mpの再生準備
    public void mpCreate() {
        this.mpSekasitagari = MediaPlayer.create(this, R.raw.sekasitagari);
        this.mpTyakusyu = MediaPlayer.create(this, R.raw.tyakusyu);
        this.mpSekasitagari.setLooping(true);//ループ設定
        if (mpSekasitagari != null) {
            mpSekasitagari.start();
        }
    }

    //アプリがユーザーから見えなくなった時の処理
    @Override
    //onStop()
    protected void onPause() {
        super.onPause();
        this.mpSekasitagari.pause();//停止//mp.stop();
        if (this.mpTyakusyu.isPlaying()) {
            this.mpTyakusyu.pause();
        }
    }

    //アプリが再開された時の処理
    @Override
    protected void onResume() {
        super.onResume();
        this.mpSekasitagari.start();//再開
    }

    //バックボタンが押された時の処理変更
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("ばろんオセロを終了しますか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mpSekasitagari.pause();//mp.stop();
                        if (mpTyakusyu.isPlaying()) {
                            mpTyakusyu.pause();
                        }
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
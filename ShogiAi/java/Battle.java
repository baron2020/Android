package com.teamshiny.shogiai;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//将棋棋譜並べアプリ
public class Battle extends AppCompatActivity {
    final Map<String, String> banRecordMap = new HashMap<String, String>();//盤面データ
    final Map<String, String> komadaiRecordMap = new HashMap<String, String>();//駒台上下データ
    private String[] banRecordMapKeys;
    private String[] komadaiRecordMapKeys;
    private List<String> komadaiTopList = new ArrayList<String>();//駒台(上)(文字列型)
    private List<String> komadaiBottomList = new ArrayList<String>();//駒台(下)(文字列型)
    private String[] masuNameArray;//マス名前配列
    int[] masuIdArray;//マスid配列
    ImageView[] masuImageArray;//マス配列
    int[] numIdArray;//駒台枚数id配列
    ImageView[] numImageArray;//駒台枚数配列
    MediaPlayer mpKomaoto = null;//着手音
    private com.teamshiny.shogiai.AI aiClassifier;//将棋AI
    private String teban = "";//手番
    private boolean gameEndFlg = false;//ゲーム終了フラグ,お疲れでしたm(_ _)m

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.battle);
        this.mpKomaoto = MediaPlayer.create(this, R.raw.komaoto);
        getRule();
        syokika();//初期化
        gameDataSyokika();//盤面,駒台の初期化
        changeDesign();//デザイン変更
        gameRecordPlayBack();//盤面,駒台の再生
        if (this.teban.equals("com")) {
            comProcess();
        }
    }

    //データを受け取り、ルール(持ち時間,先後)を設定。
    public void getRule() {
        Intent intent = getIntent();
        String key1 = intent.getStringExtra("tebanKey");//"先手", "後手"
        this.teban = "あなた";
        if (key1.equals("先手")) {
            this.teban = "あなた";
            ((ImageView) findViewById(R.id.tebanImage)).setImageResource(R.drawable.anata);
            Toast.makeText(this, "あなたが先手です。", Toast.LENGTH_LONG).show();
        } else if (key1.equals("後手")) {
            this.teban = "com";
            ((ImageView) findViewById(R.id.tebanImage)).setImageResource(R.drawable.com);
            Toast.makeText(this, "あなたが後手です。", Toast.LENGTH_LONG).show();
        }
    }

    //comの全処理
    public void comProcess() {
        int time = 1000;
        //着手1.5秒前に着手ポイントを表示する。
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //ここに実行するコードを記載
                processAI();
            }
        }, time);
    }

    String toukaAIMasu = "";//AIの着手したマス

    //AIの着手をまとめた処理
    public void processAI() {
        guiGouhousyu();
        aiClassifier = new AI(this, hantenData(banRecordMap), komadaiBottomList, komadaiTopList);
        int digit = aiClassifier.classify();//Log.i("baron", "digit :  " + digit);
        List<Integer> maxLabel20 = aiClassifier.getMaxIndex20List(); //着手ラベル20(高い順)
        List<Float> maxValue20 = aiClassifier.getMaxValue20List();//確率20(高い順)
        BaronAI baron = new BaronAI(hantenData(banRecordMap), komadaiTopList, maxLabel20, maxValue20);
        String finalAI = baron.returnFinalAI();//GUIから合法手を取得
        toukaKaijyo(toukaAIMasu);
        if (finalAI.equals("負けました")) {
            this.gameEndFlg = true;//ゲーム終了フラグ
            ((TextView) findViewById(R.id.gameEndText)).setText("お疲れでした(*_ _)");
            return;
        } else {
            com(finalAI);
            changeTeban();
            touka(toukaAIMasu);
            checkGameEnd();
        }
    }

    //盤面に、王,玉が無ければ終了する
    public void checkGameEnd() {
        boolean endFlg = true;
        for (int i = 0; i < this.banRecordMapKeys.length; i++) {
            if (this.banRecordMap.get(this.banRecordMapKeys[i]).equals("OU")) {
                endFlg = false;//"王の位置:  " + this.banRecordMapKeys[i]
                break;
            }
        }
        if (endFlg) {
            this.gameEndFlg = true;//ゲーム終了フラグ
            ((TextView) findViewById(R.id.gameEndText)).setText("お疲れでした(*_ _)");
            return;
        }
        for (int i = 0; i < this.banRecordMapKeys.length; i++) {
            if (this.banRecordMap.get(this.banRecordMapKeys[i]).equals("gy")) {
                endFlg = false;
                break;
            }
        }
        if (endFlg) {
            this.gameEndFlg = true;//ゲーム終了フラグ
            ((TextView) findViewById(R.id.gameEndText)).setText("お疲れでした(*_ _)");
            return;
        }
    }

    //AI着手
    public void com(String targetData) {
        String endMasu = targetData.substring(0, 4); //移動後のマス(左から4文字切り出し)
        String piece = targetData.substring(4, 6); //駒
        String startMasu = targetData.substring(6, 10); //移動前のマス
        int nari;
        if (targetData.substring(targetData.length() - 1).equals("ま")) {
            nari = 0;
        } else {
            nari = Integer.parseInt(targetData.substring(targetData.length() - 1));//末尾を取得
        }
        com1(endMasu, startMasu, piece, nari);
    }

    //AI着手
    public void com1(String endMasu, String startMasu, String piece, int nari) {
        String[] pieceArray1 = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "TO", "NY", "NK", "NG", "UM", "RY"};
        String[] pieceArray2 = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "to", "ny", "nk", "ng", "um", "ry"};
        String[] gotePieceArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "OU", "fu", "ky", "ke", "gi", "ka", "hi"};
        String movePiece = pieceArray2[Arrays.asList(pieceArray1).indexOf(piece)];
        String moveEnd = hantenMasu(endMasu);
        if (startMasu.equals("もちごま")) {
            //持ち駒使用
            deleteKomadaiPiece(this.komadaiTopList, movePiece); //駒台の駒を削除(駒台上リストから削除)
            this.banRecordMap.put(moveEnd, movePiece);//追加
            updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
            updateTargetMasuDisplay(moveEnd);//指定したマスのデータを反映して表示する。
            this.mpKomaoto.start();
            toukaAIMasu = moveEnd;
            return;
        } else {
            String moveStart = hantenMasu(startMasu);
            if (nari == 1) {
                //成り
                String[] promotionNameArray1 = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy"};
                String[] promotionNameArray2 = {"to", "ny", "nk", "ng", "ki", "um", "ry", "gy"};
                int index = Arrays.asList(promotionNameArray1).indexOf(movePiece);
                movePiece = promotionNameArray2[index];
            }
            this.banRecordMap.put(moveStart, "None");//削除
            if (this.banRecordMap.get(moveEnd).equals("None")) {
                //駒をとっていない
                this.banRecordMap.put(moveEnd, movePiece);//追加
                updateTargetMasuDisplay(moveStart);
                updateTargetMasuDisplay(moveEnd);
                this.mpKomaoto.start();
            } else {
                //駒をとった
                String getPiece = gotePieceArray[Arrays.asList(pieceArray1).indexOf(this.banRecordMap.get(moveEnd))];
                this.komadaiTopList.add(getPiece);//後手の駒台に追加
                this.banRecordMap.put(moveEnd, movePiece);//追加
                updateTargetMasuDisplay(moveStart);
                updateTargetMasuDisplay(moveEnd);
                updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
                this.mpKomaoto.start();
            }
        }
        toukaAIMasu = moveEnd;
    }

    //マスを反転して返す
    public String hantenMasu(String targetMasu) {
        String[] keys = {
                "d9s9", "d9s8", "d9s7", "d9s6", "d9s5", "d9s4", "d9s3", "d9s2", "d9s1",
                "d8s9", "d8s8", "d8s7", "d8s6", "d8s5", "d8s4", "d8s3", "d8s2", "d8s1",
                "d7s9", "d7s8", "d7s7", "d7s6", "d7s5", "d7s4", "d7s3", "d7s2", "d7s1",
                "d6s9", "d6s8", "d6s7", "d6s6", "d6s5", "d6s4", "d6s3", "d6s2", "d6s1",
                "d5s9", "d5s8", "d5s7", "d5s6", "d5s5", "d5s4", "d5s3", "d5s2", "d5s1",
                "d4s9", "d4s8", "d4s7", "d4s6", "d4s5", "d4s4", "d4s3", "d4s2", "d4s1",
                "d3s9", "d3s8", "d3s7", "d3s6", "d3s5", "d3s4", "d3s3", "d3s2", "d3s1",
                "d2s9", "d2s8", "d2s7", "d2s6", "d2s5", "d2s4", "d2s3", "d2s2", "d2s1",
                "d1s9", "d1s8", "d1s7", "d1s6", "d1s5", "d1s4", "d1s3", "d1s2", "d1s1",
        };
        int index = Arrays.asList(keys).indexOf(targetMasu);
        return this.banRecordMapKeys[index];
    }

    //AI着手
    public void guiGouhousyu() {
        List<String> guiGouhousyuList = new ArrayList<String>();//GUIから合法手を検索して格納
        String[] keys = {
                "d1s1", "d1s2", "d1s3", "d1s4", "d1s5", "d1s6", "d1s7", "d1s8", "d1s9",
                "d2s1", "d2s2", "d2s3", "d2s4", "d2s5", "d2s6", "d2s7", "d2s8", "d2s9",
                "d3s1", "d3s2", "d3s3", "d3s4", "d3s5", "d3s6", "d3s7", "d3s8", "d3s9",
                "d4s1", "d4s2", "d4s3", "d4s4", "d4s5", "d4s6", "d4s7", "d4s8", "d4s9",
                "d5s1", "d5s2", "d5s3", "d5s4", "d5s5", "d5s6", "d5s7", "d5s8", "d5s9",
                "d6s1", "d6s2", "d6s3", "d6s4", "d6s5", "d6s6", "d6s7", "d6s8", "d6s9",
                "d7s1", "d7s2", "d7s3", "d7s4", "d7s5", "d7s6", "d7s7", "d7s8", "d7s9",
                "d8s1", "d8s2", "d8s3", "d8s4", "d8s5", "d8s6", "d8s7", "d8s8", "d8s9",
                "d9s1", "d9s2", "d9s3", "d9s4", "d9s5", "d9s6", "d9s7", "d9s8", "d9s9"
        };
        String[] gotePieceNameArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "fu", "ky", "ke", "gi", "ka", "hi"};
        //上、左上、右上、左、右、下、左下、右下、左桂、右桂
        int[][] pieceMotionYX = new int[][]{
                {1, 0}, {1, 1}, {1, -1}, {0, 1}, {0, -1}, {-1, 0}, {-1, 1}, {-1, -1}, {2, 1}, {2, -1},
                {1, 0}, {1, 1}, {1, -1}, {0, 1}, {0, -1}, {-1, 0}, {-1, 1}, {-1, -1}, {2, 1}, {2, -1}
        };

        for (int i = 0; i < keys.length; i++) {
            if (Arrays.asList(gotePieceNameArray).indexOf(banRecordMap.get(keys[i])) != -1) {
                guiGouhousyuList.add(keys[i]);
            }
        }
    }

    //着手しやすい形に変換
    private void changeTeban() {
        if (this.teban.equals("あなた")) {
            this.teban = "com";
            ((ImageView) findViewById(R.id.tebanImage)).setImageResource(R.drawable.com);
        } else if (this.teban.equals("com")) {
            this.teban = "あなた";
            ((ImageView) findViewById(R.id.tebanImage)).setImageResource(R.drawable.anata);
        }
    }

    //盤面をひっくり返す
    private Map<String, String> hantenData(Map<String, String> targetMap) {
        Map<String, String> hantenMap = new HashMap<String, String>();//反転した将棋盤情報
        String[] keys = {
                "d1s1", "d1s2", "d1s3", "d1s4", "d1s5", "d1s6", "d1s7", "d1s8", "d1s9",
                "d2s1", "d2s2", "d2s3", "d2s4", "d2s5", "d2s6", "d2s7", "d2s8", "d2s9",
                "d3s1", "d3s2", "d3s3", "d3s4", "d3s5", "d3s6", "d3s7", "d3s8", "d3s9",
                "d4s1", "d4s2", "d4s3", "d4s4", "d4s5", "d4s6", "d4s7", "d4s8", "d4s9",
                "d5s1", "d5s2", "d5s3", "d5s4", "d5s5", "d5s6", "d5s7", "d5s8", "d5s9",
                "d6s1", "d6s2", "d6s3", "d6s4", "d6s5", "d6s6", "d6s7", "d6s8", "d6s9",
                "d7s1", "d7s2", "d7s3", "d7s4", "d7s5", "d7s6", "d7s7", "d7s8", "d7s9",
                "d8s1", "d8s2", "d8s3", "d8s4", "d8s5", "d8s6", "d8s7", "d8s8", "d8s9",
                "d9s1", "d9s2", "d9s3", "d9s4", "d9s5", "d9s6", "d9s7", "d9s8", "d9s9"
        };
        String[] sentePieceList = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "TO", "NY", "NK", "NG", "UM", "RY"};
        String[] gotePieceList = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "to", "ny", "nk", "ng", "um", "ry"};
        //反転データの挿入
        for (int i = 0; i < keys.length; i++) {
            if (targetMap.get(keys[i]).equals("None")) {
                hantenMap.put(keys[80 - i], "None");
            } else {
                int index = 0;
                //駒がある
                if (Arrays.asList(sentePieceList).indexOf(targetMap.get(keys[i])) != -1) {
                    //含まれている
                    index = Arrays.asList(sentePieceList).indexOf(targetMap.get(keys[i]));
                    hantenMap.put(keys[80 - i], gotePieceList[index]);
                    //return;
                } else if (Arrays.asList(gotePieceList).indexOf(targetMap.get(keys[i])) != -1) {
                    //含まれている
                    index = Arrays.asList(gotePieceList).indexOf(targetMap.get(keys[i]));
                    hantenMap.put(keys[80 - i], sentePieceList[index]);
                }
            }
        }
        return hantenMap;
    }

    //初期化
    public void syokika() {
        this.banRecordMapKeys = new String[]{
                "d1s1", "d1s2", "d1s3", "d1s4", "d1s5", "d1s6", "d1s7", "d1s8", "d1s9",
                "d2s1", "d2s2", "d2s3", "d2s4", "d2s5", "d2s6", "d2s7", "d2s8", "d2s9",
                "d3s1", "d3s2", "d3s3", "d3s4", "d3s5", "d3s6", "d3s7", "d3s8", "d3s9",
                "d4s1", "d4s2", "d4s3", "d4s4", "d4s5", "d4s6", "d4s7", "d4s8", "d4s9",
                "d5s1", "d5s2", "d5s3", "d5s4", "d5s5", "d5s6", "d5s7", "d5s8", "d5s9",
                "d6s1", "d6s2", "d6s3", "d6s4", "d6s5", "d6s6", "d6s7", "d6s8", "d6s9",
                "d7s1", "d7s2", "d7s3", "d7s4", "d7s5", "d7s6", "d7s7", "d7s8", "d7s9",
                "d8s1", "d8s2", "d8s3", "d8s4", "d8s5", "d8s6", "d8s7", "d8s8", "d8s9",
                "d9s1", "d9s2", "d9s3", "d9s4", "d9s5", "d9s6", "d9s7", "d9s8", "d9s9"
        };
        this.komadaiRecordMapKeys = new String[]{
                "k1", "k2", "k3", "k4", "k5", "k6", "k7", "k8", "k9",
                "k11", "k12", "k13", "k14", "k15", "k16", "k17", "k18", "k19"
        };
        this.masuNameArray = new String[]{
                "d1s1", "d1s2", "d1s3", "d1s4", "d1s5", "d1s6", "d1s7", "d1s8", "d1s9",
                "d2s1", "d2s2", "d2s3", "d2s4", "d2s5", "d2s6", "d2s7", "d2s8", "d2s9",
                "d3s1", "d3s2", "d3s3", "d3s4", "d3s5", "d3s6", "d3s7", "d3s8", "d3s9",
                "d4s1", "d4s2", "d4s3", "d4s4", "d4s5", "d4s6", "d4s7", "d4s8", "d4s9",
                "d5s1", "d5s2", "d5s3", "d5s4", "d5s5", "d5s6", "d5s7", "d5s8", "d5s9",
                "d6s1", "d6s2", "d6s3", "d6s4", "d6s5", "d6s6", "d6s7", "d6s8", "d6s9",
                "d7s1", "d7s2", "d7s3", "d7s4", "d7s5", "d7s6", "d7s7", "d7s8", "d7s9",
                "d8s1", "d8s2", "d8s3", "d8s4", "d8s5", "d8s6", "d8s7", "d8s8", "d8s9",
                "d9s1", "d9s2", "d9s3", "d9s4", "d9s5", "d9s6", "d9s7", "d9s8", "d9s9",
                "k1", "k2", "k3", "k4", "k5", "k6", "k7", "k8", "k9",
                "k11", "k12", "k13", "k14", "k15", "k16", "k17", "k18", "k19"
        };
        this.masuIdArray = new int[]{
                R.id.d1s1, R.id.d1s2, R.id.d1s3, R.id.d1s4, R.id.d1s5, R.id.d1s6, R.id.d1s7, R.id.d1s8, R.id.d1s9,
                R.id.d2s1, R.id.d2s2, R.id.d2s3, R.id.d2s4, R.id.d2s5, R.id.d2s6, R.id.d2s7, R.id.d2s8, R.id.d2s9,
                R.id.d3s1, R.id.d3s2, R.id.d3s3, R.id.d3s4, R.id.d3s5, R.id.d3s6, R.id.d3s7, R.id.d3s8, R.id.d3s9,
                R.id.d4s1, R.id.d4s2, R.id.d4s3, R.id.d4s4, R.id.d4s5, R.id.d4s6, R.id.d4s7, R.id.d4s8, R.id.d4s9,
                R.id.d5s1, R.id.d5s2, R.id.d5s3, R.id.d5s4, R.id.d5s5, R.id.d5s6, R.id.d5s7, R.id.d5s8, R.id.d5s9,
                R.id.d6s1, R.id.d6s2, R.id.d6s3, R.id.d6s4, R.id.d6s5, R.id.d6s6, R.id.d6s7, R.id.d6s8, R.id.d6s9,
                R.id.d7s1, R.id.d7s2, R.id.d7s3, R.id.d7s4, R.id.d7s5, R.id.d7s6, R.id.d7s7, R.id.d7s8, R.id.d7s9,
                R.id.d8s1, R.id.d8s2, R.id.d8s3, R.id.d8s4, R.id.d8s5, R.id.d8s6, R.id.d8s7, R.id.d8s8, R.id.d8s9,
                R.id.d9s1, R.id.d9s2, R.id.d9s3, R.id.d9s4, R.id.d9s5, R.id.d9s6, R.id.d9s7, R.id.d9s8, R.id.d9s9,
                R.id.k1, R.id.k2, R.id.k3, R.id.k4, R.id.k5, R.id.k6, R.id.k7, R.id.k8, R.id.k9,
                R.id.k11, R.id.k12, R.id.k13, R.id.k14, R.id.k15, R.id.k16, R.id.k17, R.id.k18, R.id.k19
        };
        this.masuImageArray = new ImageView[this.masuIdArray.length];
        for (int i = 0; i < this.masuIdArray.length; i++) {
            this.masuImageArray[i] = (ImageView) findViewById(this.masuIdArray[i]);
        }
        //駒台枚数表示
        this.numIdArray = new int[]{
                R.id.n1, R.id.n2, R.id.n3, R.id.n4, R.id.n5, R.id.n6, R.id.n7, R.id.n8, R.id.n9,
                R.id.n11, R.id.n12, R.id.n13, R.id.n14, R.id.n15, R.id.n16, R.id.n17, R.id.n18, R.id.n19
        };
        this.numImageArray = new ImageView[this.numIdArray.length];
        for (int i = 0; i < this.numIdArray.length; i++) {
            this.numImageArray[i] = (ImageView) findViewById(this.numIdArray[i]);
        }
    }

    //局面データの初期化
    public void gameDataSyokika() {
        String[] setBoardData = {
                "ky", "ke", "gi", "ki", "gy", "ki", "gi", "ke", "ky",
                "None", "hi", "None", "None", "None", "None", "None", "ka", "None",
                "fu", "fu", "fu", "fu", "fu", "fu", "fu", "fu", "fu",
                "None", "None", "None", "None", "None", "None", "None", "None", "None",
                "None", "None", "None", "None", "None", "None", "None", "None", "None",
                "None", "None", "None", "None", "None", "None", "None", "None", "None",
                "FU", "FU", "FU", "FU", "FU", "FU", "FU", "FU", "FU",
                "None", "KA", "None", "None", "None", "None", "None", "HI", "None",
                "KY", "KE", "GI", "KI", "OU", "KI", "GI", "KE", "KY"
        };
        //盤面,駒台データの初期化
        for (int i = 0; i < this.banRecordMapKeys.length; i++) {
            this.banRecordMap.put(this.banRecordMapKeys[i], setBoardData[i]);
        }
    }

    //局面の再生
    public void gameRecordPlayBack() {
        String[] bottomPieceNameArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "TO", "NY", "NK", "NG", "UM", "RY"};
        String[] topPieceNameArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "to", "ny", "nk", "ng", "um", "ry"};
        //盤上の駒
        int[] banPieceArray = {R.drawable.fu, R.drawable.ky, R.drawable.ke, R.drawable.gi, R.drawable.ki,
                R.drawable.ka, R.drawable.hi, R.drawable.ou,
                R.drawable.to, R.drawable.ny, R.drawable.nk, R.drawable.ng, R.drawable.um, R.drawable.ry
        };
        promotionKeima();//桂馬が強制的に成るマス(１,２段目)(８,９段目)なら成り駒にする。
        promotionFuKyou();//歩,香が強制的に成るマス(１段目)(９段目)なら成り駒にする。
        //盤面の再生
        for (int i = 0; i < this.banRecordMapKeys.length; i++) {
            if (Arrays.asList(bottomPieceNameArray).contains(this.banRecordMap.get(this.banRecordMapKeys[i]))) {
                int index = Arrays.asList(bottomPieceNameArray).indexOf(this.banRecordMap.get(this.banRecordMapKeys[i]));
                ((ImageView) findViewById(this.masuIdArray[i])).setImageResource(banPieceArray[index]);
            } else if (Arrays.asList(topPieceNameArray).contains(this.banRecordMap.get(this.banRecordMapKeys[i]))) {
                int index = Arrays.asList(topPieceNameArray).indexOf(this.banRecordMap.get(this.banRecordMapKeys[i]));
                if (index == 7) {
                    changeImage180((ImageView) findViewById(this.masuIdArray[i]), R.drawable.gy);
                } else {
                    changeImage180((ImageView) findViewById(this.masuIdArray[i]), banPieceArray[index]);
                }
            } else if (this.banRecordMap.get(this.banRecordMapKeys[i]).equals("None")) {
                ((ImageView) findViewById(this.masuIdArray[i])).setImageResource(R.drawable.masu);
            }
        }
        updateMotigomaDisplay(); //持ち駒を更新して表示する。
    }

    //駒台を表示(更新)する。
    public void updateMotigomaDisplay() {
        updateMotigomaData();//持ち駒データを更新。枚数表示。
        //駒台の名前
        String[] topKomadaiPieceNameArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "OU", "gy"};
        String[] bottomKomadaiPieceNameArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "gy"};
        //駒台のid
        int[] komadaIdArray = {R.id.k1, R.id.k2, R.id.k3, R.id.k4, R.id.k5, R.id.k6, R.id.k7, R.id.k8, R.id.k9,
                R.id.k11, R.id.k12, R.id.k13, R.id.k14, R.id.k15, R.id.k16, R.id.k17, R.id.k18, R.id.k19
        };
        //駒台の駒
        int[] komadaiPieceArray = {R.drawable.k_fu, R.drawable.k_ky, R.drawable.k_ke,
                R.drawable.k_gi, R.drawable.k_ki,
                R.drawable.k_ka, R.drawable.k_hi, R.drawable.k_ou, R.drawable.k_gy
        };
        for (int i = 0; i < this.komadaiRecordMapKeys.length; i++) {
            //"k1", "k2", "k3", "k4", "k5", "k6", "k7", "k8", "k9","k11", "k12", "k13", "k14", "k15", "k16", "k17", "k18", "k19"
            if (this.komadaiRecordMap.get(this.komadaiRecordMapKeys[i]).equals("None")) {
                ((ImageView) findViewById(komadaIdArray[i])).setImageResource(R.drawable.komadai);
            } else {
                if (i <= 8) {
                    //上
                    int index = Arrays.asList(topKomadaiPieceNameArray).indexOf(this.komadaiRecordMap.get(this.komadaiRecordMapKeys[i]));
                    changeImage180((ImageView) findViewById(komadaIdArray[i]), komadaiPieceArray[index]);
                } else {
                    //下
                    int index = Arrays.asList(bottomKomadaiPieceNameArray).indexOf(this.komadaiRecordMap.get(this.komadaiRecordMapKeys[i]));
                    ((ImageView) findViewById(komadaIdArray[i])).setImageResource(komadaiPieceArray[index]);
                }
            }
        }
    }

    //持ち駒データを反映させる。
    public void updateMotigomaData() {
        //駒別枚数データ(歩,香,桂,銀,金,角,飛,王,玉)
        int[] komadaiTopNumArray = {0, 0, 0, 0, 0, 0, 0, 0, 0};//"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU","gy"
        int[] komadaiBottomNumArray = {0, 0, 0, 0, 0, 0, 0, 0, 0};//"fu", "ky", "ke", "gi", "ki", "ka", "hi", "OU","gy"
        //駒台(上)
        for (int i = 0; i < this.komadaiTopList.size(); i++) {
            if (this.komadaiTopList.get(i).equals("fu")) {
                komadaiTopNumArray[0] += 1;
            } else if (this.komadaiTopList.get(i).equals("ky")) {
                komadaiTopNumArray[1] += 1;
            } else if (this.komadaiTopList.get(i).equals("ke")) {
                komadaiTopNumArray[2] += 1;
            } else if (this.komadaiTopList.get(i).equals("gi")) {
                komadaiTopNumArray[3] += 1;
            } else if (this.komadaiTopList.get(i).equals("ki")) {
                komadaiTopNumArray[4] += 1;
            } else if (this.komadaiTopList.get(i).equals("ka")) {
                komadaiTopNumArray[5] += 1;
            } else if (this.komadaiTopList.get(i).equals("hi")) {
                komadaiTopNumArray[6] += 1;
            } else if (this.komadaiTopList.get(i).equals("OU")) {
                komadaiTopNumArray[7] += 1;
            } else if (this.komadaiTopList.get(i).equals("gy")) {
                komadaiTopNumArray[8] += 1;
            }
        }
        //駒台(下)
        for (int i = 0; i < this.komadaiBottomList.size(); i++) {
            if (this.komadaiBottomList.get(i).equals("FU")) {
                komadaiBottomNumArray[0] += 1;
            } else if (this.komadaiBottomList.get(i).equals("KY")) {
                komadaiBottomNumArray[1] += 1;
            } else if (this.komadaiBottomList.get(i).equals("KE")) {
                komadaiBottomNumArray[2] += 1;
            } else if (this.komadaiBottomList.get(i).equals("GI")) {
                komadaiBottomNumArray[3] += 1;
            } else if (this.komadaiBottomList.get(i).equals("KI")) {
                komadaiBottomNumArray[4] += 1;
            } else if (this.komadaiBottomList.get(i).equals("KA")) {
                komadaiBottomNumArray[5] += 1;
            } else if (this.komadaiBottomList.get(i).equals("HI")) {
                komadaiBottomNumArray[6] += 1;
            } else if (this.komadaiBottomList.get(i).equals("OU")) {
                komadaiBottomNumArray[7] += 1;
            } else if (this.komadaiBottomList.get(i).equals("gy")) {
                komadaiBottomNumArray[8] += 1;
            }
        }
        //歩,香,桂,銀,金,角,飛,王,玉
        String[] komadaiTopKeys = {"k9", "k8", "k7", "k6", "k5", "k4", "k3", "k2", "k1"};
        String[] komadaiBottomKeys = {"k11", "k12", "k13", "k14", "k15", "k16", "k17", "k18", "k19"};
        String[] komadaiTopValues = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "OU", "gy"};
        String[] komadaiBottomValues = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "gy"};
        int[] numTopId = {R.id.n9, R.id.n8, R.id.n7, R.id.n6, R.id.n5, R.id.n4, R.id.n3, R.id.n2, R.id.n1};
        int[] numBottomId = {R.id.n11, R.id.n12, R.id.n13, R.id.n14, R.id.n15, R.id.n16, R.id.n17, R.id.n18, R.id.n19};
        int[] setNumImageArray = {
                R.drawable.num1, R.drawable.num2, R.drawable.num3, R.drawable.num4, R.drawable.num5, R.drawable.num6,
                R.drawable.num7, R.drawable.num8, R.drawable.num9, R.drawable.num10, R.drawable.num11, R.drawable.num12,
                R.drawable.num13, R.drawable.num14, R.drawable.num15, R.drawable.num16, R.drawable.num17, R.drawable.num18
        };
        int setIndex1 = 0;//配置場所
        int setIndex2 = 0;//配置場所
        for (int i = 0; i < komadaiTopNumArray.length; i++) {
            this.komadaiRecordMap.put(komadaiTopKeys[i], "None");
            ((ImageView) findViewById(numTopId[i])).setImageResource(R.drawable.komadai);
            if (komadaiTopNumArray[i] != 0) {
                this.komadaiRecordMap.put(komadaiTopKeys[setIndex1], komadaiTopValues[i]);
                changeImage180((ImageView) findViewById(numTopId[setIndex1]), setNumImageArray[komadaiTopNumArray[i] - 1]);
                setIndex1++;
            }
        }
        for (int i = 0; i < komadaiBottomNumArray.length; i++) {
            this.komadaiRecordMap.put(komadaiBottomKeys[i], "None");
            ((ImageView) findViewById(numBottomId[i])).setImageResource(R.drawable.komadai);
            if (komadaiBottomNumArray[i] != 0) {
                this.komadaiRecordMap.put(komadaiBottomKeys[setIndex2], komadaiBottomValues[i]);
                ((ImageView) findViewById(numBottomId[setIndex2])).setImageResource(setNumImageArray[komadaiBottomNumArray[i] - 1]);
                setIndex2++;
            }
        }
    }

    //桂馬が強制的に成るマス(１,２段目)(８,９段目)なら成り駒にする。
    public void promotionKeima() {
        //0~17(１,２段目),63~80
        for (int i = 0; i < this.banRecordMapKeys.length; i++) {
            if (i <= 17) {
                if (this.banRecordMap.get(this.banRecordMapKeys[i]).equals("KE")) {
                    this.banRecordMap.put(this.banRecordMapKeys[i], "NK");//削除
                }
            } else if (i >= 63) {
                if (this.banRecordMap.get(this.banRecordMapKeys[i]).equals("ke")) {
                    this.banRecordMap.put(this.banRecordMapKeys[i], "nk");//削除
                }
            }
        }
    }

    //歩,香が強制的に成るマス(１段目)(９段目)なら成り駒にする。
    public void promotionFuKyou() {
        //0~8(１,２段目),72~80
        for (int i = 0; i < this.banRecordMapKeys.length; i++) {
            if (i <= 8) {
                if (this.banRecordMap.get(this.banRecordMapKeys[i]).equals("FU")) {
                    this.banRecordMap.put(this.banRecordMapKeys[i], "TO");//削除
                } else if (this.banRecordMap.get(this.banRecordMapKeys[i]).equals("KY")) {
                    this.banRecordMap.put(this.banRecordMapKeys[i], "NY");//削除
                }
            } else if (i >= 72) {
                if (this.banRecordMap.get(this.banRecordMapKeys[i]).equals("fu")) {
                    this.banRecordMap.put(this.banRecordMapKeys[i], "to");//削除
                } else if (this.banRecordMap.get(this.banRecordMapKeys[i]).equals("ky")) {
                    this.banRecordMap.put(this.banRecordMapKeys[i], "ny");//削除
                }
            }
        }
    }

    //駒の反転
    public void changeImage180(ImageView targetPoint, int targetImageId) {
        Bitmap bitmapOrigin = BitmapFactory.decodeResource(getResources(), targetImageId);
        //画像の横,縦サイズを取得
        int imageWidth = bitmapOrigin.getWidth();
        int imageHeight = bitmapOrigin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(180, imageWidth / 2, imageHeight / 2);
        Bitmap bitmapRotate = Bitmap.createBitmap(bitmapOrigin, 0, 0, imageWidth, imageHeight, matrix, true);
        targetPoint.setImageBitmap(bitmapRotate);
    }

    //マスクリック関連--------------------------------------------------------------------------------
    private String firstClickMasu = "";//最初にクリックしたマス
    private String firstClickPiece = "";//最初にクリックした駒名
    private boolean firstBanInOutFlg = false;//盤上をクリックしたかどうか？(盤上はtrue,駒台はfalse)
    private String secondClickMasu = "";//二回目にクリックしたマス
    private String secondClickPiece = "";//二回目にクリックした駒名
    private boolean secondBanInOutFlg = false;//盤上をクリックしたかどうか？
    private boolean choiceFlg = false;//駒を選択している状態か？
    private boolean kinjiteFlg = false;//禁じ手フラグ
    private boolean nifuFlg = false;//二歩フラグ
    private List<String> playerGouhousyu = new ArrayList<String>();//プレイヤー合法手

    //キャンセル処理
    public void cancel() {
        this.firstClickMasu = "";//最初にクリックしたマス
        this.firstClickPiece = "";//最初にクリックした駒名
        this.firstBanInOutFlg = false;//
        this.secondClickMasu = "";//二回目にクリックしたマス
        this.secondClickPiece = "";//二回目にクリックした駒名
        this.secondBanInOutFlg = false;//
        this.choiceFlg = false;//駒を選択している状態か？
        this.kinjiteFlg = false;
        this.nifuFlg = false;
        playerGouhousyu.clear();
        touka(toukaAIMasu);
    }

    //合法手リストに格納
    public void setPlayerGouhousyu(String targetMasu, String targetPiece) {
        //10種類の駒と10種類の動き
        int[][] pieceMotionTable = new int[][]{
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},//歩R
                {2, 0, 0, 0, 0, 0, 0, 0, 0, 0},//香
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1},//桂
                {1, 1, 0, 1, 0, 1, 0, 1, 0, 0}, //銀
                {1, 1, 1, 0, 1, 0, 1, 1, 0, 0},//金
                {0, 2, 0, 2, 0, 2, 0, 2, 0, 0},//角
                {2, 0, 2, 0, 2, 0, 2, 0, 0, 0},//飛
                {1, 1, 1, 1, 1, 1, 1, 1, 0, 0},//王
                {1, 2, 1, 2, 1, 2, 1, 2, 0, 0}, //馬
                {2, 1, 2, 1, 2, 1, 2, 1, 0, 0}//竜
        };
        //駒の動き
        //８×９
        //７０１
        //６駒２
        //５４３
        int[][] pieceMotionYX = new int[][]{
                {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-2, -1}, {-2, 1},

        };
        String[] playerPieceArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "UM", "RY", "TO", "NY", "NK", "NG"};
        int choicePieceNumber = 0;
        choicePieceNumber = Arrays.asList(playerPieceArray).indexOf(targetPiece);
        int indexNumber = 0;
        //ときん,成香,成桂,成銀はindexNumberを4にし、金と同じ動きを参照する
        if (choicePieceNumber >= 10) {
            choicePieceNumber = 4;
        }
        int typeMotion, motionY, motionX, addY, addX;
        String checkMasu;//仮の移動先のマス

        for (int i = 0; i < pieceMotionYX.length; i++) {
            typeMotion = pieceMotionTable[choicePieceNumber][i];
            motionY = Integer.parseInt(targetMasu.substring(1, 2));//二文字目を取得
            motionX = Integer.parseInt(targetMasu.substring(targetMasu.length() - 1));//末尾を取得
            if (typeMotion == 0) {
                continue;
            }
            if (typeMotion >= 1) {
                addY = pieceMotionYX[i][0];
                addX = pieceMotionYX[i][1];
                do {
                    motionY += addY;
                    motionX += addX;
                    if ((motionY <= 0) || (motionY >= 10) || (motionX <= 0) || (motionX >= 10)) {
                        break;//移動先が盤外であればスルーする
                    }
                    checkMasu = "d" + motionY + "s" + motionX;
                    if (Arrays.asList(playerPieceArray).indexOf(this.banRecordMap.get(checkMasu)) != -1) {
                        break;//移動先に自陣の駒がある
                    }
                    this.playerGouhousyu.add(checkMasu);//playerの合法手リストに格納
                    if (typeMotion == 1) {
                        break;//１の時は繰り返さずにdo～whileを抜ける
                    }
                } while (this.banRecordMap.get(checkMasu).equals("None"));//移動先に駒がない＆飛車,角,香,竜,馬の２の動きの間は繰り返す。
            }
        }
    }

    //合法手リストに格納
    public void setMotigomaGouhousyu() {
        if (this.firstClickPiece.equals("FU")) {
            checkUseFU();
        } else if (this.firstClickPiece.equals("KY")) {
            checkUseKY();
        } else if (this.firstClickPiece.equals("KE")) {
            checkUseKE();
        } else {
            for (int i = 0; i < this.banRecordMapKeys.length; i++) {
                if (this.banRecordMap.get(this.banRecordMapKeys[i]).equals("None")) {
                    this.playerGouhousyu.add(this.banRecordMapKeys[i]);
                }
            }
        }
        //Log.i("baron", "合法手マスリスト:  " + this.playerGouhousyu);
        toukaGouhousyu();
    }

    //持ち駒の歩みの使用確認
    public void checkUseFU() {
        String[][] checkMasuArray = {
                {"d2s1", "d3s1", "d4s1", "d5s1", "d6s1", "d7s1", "d8s1", "d9s1"},
                {"d2s2", "d3s2", "d4s2", "d5s2", "d6s2", "d7s2", "d8s2", "d9s2"},
                {"d2s3", "d3s3", "d4s3", "d5s3", "d6s3", "d7s3", "d8s3", "d9s3"},
                {"d2s4", "d3s4", "d4s4", "d5s4", "d6s4", "d7s4", "d8s4", "d9s4"},
                {"d2s5", "d3s5", "d4s5", "d5s5", "d6s5", "d7s5", "d8s5", "d9s5"},
                {"d2s6", "d3s6", "d4s6", "d5s6", "d6s6", "d7s6", "d8s6", "d9s6"},
                {"d2s7", "d3s7", "d4s7", "d5s7", "d6s7", "d7s7", "d8s7", "d9s7"},
                {"d2s8", "d3s8", "d4s8", "d5s8", "d6s8", "d7s8", "d8s8", "d9s8"},
                {"d2s9", "d3s9", "d4s9", "d5s9", "d6s9", "d7s9", "d8s9", "d9s9"}
        };
        int[] checkArray = {0, 0, 0, 0, 0, 0, 0, 0, 0};//"s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8","s9"
        for (int i = 0; i < checkMasuArray.length; i++) {
            for (int j = 0; j < checkMasuArray[i].length; j++) {
                if (this.banRecordMap.get(checkMasuArray[i][j]).equals("FU")) {
                    checkArray[i] += 1;
                    break;
                }
            }
        }
        for (int i = 0; i < checkMasuArray.length; i++) {
            for (int j = 0; j < checkMasuArray[i].length; j++) {
                if (checkArray[i] != 0) {
                    break;
                }
                if (this.banRecordMap.get(checkMasuArray[i][j]).equals("None")) {
                    this.playerGouhousyu.add(checkMasuArray[i][j]);
                }
            }
        }
    }

    //持ち駒の香の使用確認
    public void checkUseKY() {
        String[][] checkMasuArray = {
                {"d2s1", "d3s1", "d4s1", "d5s1", "d6s1", "d7s1", "d8s1", "d9s1"},
                {"d2s2", "d3s2", "d4s2", "d5s2", "d6s2", "d7s2", "d8s2", "d9s2"},
                {"d2s3", "d3s3", "d4s3", "d5s3", "d6s3", "d7s3", "d8s3", "d9s3"},
                {"d2s4", "d3s4", "d4s4", "d5s4", "d6s4", "d7s4", "d8s4", "d9s4"},
                {"d2s5", "d3s5", "d4s5", "d5s5", "d6s5", "d7s5", "d8s5", "d9s5"},
                {"d2s6", "d3s6", "d4s6", "d5s6", "d6s6", "d7s6", "d8s6", "d9s6"},
                {"d2s7", "d3s7", "d4s7", "d5s7", "d6s7", "d7s7", "d8s7", "d9s7"},
                {"d2s8", "d3s8", "d4s8", "d5s8", "d6s8", "d7s8", "d8s8", "d9s8"},
                {"d2s9", "d3s9", "d4s9", "d5s9", "d6s9", "d7s9", "d8s9", "d9s9"}
        };

        for (int i = 0; i < checkMasuArray.length; i++) {
            for (int j = 0; j < checkMasuArray[i].length; j++) {
                if (this.banRecordMap.get(checkMasuArray[i][j]).equals("None")) {
                    this.playerGouhousyu.add(checkMasuArray[i][j]);
                }
            }
        }
    }

    //持ち駒の桂の使用確認
    public void checkUseKE() {
        String[][] checkMasuArray = {
                {"d3s1", "d4s1", "d5s1", "d6s1", "d7s1", "d8s1", "d9s1"},
                {"d3s2", "d4s2", "d5s2", "d6s2", "d7s2", "d8s2", "d9s2"},
                {"d3s3", "d4s3", "d5s3", "d6s3", "d7s3", "d8s3", "d9s3"},
                {"d3s4", "d4s4", "d5s4", "d6s4", "d7s4", "d8s4", "d9s4"},
                {"d3s5", "d4s5", "d5s5", "d6s5", "d7s5", "d8s5", "d9s5"},
                {"d3s6", "d4s6", "d5s6", "d6s6", "d7s6", "d8s6", "d9s6"},
                {"d3s7", "d4s7", "d5s7", "d6s7", "d7s7", "d8s7", "d9s7"},
                {"d3s8", "d4s8", "d5s8", "d6s8", "d7s8", "d8s8", "d9s8"},
                {"d3s9", "d4s9", "d5s9", "d6s9", "d7s9", "d8s9", "d9s9"}
        };

        for (int i = 0; i < checkMasuArray.length; i++) {
            for (int j = 0; j < checkMasuArray[i].length; j++) {
                if (this.banRecordMap.get(checkMasuArray[i][j]).equals("None")) {
                    this.playerGouhousyu.add(checkMasuArray[i][j]);
                }
            }
        }
    }

    //昇格確認をしつつ着手
    public void tyakusyu1() {
        String[] checkPieceArray = {"FU", "KY", "KE", "GI", "KA", "HI"};
        String[] promotionPieceArray = {"TO", "NY", "NK", "NG", "UM", "RY"};
        int index = Arrays.asList(checkPieceArray).indexOf(this.firstClickPiece);
        int checkStartDan = Integer.parseInt(this.firstClickMasu.substring(1, 2));//二文字目を取得
        int checkEndDan = Integer.parseInt(this.secondClickMasu.substring(1, 2));//二文字目を取得
        if ((firstBanInOutFlg) && (this.firstClickPiece.equals("FU")) && (checkEndDan == 1)) {
            //歩の一段目の移動
            tyakusyu2(firstClickPiece);
        } else if ((firstBanInOutFlg) && (this.firstClickPiece.equals("KY")) && (checkEndDan == 1)) {
            //香の一段目の移動
            tyakusyu2(firstClickPiece);
        } else if ((firstBanInOutFlg) && (this.firstClickPiece.equals("KE")) && (checkEndDan <= 2)) {
            //桂の一,二段目の移動
            tyakusyu2(firstClickPiece);
        } else if (((firstBanInOutFlg) && (index != -1) && (checkEndDan <= 3)) ||
                ((firstBanInOutFlg) && (this.firstClickPiece.equals("GI")) && (checkStartDan <= 3)) ||
                ((firstBanInOutFlg) && (this.firstClickPiece.equals("KA")) && (checkStartDan <= 3)) ||
                ((firstBanInOutFlg) && (this.firstClickPiece.equals("HI")) && (checkStartDan <= 3))
        ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("成りますか？");
            builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tyakusyu2(promotionPieceArray[index]);
                }
            });
            builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tyakusyu2(firstClickPiece);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            tyakusyu2(firstClickPiece);
        }
    }

    //着手2
    public void tyakusyu2(String targetPiece) {
        toukaKaijyo(this.firstClickMasu);
        toukaKaijyoGouhousyu();
        movePiece(this.firstClickMasu, this.secondClickMasu, targetPiece);
        cancel();
        toukaKaijyo(toukaAIMasu);
        changeTeban();
        checkGameEnd();
        if (this.gameEndFlg) {
            return;
        }
        comProcess();
        return;
    }


    //合法手マスを透過する。
    public void toukaGouhousyu() {
        if (this.playerGouhousyu.size() == 0) {
            return;
        } else {
            for (int i = 0; i < this.playerGouhousyu.size(); i++) {
                int index = Arrays.asList(this.banRecordMapKeys).indexOf(this.playerGouhousyu.get(i));
                ((ImageView) findViewById(this.masuIdArray[index])).setImageAlpha(125);//0-255
            }
        }
    }

    //合法手マスの透過を元に戻す。
    public void toukaKaijyoGouhousyu() {
        if (this.playerGouhousyu.size() == 0) {
            return;
        } else {
            for (int i = 0; i < this.playerGouhousyu.size(); i++) {
                int index = Arrays.asList(this.banRecordMapKeys).indexOf(this.playerGouhousyu.get(i));
                ((ImageView) findViewById(this.masuIdArray[index])).setImageAlpha(255);//0-255
            }
        }
    }

    //マスクリック
    public void masuClick(View view) {
        if (this.gameEndFlg) {
            //ゲームが終了している
            return;
        }
        int id = view.getId();//クリックしたマスのid
        if (this.choiceFlg == false) {
            setFirstClickMasuAndFirstClickPiece(id);//クリックしたマス名と駒名をセットする。
            String[] sentePieceNameArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "TO", "NY", "NK", "NG", "UM", "RY"};
            if (Arrays.asList(sentePieceNameArray).indexOf(this.firstClickPiece) == -1) {
                //先手の駒でない
                return;
            } else {
                //先手の駒なら
                if (this.firstBanInOutFlg) {
                    //盤内なら合法手を表示する
                    setPlayerGouhousyu(this.firstClickMasu, this.firstClickPiece);
                    toukaGouhousyu();
                } else {
                    setMotigomaGouhousyu();//持ち駒の合法手を格納する
                }
                touka(this.firstClickMasu);//最初の駒を選択しているなら透過する。
                this.choiceFlg = true;
                return;
            }
        } else {
            setSecondClickMasuAndFirstClickPiece(id);//二回目にクリックしたマス名と駒名をセットする。
            if (this.playerGouhousyu.contains(this.secondClickMasu)) {
                //合法手をクリックした。
                tyakusyu1();//昇格確認して着手
            } else {
                //合法手でない
                toukaKaijyo(this.firstClickMasu);
                toukaKaijyoGouhousyu();
                cancel();
                return;
            }
        }
    }

    //クリックしたマス名と駒名をセットする。マス名("d1s1","k1"など),駒名("FU",,"RFU","None"など)
    public void setFirstClickMasuAndFirstClickPiece(int targetId) {
        for (int i = 0; i < this.masuIdArray.length; i++) {
            if (this.masuIdArray[i] == (targetId)) {
                this.firstClickMasu = this.masuNameArray[i];//最初にクリックしたマス
                if (i <= 80) {
                    //盤内のマス
                    this.firstClickPiece = this.banRecordMap.get(this.banRecordMapKeys[i]);//最初にクリックした駒名
                    this.firstBanInOutFlg = true;//盤内
                } else if (i >= 81) {
                    //駒台のマス
                    this.firstClickPiece = this.komadaiRecordMap.get(this.komadaiRecordMapKeys[-81 + i]);//最初にクリックした駒名
                    this.firstBanInOutFlg = false;//盤外
                }
            }
        }
    }

    //二回目にクリックしたマス名と駒名をセットする。マス名("d1s1","k1"など),駒名("FU",,"RFU","None"など)
    public void setSecondClickMasuAndFirstClickPiece(int targetId) {
        for (int i = 0; i < this.masuIdArray.length; i++) {
            if (this.masuIdArray[i] == (targetId)) {
                this.secondClickMasu = this.masuNameArray[i];//２回目にクリックしたマス
                if (i <= 80) {
                    //盤内のマス
                    this.secondClickPiece = this.banRecordMap.get(this.banRecordMapKeys[i]);//２回目にクリックした駒名
                    this.secondBanInOutFlg = true;//盤内
                } else if (i >= 81) {
                    //駒台のマス
                    this.secondClickPiece = this.komadaiRecordMap.get(this.komadaiRecordMapKeys[-81 + i]);//２回目にクリックした駒名
                    this.secondBanInOutFlg = false;//盤外
                }
            }
        }
    }

    //選択している駒を移動する。
    public void movePiece(String moveStartMasu, String moveEndMasu, String movePiece) {
        if ((this.firstBanInOutFlg == false) && (this.secondBanInOutFlg == true)) {
            //持ち駒を使用
            //そのまま置く
            deleteKomadaiPiece(this.komadaiBottomList, movePiece); //駒台の駒を削除(駒台上リストから削除)
            this.banRecordMap.put(moveEndMasu, movePiece);//追加
            updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
            updateTargetMasuDisplay(moveEndMasu);//指定したマスのデータを反映して表示する。
            this.mpKomaoto.start();
            return;
        } else if ((this.firstBanInOutFlg == true) && (this.secondBanInOutFlg == true)) {
            //盤上から盤上をクリック。
            if (!(this.banRecordMap.get(moveEndMasu).equals("None"))) {
                //駒がある時は取る。
                this.banRecordMap.put(moveEndMasu, movePiece);//追加
                this.banRecordMap.put(moveStartMasu, "None");//削除
                String addPieceName = changeKomadaiPieceName(this.firstClickPiece, this.secondClickPiece);//自陣の駒台に置ける名前に変更
                addKomadaiPiece(this.firstClickPiece, addPieceName);
                updateTargetMasuDisplay(moveStartMasu);//指定したマスのデータを反映して表示する。
                updateTargetMasuDisplay(moveEndMasu);//指定したマスのデータを反映して表示する。
                updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
                this.mpKomaoto.start();
                return;
            } else {
                //駒がない時はそのまま置く
                this.banRecordMap.put(moveEndMasu, movePiece);//追加
                this.banRecordMap.put(moveStartMasu, "None");//削除
                updateTargetMasuDisplay(moveStartMasu);//指定したマスのデータを反映して表示する。
                updateTargetMasuDisplay(moveEndMasu);//指定したマスのデータを反映して表示する。
                this.mpKomaoto.start();
                return;
            }
        }
    }


    //駒台リストから指定した駒を1枚削除する。
    public void deleteKomadaiPiece(List<String> targetList, String targePiece) {
        for (int i = 0; i < targetList.size(); i++) {
            if (targetList.get(i).equals(targePiece)) {
                targetList.remove(i);
                break;
            }
        }
    }

    //指定したマスのデータを反映して表示する。
    public void updateTargetMasuDisplay(String targetMasu) {
        //moveEndMasu
        String[] sentePieceNameArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "TO", "NY", "NK", "NG", "UM", "RY"};
        String[] gotePieceNameArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "to", "ny", "nk", "ng", "um", "ry"};
        //盤上の駒
        int[] pieceIdArray = {R.drawable.fu, R.drawable.ky, R.drawable.ke, R.drawable.gi, R.drawable.ki,
                R.drawable.ka, R.drawable.hi, R.drawable.ou,
                R.drawable.to, R.drawable.ny, R.drawable.nk, R.drawable.ng, R.drawable.um, R.drawable.ry
        };
        promotionKeima();//桂馬が強制的に成るマス(１,２段目)(８,９段目)なら成り駒にする。
        promotionFuKyou();//歩,香が強制的に成るマス(１段目)(９段目)なら成り駒にする。
        String masuData = this.banRecordMap.get(targetMasu);//マスの情報
        int masuIdIndex = Arrays.asList(this.masuNameArray).indexOf(targetMasu);
        if (masuData.equals("None")) {
            ((ImageView) findViewById(this.masuIdArray[masuIdIndex])).setImageResource(R.drawable.masu);
            return;
        }
        int pieceIdIndex = 0;
        if (Arrays.asList(sentePieceNameArray).contains(masuData)) {
            pieceIdIndex = Arrays.asList(sentePieceNameArray).indexOf(masuData);
        } else if (Arrays.asList(gotePieceNameArray).contains(masuData)) {
            pieceIdIndex = Arrays.asList(gotePieceNameArray).indexOf(masuData);
        }
        if (Arrays.asList(sentePieceNameArray).contains(masuData)) {
            ((ImageView) findViewById(this.masuIdArray[masuIdIndex])).setImageResource(pieceIdArray[pieceIdIndex]);
        } else if (Arrays.asList(gotePieceNameArray).contains(masuData)) {
            if (pieceIdIndex == 7) {
                changeImage180((ImageView) findViewById(this.masuIdArray[masuIdIndex]), R.drawable.gy);
            } else {
                changeImage180((ImageView) findViewById(this.masuIdArray[masuIdIndex]), pieceIdArray[pieceIdIndex]);
            }
        }
    }


    //駒台数表示クリック
    public void numClick(View view) {
        int id = view.getId();//クリックしたマスのid
        String[] komadaiBottomArray = {"k11", "k12", "k13", "k14", "k15", "k16", "k17", "k18", "k19"};
        int indexNumber = 0;
        for (int i = 0; i < this.numIdArray.length; i++) {
            if (this.numIdArray[i] == id) {
                indexNumber = i - 9;
            }
        }
        if (this.choiceFlg == false) {
            return;
        }

    }

    //自陣の駒台に置ける駒名にする。
    public String changeKomadaiPieceName(String firstPieceName, String secondPieceName) {
        String[] komadaiTopArray = {"k1", "k2", "k3", "k4", "k5", "k6", "k7", "k8", "k9"};
        String[] komadaiBottomArray = {"k11", "k12", "k13", "k14", "k15", "k16", "k17", "k18", "k19"};
        String[] topPieceNameArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "OU", "gy", "to", "ny", "nk", "ng", "um", "ry"};
        String[] bottomPieceNameArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "gy", "TO", "NY", "NK", "NG", "UM", "RY"};
        String[] komadaiTopPieceNameArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "OU", "gy", "fu", "ky", "ke", "gi", "ka", "hi"};
        String[] komadaiBottomPieceNameArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "gy", "FU", "KY", "KE", "GI", "KA", "HI"};
        String returnPieceName = "";
        int index = 0;
        if (Arrays.asList(topPieceNameArray).contains(secondPieceName)) {
            //自陣の成り駒を元の駒に変える。
            index = Arrays.asList(topPieceNameArray).indexOf(secondPieceName);
        } else if (Arrays.asList(bottomPieceNameArray).contains(secondPieceName)) {
            //敵陣の駒を敵陣の駒に変える。
            index = Arrays.asList(bottomPieceNameArray).indexOf(secondPieceName);
        }
        if (Arrays.asList(komadaiTopArray).contains(this.firstClickMasu)) {
            //上の駒台に置ける名前に変更する。
            returnPieceName = komadaiTopPieceNameArray[index];
        } else if (Arrays.asList(komadaiBottomArray).contains(this.firstClickMasu)) {
            //下の駒台に置ける名前に変更する。
            returnPieceName = komadaiBottomPieceNameArray[index];
        } else {
            if (firstPieceName.equals("gy")) {
                //上の駒台に置ける名前に変更する。
                returnPieceName = komadaiTopPieceNameArray[index];
            } else if (firstPieceName.equals("OU")) {
                //下の駒台に置ける名前に変更する。
                returnPieceName = komadaiBottomPieceNameArray[index];
            } else {
                if (Arrays.asList(topPieceNameArray).contains(firstPieceName)) {
                    //上の駒台に置ける名前に変更する。
                    returnPieceName = komadaiTopPieceNameArray[index];
                } else if (Arrays.asList(bottomPieceNameArray).contains(firstPieceName)) {
                    //下の駒台に置ける名前に変更する。
                    returnPieceName = komadaiBottomPieceNameArray[index];
                }
            }
        }
        return returnPieceName;
    }

    //自陣の駒台に追加する。
    public void addKomadaiPiece(String firstPieceName, String addPieceName) {
        String[] topPieceNameArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "to", "ny", "nk", "ng", "um", "ry"};
        String[] bottomPieceNameArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "TO", "NY", "NK", "NG", "UM", "RY"};
        if (Arrays.asList(topPieceNameArray).contains(firstPieceName)) {
            this.komadaiTopList.add(addPieceName);//駒台上に追加
        } else if (Arrays.asList(bottomPieceNameArray).contains(firstPieceName)) {
            this.komadaiBottomList.add(addPieceName);//駒台下に追加
        }
    }


    //デザイン変更
    public void changeDesign() {
        Point screenSize = getDisplaySize();//画面サイズの取得
        TableLayout board = (TableLayout) findViewById(R.id.board);//盤
        TextView boardTop = (TextView) findViewById(R.id.boardTop);//盤上
        TextView boardBottom = (TextView) findViewById(R.id.boardBottom);//盤下
        LinearLayout numArea1 = (LinearLayout) findViewById(R.id.numArea1);//駒台枚数1
        LinearLayout numArea2 = (LinearLayout) findViewById(R.id.numArea2);//駒台枚数2
        LinearLayout komadaiArea1 = (LinearLayout) findViewById(R.id.komadaiArea1);//駒台1
        LinearLayout komadaiArea2 = (LinearLayout) findViewById(R.id.komadaiArea2);//駒台2
        //ImageView menuImage = (ImageView) findViewById(R.id.menu);//メニュー
        //デザイン変更
        ChangeDesign cd = new ChangeDesign(screenSize, this.masuImageArray, board, boardTop, boardBottom,
                numArea1, numArea2, komadaiArea1, komadaiArea2, this.numImageArray);//インスタンス生成
    }

    //画面サイズの取得
    public Point getDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
//        Log.i("baron", "スクリーンサイズ(横) :  " + screenSize.x);
//        Log.i("baron", "スクリーンサイズ(縦) :  " + screenSize.y);
        return screenSize;
    }

    //選択した駒を透過する。
    public void touka(String targetMasu) {
        if (targetMasu.equals("")) {
            return;
        }
        if (Arrays.asList(this.banRecordMapKeys).contains(targetMasu)) {
            if (!(this.banRecordMap.get(targetMasu).equals("None"))) {
                int index = Arrays.asList(this.banRecordMapKeys).indexOf(targetMasu);
                ((ImageView) findViewById(this.masuIdArray[index])).setImageAlpha(125);//0-255
            }
        } else if (Arrays.asList(this.komadaiRecordMapKeys).contains(targetMasu)) {
            if (!(this.komadaiRecordMap.get(targetMasu).equals("None"))) {
                int index = Arrays.asList(this.komadaiRecordMapKeys).indexOf(targetMasu);
                ((ImageView) findViewById(this.masuIdArray[81 + index])).setImageAlpha(125);//0-255
            }
        }
    }

    //選択している駒の透過を元に戻す。
    public void toukaKaijyo(String targetMasu) {
        if (targetMasu.equals("")) {
            return;
        }
        if (Arrays.asList(this.banRecordMapKeys).contains(targetMasu)) {
            if (!(this.banRecordMap.get(targetMasu).equals("None"))) {
                int index = Arrays.asList(this.banRecordMapKeys).indexOf(targetMasu);
                ((ImageView) findViewById(this.masuIdArray[index])).setImageAlpha(255);//0-255
            }
        } else if (Arrays.asList(this.komadaiRecordMapKeys).contains(targetMasu)) {
            if (!(this.komadaiRecordMap.get(targetMasu).equals("None"))) {
                int index = Arrays.asList(this.komadaiRecordMapKeys).indexOf(targetMasu);
                ((ImageView) findViewById(this.masuIdArray[81 + index])).setImageAlpha(255);//0-255
            }
        }
    }

    //手番表示
    public void tebanShowEvent(View view) {
        if ((this.teban.equals("com")) || (this.gameEndFlg)) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (this.teban.equals("あなた")) {
            builder.setMessage("あなたの手番です。");
        } else if (this.teban.equals("com")) {
            builder.setMessage("comの手番です。");
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //手番表示
    public void endButtonEvent(View view) {
        if ((this.teban.equals("com")) || (this.gameEndFlg)) {
            return;
        }
        new android.app.AlertDialog.Builder(this)
                .setMessage("対局を終了しますか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gameEndFlg = true;//ゲーム終了フラグ
                        ((TextView) findViewById(R.id.gameEndText)).setText("お疲れでした(*_ _)");
                        return;
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    //バックボタン
    public void backButtonEvent(View view) {
        new android.app.AlertDialog.Builder(this)
                .setMessage("最初に戻りますか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        intentMainActivity();
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    //最初に戻る。
    public void intentMainActivity() {
        Intent intent = new Intent(getApplication(), MainActivity.class);
        startActivity(intent);//画面推移
        finish();//画面を終了し、バックキーで戻らせない。
    }

    //タッチイベントを取得する
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //盤外をタッチ中
                toukaKaijyo(this.firstClickMasu);
                toukaKaijyoGouhousyu();
                cancel();
                break;
            case MotionEvent.ACTION_UP:
                //タッチしていない。
                break;
        }
        return true;//true:タッチイベントを消化したことになり、他のViewやActivityへの通知を抑制する。
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
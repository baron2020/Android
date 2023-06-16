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
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//将棋棋譜並べアプリ
public class Consideration extends AppCompatActivity {
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consideration);
        this.mpKomaoto = MediaPlayer.create(this, R.raw.komaoto);
        syokika();//初期化
        gameDataSyokika();//盤面,駒台の初期化
        changeDesign();//デザイン変更
        gameRecordPlayBack();//盤面,駒台の再生
    }

    //推論
    public void predict(View view) {
        aiClassifier = new AI(this, banRecordMap, komadaiTopList, komadaiBottomList);
        int digit = aiClassifier.classify();
        List<Integer> maxLabel20 = aiClassifier.getMaxIndex20List(); //着手ラベル20(高い順)
        List<Float> maxValue20 = aiClassifier.getMaxValue20List();//確率20(高い順)
        BaronAI baron = new BaronAI(banRecordMap, returnChangeMotigomaList(komadaiBottomList), maxLabel20, maxValue20);
        String finalAI = baron.returnFinalAI();//GUIから合法手を取得
        List<String> finalList = baron.getFinalList();
        top3Display(finalAI,finalList);
    }

    //top3を表示する
    private void top3Display(String targetFinalAI,List<String> targetList) {
        String text1 = "候補：";
        if((targetFinalAI!="")&&(targetList.size()==0)){
            text1 += changeKifu(targetFinalAI);
            ((TextView) findViewById(R.id.text1)).setText(text1);
            return;
        }
        for (int i = 0; i < targetList.size(); i++) {
            text1 += changeKifu(targetList.get(i));
            if (i == 2) {
                break;
            } else {
                if (i == targetList.size() - 1) {
                    break;
                } else {
                    text1 += ",";
                }
            }
        }
        ((TextView) findViewById(R.id.text1)).setText(text1);
    }

    //棋譜チェンジ
    private String changeKifu(String targetData) {
        String[] masuArray = new String[]{
                "d1s1", "d1s2", "d1s3", "d1s4", "d1s5", "d1s6", "d1s7", "d1s8", "d1s9",
                "d2s1", "d2s2", "d2s3", "d2s4", "d2s5", "d2s6", "d2s7", "d2s8", "d2s9",
                "d3s1", "d3s2", "d3s3", "d3s4", "d3s5", "d3s6", "d3s7", "d3s8", "d3s9",
                "d4s1", "d4s2", "d4s3", "d4s4", "d4s5", "d4s6", "d4s7", "d4s8", "d4s9",
                "d5s1", "d5s2", "d5s3", "d5s4", "d5s5", "d5s6", "d5s7", "d5s8", "d5s9",
                "d6s1", "d6s2", "d6s3", "d6s4", "d6s5", "d6s6", "d6s7", "d6s8", "d6s9",
                "d7s1", "d7s2", "d7s3", "d7s4", "d7s5", "d7s6", "d7s7", "d7s8", "d7s9",
                "d8s1", "d8s2", "d8s3", "d8s4", "d8s5", "d8s6", "d8s7", "d8s8", "d8s9",
                "d9s1", "d9s2", "d9s3", "d9s4", "d9s5", "d9s6", "d9s7", "d9s8", "d9s9",
                "もちごま"
        };
        String[] kifuMasuArray = {
                "９一", "８一", "７一", "６一", "５一", "４一", "３一", "２一", "１一",
                "９二", "８二", "７二", "６二", "５二", "４二", "３二", "２二", "１二",
                "９三", "８三", "７三", "６三", "５三", "４三", "３三", "２三", "１三",
                "９四", "８四", "７四", "６四", "５四", "４四", "３四", "２四", "１四",
                "９五", "８五", "７五", "６五", "５五", "４五", "３五", "２五", "１五",
                "９六", "８六", "７六", "６六", "５六", "４六", "３六", "２六", "１六",
                "９七", "８七", "７七", "６七", "５七", "４七", "３七", "２七", "１七",
                "９八", "８八", "７八", "６八", "５八", "４八", "３八", "２八", "１八",
                "９九", "８九", "７九", "６九", "５九", "４九", "３九", "２九", "１九",
                "打"
        };
        String[] pieceArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "TO", "NY", "NK", "NG", "UM", "RY"};
        String[] kifuPieceArray = {"歩", "香", "桂", "銀", "金", "角", "飛", "王", "と", "成香", "成桂", "成銀", "馬", "竜"};
        String endMasu = targetData.substring(0, 4); //移動後のマス(左から4文字切り出し)
        String piece = targetData.substring(4, 6); //駒
        String startMasu = targetData.substring(6, 10); //移動前のマス
        int nari;
        if (targetData.substring(targetData.length() - 1).equals("ま")) {
            nari = 0;
        } else {
            nari = Integer.parseInt(targetData.substring(targetData.length() - 1));//末尾を取得
        }
        String temp1 = kifuMasuArray[Arrays.asList(masuArray).indexOf(endMasu)];
        String temp2 = kifuPieceArray[Arrays.asList(pieceArray).indexOf(piece)];
        String temp3 = kifuMasuArray[Arrays.asList(masuArray).indexOf(startMasu)];
        String returnAddData = temp1 + temp2 + "(" + temp3 + ")";
        if (nari == 1) {
            returnAddData += "成";
        }
        return returnAddData;
    }

    //リストを受け取り小文字リストに変換して返す
    private List<String> returnChangeMotigomaList(List<String> targetList) {
        List<String> returnList = new ArrayList<String>();
        String[] pieceArray1 = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "gy"};
        String[] pieceArray2 = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "OU", "gy"};
        for (int i = 0; i < targetList.size(); i++) {
            String addPiece = pieceArray2[Arrays.asList(pieceArray1).indexOf(targetList.get(i))];
            returnList.add(addPiece);
        }
        return returnList;
    }

    //推論結果から着手に変換pred
    private String changePredictData(int digit) {
        int masu = digit % 81;
        int ugoki = digit / 81;
        String[] masuArray = {
                "９一", "８一", "７一", "６一", "５一", "４一", "３一", "２一", "１一",
                "９二", "８二", "７二", "６二", "５二", "４二", "３二", "２二", "１二",
                "９三", "８三", "７三", "６三", "５三", "４三", "３三", "２三", "１三",
                "９四", "８四", "７四", "６四", "５四", "４四", "３四", "２四", "１四",
                "９五", "８五", "７五", "６五", "５五", "４五", "３五", "２五", "１五",
                "９六", "８六", "７六", "６六", "５六", "４六", "３六", "２六", "１六",
                "９七", "８七", "７七", "６七", "５七", "４七", "３七", "２七", "１七",
                "９八", "８八", "７八", "６八", "５八", "４八", "３八", "２八", "１八",
                "９九", "８九", "７九", "６九", "５九", "４九", "３九", "２九", "１九"
        };
        String[] ugokiArray = {
                "上", "左上", "右上", "左", "右", "下", "左下", "右下", "左桂", "右桂",
                "上(成)", "左上(成)", "右上(成)", "左(成)", "右(成)", "下(成)", "左下(成)", "右下(成)", "左桂(成)", "右桂(成)",
                "歩打", "香打", "桂打", "銀打", "金打", "角打", "飛車打"
        };
        String kouhosyu = masuArray[masu] + " " + ugokiArray[ugoki];
        return kouhosyu;
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
    }

    //マスクリック
    public void masuClick(View view) {
        int id = view.getId();//クリックしたマスのid
        if (this.choiceFlg == false) {
            setFirstClickMasuAndFirstClickPiece(id);//クリックしたマス名と駒名をセットする。
            if (this.firstClickPiece.equals("None")) {
                //最初の駒を選択していない。
                return;
            } else {
                touka(this.firstClickMasu);//最初の駒を選択しているなら透過する。
                this.choiceFlg = true;
                return;
            }
        } else {
            setSecondClickMasuAndFirstClickPiece(id);//二回目にクリックしたマス名と駒名をセットする。
            if ((this.firstBanInOutFlg) && (this.secondBanInOutFlg) && (this.firstClickMasu.equals(this.secondClickMasu))) {
                //盤上の同じマスの同じ駒をクリックした。
                toukaKaijyo(this.firstClickMasu);
                changePromotionOrNoPromotion(this.firstClickMasu, this.firstClickPiece);//成り,成らずを変える。
                cancel();
            } else {
                //違うマスをクリックした。
                toukaKaijyo(this.firstClickMasu);
                movePiece(this.firstClickMasu, this.secondClickMasu);
                cancel();
            }
        }
    }

    //選択している駒を移動する。
    public void movePiece(String moveStartMasu, String moveEndMasu) {
        String[] komadaiTopArray = {"k1", "k2", "k3", "k4", "k5", "k6", "k7", "k8", "k9"};
        String[] komadaiBottomArray = {"k11", "k12", "k13", "k14", "k15", "k16", "k17", "k18", "k19"};
        //this.banRecordMap(盤上の駒データ),this.komadaiRecordMap(駒台の駒データ)
        if ((Arrays.asList(komadaiTopArray).contains(moveStartMasu)) && (Arrays.asList(komadaiTopArray).contains(moveEndMasu))) {
            //1.駒台上から駒台上をクリック。
            return;
        } else if ((Arrays.asList(komadaiBottomArray).contains(moveStartMasu)) && (Arrays.asList(komadaiBottomArray).contains(moveEndMasu))) {
            //2.駒台下から駒台下をクリック。
            return;
        } else if ((Arrays.asList(komadaiTopArray).contains(moveStartMasu)) && (Arrays.asList(komadaiBottomArray).contains(moveEndMasu))) {
            //駒台上から駒台下をクリック。
            deleteKomadaiPiece(this.komadaiTopList, this.firstClickPiece); //駒台の駒を削除(駒台上リストから削除)
            String addPieceName = changeTopBottomPieceName(this.firstClickPiece);
            this.komadaiBottomList.add(addPieceName);//追加
            updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
            this.mpKomaoto.start();
            return;
        } else if ((Arrays.asList(komadaiBottomArray).contains(moveStartMasu)) && (Arrays.asList(komadaiTopArray).contains(moveEndMasu))) {
            //駒台下から駒台上をクリック。
            deleteKomadaiPiece(this.komadaiBottomList, this.firstClickPiece); //駒台の駒を削除(駒台上リストから削除)
            String addPieceName = changeTopBottomPieceName(this.firstClickPiece);
            this.komadaiTopList.add(addPieceName);//追加
            updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
            this.mpKomaoto.start();
            return;
        } else if ((Arrays.asList(komadaiTopArray).contains(moveStartMasu)) && (Arrays.asList(this.banRecordMapKeys).contains(moveEndMasu))) {
            //駒台上から盤上をクリック(2回目にクリックしたマスの駒と交換する)
            checkNifu(this.firstClickMasu, this.secondClickMasu, this.firstClickPiece, 0);//二歩チェック
            if (this.nifuFlg) {
                return;
            }
            if (!(this.banRecordMap.get(moveEndMasu).equals("None"))) {
                //駒がある時は交換する。
                deleteKomadaiPiece(this.komadaiTopList, this.firstClickPiece); //駒台の駒を削除(駒台上リストから削除)
                this.banRecordMap.put(moveEndMasu, this.firstClickPiece);//盤上に追加
                String addPieceName = changeKomadaiPieceName(this.firstClickPiece, this.secondClickPiece);//自陣の駒台に置ける名前に変更
                this.komadaiTopList.add(addPieceName);//駒台上に追加
                updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
                updateTargetMasuDisplay(moveEndMasu);//指定したマスのデータを反映して表示する。
                this.mpKomaoto.start();
                return;
            } else {
                //駒がない時はそのまま置く
                deleteKomadaiPiece(this.komadaiTopList, this.firstClickPiece); //駒台の駒を削除(駒台上リストから削除)
                this.banRecordMap.put(moveEndMasu, this.firstClickPiece);//追加
                updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
                updateTargetMasuDisplay(moveEndMasu);//指定したマスのデータを反映して表示する。
                this.mpKomaoto.start();
                return;
            }
        } else if ((Arrays.asList(komadaiBottomArray).contains(moveStartMasu)) && (Arrays.asList(this.banRecordMapKeys).contains(moveEndMasu))) {
            //駒台下から盤上をクリック(2回目にクリックしたマスの駒と交換する)
            checkNifu(this.firstClickMasu, this.secondClickMasu, this.firstClickPiece, 0);//二歩チェック
            if (this.nifuFlg) {
                return;
            }
            if (!(this.banRecordMap.get(moveEndMasu).equals("None"))) {
                //駒がある時は交換する。
                deleteKomadaiPiece(this.komadaiBottomList, this.firstClickPiece); //駒台の駒を削除(駒台上リストから削除)
                this.banRecordMap.put(moveEndMasu, this.firstClickPiece);//盤上に追加
                String addPieceName = changeKomadaiPieceName(this.firstClickPiece, this.secondClickPiece);//自陣の駒台に置ける名前に変更
                this.komadaiBottomList.add(addPieceName);//駒台上に追加
                updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
                updateTargetMasuDisplay(moveEndMasu);//指定したマスのデータを反映して表示する。
                this.mpKomaoto.start();
                return;
            } else {
                //駒がない時はそのまま置く
                deleteKomadaiPiece(this.komadaiBottomList, this.firstClickPiece); //駒台の駒を削除(駒台上リストから削除)
                this.banRecordMap.put(moveEndMasu, this.firstClickPiece);//追加
                updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
                updateTargetMasuDisplay(moveEndMasu);//指定したマスのデータを反映して表示する。
                this.mpKomaoto.start();
                return;
            }
        } else if ((Arrays.asList(this.banRecordMapKeys).contains(moveStartMasu)) && (Arrays.asList(this.banRecordMapKeys).contains(moveEndMasu))) {
            //盤上から盤上をクリック。
            checkNifu(this.firstClickMasu, this.secondClickMasu, this.firstClickPiece, 0);//二歩チェック
            if (this.nifuFlg) {
                return;
            }
            if (!(this.banRecordMap.get(moveEndMasu).equals("None"))) {
                //駒がある時は取る。
                this.banRecordMap.put(moveEndMasu, this.banRecordMap.get(moveStartMasu));//追加
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
                this.banRecordMap.put(moveEndMasu, this.banRecordMap.get(moveStartMasu));//追加
                this.banRecordMap.put(moveStartMasu, "None");//削除
                updateTargetMasuDisplay(moveStartMasu);//指定したマスのデータを反映して表示する。
                updateTargetMasuDisplay(moveEndMasu);//指定したマスのデータを反映して表示する。
                this.mpKomaoto.start();
                return;
            }
        } else if ((Arrays.asList(this.banRecordMapKeys).contains(moveStartMasu)) && (Arrays.asList(komadaiTopArray).contains(moveEndMasu))) {
            //盤上から駒台上をクリック。
            this.banRecordMap.put(moveStartMasu, "None");//削除
            if ((this.firstClickPiece.equals("OU")) || (this.firstClickPiece.equals("gy"))) {
                this.komadaiTopList.add(this.firstClickPiece);//追加
            } else {
                String addPieceName = changeNoPromotionPieceName(this.firstClickPiece, "top");
                this.komadaiTopList.add(addPieceName);//追加
            }
            updateTargetMasuDisplay(moveStartMasu);//指定したマスのデータを反映して表示する。
            updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
            this.mpKomaoto.start();
            return;
        } else if ((Arrays.asList(this.banRecordMapKeys).contains(moveStartMasu)) && (Arrays.asList(komadaiBottomArray).contains(moveEndMasu))) {
            //盤上から駒台下をクリック。
            this.banRecordMap.put(moveStartMasu, "None");//削除
            if ((this.firstClickPiece.equals("OU")) || (this.firstClickPiece.equals("gy"))) {
                this.komadaiBottomList.add(this.firstClickPiece);//追加
            } else {
                String addPieceName = changeNoPromotionPieceName(this.firstClickPiece, "bottom");
                this.komadaiBottomList.add(addPieceName);//追加
            }
            updateTargetMasuDisplay(moveStartMasu);//指定したマスのデータを反映して表示する。
            updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
            this.mpKomaoto.start();
            return;
        } else {
            return;
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

    //昇格できる駒なら昇格する。昇格している駒なら元に戻す。
    public void changePromotionOrNoPromotion(String targetMasu, String targetPiece) {
        String[] searchPieceNameArray = {"FU", "KY", "KE", "GI", "KA", "HI", "TO", "NY", "NK", "NG", "UM", "RY",
                "fu", "ky", "ke", "gi", "ka", "hi", "to", "ny", "nk", "ng", "ym", "ry"};
        String[] bottomPieceNameArray = {"FU", "KY", "KE", "GI", "KA", "HI", "TO", "NY", "NK", "NG", "UM", "RY",};
        String[] topPieceNameArray = {"fu", "ky", "ke", "gi", "ka", "hi", "to", "ny", "nk", "ng", "um", "ry"};
        String tempPieceName = "";
        if (!(Arrays.asList(searchPieceNameArray).contains(targetPiece))) {
            //成れる駒でも成り駒でもない。
            return;
        } else {
            checkKinjite(targetMasu, targetPiece);//禁じ手になるなら無効にする
            if (this.kinjiteFlg) {
                return;
            }
            if (Arrays.asList(bottomPieceNameArray).contains(targetPiece)) {
                int index = Arrays.asList(bottomPieceNameArray).indexOf(targetPiece);
                if (index <= 5) {
                    //成り駒でない
                    tempPieceName = bottomPieceNameArray[index + 6];
                } else {
                    //成り駒
                    tempPieceName = bottomPieceNameArray[index - 6];
                }
            } else if (Arrays.asList(topPieceNameArray).contains(targetPiece)) {
                int index = Arrays.asList(topPieceNameArray).indexOf(targetPiece);
                if (index <= 5) {
                    tempPieceName = topPieceNameArray[index + 6];
                } else {
                    tempPieceName = topPieceNameArray[index - 6];
                }
            }
            this.banRecordMap.put(targetMasu, tempPieceName);//追加
            updateTargetMasuDisplay(targetMasu);
            this.mpKomaoto.start();
            return;
        }
    }

    //駒台数表示クリック
    public void numClick(View view) {
        int id = view.getId();//クリックしたマスのid
        String[] komadaiTopArray = {"k1", "k2", "k3", "k4", "k5", "k6", "k7", "k8", "k9"};
        String[] komadaiBottomArray = {"k11", "k12", "k13", "k14", "k15", "k16", "k17", "k18", "k19"};
        //R.id.n1～R.id.n9,R.id.n11～R.id.n19
        int indexNumber = 0;
        for (int i = 0; i < this.numIdArray.length; i++) {
            if (this.numIdArray[i] == id) {
                indexNumber = i;
            }
        }
        if (this.choiceFlg == false) {
            return;
        } else {
            toukaKaijyo(this.firstClickMasu);
            if ((Arrays.asList(komadaiTopArray).contains(this.firstClickMasu)) && (indexNumber <= 8)) {
                //駒台上→駒台上をクリック。上上無効
                cancel();
                return;
            } else if ((Arrays.asList(komadaiBottomArray).contains(this.firstClickMasu)) && (indexNumber >= 9)) {
                //駒台下→駒台下をクリック。下下無効
                cancel();
                return;
            } else if ((Arrays.asList(komadaiTopArray).contains(this.firstClickMasu)) && (indexNumber >= 9)) {
                //駒台上→駒台下をクリック。上下許可
                deleteKomadaiPiece(this.komadaiTopList, this.firstClickPiece); //駒台の駒を削除(駒台上リストから削除)
                String addPieceName = changeTopBottomPieceName(this.firstClickPiece);
                this.komadaiBottomList.add(addPieceName);//追加
                updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
                this.mpKomaoto.start();
                cancel();
                return;
            } else if ((Arrays.asList(komadaiBottomArray).contains(this.firstClickMasu)) && (indexNumber <= 8)) {
                //駒台下→駒台上をクリック。下上許可
                deleteKomadaiPiece(this.komadaiBottomList, this.firstClickPiece); //駒台の駒を削除(駒台上リストから削除)
                String addPieceName = changeTopBottomPieceName(this.firstClickPiece);
                this.komadaiTopList.add(addPieceName);//追加
                updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
                this.mpKomaoto.start();
                cancel();
                return;
            } else if ((this.firstBanInOutFlg) && (indexNumber <= 8)) {
                //盤→駒台上をクリック。盤上移動
                this.banRecordMap.put(this.firstClickMasu, "None");//削除
                if ((this.firstClickPiece.equals("OU")) || (this.firstClickPiece.equals("gy"))) {
                    this.komadaiTopList.add(this.firstClickPiece);//追加
                } else {
                    String addPieceName = changeNoPromotionPieceName(this.firstClickPiece, "top");
                    this.komadaiTopList.add(addPieceName);//追加
                }
                updateTargetMasuDisplay(this.firstClickMasu);//指定したマスのデータを反映して表示する。
                updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
                this.mpKomaoto.start();
                cancel();
                return;
            } else if ((this.firstBanInOutFlg) && (indexNumber >= 9)) {
                //盤→駒台下をクリック。盤下移動
                this.banRecordMap.put(this.firstClickMasu, "None");//削除
                if ((this.firstClickPiece.equals("OU")) || (this.firstClickPiece.equals("gy"))) {
                    this.komadaiBottomList.add(this.firstClickPiece);//追加
                } else {
                    String addPieceName = changeNoPromotionPieceName(this.firstClickPiece, "bottom");
                    this.komadaiBottomList.add(addPieceName);//追加
                }
                updateTargetMasuDisplay(this.firstClickMasu);//指定したマスのデータを反映して表示する。
                updateMotigomaDisplay(); //持ち駒のデータを反映して表示する。
                this.mpKomaoto.start();
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
                this.secondClickMasu = this.masuNameArray[i];//最初にクリックしたマス
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

    //禁じ手確認
    public void checkKinjite(String targetMasu, String targetPiece) {
        int checkDan = Integer.parseInt(targetMasu.substring(1, 2));//二文字目を取得
        //一段目のときんなら歩に戻れない。一段目の成香なら香に戻れない。一,二段目の成桂なら桂に戻れない。
        if (((checkDan == 1) && ((targetPiece.equals("TO")) || (targetPiece.equals("NY")))) ||
                ((checkDan == 1) && ((targetPiece.equals("NK")))) ||
                ((checkDan == 2) && ((targetPiece.equals("NK")))) ||
                ((checkDan == 9) && ((targetPiece.equals("to")) || (targetPiece.equals("ny")))) ||
                ((checkDan == 9) && ((targetPiece.equals("nk")))) ||
                ((checkDan == 8) && ((targetPiece.equals("nk"))))) {
            this.kinjiteFlg = true;
            return;
        }
        //二歩になるならときん→歩に戻れない。
        if (targetPiece.equals("TO")) {
            checkNifu(targetMasu, targetMasu, "FU", 1);
        } else if (targetPiece.equals("to")) {
            checkNifu(targetMasu, targetMasu, "fu", 1);
        }
    }

    //二歩チェックメソッド
    public void checkNifu(String startMasu, String endMasu, String searchPiece, int checkKinjite) {
        String[][] checkMasuArray = {
                {"d1s1", "d2s1", "d3s1", "d4s1", "d5s1", "d6s1", "d7s1", "d8s1", "d9s1"},
                {"d1s2", "d2s2", "d3s2", "d4s2", "d5s2", "d6s2", "d7s2", "d8s2", "d9s2"},
                {"d1s3", "d2s3", "d3s3", "d4s3", "d5s3", "d6s3", "d7s3", "d8s3", "d9s3"},
                {"d1s4", "d2s4", "d3s4", "d4s4", "d5s4", "d6s4", "d7s4", "d8s4", "d9s4"},
                {"d1s5", "d2s5", "d3s5", "d4s5", "d5s5", "d6s5", "d7s5", "d8s5", "d9s5"},
                {"d1s6", "d2s6", "d3s6", "d4s6", "d5s6", "d6s6", "d7s6", "d8s6", "d9s6"},
                {"d1s7", "d2s7", "d3s7", "d4s7", "d5s7", "d6s7", "d7s7", "d8s7", "d9s7"},
                {"d1s8", "d2s8", "d3s8", "d4s8", "d5s8", "d6s8", "d7s8", "d8s8", "d9s8"},
                {"d1s9", "d2s9", "d3s9", "d4s9", "d5s9", "d6s9", "d7s9", "d8s9", "d9s9"}
        };
        String checkPiece = searchPiece;//確認対象の駒
        int startSuji = Integer.parseInt(startMasu.substring(startMasu.length() - 1));//末尾を取得
        int checkSuji = Integer.parseInt(endMasu.substring(endMasu.length() - 1));//末尾を取得
        if (checkKinjite == 1) {
            for (int i = 0; i < checkMasuArray[checkSuji - 1].length; i++) {
                if (this.banRecordMap.get(checkMasuArray[checkSuji - 1][i]).equals(checkPiece)) {
                    //禁じ手二歩
                    this.kinjiteFlg = true;
                }
            }
            return;
        }
        if ((checkPiece.equals("FU")) || (checkPiece.equals("fu"))) {
            if ((this.firstBanInOutFlg) && (this.secondBanInOutFlg) && (checkSuji == startSuji)) {
                //盤→盤(同じ筋の移動)
                return;
            } else {
                for (int i = 0; i < checkMasuArray[checkSuji - 1].length; i++) {
                    if (this.banRecordMap.get(checkMasuArray[checkSuji - 1][i]).equals(checkPiece)) {
                        //二歩
                        this.nifuFlg = true;
                    }
                }
            }
        } else {
            //歩でない
            return;
        }
    }

    //自陣の駒名を敵陣の駒名に、敵陣の駒名を自陣の駒名に変える。
    public String changeTopBottomPieceName(String targetPiece) {
        String[] bottomPieceNameArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "gy"};
        String[] topPieceNameArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "OU", "gy"};
        String returnPieceName = "";
        if (Arrays.asList(bottomPieceNameArray).contains(targetPiece)) {
            //自陣の駒を敵陣の駒に変える。
            int index = Arrays.asList(bottomPieceNameArray).indexOf(targetPiece);
            returnPieceName = topPieceNameArray[index];
        } else if (Arrays.asList(topPieceNameArray).contains(targetPiece)) {
            //自陣の駒を敵陣の駒に変える。
            int index = Arrays.asList(topPieceNameArray).indexOf(targetPiece);
            returnPieceName = bottomPieceNameArray[index];
        }
        return returnPieceName;
    }

    //成り駒名を元の駒名に戻す。
    public String changeNoPromotionPieceName(String targetPiece, String topBottom) {
        String[] bottomPieceNameArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "TO", "NY", "NK", "NG", "UM", "RY"};
        String[] topPieceNameArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "to", "ny", "nk", "ng", "um", "ry"};
        String[] komadaiBottomPieceNameArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "FU", "KY", "KE", "GI", "KA", "HI"};
        String[] komadaiTopPieceNameArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "fu", "ky", "ke", "gi", "ka", "hi"};
        String returnPieceName = "";
        int index = 0;
        if (Arrays.asList(bottomPieceNameArray).contains(targetPiece)) {
            //自陣の成り駒を元の駒に変える。
            index = Arrays.asList(bottomPieceNameArray).indexOf(targetPiece);
        } else if (Arrays.asList(topPieceNameArray).contains(targetPiece)) {
            //敵陣の駒を敵陣の駒に変える。
            index = Arrays.asList(topPieceNameArray).indexOf(targetPiece);
        }
        if (topBottom.equals("top")) {
            returnPieceName = komadaiTopPieceNameArray[index];
        } else if (topBottom.equals("bottom")) {
            returnPieceName = komadaiBottomPieceNameArray[index];
        }
        return returnPieceName;
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

    //駒台リストから指定した駒を1枚削除する。
    public void deleteKomadaiPiece(List<String> targetList, String targePiece) {
        for (int i = 0; i < targetList.size(); i++) {
            if (targetList.get(i).equals(targePiece)) {
                targetList.remove(i);
                break;
            }
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
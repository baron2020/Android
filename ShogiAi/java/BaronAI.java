package com.teamshiny.shogiai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//AIの最終候補手を絞る
public class BaronAI {
    private Map<String, String> shogiBanMap;
    private List<String> motigomaList;//持ち駒リスト

    private List<Integer> maxLabel20; //着手ラベル20(高い順)
    private List<Float> maxValue20;//確率20(高い順)
    private List<Integer> gouhousyuIntList;//合法手ラベル
    private List<Float> probabilityList;//合法手確率
    private List<String> comGuiGouhousyu;//guiから合法手を取得
    private List<String> comPredictGouhousyuList;//予測から合法手を取得
    private List<Integer> aiPromotionList;//成りか成らずか

    private List<String> finalList;//最終候補手

    public BaronAI(Map<String, String> shogiBanMap, List<String> motigomaList, List<Integer> maxLabel20, List<Float> maxValue20) {
        this.shogiBanMap = shogiBanMap;
        this.motigomaList = motigomaList;
        this.maxLabel20 = maxLabel20;
        this.maxValue20 = maxValue20;
        //Log.i("baron", "王手をかけられている？" + checkOute( this.shogiBanMap ));
    }

    //最終候補を一手返す
    public String returnFinalAI() {
        setComGuiGouhousyu();
        this.finalList = new ArrayList<>();//合法手リスト
        String finalAI = "";//最終着手
        //相手の王があれば狙う処理---------------------------------------------------------------------
        String rivalKingMasu=checkRivalKingMasu();
        //Log.i("baron", "相手の王の位置:"+rivalKingMasu);
        //Log.i("baron", "バロンAIGUI合法手:" + comGuiGouhousyu);
        for(int i=0;i<comGuiGouhousyu.size();i++){
            String temp=comGuiGouhousyu.get(i).substring(0, 4);//左から4文字切り出し
            if(rivalKingMasu.equals(temp)){
                finalAI=comGuiGouhousyu.get(i);
                break;
            }
        }
        //Log.i("baron", "finalAI:" + finalAI);
        if(finalAI!=""){
            //Log.i("baron", "finalAI王が取れます。" +finalAI);
            return finalAI;//最終手
        }
        //------------------------------------------------------------------------------------------
        setComPredictGouhousyu();
        //Log.i("baron", "バロンの持ち駒" + motigomaList);
        //Log.i("baron", "this.maxLabel20" + this.maxLabel20);
        //[52, 47, 146, 309, 45, 50, 53, 148, 49, 149, 231, 310, 228, 51, 147, 69, 988, 48, 65, 230]
        //Log.i("baron", "this.maxValue20" + this.maxValue20);
        //Log.i("baron", "バロンAI合法手ラベル" + gouhousyuIntList);
        //Log.i("baron", "バロンAI合法手確率" + probabilityList);
        //Log.i("baron", "バロンAIpre合法手" + comPredictGouhousyuList);
        //Log.i("baron", "バロンAI成り" + aiPromotionList);
        int preLen = comPredictGouhousyuList.size();
        int guiLen = comGuiGouhousyu.size();
        for (int i = 0; i < preLen; i++) {
            for (int j = 0; j < guiLen; j++) {
                if (comPredictGouhousyuList.get(i).equals(comGuiGouhousyu.get(j))) {
                    finalList.add(comPredictGouhousyuList.get(i) + aiPromotionList.get(i));
                }
            }
        }

        if (guiLen == 0) {
            finalAI = "負けました";
        } else {
            if (finalList.size() >= 1) {
                finalAI = finalList.get(0);
            } else {
                //予測に合法手が無いため、ランダムに候補手を選びます。
                Random rand = new Random();
                int random = rand.nextInt(guiLen);//0～指定した範囲の乱数を生成する
                finalAI = comGuiGouhousyu.get(random);
            }
        }
        return finalAI;//最終手
    }

    //相手の王を狙う処理
    public  String checkRivalKingMasu() {
        String[] shogiBanMapKeys = new String[]{
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
        String rivalKingMasu="";
        for(int i=0;i<shogiBanMapKeys.length;i++){
            if(this.shogiBanMap.get(shogiBanMapKeys[i]).equals("gy")){
                rivalKingMasu=shogiBanMapKeys[i];
            }
        }
        return rivalKingMasu;
    }

    //guiからAIの合法手をリストに格納する。
    public void setComGuiGouhousyu() {
        this.comGuiGouhousyu = new ArrayList<String>();//バロンの合法手
        List<String> existMasuList = new ArrayList<String>();//バロンの合法手
        String[] shogiBanMapKeys = new String[]{
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
        String[] comPieceList = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "TO", "NY", "NK", "NG", "UM", "RY"};

        for (int i = 0; i < shogiBanMapKeys.length; i++) {
            if (Arrays.asList(comPieceList).contains(shogiBanMap.get(shogiBanMapKeys[i]))) {
                existMasuList.add(shogiBanMapKeys[i]);
            }
        }
        setComGuiGouhousyu2(existMasuList);
        setComGuiGouhousyu3(this.motigomaList);
    }

    //GUIからcomの合法手をリストに格納する。
    public void setComGuiGouhousyu2(List<String> targetMasuList) {
        //10種類の駒と10種類の動き
        //駒の動き
        //８×９
        //７０１
        //６駒２
        //５４３
        int[][] pieceMotionYX = new int[][]{
                {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-2, -1}, {-2, 1}
        };
        int[][] pieceMotionTable = new int[][]{
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},//歩
                {2, 0, 0, 0, 0, 0, 0, 0, 0, 0},//香
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1},//桂
                {1, 1, 0, 1, 0, 1, 0, 1, 0, 0}, //銀
                {1, 1, 1, 0, 1, 0, 1, 1, 0, 0},//金
                {0, 2, 0, 2, 0, 2, 0, 2, 0, 0},//角
                {2, 0, 2, 0, 2, 0, 2, 0, 0, 0},//飛
                {1, 1, 1, 1, 1, 1, 1, 1, 0, 0},//王
                {1, 2, 1, 2, 1, 2, 1, 2, 0, 0},//馬
                {2, 1, 2, 1, 2, 1, 2, 1, 0, 0}//竜
        };
        String[] comPieceArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "UM", "RY", "TO", "NY", "NK", "NG"};
        for (int i = 0; i < targetMasuList.size(); i++) {
            int choicePieceNumber = Arrays.asList(comPieceArray).indexOf(shogiBanMap.get(targetMasuList.get(i)));
            int indexNumber = 0;
            //ときん,成香,成桂,成銀はindexNumberを4にし、金と同じ動きを参照する
            if (choicePieceNumber >= 10) {
                choicePieceNumber = 4;
            }
            int typeMotion, motionY, motionX, addY, addX;
            String checkMasu;//仮の移動先のマス
            for (int j = 0; j < pieceMotionTable[0].length; j++) {
                typeMotion = pieceMotionTable[choicePieceNumber][j];
                motionY = Integer.parseInt(targetMasuList.get(i).substring(1, 2));//二文字目を取得
                motionX = Integer.parseInt(targetMasuList.get(i).substring(targetMasuList.get(i).length() - 1));//末尾を取得
                if (typeMotion == 0) {
                    continue;
                }
                if (typeMotion >= 1) {
                    addY = pieceMotionYX[j][0];
                    addX = pieceMotionYX[j][1];
                    do {
                        motionY += addY;
                        motionX += addX;
                        if ((motionY <= 0) || (motionY >= 10) || (motionX <= 0) || (motionX >= 10)) {
                            break;//移動先が盤外であればスルーする
                        }
                        checkMasu = "d" + motionY + "s" + motionX;
                        if (Arrays.asList(comPieceArray).contains(this.shogiBanMap.get(checkMasu))) {
                            break;//移動先に自陣の駒がある
                        }
                        if (checkKinjite(this.shogiBanMap, checkMasu, this.shogiBanMap.get(targetMasuList.get(i)), targetMasuList.get(i))) {
                            continue;//禁じ手である
                        }
                        //checkMasu:移動先のマス、shogiBanMap.get(targetMasuList.get(i)):駒、targetMasuList.get(i):移動前のマス
                        String addGouhousyu = checkMasu + this.shogiBanMap.get(targetMasuList.get(i)) + targetMasuList.get(i);
                        this.comGuiGouhousyu.add(addGouhousyu);//playerの合法手リストに格納
                        if (typeMotion == 1) {
                            break;//１の時は繰り返さずにdo～whileを抜ける
                        }
                    } while ((typeMotion == 2) && (this.shogiBanMap.get(checkMasu).equals("None")));//移動先に駒がない＆飛車,角,香,竜,馬の２の動きの間は繰り返す。
                }
            }
        }
    }

    //将棋盤を仮に動かし禁じ手かどうか確認する
    public boolean checkKinjite(Map<String, String> targetMap, String endMasu, String movePiece, String startMasu) {
        Map<String, String> tempShogiBanMap = new HashMap<String, String>(targetMap);
        tempShogiBanMap.put(startMasu, "None");
        tempShogiBanMap.put(endMasu, movePiece);
        boolean kinjiteFlg = checkOute(tempShogiBanMap);
        return kinjiteFlg;
    }

    //王手確認する。
    public boolean checkOute(Map<String, String> targetMap) {
        List<String> rivalMotionList = new ArrayList<>();//相手の駒の合法手
        List<String> rivalExistList = new ArrayList<>();//相手の駒の位置
        //10種類の駒と10種類の動き
        int[][] pieceMotionTable = new int[][]{
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0},//歩
                {2, 0, 0, 0, 0, 0, 0, 0, 0, 0},//香
                {0, 0, 0, 0, 0, 0, 0, 0, 1, 1},//桂
                {1, 1, 0, 1, 0, 1, 0, 1, 0, 0}, //銀
                {1, 1, 1, 0, 1, 0, 1, 1, 0, 0},//金
                {0, 2, 0, 2, 0, 2, 0, 2, 0, 0},//角
                {2, 0, 2, 0, 2, 0, 2, 0, 0, 0},//飛
                {1, 1, 1, 1, 1, 1, 1, 1, 0, 0},//王
                {1, 2, 1, 2, 1, 2, 1, 2, 0, 0},//馬
                {2, 1, 2, 1, 2, 1, 2, 1, 0, 0}//竜
        };
        //駒の動き
        //８×９
        //７０１
        //６駒２
        //５４３
        int[][] pieceMotionYX = new int[][]{
                {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {2, 1}, {2, -1}
        };
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
        String[] targetPieceArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "um", "ry", "to", "ny", "nk", "ng"};
        for (int i = 0; i < keys.length; i++) {
            if (!(Arrays.asList(targetPieceArray).contains(targetMap.get(keys[i])))) {
                continue;
            } else {
                //含まれている駒なら
                rivalExistList.add(keys[i] + targetMap.get(keys[i]));
                int choicePieceNumber = Arrays.asList(targetPieceArray).indexOf(targetMap.get(keys[i]));
                //ときん,成香,成桂,成銀はindexNumberを4にし、金と同じ動きを参照する
                if (choicePieceNumber >= 10) {
                    choicePieceNumber = 4;
                }
                int typeMotion, motionY, motionX, addY, addX;
                String checkMasu;//仮の移動先のマス
                for (int j = 0; j < pieceMotionTable[0].length; j++) {
                    typeMotion = pieceMotionTable[choicePieceNumber][j];
                    motionY = Integer.parseInt(keys[i].substring(1, 2));//二文字目を取得
                    motionX = Integer.parseInt(keys[i].substring(keys[i].length() - 1));//末尾を取得

                    if (typeMotion == 0) {
                        continue;
                    }
                    if (typeMotion >= 1) {
                        addY = pieceMotionYX[j][0];
                        addX = pieceMotionYX[j][1];
                        do {
                            motionY += addY;
                            motionX += addX;
                            if ((motionY <= 0) || (motionY >= 10) || (motionX <= 0) || (motionX >= 10)) {
                                break;//移動先が盤外であればスルーする
                            }
                            checkMasu = "d" + motionY + "s" + motionX;
                            if (Arrays.asList(targetPieceArray).contains(targetMap.get(checkMasu))) {
                                break;//移動先に自陣の駒がある
                            }
                            rivalMotionList.add(checkMasu);//ライバルの合法手リストに格納
                            if (typeMotion == 1) {
                                break;//１の時は繰り返さずにdo～whileを抜ける
                            }
                        } while ((typeMotion == 2) && (targetMap.get(checkMasu).equals("None")));//移動先に駒がない＆飛車,角,香,竜,馬の２の動きの間は繰り返す。
                    }
                }
            }
        }
        String kingMasu = "";
        for (int i = 0; i < keys.length; i++) {
            if (targetMap.get(keys[i]).equals("OU")) {
                kingMasu = keys[i];
            }
        }
        boolean outeFlg;
        if (rivalMotionList.contains(kingMasu)) {
            //含まれている
            outeFlg = true;
        } else {
            outeFlg = false;
        }
        return outeFlg;
    }


    //持ち駒から合法手をリストに格納する。
    public void setComGuiGouhousyu3(List<String> targetMotigomaList) {
        //駒別枚数データ(歩,香,桂,銀,金,角,飛)
        String[] comPieceArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI"};
        int[] motigomaNumArray = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < targetMotigomaList.size(); i++) {
            if (targetMotigomaList.get(i).equals("fu")) {
                motigomaNumArray[0] += 1;
            } else if (targetMotigomaList.get(i).equals("ky")) {
                motigomaNumArray[1] += 1;
            } else if (targetMotigomaList.get(i).equals("ke")) {
                motigomaNumArray[2] += 1;
            } else if (targetMotigomaList.get(i).equals("gi")) {
                motigomaNumArray[3] += 1;
            } else if (targetMotigomaList.get(i).equals("ki")) {
                motigomaNumArray[4] += 1;
            } else if (targetMotigomaList.get(i).equals("ka")) {
                motigomaNumArray[5] += 1;
            } else if (targetMotigomaList.get(i).equals("hi")) {
                motigomaNumArray[6] += 1;
            }
        }
        //Log.i("baron", "バロンmotigomaNumArray" + motigomaNumArray);
        for (int i = 0; i < motigomaNumArray.length; i++) {
            if (motigomaNumArray[i] == 0) {
                continue;
            }
            if (i == 0) {
                checkUseFU();
            } else if (i == 1) {
                checkUseKY();
            } else if (i == 2) {
                checkUseKE();
            } else {
                checkUseMotigoma(comPieceArray[i]);
            }
        }
    }

    //持ち駒の歩の合法手確認
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
        int[] checkNifuArray = {0, 0, 0, 0, 0, 0, 0, 0, 0};//"s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8","s9"
        for (int i = 0; i < checkMasuArray.length; i++) {
            for (int j = 0; j < checkMasuArray[i].length; j++) {
                if (shogiBanMap.get(checkMasuArray[i][j]).equals("FU")) {
                    checkNifuArray[i] += 1;
                    break;
                }
            }
        }
        //Log.i("baron", "二歩チェック:  " + Arrays.toString(checkNifuArray));
        for (int i = 0; i < checkMasuArray.length; i++) {
            for (int j = 0; j < checkMasuArray[i].length; j++) {
                if (checkNifuArray[i] != 0) {
                    break;
                }
                if (shogiBanMap.get(checkMasuArray[i][j]).equals("None")) {
                    if (checkKinjite(shogiBanMap, checkMasuArray[i][j], "FU", checkMasuArray[i][j])) {
                        continue;//禁じ手である
                    }
                    String addGouhousyu = checkMasuArray[i][j] + "FU" + "もちごま";
                    this.comGuiGouhousyu.add(addGouhousyu);
                }
            }
        }
    }

    //持ち駒の香の合法手確認
    public void checkUseKY() {
        String[] checkMasuArray = {
                "d2s1", "d3s1", "d4s1", "d5s1", "d6s1", "d7s1", "d8s1", "d9s1",
                "d2s2", "d3s2", "d4s2", "d5s2", "d6s2", "d7s2", "d8s2", "d9s2",
                "d2s3", "d3s3", "d4s3", "d5s3", "d6s3", "d7s3", "d8s3", "d9s3",
                "d2s4", "d3s4", "d4s4", "d5s4", "d6s4", "d7s4", "d8s4", "d9s4",
                "d2s5", "d3s5", "d4s5", "d5s5", "d6s5", "d7s5", "d8s5", "d9s5",
                "d2s6", "d3s6", "d4s6", "d5s6", "d6s6", "d7s6", "d8s6", "d9s6",
                "d2s7", "d3s7", "d4s7", "d5s7", "d6s7", "d7s7", "d8s7", "d9s7",
                "d2s8", "d3s8", "d4s8", "d5s8", "d6s8", "d7s8", "d8s8", "d9s8",
                "d2s9", "d3s9", "d4s9", "d5s9", "d6s9", "d7s9", "d8s9", "d9s9"
        };
        for (int i = 0; i < checkMasuArray.length; i++) {
            if (shogiBanMap.get(checkMasuArray[i]).equals("None")) {
                if (checkKinjite(this.shogiBanMap, checkMasuArray[i], "KY", checkMasuArray[i])) {
                    continue;//禁じ手である
                }
                String addGouhousyu = checkMasuArray[i] + "KY" + "もちごま";
                this.comGuiGouhousyu.add(addGouhousyu);
            }
        }
    }

    //持ち駒の桂の合法手確認
    public void checkUseKE() {
        String[] checkMasuArray = {
                "d3s1", "d4s1", "d5s1", "d6s1", "d7s1", "d8s1", "d9s1",
                "d3s2", "d4s2", "d5s2", "d6s2", "d7s2", "d8s2", "d9s2",
                "d3s3", "d4s3", "d5s3", "d6s3", "d7s3", "d8s3", "d9s3",
                "d3s4", "d4s4", "d5s4", "d6s4", "d7s4", "d8s4", "d9s4",
                "d3s5", "d4s5", "d5s5", "d6s5", "d7s5", "d8s5", "d9s5",
                "d3s6", "d4s6", "d5s6", "d6s6", "d7s6", "d8s6", "d9s6",
                "d3s7", "d4s7", "d5s7", "d6s7", "d7s7", "d8s7", "d9s7",
                "d3s8", "d4s8", "d5s8", "d6s8", "d7s8", "d8s8", "d9s8",
                "d3s9", "d4s9", "d5s9", "d6s9", "d7s9", "d8s9", "d9s9"
        };

        for (int i = 0; i < checkMasuArray.length; i++) {
            if (shogiBanMap.get(checkMasuArray[i]).equals("None")) {
                if (checkKinjite(this.shogiBanMap, checkMasuArray[i], "KE", checkMasuArray[i])) {
                    continue;//禁じ手である
                }
                String addGouhousyu = checkMasuArray[i] + "KE" + "もちごま";
                this.comGuiGouhousyu.add(addGouhousyu);
            }
        }
    }

    //持ち駒の合法手確認
    public void checkUseMotigoma(String targetPiece) {
        String[] checkMasuArray = {
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
        for (int i = 0; i < checkMasuArray.length; i++) {
            if (shogiBanMap.get(checkMasuArray[i]).equals("None")) {
                if (checkKinjite(this.shogiBanMap, checkMasuArray[i], targetPiece, checkMasuArray[i])) {
                    continue;//禁じ手である
                }
                String addGouhousyu = checkMasuArray[i] + targetPiece + "もちごま";
                this.comGuiGouhousyu.add(addGouhousyu);
            }
        }
    }


    public void setComPredictGouhousyu() {
        this.gouhousyuIntList = new ArrayList<>();//合法手ラベル
        this.probabilityList = new ArrayList<>();//合法手確率
        this.comPredictGouhousyuList = new ArrayList<>();//aiの合法手リスト
        this.aiPromotionList = new ArrayList<>();//成り成らず
        for (int i = 0; i < this.maxLabel20.size(); i++) {
            setComPredictGouhousyu2(this.maxLabel20.get(i), this.maxValue20.get(i));
        }
    }

    //添え字,確率を受け取り、添え字が合法手ならリストに格納する
    private void setComPredictGouhousyu2(int targetLabel, float targetOut) {
        String[] masuKeys = new String[]{
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
        //上、左上、右上、左、右、下、左下、右下、左桂、右桂
        int[][] motionYX = new int[][]{
                {1, 0}, {1, 1}, {1, -1}, {0, 1}, {0, -1}, {-1, 0}, {-1, 1}, {-1, -1}, {2, 1}, {2, -1},
                {1, 0}, {1, 1}, {1, -1}, {0, 1}, {0, -1}, {-1, 0}, {-1, 1}, {-1, -1}, {2, 1}, {2, -1}
        };
        int masuInt = targetLabel % 81;//移動先のマス
        int motionInt = targetLabel / 81;//移動の動き
        String targetMasu = masuKeys[masuInt];
        int targetY = Integer.parseInt(targetMasu.substring(1, 2));//Y
        int targetX = Integer.parseInt(targetMasu.substring(3, 4));//X
        String[] comPieceArray = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "TO", "NY", "NK", "NG", "UM", "RY"};
        if (Arrays.asList(comPieceArray).contains(shogiBanMap.get(targetMasu))) {
            //自陣の駒がある
            return;
        }
        //移動前:Before moving
        //移動後:After moving
        int loopCount = 0;
        if ((motionInt == 0) || (motionInt == 10)) {
            //上の動き
            while (true) {
                targetY += motionYX[motionInt][0];
                targetX += motionYX[motionInt][1];
                if ((targetY <= 0) || (targetY >= 10) || (targetX <= 0) || (targetX >= 10)) {
                    break;
                }
                String cheakMasu = "d" + targetY + "s" + targetX;
                if (shogiBanMap.get(cheakMasu).equals("None")) {
                    loopCount++;
                    continue;
                }
                String[] cheakNoArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "to", "ny", "nk", "ng", "um", "ry", "KE", "KA"};
                if (Arrays.asList(cheakNoArray).contains(shogiBanMap.get(cheakMasu))) {
                    //NGリストに含まれている駒なら
                    break;
                }
                if (loopCount == 0) {
                    this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                    float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                    this.probabilityList.add(addValue);//確率
                    String targetPiece = shogiBanMap.get(cheakMasu);//移動する駒
                    String afterMasu = masuKeys[masuInt];//移動先
                    String beforeMasu = cheakMasu;//移動前
                    String aiGouhousyu = afterMasu + targetPiece + beforeMasu;
                    this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                    if (motionInt == 0) {
                        this.aiPromotionList.add(0);//成らず
                    } else {
                        this.aiPromotionList.add(1);//成り
                    }
                    break;
                } else {
                    if ((shogiBanMap.get(cheakMasu).equals("KY")) || (shogiBanMap.get(cheakMasu).equals("HI")) || (shogiBanMap.get(cheakMasu).equals("RY"))) {
                        this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                        float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                        this.probabilityList.add(addValue);//確率
                        String targetPiece = shogiBanMap.get(cheakMasu);//移動する駒
                        String afterMasu = masuKeys[masuInt];//移動先
                        String beforeMasu = cheakMasu;//移動前
                        String aiGouhousyu = afterMasu + targetPiece + beforeMasu;
                        this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                        if (motionInt == 0) {
                            this.aiPromotionList.add(0);//成らず
                        } else {
                            this.aiPromotionList.add(1);//成り
                        }
                        break;
                    } else {
                        break;
                    }
                }
            }
        } else if ((motionInt == 1) || (motionInt == 2) || (motionInt == 11) || (motionInt == 12)) {
            //左上,右上の動き
            while (true) {
                targetY += motionYX[motionInt][0];
                targetX += motionYX[motionInt][1];
                if ((targetY <= 0) || (targetY >= 10) || (targetX <= 0) || (targetX >= 10)) {
                    break;
                }
                String cheakMasu = "d" + targetY + "s" + targetX;
                if (shogiBanMap.get(cheakMasu).equals("None")) {
                    loopCount++;
                    continue;
                }
                String[] cheakNoArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "to", "ny", "nk", "ng", "um", "ry", "FU", "KY", "KE", "HI"};
                if (Arrays.asList(cheakNoArray).contains(shogiBanMap.get(cheakMasu))) {
                    //NGリストに含まれている駒なら
                    break;
                }
                if (loopCount == 0) {
                    this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                    float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                    this.probabilityList.add(addValue);//確率
                    String targetPiece = shogiBanMap.get(cheakMasu);//移動する駒
                    String afterMasu = masuKeys[masuInt];//移動先
                    String beforeMasu = cheakMasu;//移動前
                    String aiGouhousyu = afterMasu + targetPiece + beforeMasu;
                    this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                    if ((motionInt == 1) || (motionInt == 2)) {
                        this.aiPromotionList.add(0);//成らず
                    } else {
                        this.aiPromotionList.add(1);//成り
                    }
                    break;
                } else {
                    if ((shogiBanMap.get(cheakMasu).equals("KA")) || (shogiBanMap.get(cheakMasu).equals("UM"))) {
                        this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                        float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                        this.probabilityList.add(addValue);//確率
                        String targetPiece = shogiBanMap.get(cheakMasu);//移動する駒
                        String afterMasu = masuKeys[masuInt];//移動先
                        String beforeMasu = cheakMasu;//移動前
                        String aiGouhousyu = afterMasu + targetPiece + beforeMasu;
                        this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                        if ((motionInt == 1) || (motionInt == 2)) {
                            this.aiPromotionList.add(0);//成らず
                        } else {
                            this.aiPromotionList.add(1);//成り
                        }
                        break;
                    } else {
                        break;
                    }
                }
            }
        } else if ((motionInt == 3) || (motionInt == 4) || (motionInt == 5) || (motionInt == 13) || (motionInt == 14) || (motionInt == 15)) {
            //左,右,下の動き
            while (true) {
                targetY += motionYX[motionInt][0];
                targetX += motionYX[motionInt][1];
                if ((targetY <= 0) || (targetY >= 10) || (targetX <= 0) || (targetX >= 10)) {
                    break;
                }
                String cheakMasu = "d" + targetY + "s" + targetX;

                if (shogiBanMap.get(cheakMasu).equals("None")) {
                    loopCount++;
                    continue;
                }
                String[] cheakNoArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "to", "ny", "nk", "ng", "um", "ry", "FU", "KY", "KE", "GI", "KA"};
                if (Arrays.asList(cheakNoArray).contains(shogiBanMap.get(cheakMasu))) {
                    //NGリストに含まれている駒なら
                    break;
                }
                if (loopCount == 0) {
                    this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                    float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                    this.probabilityList.add(addValue);//確率
                    String targetPiece = shogiBanMap.get(cheakMasu);//移動する駒
                    String afterMasu = masuKeys[masuInt];//移動先
                    String beforeMasu = cheakMasu;//移動前
                    String aiGouhousyu = afterMasu + targetPiece + beforeMasu;
                    this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                    if ((motionInt == 3) || (motionInt == 4) || (motionInt == 5)) {
                        this.aiPromotionList.add(0);//成らず
                    } else {
                        this.aiPromotionList.add(1);//成り
                    }
                    break;
                } else {
                    if ((shogiBanMap.get(cheakMasu).equals("HI")) || (shogiBanMap.get(cheakMasu).equals("RY"))) {
                        this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                        float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                        this.probabilityList.add(addValue);//確率
                        String targetPiece = shogiBanMap.get(cheakMasu);//移動する駒
                        String afterMasu = masuKeys[masuInt];//移動先
                        String beforeMasu = cheakMasu;//移動前
                        String aiGouhousyu = afterMasu + targetPiece + beforeMasu;
                        this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                        if ((motionInt == 3) || (motionInt == 4) || (motionInt == 5)) {
                            this.aiPromotionList.add(0);//成らず
                        } else {
                            this.aiPromotionList.add(1);//成り
                        }
                        break;
                    } else {
                        break;
                    }
                }
            }
        } else if ((motionInt == 6) || (motionInt == 7) || (motionInt == 16) || (motionInt == 17)) {
            //左下,右下の動き
            while (true) {
                targetY += motionYX[motionInt][0];
                targetX += motionYX[motionInt][1];
                if ((targetY <= 0) || (targetY >= 10) || (targetX <= 0) || (targetX >= 10)) {
                    break;
                }
                String cheakMasu = "d" + targetY + "s" + targetX;
                if (shogiBanMap.get(cheakMasu).equals("None")) {
                    loopCount++;
                    continue;
                }
                String[] cheakNoArray = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "to", "ny", "nk", "ng", "um", "ry", "FU", "KY", "KE", "KI", "HI", "TO", "NY", "NK", "NG"};
                if (Arrays.asList(cheakNoArray).contains(shogiBanMap.get(cheakMasu))) {
                    //NGリストに含まれている駒なら
                    break;
                }
                if (loopCount == 0) {
                    this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                    float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                    this.probabilityList.add(addValue);//確率
                    String targetPiece = shogiBanMap.get(cheakMasu);//移動する駒
                    String afterMasu = masuKeys[masuInt];//移動先
                    String beforeMasu = cheakMasu;//移動前
                    String aiGouhousyu = afterMasu + targetPiece + beforeMasu;
                    this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                    if ((motionInt == 6) || (motionInt == 7)) {
                        this.aiPromotionList.add(0);//成らず
                    } else {
                        this.aiPromotionList.add(1);//成り
                    }
                    break;
                } else {
                    if ((shogiBanMap.get(cheakMasu).equals("KA")) || (shogiBanMap.get(cheakMasu).equals("UM")) || (shogiBanMap.get(cheakMasu).equals("RY"))) {
                        this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                        float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                        this.probabilityList.add(addValue);//確率
                        String targetPiece = shogiBanMap.get(cheakMasu);//移動する駒
                        String afterMasu = masuKeys[masuInt];//移動先
                        String beforeMasu = cheakMasu;//移動前
                        String aiGouhousyu = afterMasu + targetPiece + beforeMasu;
                        this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                        if ((motionInt == 6) || (motionInt == 7)) {
                            this.aiPromotionList.add(0);//成らず
                        } else {
                            this.aiPromotionList.add(1);//成り
                        }
                        break;
                    } else {
                        break;
                    }
                }
            }
        } else if ((motionInt == 8) || (motionInt == 9) || (motionInt == 18) || (motionInt == 19)) {
            //桂馬の動き
            while (true) {
                targetY += motionYX[motionInt][0];
                targetX += motionYX[motionInt][1];
                if ((targetY <= 0) || (targetY >= 10) || (targetX <= 0) || (targetX >= 10)) {
                    break;
                }
                String cheakMasu = "d" + targetY + "s" + targetX;
                if (shogiBanMap.get(cheakMasu).equals("KE")) {
                    this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                    float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                    this.probabilityList.add(addValue);//確率
                    String targetPiece = shogiBanMap.get(cheakMasu);//移動する駒
                    String afterMasu = masuKeys[masuInt];//移動先
                    String beforeMasu = cheakMasu;//移動前
                    String aiGouhousyu = afterMasu + targetPiece + beforeMasu;
                    this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                    if ((motionInt == 8) || (motionInt == 9)) {
                        this.aiPromotionList.add(0);//成らず
                    } else {
                        this.aiPromotionList.add(1);//成り
                    }
                    break;
                } else {
                    //桂馬でない
                    break;
                }
            }
        } else if (motionInt == 20) {
            //持ち駒の歩を打つ
            //二歩チェック
            if ((shogiBanMap.get(targetMasu).equals("None")) && (motigomaList.contains("fu"))) {
                this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                this.probabilityList.add(addValue);//確率
                String afterMasu = masuKeys[masuInt];//移動先
                String aiGouhousyu = afterMasu + "FU" + "もちごま";
                this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                this.aiPromotionList.add(0);//成らず
            }

        } else if (motionInt == 21) {
            //持ち駒の香を打つ
            if ((shogiBanMap.get(targetMasu).equals("None")) && (motigomaList.contains("ky"))) {
                this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                this.probabilityList.add(addValue);//確率
                String afterMasu = masuKeys[masuInt];//移動先
                String aiGouhousyu = afterMasu + "KY" + "もちごま";
                this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                this.aiPromotionList.add(0);//成らず
            } else if (motionInt == 22) {
                //持ち駒の桂を打つ
                if ((shogiBanMap.get(targetMasu).equals("None")) && (motigomaList.contains("ke"))) {
                    this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                    float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                    this.probabilityList.add(addValue);//確率
                    String afterMasu = masuKeys[masuInt];//移動先
                    String aiGouhousyu = afterMasu + "KE" + "もちごま";
                    this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                    this.aiPromotionList.add(0);//成らず
                }
            }
        } else if (motionInt == 23) {
            //持ち駒の銀を打つ
            if ((shogiBanMap.get(targetMasu).equals("None")) && (motigomaList.contains("gi"))) {
                this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                this.probabilityList.add(addValue);//確率
                String afterMasu = masuKeys[masuInt];//移動先
                String aiGouhousyu = afterMasu + "GI" + "もちごま";
                this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                this.aiPromotionList.add(0);//成らず
            }
        } else if (motionInt == 24) {
            //持ち駒の金を打つ
            if ((shogiBanMap.get(targetMasu).equals("None")) && (motigomaList.contains("ki"))) {
                this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                this.probabilityList.add(addValue);//確率
                String afterMasu = masuKeys[masuInt];//移動先
                String aiGouhousyu = afterMasu + "KI" + "もちごま";
                this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                this.aiPromotionList.add(0);//成り成らず
            }
        } else if (motionInt == 25) {
            //持ち駒の角を打つ
            if ((shogiBanMap.get(targetMasu).equals("None")) && (motigomaList.contains("ka"))) {
                this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                this.probabilityList.add(addValue);//確率
                String afterMasu = masuKeys[masuInt];//移動先
                String aiGouhousyu = afterMasu + "KA" + "もちごま";
                this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                this.aiPromotionList.add(0);//成り成らず
            }
        } else if (motionInt == 26) {
            //持ち駒の飛を打つ
            if ((shogiBanMap.get(targetMasu).equals("None")) && (motigomaList.contains("hi"))) {
                this.gouhousyuIntList.add(targetLabel);//合法手ラベル
                float addValue = (float) ((Math.floor(targetOut * 1000)) / 1000);
                this.probabilityList.add(addValue);//確率
                String afterMasu = masuKeys[masuInt];//移動先
                String aiGouhousyu = afterMasu + "HI" + "もちごま";
                this.comPredictGouhousyuList.add(aiGouhousyu);//aiの合法手リスト
                this.aiPromotionList.add(0);//成り成らず
            }
        }
    }

    //最終候補手リストを返す
    public List<String> getFinalList() {
        return this.finalList;
    }
}

package com.baron.baronothello;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BaronAI {
    private Map<String, String> gameRecord;//盤面情報
    private String baronTeban;//バロンの手番
    private String playerTeban;//playerの手番
    private List<String> gouhousyuArray;//合法手配列

    BaronAI(Map<String, String> gameRecord, String baronTeban) {
        this.gameRecord = new HashMap<>(gameRecord);
        this.baronTeban = baronTeban;
        if (this.baronTeban.equals("black")) {
            this.playerTeban = "white";
        } else if (this.baronTeban.equals("white")) {
            this.playerTeban = "black";
        }
        this.gouhousyuArray = new ArrayList<String>(searchGouhousyu(this.gameRecord, this.baronTeban));
    }

    //バロンの着手
    public String baronTyakusyu() {
        String baronAI;
        if (this.gouhousyuArray.size() == 0) {
            baronAI = "パス";
        } else {
            //baronAI=randomTyakusyu(this.gouhousyuArray); //ランダムな着手
            baronAI = returnMaxHyoukaTyakusyu();//バロンの最大評価値着手
        }
        return baronAI;
    }

    //com思考時間
    public int returnRandomTime() {
        Random rand = new Random();
        int randomTime = (rand.nextInt(4000)) + 1000;//0以上x未満の乱数
        return randomTime;
    }

    //com思考時間
    public class waitTime extends Thread {
        //@Override
        public void run() {
            try {
                int randomTime = returnRandomTime();
                Thread.sleep(randomTime);//1000=1秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //評価により着手を返す。
    public String returnMaxHyoukaTyakusyu() {
        List<Integer> hyoukaArray = new ArrayList<Integer>();//comの合法手の評価点
        List<String> switchArray = null;//手番により切り替え
        List<String> blackGouhousyuArray = new ArrayList<String>(searchGouhousyu(this.gameRecord, "black"));
        List<String> whiteGouhousyuArray = new ArrayList<String>(searchGouhousyu(this.gameRecord, "white"));
        //バロンの合法手を仮想的に動かす
        if (this.baronTeban.equals("black")) {
            switchArray = new ArrayList<String>(blackGouhousyuArray);
        } else if (this.baronTeban.equals("white")) {
            switchArray = new ArrayList<String>(whiteGouhousyuArray);
        }
        for (int i = 0; i < switchArray.size(); i++) {
            int hyoukaValue = 0;
            //指定局面と手番と着手を渡し、指定局面を仮想的に動かして、動かした後の局面を返す。
            Map<String, String> moveTemp = new HashMap<>(virtualMove(this.gameRecord, this.baronTeban, switchArray.get(i)));
            List<String> moveComGouhousyuArray = new ArrayList<String>(searchGouhousyu(moveTemp, this.baronTeban));
            List<String> movePlayerGouhousyuArray = new ArrayList<String>(searchGouhousyu(moveTemp, this.playerTeban));
            //評価
            //1相手の合法手の数を考慮
            //1.1相手の合法手の数が0
            //1.2相手の合法手の数が1＆それがXである
            if (movePlayerGouhousyuArray.size() == 0) {
                hyoukaArray.add(10000);
                continue;
            } else if ((movePlayerGouhousyuArray.size() == 1) && (checkArrayX(movePlayerGouhousyuArray))) {
                hyoukaArray.add(10000);
                continue;
            }
            //2自分の合法手の中から確定石の確認
            //2.1隅
            if (checkStringSumi(switchArray.get(i))) {
                hyoukaArray.add(10000);
                continue;
            }
            //2.2やすり攻め
            List<String> finalStoneList = new ArrayList<String>(returnFinalStoneList());
            boolean continueFlg = false;
            for (int f = 0; f < finalStoneList.size(); f++) {
                if (finalStoneList.get(f).equals(switchArray.get(i))) {
                    hyoukaArray.add(5000);
                    continueFlg = true;
                    break;
                }
            }
            if (continueFlg) {
                continue;
            }
            //３それ以外であれば、X,C,合法手の数,隅を考慮する
            //3.1自分の候補手がXである。
            //3.2自分の候補手がCである。
            if (checkStringX(switchArray.get(i))) {
                hyoukaValue += (-1000);
            } else if (checkStringC(switchArray.get(i))) {
                hyoukaValue += (-100);
            }
            //3.3相手の合法手の中に隅が含まれている。
            if (checkArraySumi(movePlayerGouhousyuArray)) {
                hyoukaValue += (-1000);
            }
            int value1 = moveComGouhousyuArray.size() * 50;
            int value2 = movePlayerGouhousyuArray.size() * -100;
            hyoukaValue += value1;
            hyoukaValue += value2;
            hyoukaArray.add(hyoukaValue);
        }
        //評価値配列から一番値の大きい値を探す。
        int maxIndexNumber = returnMaxIndexNumber(hyoukaArray);
        List<Integer> maxList = new ArrayList<Integer>();//maxリスト
        //最大評価値が複数ある場合はランダム要素を考慮する。
        for (int i = 0; i < switchArray.size(); i++) {
            if (hyoukaArray.get(i).equals(hyoukaArray.get(maxIndexNumber))) {
                maxList.add(i);
            }
        }
        if (maxList.size() >= 2) {
            Random rand = new Random();
            int randomNumber = rand.nextInt(maxList.size());//0以上x未満の乱数
            maxIndexNumber = maxList.get(randomNumber);
        } else {
        }
        return switchArray.get(maxIndexNumber);
    }

    //指定の配列を受け取り、配列の中にXが存在するか調べてtrue,falseを返す。
    public boolean checkArrayX(List<String> targetArray) {
        String[] XArray = new String[]{"d2s2", "d2s7", "d7s2", "d7s7"};//検索対象のX
        for (int i = 0; i < targetArray.size(); i++) {
            if (Arrays.asList(XArray).contains(targetArray.get(i))) {
                return true;
            }
        }
        return false;
    }

    //指定の配列を受け取り、配列の中に隅が存在するか調べてtrue,falseを返す。
    public boolean checkArraySumi(List<String> targetArray) {
        String[] sumiArray = new String[]{"d1s1", "d1s8", "d8s1", "d8s8"};//検索対象の隅
        for (int i = 0; i < targetArray.size(); i++) {
            if (Arrays.asList(sumiArray).contains(targetArray.get(i))) {
                return true;
            }
        }
        return false;
    }

    //指定の文字列を受け取り、その文字列が隅か調べてtrue,falseを返す。
    public boolean checkStringSumi(String targetPoint) {
        String[] sumiArray = new String[]{"d1s1", "d1s8", "d8s1", "d8s8"};//検索対象の隅
        if (Arrays.asList(sumiArray).contains(targetPoint)) {
            return true;
        } else {
            return false;
        }
    }

    //指定の文字列を受け取り、その文字列がXか調べてtrue,falseを返す。
    public boolean checkStringX(String targetPoint) {
        String[] XArray = new String[]{"d2s2", "d2s7", "d7s2", "d7s7"};//検索対象のX
        if (Arrays.asList(XArray).contains(targetPoint)) {
            return true;
        } else {
            return false;
        }
    }

    //指定の文字列を受け取り、その文字列がCか調べてtrue,falseを返す。
    public boolean checkStringC(String targetPoint) {
        String[] CArray = new String[]{"d1s2", "d1s7", "d2s1", "d2s8", "d7s1", "d7s8", "d8s2", "d8s7"};//検索対象のC
        if (Arrays.asList(CArray).contains(targetPoint)) {
            return true;
        } else {
            return false;
        }
    }

    //指定の配列を受け取り、最大値のインデックスを返す。
    public int returnMaxIndexNumber(List<Integer> targetArray) {
        int maxIndexNumber = 0;
        int maxHyoukaValue = targetArray.get(0);
        for (int i = 0; i < targetArray.size(); i++) {
            if (maxHyoukaValue < targetArray.get(i)) {
                maxHyoukaValue = targetArray.get(i);
                maxIndexNumber = i;
            }
        }
        return maxIndexNumber;
    }

    //comの確定石になるマスをリストにして返す。(隅,やすり攻め可能なマス)
    public List<String> returnFinalStoneList() {
        String[] sumiArray = {"d1s1", "d1s8", "d8s1", "d8s8"};//隅のマス
        String[] sumiStartArray = {"d1s1", "d1s8", "d1s8", "d8s8", "d8s8", "d8s1", "d8s1", "d1s1"};//隅の検索順
        //検索順配列
        String[][] searchArray = {
                {"d1s2", "d1s3", "d1s4", "d1s5", "d1s6", "d1s7"},
                {"d1s7", "d1s6", "d1s5", "d1s4", "d1s3", "d1s2"},
                {"d2s8", "d3s8", "d4s8", "d5s8", "d6s8", "d7s8"},
                {"d7s8", "d6s8", "d5s8", "d4s8", "d3s8", "d2s8"},
                {"d8s7", "d8s6", "d8s5", "d8s4", "d8s3", "d8s2"},
                {"d8s2", "d8s3", "d8s4", "d8s5", "d8s6", "d8s7"},
                {"d7s1", "d6s1", "d5s1", "d4s1", "d3s1", "d2s1"},
                {"d2s1", "d3s1", "d4s1", "d5s1", "d6s1", "d7s1"}
        };

        //comの確定石を格納したリスト
        List<String> finalStoneList = new ArrayList<String>();

        //隅を確定石リストに格納する。
        for (int i = 0; i < sumiArray.length; i++) {
            if (this.gameRecord.get(sumiArray[i]).equals("None")) {
                finalStoneList.add(sumiArray[i]);
            }
        }
        //辺の確定石を探す。
        for (int y = 0; y < sumiStartArray.length; y++) {
            int playerStoneCount = 0;
            //対象の隅にcomの石が無い。
            if (!(this.gameRecord.get(sumiStartArray[y]).equals(this.baronTeban))) {
                continue;
            }
            for (int x = 0; x < searchArray[y].length; x++) {
                //1.1空きマスが見つかるまでにplayerの石が無い。
                if ((playerStoneCount == 0) && (this.gameRecord.get(searchArray[y][x]).equals("None"))) {
                    finalStoneList.add(searchArray[y][x]);
                    break;
                }
                //1.2playerの石が見つかった。
                if (this.gameRecord.get(searchArray[y][x]).equals(this.playerTeban)) {
                    playerStoneCount++;
                    continue;
                }
                //1.3playerの石が見つかった後にcomの石がある。
                if ((playerStoneCount >= 1) && (this.gameRecord.get(searchArray[y][x]).equals(this.baronTeban))) {
                    break;
                }
                //1.4playerの石が1個以上の連続で見つかった後に空きマスが見つかる。
                if ((playerStoneCount >= 1) && (this.gameRecord.get(searchArray[y][x]).equals("None"))) {
                    finalStoneList.add(searchArray[y][x]);
                    break;
                }
            }
        }
        return finalStoneList;
    }

    //指定局面と手番を渡し、合法手配列を返す。
    public List<String> searchGouhousyu(Map<String, String> targetGameRecord, String targetTeban) {
        List<String> returnGouhousyuArray;//合法手配列
        String[] gameRecordKeys = new String[]{"d1s1", "d1s2", "d1s3", "d1s4", "d1s5", "d1s6", "d1s7", "d1s8",
                "d2s1", "d2s2", "d2s3", "d2s4", "d2s5", "d2s6", "d2s7", "d2s8",
                "d3s1", "d3s2", "d3s3", "d3s4", "d3s5", "d3s6", "d3s7", "d3s8",
                "d4s1", "d4s2", "d4s3", "d4s4", "d4s5", "d4s6", "d4s7", "d4s8",
                "d5s1", "d5s2", "d5s3", "d5s4", "d5s5", "d5s6", "d5s7", "d5s8",
                "d6s1", "d6s2", "d6s3", "d6s4", "d6s5", "d6s6", "d6s7", "d6s8",
                "d7s1", "d7s2", "d7s3", "d7s4", "d7s5", "d7s6", "d7s7", "d7s8",
                "d8s1", "d8s2", "d8s3", "d8s4", "d8s5", "d8s6", "d8s7", "d8s8"};

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

        if (targetTeban.equals("black")) {
            switchArray = useBlackArray;
        } else if (targetTeban.equals("white")) {
            switchArray = useWhiteArray;
        }
        for (int i = 0; i < gameRecordKeys.length; i++) {
            //gameRecordKeys[i]:合法手確認の対象のマス
            if (!(targetGameRecord.get(gameRecordKeys[i]).equals("None"))) {
                continue;//合法手確認の対象のマスに石があれば抜ける
            }
            targetDan = Integer.parseInt(gameRecordKeys[i].substring(1, 2));//二文字目の段の切り出し
            targetSuji = Integer.parseInt(gameRecordKeys[i].substring(3, 4));//四文字目の筋の切り出し
            for (int j = 0; j < allDirectionArray.length; j++) {
                existRivalStoneFlg = false;//ライバルの石が間に存在しないフラグをFalseにする
                checkDan = targetDan;
                checkSuji = targetSuji;
                while (true) {
                    checkDan += allDirectionArray[j][0];
                    checkSuji += allDirectionArray[j][1];
                    checkMasu = 'd' + String.valueOf(checkDan) + 's' + String.valueOf(checkSuji);
                    if ((checkDan == 0) || (checkSuji == 0) || (checkDan == 9) || (checkSuji == 9)) {
                        break;//盤外であれば抜ける
                    } else {
                        //盤内であれば
                        if (targetGameRecord.get(checkMasu).equals("None")) {
                            break;//一マス先に石がなければ抜ける
                        }
                        if ((existRivalStoneFlg == false) && (targetGameRecord.get(checkMasu).equals(switchArray[0]))) {
                            //[0]:自石
                            break;//#間にライバルの石がない＆一マス先が自石ならぬける
                        }
                        if (targetGameRecord.get(checkMasu).equals(switchArray[1])) {
                            //[1]:ライバルの石
                            existRivalStoneFlg = true;
                            continue;//マスの確認方向を一マス伸ばし処理を続ける
                        }
                        if ((existRivalStoneFlg) && (targetGameRecord.get(checkMasu).equals(switchArray[0]))) {
                            //[0]:自石
                            tempGouhousyuArray.add(gameRecordKeys[i]);//合法手を配列に格納
                            existRivalStoneFlg = false;//フラグをFalseに戻す
                            break;//ループを抜ける
                        }
                    }
                }
            }
        }
        return returnGouhousyuArray = new ArrayList<String>(new HashSet<>(tempGouhousyuArray));//配列から重複した値を削除する
    }

    //指定局面と手番と着手を渡し、指定局面を仮想的に動かして、動かした後の局面を返す。
    public Map<String, String> virtualMove(Map<String, String> targetGameRecord, String targetTeban, String startingPoint) {
        int[][] allDirectionArray = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};//8方向(上,右上,右,右下,下,左下,左,左上)
        List<String> turnOverStoneArray = new ArrayList<String>();//反転対象配列
        boolean turnOverFlg;//反転動作確認に使用
        String[] switchArray = null;//手番により切り替え
        String[] useBlackArray = {"black", "white"};//手番黒用
        String[] useWhiteArray = {"white", "black"};//手番白用
        int targetDan, targetSuji, checkDan, checkSuji, targetX, targetY;
        String checkMasu;
        //開始局面のコピー
        Map<String, String> startGameRecord = new HashMap<>(targetGameRecord);
        if (targetTeban.equals("black")) {
            switchArray = useBlackArray;
        } else if (targetTeban.equals("white")) {
            switchArray = useWhiteArray;
        }
        //着手したマスに石を置く。
        startGameRecord.put(startingPoint, targetTeban);
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
                if (startGameRecord.get(checkMasu).equals("None")) {
                    //盤内であれば
                    turnOverStoneArray.clear();
                    break;//一マス先に石がなければ抜ける
                }
                if ((!turnOverFlg) && (startGameRecord.get(checkMasu).equals(switchArray[0]))) {
                    //[0]:自石
                    turnOverStoneArray.clear();
                    break;//間にライバルの石がない＆一マス先が自石ならぬける
                }
                if (startGameRecord.get(checkMasu).equals(switchArray[1])) {
                    //[1]:ライバルの石
                    turnOverFlg = true;
                    turnOverStoneArray.add(checkMasu);//反転対象の石が置かれているマスを配列に格納する
                    continue;//マスの確認方向を一マス伸ばし処理を続ける
                }
                if ((turnOverFlg) && (startGameRecord.get(checkMasu).equals(switchArray[0]))) {
                    //[0]:自石
                    //配列をもとに反転させる
                    for (int i = 0; i < turnOverStoneArray.size(); i++) {
                        startGameRecord.put(turnOverStoneArray.get(i), targetTeban);//盤面情報の更新
                    }
                    turnOverFlg = false;//フラグをFalseに戻す
                    break;//ループを抜ける
                }
            }
        }
        return startGameRecord;
    }

    //合法手からランダムな着手を返す。
    public String randomTyakusyu(List<String> targetArray) {
        Random rand = new Random();
        int randomNumber = rand.nextInt(targetArray.size());//0以上x未満の乱数
        String randomTyakusyu = targetArray.get(randomNumber);
        return targetArray.get(randomNumber);
    }

    //着手前の合法手の数の確認
    public void checkGouhousyu() {
        List<String> blackGouhousyuArray = new ArrayList<String>(searchGouhousyu(this.gameRecord, "black"));
        List<String> whiteGouhousyuArray = new ArrayList<String>(searchGouhousyu(this.gameRecord, "white"));
    }
}

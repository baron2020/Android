package com.teamshiny.shogiai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataConversion {
    private Map<String, String> shogiBanMap;//盤情報
    private List<String> goteMotigomaList;//駒台1
    private List<String> senteMotigomaList;//駒台2
    private List<Integer> inputData = new ArrayList<>();//104*9*9を一次元にした配列
    private List<Integer> finalInputData = new ArrayList<>();//9*9*104を一次元にした配列

    public DataConversion(Map<String, String> banRecordMap,
                          List<String> komadaiTopList, List<String> komadaiBottomList) {
        this.shogiBanMap = new HashMap<String, String>(banRecordMap);
        this.goteMotigomaList = new ArrayList<String>(komadaiTopList);
        this.senteMotigomaList = new ArrayList<String>(komadaiBottomList);
    }

    //盤データの変形1
    public List<Integer> dataChange1() {
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
        String[] sentePieceName = {"FU", "KY", "KE", "GI", "KI", "KA", "HI", "OU", "TO", "NY", "NK", "NG", "UM", "RY"};
        String[] gotePieceName = {"fu", "ky", "ke", "gi", "ki", "ka", "hi", "gy", "to", "ny", "nk", "ng", "um", "ry"};
        //持ち駒カウント---------------------------------------------------------------------------------
        //駒別枚数データ(歩,香,桂,銀,金,角,飛,王,玉)
        int[] senteMotigomaNumArray = {0, 0, 0, 0, 0, 0, 0};//"FU", "KY", "KE", "GI", "KI", "KA", "HI"
        int[] goteMotigomaNumArray = {0, 0, 0, 0, 0, 0, 0};//"fu", "ky", "ke", "gi", "ki", "ka", "hi"
        //先手持ち駒
        for (int i = 0; i < this.senteMotigomaList.size(); i++) {
            if (this.senteMotigomaList.get(i).equals("FU")) {
                senteMotigomaNumArray[0] += 1;
            } else if (this.senteMotigomaList.get(i).equals("KY")) {
                senteMotigomaNumArray[1] += 1;
            } else if (this.senteMotigomaList.get(i).equals("KE")) {
                senteMotigomaNumArray[2] += 1;
            } else if (this.senteMotigomaList.get(i).equals("GI")) {
                senteMotigomaNumArray[3] += 1;
            } else if (this.senteMotigomaList.get(i).equals("KI")) {
                senteMotigomaNumArray[4] += 1;
            } else if (this.senteMotigomaList.get(i).equals("KA")) {
                senteMotigomaNumArray[5] += 1;
            } else if (this.senteMotigomaList.get(i).equals("HI")) {
                senteMotigomaNumArray[6] += 1;
            }
        }
        //後手持ち駒
        for (int i = 0; i < this.goteMotigomaList.size(); i++) {
            if (this.goteMotigomaList.get(i).equals("fu")) {
                goteMotigomaNumArray[0] += 1;
            } else if (this.goteMotigomaList.get(i).equals("ky")) {
                goteMotigomaNumArray[1] += 1;
            } else if (this.goteMotigomaList.get(i).equals("ke")) {
                goteMotigomaNumArray[2] += 1;
            } else if (this.goteMotigomaList.get(i).equals("gi")) {
                goteMotigomaNumArray[3] += 1;
            } else if (this.goteMotigomaList.get(i).equals("ki")) {
                goteMotigomaNumArray[4] += 1;
            } else if (this.goteMotigomaList.get(i).equals("ka")) {
                goteMotigomaNumArray[5] += 1;
            } else if (this.goteMotigomaList.get(i).equals("hi")) {
                goteMotigomaNumArray[6] += 1;
            }
        }

        //先手の情報を格納----------------------------------------------------------------------------
        for (int i = 0; i < sentePieceName.length; i++) {
            for (int j = 0; j < shogiBanMapKeys.length; j++) {
                if (sentePieceName[i].equals(this.shogiBanMap.get(shogiBanMapKeys[j]))) {
                    //一致している
                    this.inputData.add(1);
                } else {
                    //一致していない
                    this.inputData.add(0);
                }
            }
        }
        //先手の持ち駒報を格納
        for (int i = 0; i < senteMotigomaNumArray.length; i++) {
            //歩
            if (i == 0) {
                if (senteMotigomaNumArray[i] == 0) {
                    for (int n = 0; n < (9 * 9 * 18); n++) {
                        this.inputData.add(0);
                    }
                } else if (senteMotigomaNumArray[i] != 0) {
                    for (int n = 0; n < (9 * 9 * (senteMotigomaNumArray[i])); n++) {
                        this.inputData.add(1);
                    }
                    for (int n = 0; n < (9 * 9 * (18 - senteMotigomaNumArray[i])); n++) {
                        this.inputData.add(0);
                    }
                }
                continue;
            }
            //香,桂,銀,金
            else if ((i >= 1) && (i <= 4)) {
                if (senteMotigomaNumArray[i] == 0) {
                    for (int n = 0; n < (9 * 9 * 4); n++) {
                        this.inputData.add(0);
                    }
                } else if (senteMotigomaNumArray[i] != 0) {
                    for (int n = 0; n < (9 * 9 * (senteMotigomaNumArray[i])); n++) {
                        this.inputData.add(1);
                    }
                    for (int n = 0; n < (9 * 9 * (4 - senteMotigomaNumArray[i])); n++) {
                        this.inputData.add(0);
                    }
                }
                continue;
            }
            //角,飛車
            else if ((i == 5) || (i == 6)) {
                if (senteMotigomaNumArray[i] == 0) {
                    for (int n = 0; n < (9 * 9 * 2); n++) {
                        this.inputData.add(0);
                    }
                } else if (senteMotigomaNumArray[i] != 0) {
                    for (int n = 0; n < (9 * 9 * (senteMotigomaNumArray[i])); n++) {
                        this.inputData.add(1);
                    }
                    for (int n = 0; n < (9 * 9 * (2 - senteMotigomaNumArray[i])); n++) {
                        this.inputData.add(0);
                    }
                }
                continue;
            }
        }
        //先手の情報を格納----------------------------------------------------------------------------
        //Log.i("baron", "allDataサイズ☆☆☆☆☆："+this.inputData.size());
        //後手の情報を格納----------------------------------------------------------------------------
        for (int i = 0; i < gotePieceName.length; i++) {
            for (int j = 0; j < shogiBanMapKeys.length; j++) {
                if (gotePieceName[i].equals(this.shogiBanMap.get(shogiBanMapKeys[j]))) {
                    //一致している
                    this.inputData.add(1);
                } else {
                    //一致していない
                    this.inputData.add(0);
                }
            }
        }
        for (int i = 0; i < goteMotigomaNumArray.length; i++) {
            //歩
            if (i == 0) {
                if (goteMotigomaNumArray[i] == 0) {
                    for (int n = 0; n < (9 * 9 * 18); n++) {
                        this.inputData.add(0);
                    }
                } else if (goteMotigomaNumArray[i] != 0) {
                    for (int n = 0; n < (9 * 9 * (goteMotigomaNumArray[i])); n++) {
                        this.inputData.add(1);
                    }
                    for (int n = 0; n < (9 * 9 * (18 - goteMotigomaNumArray[i])); n++) {
                        this.inputData.add(0);
                    }
                }
                continue;
            }
            //香,桂,銀,金
            else if ((i >= 1) && (i <= 4)) {
                if (goteMotigomaNumArray[i] == 0) {
                    for (int n = 0; n < (9 * 9 * 4); n++) {
                        this.inputData.add(0);
                    }
                } else if (goteMotigomaNumArray[i] != 0) {
                    for (int n = 0; n < (9 * 9 * (goteMotigomaNumArray[i])); n++) {
                        this.inputData.add(1);
                    }
                    for (int n = 0; n < (9 * 9 * (4 - goteMotigomaNumArray[i])); n++) {
                        this.inputData.add(0);
                    }
                }
                continue;
            }
            //角,飛車
            else if ((i == 5) || (i == 6)) {
                if (goteMotigomaNumArray[i] == 0) {
                    for (int n = 0; n < (9 * 9 * 2); n++) {
                        this.inputData.add(0);
                    }
                } else if (goteMotigomaNumArray[i] != 0) {
                    for (int n = 0; n < (9 * 9 * (goteMotigomaNumArray[i])); n++) {
                        this.inputData.add(1);
                    }
                    for (int n = 0; n < (9 * 9 * (2 - goteMotigomaNumArray[i])); n++) {
                        this.inputData.add(0);
                    }
                }
                continue;
            }
        }
        //後手の情報を格納----------------------------------------------------------------------------
        //Log.i("baron", "allDataサイズ☆☆☆☆☆："+this.inputData.size());
        //inputData(104*9*9)をfinalInputData(9*9*104)に変換------------------------------------------
        for (int i = 0; i < 81; i++) {
            for (int z = 0; z < 104; z++) {
                int index = i + (z * 81);
                this.finalInputData.add(inputData.get(index));
            }
        }
        //Log.i("baron", "finalInputData☆☆☆☆☆："+this.inputData);
        //Log.i("baron", "finalInputDataサイズ☆☆☆☆☆："+this.finalInputData.size());
        return this.finalInputData;
    }
}

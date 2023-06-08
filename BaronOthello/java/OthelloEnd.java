package com.baron.baronothello;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class OthelloEnd extends AppCompatActivity {
    private List<Map> allGameRecord;//棋譜(データ型)
    private List<String> kihuArray;//棋譜(文字列型)
    private String playerBW;//playerは白か黒か？
    final List<Integer> countArray = new ArrayList<>();//何手目か？
    private int maxCount;//最終局面の手数
    private String[] gameRecordKeys;
    private ImageView[] masuImageArray;
    Spinner spinner;//プルダウンリスト
    MediaPlayer mpTyakusyu = null;//着手音

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.othello_end);
        getData();//データの受け取り
        initialize();//初期化
        changeDesign();
        spinner.setSelection(this.countArray.get(0));//最終局面をデフォルトに設定
    }

    //データの受け取り
    public void getData() {
        Intent intent = getIntent();
        String playerBWKey = intent.getStringExtra("playerBWKey");
        if (playerBWKey.equals("black")) {
            this.playerBW = "black";//playerは白か黒か？
            ((ImageView) findViewById(R.id.player)).setImageResource(R.drawable.black);
            ((ImageView) findViewById(R.id.com)).setImageResource(R.drawable.white);
        } else if (playerBWKey.equals("white")) {
            this.playerBW = "white";//playerは白か黒か？
            ((ImageView) findViewById(R.id.player)).setImageResource(R.drawable.white);
            ((ImageView) findViewById(R.id.com)).setImageResource(R.drawable.black);
        }
        this.allGameRecord = new ArrayList<>((List<Map>) intent.getSerializableExtra("allGameRecordKey"));
        //this.allGameRecord=(List<Map>) intent.getSerializableExtra("allGameRecordKey");
        this.kihuArray = new ArrayList<String>((Collection<? extends String>) intent.getSerializableExtra("kihuKey"));//棋譜保存用(文字列型)
        this.countArray.add(this.allGameRecord.size() - 1);
        this.maxCount = this.allGameRecord.size() - 1;
        //ドロップダウンリストの準備
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, returnDropdownArray());
        spinner = (Spinner) findViewById(R.id.spinner);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                countArray.clear();
                countArray.add(position);
                gameRecordPlayBack(position);//局面の生成
                checkStoneNum(position);//石の数の更新
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //バックボタンで戻ったり選択されなかったときの処理
            }
        });
    }

    //ドロップダウンリスト表示用配列を返す
    public String[] returnDropdownArray() {
        String[] dropdownArray = new String[this.kihuArray.size()];
        String tebanDisplay = "黒";
        int intCount = 0;
        for (int i = 0; i < dropdownArray.length; i++) {
            if (i == 0) {
                dropdownArray[i] = " " + this.kihuArray.get(i);
            } else if (i == dropdownArray.length - 1) {
                dropdownArray[i] = " " + this.kihuArray.get(i);
            } else if (this.kihuArray.get(i).equals("パス")) {
                dropdownArray[i] = " " + tebanDisplay + " " + this.kihuArray.get(i);
                if (tebanDisplay.equals("黒")) {
                    tebanDisplay = "白";
                } else if (tebanDisplay.equals("白")) {
                    tebanDisplay = "黒";
                }
                continue;
            } else if (this.kihuArray.get(i).equals("連続パス")) {
                dropdownArray[i] = " " + tebanDisplay + " " + this.kihuArray.get(i);
                continue;
            } else {
                String line = " ";//表
                String stringCount = String.valueOf(intCount);
                line += stringCount + " " + tebanDisplay + " " + this.kihuArray.get(i);
                dropdownArray[i] = line;
                if (tebanDisplay.equals("黒")) {
                    tebanDisplay = "白";
                } else if (tebanDisplay.equals("白")) {
                    tebanDisplay = "黒";
                }
            }
            intCount++;
        }
        return dropdownArray;
    }

    //初期化
    public void initialize() {
        this.gameRecordKeys = new String[]{"d1s1", "d1s2", "d1s3", "d1s4", "d1s5", "d1s6", "d1s7", "d1s8",
                "d2s1", "d2s2", "d2s3", "d2s4", "d2s5", "d2s6", "d2s7", "d2s8",
                "d3s1", "d3s2", "d3s3", "d3s4", "d3s5", "d3s6", "d3s7", "d3s8",
                "d4s1", "d4s2", "d4s3", "d4s4", "d4s5", "d4s6", "d4s7", "d4s8",
                "d5s1", "d5s2", "d5s3", "d5s4", "d5s5", "d5s6", "d5s7", "d5s8",
                "d6s1", "d6s2", "d6s3", "d6s4", "d6s5", "d6s6", "d6s7", "d6s8",
                "d7s1", "d7s2", "d7s3", "d7s4", "d7s5", "d7s6", "d7s7", "d7s8",
                "d8s1", "d8s2", "d8s3", "d8s4", "d8s5", "d8s6", "d8s7", "d8s8"};
        int[] masuIdArray = new int[]{
                R.id.d1s1, R.id.d1s2, R.id.d1s3, R.id.d1s4, R.id.d1s5, R.id.d1s6, R.id.d1s7, R.id.d1s8,
                R.id.d2s1, R.id.d2s2, R.id.d2s3, R.id.d2s4, R.id.d2s5, R.id.d2s6, R.id.d2s7, R.id.d2s8,
                R.id.d3s1, R.id.d3s2, R.id.d3s3, R.id.d3s4, R.id.d3s5, R.id.d3s6, R.id.d3s7, R.id.d3s8,
                R.id.d4s1, R.id.d4s2, R.id.d4s3, R.id.d4s4, R.id.d4s5, R.id.d4s6, R.id.d4s7, R.id.d4s8,
                R.id.d5s1, R.id.d5s2, R.id.d5s3, R.id.d5s4, R.id.d5s5, R.id.d5s6, R.id.d5s7, R.id.d5s8,
                R.id.d6s1, R.id.d6s2, R.id.d6s3, R.id.d6s4, R.id.d6s5, R.id.d6s6, R.id.d6s7, R.id.d6s8,
                R.id.d7s1, R.id.d7s2, R.id.d7s3, R.id.d7s4, R.id.d7s5, R.id.d7s6, R.id.d7s7, R.id.d7s8,
                R.id.d8s1, R.id.d8s2, R.id.d8s3, R.id.d8s4, R.id.d8s5, R.id.d8s6, R.id.d8s7, R.id.d8s8,
                R.id.com, R.id.player
        };
        this.masuImageArray = new ImageView[masuIdArray.length];
        for (int i = 0; i < masuIdArray.length; i++) {
            this.masuImageArray[i] = (ImageView) findViewById(masuIdArray[i]);
        }
        this.mpTyakusyu = MediaPlayer.create(this, R.raw.tyakusyu);
    }

    //デザイン変更
    public void changeDesign() {
        Point screenSize = getDisplaySize();//画面サイズの取得
        TableLayout board = (TableLayout) findViewById(R.id.board);//盤
        LinearLayout playerArea = (LinearLayout) findViewById(R.id.playerArea);//playerArea
        TextView boardTop = (TextView) findViewById(R.id.boardTop);
        TextView boardBottom = (TextView) findViewById(R.id.boardBottom);
        ImageView backImage = (ImageView) findViewById(R.id.backImage);
        //盤周り,タイマー関連のデザイン調整
        TextView[] cpTextArray = {
                (TextView) findViewById(R.id.comStr),
                (TextView) findViewById(R.id.playerStr),
                (TextView) findViewById(R.id.comStoneNum),
                (TextView) findViewById(R.id.playerStoneNum)
        };

        //検討エリア
        LinearLayout kentouArea = (LinearLayout) findViewById(R.id.kentouArea);//kentouArea
        Button[] kentouButtonArray = {
                (Button) findViewById(R.id.button1),
                (Button) findViewById(R.id.button2),
                (Button) findViewById(R.id.button3),
                (Button) findViewById(R.id.button4)
        };
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        //デザイン変更
        ChangeEndDesign ced = new ChangeEndDesign(screenSize, this.masuImageArray,
                board, boardTop, boardBottom,playerArea, cpTextArray,backImage,
                kentouArea, kentouButtonArray, spinner
        );//デザイン変更クラスのインスタンス生成
    }

    //画面サイズの取得
    public Point getDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        return screenSize;
    }

    //指定局面の生成
    public void gameRecordPlayBack(int c) {
        for (int i = 0; i < this.gameRecordKeys.length; i++) {
            if (this.allGameRecord.get(c).get(this.gameRecordKeys[i]).equals("black")) {
                ((ImageView) this.masuImageArray[i]).setImageResource(R.drawable.black);
            } else if (this.allGameRecord.get(c).get(this.gameRecordKeys[i]).equals("white")) {
                ((ImageView) this.masuImageArray[i]).setImageResource(R.drawable.white);
            } else if (this.allGameRecord.get(c).get(this.gameRecordKeys[i]).equals("None")) {
                ((ImageView) this.masuImageArray[i]).setImageResource(R.drawable.green);
            }
        }
        if (!checkBattle()) {
            return;
        }
        if (c == this.maxCount) {
            return;
        } else {
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
            String targetKihu = this.kihuArray.get(c + 1);
            if (Arrays.asList(playerBlackArray).contains(targetKihu)) {
                int indexNumber = 0;
                if (playerBW.equals("black")) {
                    for (int i = 0; i < playerBlackArray.length; i++) {
                        if (playerBlackArray[i].equals(targetKihu)) {
                            indexNumber = i;
                            break;
                        }
                    }
                } else if (playerBW.equals("white")) {
                    for (int i = 0; i < playerWhiteArray.length; i++) {
                        if (playerWhiteArray[i].equals(targetKihu)) {
                            indexNumber = i;
                            break;
                        }
                    }
                }
                //cが偶数なら黒,奇数なら白
                if (c % 2 == 0) {
                    //偶数なら
                    ((ImageView) this.masuImageArray[indexNumber]).setImageResource(R.drawable.black_point);
                } else {
                    ((ImageView) this.masuImageArray[indexNumber]).setImageResource(R.drawable.white_point);
                }
            } else {
                return;
            }
        }
    }

    //石の数の更新
    public void checkStoneNum(int c) {
        //石の数の確認し,石の数のテキストを更新する。
        int tempBlackNum = 0;
        int tempWhiteNum = 0;
        for (int i = 0; i < this.gameRecordKeys.length; i++) {
            if (this.allGameRecord.get(c).get(this.gameRecordKeys[i]).equals("black")) {
                tempBlackNum++;
            } else if (this.allGameRecord.get(c).get(this.gameRecordKeys[i]).equals("white")) {
                tempWhiteNum++;
            } else if (this.allGameRecord.get(c).get(this.gameRecordKeys[i]).equals("None")) {
                ;
            }
        }
        if (this.playerBW.equals("black")) {
            ((TextView) findViewById(R.id.playerStoneNum)).setText(String.valueOf(tempBlackNum));//player：黒石
            ((TextView) findViewById(R.id.comStoneNum)).setText(String.valueOf(tempWhiteNum));//com：白石
        } else if (this.playerBW.equals("white")) {
            ((TextView) findViewById(R.id.playerStoneNum)).setText(String.valueOf(tempWhiteNum));//player：白石
            ((TextView) findViewById(R.id.comStoneNum)).setText(String.valueOf(tempBlackNum));//com：黒石
        }
    }

    //マスクリック
    public void masuClick(View view) {
        goButton(view);
    }

    //0手目に戻る
    public void minBackButton(View view) {
        this.countArray.clear();
        this.countArray.add(0);
        gameRecordPlayBack(this.countArray.get(0));//局面の生成
        checkStoneNum(this.countArray.get(0));//石の数の更新
        spinner.setSelection(this.countArray.get(0));
    }

    //1手戻る
    public void backButton(View view) {
        if (!(checkBattle())) {
            return;
        }
        if (this.countArray.get(0) == 0) {
            return;
        } else if (this.countArray.get(0) == 1) {
            this.countArray.clear();
            this.countArray.add(0);
            gameRecordPlayBack(this.countArray.get(0));//局面の生成
            checkStoneNum(this.countArray.get(0));//石の数の更新
            spinner.setSelection(this.countArray.get(0));
        } else {
            //Integer tempCount= new Integer(countArray.get(0)+1);
            while (true) {
                int tempCount = this.countArray.get(0) - 1;
                this.countArray.clear();
                this.countArray.add(tempCount);
                if ((checkKihu(this.kihuArray.get(tempCount))) &&
                        (!(checkKihu(this.kihuArray.get(tempCount + 1))))) {
                    tempCount = this.countArray.get(0) - 1;
                    this.countArray.clear();
                    this.countArray.add(tempCount);
                    break;
                } else if (checkKihu(this.kihuArray.get(tempCount))) {
                    break;
                }
            }
            gameRecordPlayBack(this.countArray.get(0));//局面の生成
            checkStoneNum(this.countArray.get(0));//石の数の更新
            spinner.setSelection(this.countArray.get(0));
        }
    }

    //1手進む
    boolean goButtonFlg = true;

    public void goButton(View view) {
        if (!goButtonFlg) {
            return;
        }
        if (this.countArray.get(0) == this.maxCount) {
            return;
        } else {
            int tempCount = this.countArray.get(0) + 1;
            this.countArray.clear();
            this.countArray.add(tempCount);
            gameRecordPlayBack(this.countArray.get(0));//局面の生成
            checkStoneNum(this.countArray.get(0));//石の数の更新
            spinner.setSelection(this.countArray.get(0));
            //音
            if (checkKihu(this.kihuArray.get(this.countArray.get(0)))) {
                tyakusyuMP3();
            }
        }
    }

    //最終局面に進む
    public void maxGoButton(View view) {
        if (countArray.get(0) == this.maxCount) {
            return;
        } else {
            int tempCount = this.maxCount;
            this.countArray.clear();
            this.countArray.add(tempCount);
            gameRecordPlayBack(this.countArray.get(0));//局面の生成
            checkStoneNum(this.countArray.get(0));//石の数の更新
            spinner.setSelection(this.countArray.get(0));
        }
    }

    //一手は指されたか？指されていたらtrue
    public boolean checkBattle() {
        boolean battleFlg = false;
        String[] checkArray = {
                "A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1",
                "A2", "B2", "C2", "D2", "E2", "F2", "G2", "H2",
                "A3", "B3", "C3", "D3", "E3", "F3", "G3", "H3",
                "A4", "B4", "C4", "D4", "E4", "F4", "G4", "H4",
                "A5", "B5", "C5", "D5", "E5", "F5", "G5", "H5",
                "A6", "B6", "C6", "D6", "E6", "F6", "G6", "H6",
                "A7", "B7", "C7", "D7", "E7", "F7", "G7", "H7",
                "A8", "B8", "C8", "D8", "E8", "F8", "G8", "H8"
        };
        for (int i = 0; i < this.kihuArray.size(); i++) {
            for (int j = 0; j < checkArray.length; j++) {
                if (this.kihuArray.get(i).equals(checkArray[j])) {
                    battleFlg = true;//一致した
                    break;
                }
            }
        }
        return battleFlg;
    }

    //棋譜配列に格納されているのは基本棋譜か？
    public boolean checkKihu(String kihu) {
        boolean chcekFlg = false;
        String[] checkArray = {
                "A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1",
                "A2", "B2", "C2", "D2", "E2", "F2", "G2", "H2",
                "A3", "B3", "C3", "D3", "E3", "F3", "G3", "H3",
                "A4", "B4", "C4", "D4", "E4", "F4", "G4", "H4",
                "A5", "B5", "C5", "D5", "E5", "F5", "G5", "H5",
                "A6", "B6", "C6", "D6", "E6", "F6", "G6", "H6",
                "A7", "B7", "C7", "D7", "E7", "F7", "G7", "H7",
                "A8", "B8", "C8", "D8", "E8", "F8", "G8", "H8"
        };
        for (int i = 0; i < checkArray.length; i++) {
            if (checkArray[i].equals(kihu)) {
                chcekFlg = true;//一致した
                break;
            }
        }
        return chcekFlg;
    }

    //着手音
    public void tyakusyuMP3() {
        mpTyakusyu.start();
    }

    public void backImageEvent(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("最初に戻りますか？");
        builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                intentMainActivity();
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

    //最初に戻る。
    public void intentMainActivity() {
        Intent intent = new Intent(getApplication(), MainActivity.class);
        startActivity(intent);//画面推移
        finish();//画面を終了し、バックキーで戻らせない。
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
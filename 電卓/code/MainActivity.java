package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.graphics.Color.*;

//電卓アプリ
public class MainActivity extends AppCompatActivity {
    List<String> NumberOrSymbolArray = new ArrayList<String>();//数字か記号か？

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setButtonEvent();//ボタンイベントの設定
        //changeButtonColor();//ボタンのデザインを変更
    }

    public void setButtonEvent() {
        int[] btId = {R.id.bt1, R.id.bt2, R.id.bt3, R.id.bt4, R.id.bt5, R.id.bt6, R.id.bt7, R.id.bt8, R.id.bt9,
                R.id.bt0, R.id.bt00, R.id.btClear, R.id.btRoot, R.id.btPercent, R.id.btPoint,
                R.id.btPlus, R.id.btMinus, R.id.btMultiplication, R.id.btDivision, R.id.btEqual};
        ButtonEventListener bel = new ButtonEventListener();//イベントリスナ
        for (int i = 0; i < btId.length; i++) {
            Button btTemp = findViewById(btId[i]);
            btTemp.setOnClickListener(bel);
        }
    }

    //ボタンイベント
    private class ButtonEventListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            TextView inputDisplay = findViewById(R.id.inputDisplay);//式テキスト
            TextView resultDisplay = findViewById(R.id.resultDisplay);//結果テキスト
            int id = view.getId();
            if (id == R.id.btClear) {
                inputDisplay.setText("");
                resultDisplay.setText("");
                NumberOrSymbolArray.clear();
                return;
            }
            int inpLen = inputDisplay.getText().length();//入力式の長さ
            Button bt = (Button) view;
            String btValue = (String) bt.getText();
            String temp = (String) inputDisplay.getText() + btValue;
            switch (id) {
                case R.id.btRoot:
                case R.id.btPercent:
                case R.id.btPoint:
                    break;
                case R.id.bt1:
                case R.id.bt2:
                case R.id.bt3:
                case R.id.bt4:
                case R.id.bt5:
                case R.id.bt6:
                case R.id.bt7:
                case R.id.bt8:
                case R.id.bt9:
                case R.id.bt0:
                case R.id.bt00:
                    //数字を押した時の処理
                    if ((NumberOrSymbolArray.size() == 0) ||
                            (NumberOrSymbolArray.get(NumberOrSymbolArray.size() - 1).equals("記号"))) {
                        NumberOrSymbolArray.add("数字");
                    }
                    inputDisplay.setText(temp);
                    break;
                case R.id.btDivision:
                case R.id.btMinus:
                case R.id.btMultiplication:
                case R.id.btPlus:
                    // "＋", "－", "×", "÷"を押した時の処理
                    symbolClick(id);
                    break;
                case R.id.btEqual:
                    //"="を押した時の処理
                    equalClick();
                    break;
            }
        }
    }

    // "+", "-", "×", "÷"を押した時の処理
    public void symbolClick(int btId) {
        TextView sikiText = findViewById(R.id.inputDisplay);//式テキスト
        String sikiStr = (String) sikiText.getText();//計算式(文字列)
        int inpLen = sikiStr.length();//入力式の長さ
        String btValue = "";
        if (btId == R.id.btPlus) {
            btValue = "+";
        } else if (btId == R.id.btMinus) {
            btValue = "-";
        } else if (btId == R.id.btMultiplication) {
            btValue = "×";
        } else if (btId == R.id.btDivision) {
            btValue = "÷";
        }
        if (inpLen == 0) {
            if (btValue.equals("-")) {
                NumberOrSymbolArray.add("記号");
                sikiText.setText("-");
                return;
            } else {
                //計算式が空の時は演算子は"－"以外受け付けない
                return;
            }
        }
        if (NumberOrSymbolArray.size() == 1) {
            if (NumberOrSymbolArray.get(NumberOrSymbolArray.size() - 1).equals("記号")) {
                if (btId == R.id.btPlus) {
                    sikiText.setText("");
                    NumberOrSymbolArray.clear();
                    return;
                } else {
                    return;
                }
            }
        }
        if (NumberOrSymbolArray.size() != 0) {
            if (NumberOrSymbolArray.get(NumberOrSymbolArray.size() - 1).equals("数字")) {
                NumberOrSymbolArray.add("記号");
                String temp = sikiStr + btValue;
                sikiText.setText(temp);
                return;
            }
            String targetTemp1 = sikiStr.substring(0, inpLen - 1);//末尾を除いた式
            String targetEnd1 = sikiStr.substring(inpLen - 1);//末尾
            if (inpLen >= 3) {
                String targetTemp2 = sikiStr.substring(0, inpLen - 2);//末尾二文字を除いた式
                String targetEnd2 = sikiStr.substring(inpLen - 2, inpLen - 1);//末尾から二文字目
                if (((targetEnd2.equals("×")) && (targetEnd1.equals("-"))) ||
                        ((targetEnd2.equals("÷")) && (targetEnd1.equals("-")))) {
                    //2記号→1記号
                    NumberOrSymbolArray.remove(NumberOrSymbolArray.size() - 1);
                    String temp = targetTemp2 + btValue;
                    sikiText.setText(temp);
                    return;
                }
            }
            if (((targetEnd1.equals("×")) && (btValue.equals("-"))) ||
                    ((targetEnd1.equals("÷")) && (btValue.equals("-")))) {
                //1記号→2記号
                NumberOrSymbolArray.add("記号");
                String temp = sikiStr + btValue;
                sikiText.setText(temp);
                return;
            }
            if ((targetEnd1.equals("+")) || (targetEnd1.equals("-")) ||
                    (targetEnd1.equals("×")) || (targetEnd1.equals("÷"))) {
                //1記号→1記号
                String temp = targetTemp1 + btValue;
                sikiText.setText(temp);
                return;
            }
        }
    }

    //"="が押された時の処理
    public void equalClick() {
        TextView sikiText = findViewById(R.id.inputDisplay);//式テキスト
        TextView resultText = findViewById(R.id.resultDisplay);//結果テキスト
        String target = (String) sikiText.getText();
        int inpLen = target.length();//入力式の長さ
        if (inpLen == 0) {
            //計算式が空の時
            return;
        }
        if (NumberOrSymbolArray.get(NumberOrSymbolArray.size() - 1).equals("記号")) {
            //記号で終了
            return;
        }
        String finalResult = getResult();//最終計算結果
        resultText.setText(finalResult);//計算結果を表示
    }

    //演算結果
    public String getResult() {
        TextView sikiText = findViewById(R.id.inputDisplay);//式テキスト
        String sikiStr = (String) sikiText.getText();//計算式(文字列)
        List<Integer> numberArray = new ArrayList<Integer>();//数字
        List<String> enzansiArray = new ArrayList<String>();//記号
        //正規表現
        String regex1 = "([0-9]+)";//半角数字
        Pattern p1 = Pattern.compile(regex1);
        Matcher m1 = p1.matcher(sikiStr);
        //1個以上の数字のパターンを抽出
        while (m1.find()) {
            numberArray.add(Integer.parseInt(m1.group()));
        }
        String regex3 = "([^0-9])";//半角数字以外
        Pattern p2 = Pattern.compile(regex3);
        Matcher m2 = p2.matcher(sikiStr);
        //数字以外のパターンを抽出＆一致した位置を取得
        while (m2.find()) {
            enzansiArray.add(m2.group());
        }
        //式の変換１
        List<String> changeArray1 = new ArrayList<String>();//式の変換
        int nIndex = 0;
        int eIndex = 0;
        for (int i = 0; i < NumberOrSymbolArray.size(); i++) {
            if (NumberOrSymbolArray.get(i).equals("数字")) {
                changeArray1.add(String.valueOf(numberArray.get(nIndex)));
                nIndex++;
            }else if (NumberOrSymbolArray.get(i).equals("記号")) {
                changeArray1.add(enzansiArray.get(eIndex));
                eIndex++;
            }else{
                continue;
            }
        }
        //式の変換２：式から"-"を変換
        List<String> changeArray2 = new ArrayList<String>();//式から"－"を変換
        for (int i = 0; i < changeArray1.size(); i++) {
            if (changeArray1.get(i).equals("-")) {
                changeArray2.add("-" + changeArray1.get(i + 1));
                i++;
            } else {
                changeArray2.add(changeArray1.get(i));
            }
        }
        //式の変換３：×÷計算
        List<String> changeArray3 = new ArrayList<String>(changeArray2);//コピー
        for (int i = 0; i < changeArray3.size(); i++) {
            if ((changeArray3.get(i).equals("×")) ||
                    (changeArray3.get(i).equals("÷"))) {
                String taget1 = changeArray3.get(i - 1);
                String taget2 = changeArray3.get(i + 1);
                int result = 0;
                if (changeArray3.get(i).equals("×")) {
                    result = Integer.parseInt(taget1) * Integer.parseInt(taget2);
                } else if (changeArray3.get(i).equals("÷")) {
                    result = Integer.parseInt(taget1) / Integer.parseInt(taget2);
                }
                changeArray3.set(i - 1, String.valueOf(result));
                changeArray3.remove(i);
                changeArray3.remove(i);
                i--;
            }
        }
        //最終計算
        int finalResult = 0;
        for (int i = 0; i < changeArray3.size(); i++) {
            if (!(changeArray3.get(i).equals("+"))) {
                finalResult += Integer.parseInt(changeArray3.get(i));
            }
        }
        return String.valueOf(finalResult);
    }

    //ボタンのデザイン変更
    public void changeButtonColor(){
        Button b1=findViewById(R.id.btClear);
    }
}

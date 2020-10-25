package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//電卓アプリ
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setButtonEvent();//ボタンイベントの設定
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
            TextView input = findViewById(R.id.input);//入力式表示テキスト
            int id = view.getId();
            if (id == R.id.btClear) {
                input.setText("式：");
                return;
            }
            Button bt = (Button) view;
            String btValue = (String) bt.getText();
            String temp = (String) input.getText() + btValue;
            input.setText(temp);
//            switch(id){
//                case R.id.bt_1:
//                case R.id.btEqual:
//            }
        }
    }
}
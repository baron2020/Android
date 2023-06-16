package com.teamshiny.shogiai;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AI {
    //Assetsに格納されているモデルファイルの名前
    private static final String MODEL_PATH = "baron50000.tflite";//"xxx.tflite"
    private Interpreter model;//モデル
    private ByteBuffer inputBuffer = null;
    private float[][] output = null;
    Map<String, String> banRecordMap = new HashMap<String, String>();//盤面データ
    List<String> komadaiTopList = new ArrayList<String>();//駒台(上)(文字列型)
    List<String> komadaiBottomList = new ArrayList<String>();//駒台(下)(文字列型)
    private DataConversion dc;//AIに渡すデータに変換するクラス
    private List<Integer> finalInputData;//9*9*104を一次元にした配列
    List<Integer> maxIndex20List;//添え字20(高い順)
    List<Float> maxValue20List;//確率20(高い順)
    List<Float> predictList;//予測結果

    public AI(Activity activity, Map<String, String> banRecordMap, List<String> komadaiTopList, List<String> komadaiBottomList) {
        try {
            this.model = new Interpreter(loadModelFile(activity));
            this.inputBuffer = ByteBuffer.allocateDirect(4 * 9 * 9 * 104);
            this.inputBuffer.order(ByteOrder.nativeOrder());
            this.output = new float[1][2187];

            this.banRecordMap = banRecordMap;
            this.komadaiTopList = komadaiTopList;
            this.komadaiBottomList = komadaiBottomList;
            dc = new DataConversion(banRecordMap, komadaiTopList, komadaiBottomList);
            finalInputData = new ArrayList<>(dc.dataChange1());//9*9*104を一次元にした配列
        } catch (IOException e) {
            //Log.i("baron", "IOException loading the tflite file");
        }
    }

    public int classify() {
//        if (model == null) {
//            //Log.e("baron", "Image classifier has not been initialized; Skipped.");
//        }
        setByteBuffer();
        modelPredict();
        return returnOutput();
    }

    //推論
    protected void modelPredict() {
        model.run(inputBuffer, output);
        model.close();//メモリの解放
    }

    //モデルに渡すデータを作成
    private void setByteBuffer() {
        inputBuffer.rewind();//最初から
        //long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < this.finalInputData.size(); ++i) {
            float putData = (float) this.finalInputData.get(i);
            inputBuffer.putFloat(putData);
        }
        //long endTime = SystemClock.uptimeMillis();
        //inputBuffer.flip();//書き込みを終了し、読み込み位置を0に戻す
    }

    //推論結果を返す
    private int returnOutput() {
        this.predictList = new ArrayList<>();
        for (int i = 0; i < output[0].length; i++) {
            float value = output[0][i];
            predictList.add(value);
        }
        //Log.i("baron", "predict最終★ :  " + predictList);
        int index = 0;
        Float out = (Float) predictList.get(0);
        this.maxIndex20List = new ArrayList<>();//最大の添え字
        this.maxValue20List = new ArrayList<>();//最大予測確率

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < predictList.size(); j++) {
                if (out < (Float) predictList.get(j)) {
                    out = (Float) predictList.get(j);
                    index = j;
                }
            }
            this.maxIndex20List.add(index);//最大添え字
            this.maxValue20List.add(out);//最大確率
            predictList.set(index, (float) 0);
            index = 0;
            out = (Float) predictList.get(0);
        }

        int maxIndex = maxIndex20List.get(0);
        //Float maxOut = (Float) maxValue20List.get(0);
        //Log.i("baron", "pre★ :  " + predictList);
        //Log.i("baron", "INDEX★ :  " + maxIndex);
        //Log.i("baron", "out★ :  " + maxOut);
        return maxIndex;
    }

    //モデルファイルをメモリーマップ化する Assets
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //添え字(高い順)
    public List<Integer> getMaxIndex20List() {
        return this.maxIndex20List;
    }

    //確率(高い順)
    public List<Float> getMaxValue20List() {
        return this.maxValue20List;
    }

}

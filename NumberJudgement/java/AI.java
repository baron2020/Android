package com.teamshiny.numberjudgement;

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
import java.util.List;

public class AI {
    //Assetsに格納されているモデルファイルの名前
    private static final String MODEL_PATH = "NumberJudgement0913.tflite";
    private Interpreter model;//モデル
    private ByteBuffer inputBuffer = null;
    private float[][] output = null;
    private List<Float> finalList;//最終確率リスト

    public AI(Activity activity) {
        try {
            this.model = new Interpreter(loadModelFile(activity));
            this.inputBuffer = ByteBuffer.allocateDirect(4 * 28 * 28);//floatサイズ4×バッチサイズ×28×28×チャンネル
            this.inputBuffer.order(ByteOrder.nativeOrder());
            this.output = new float[1][10];
        } catch (IOException e) {
            //Log.i("baron", "IOException loading the tflite file");
        }
    }

    public int judgement(List<Integer>  targetList) {
//        if (model == null) {
//            Log.i("baron", "画像分類器が初期化されていません。");
//        }
        setByteBuffer(targetList);
        modelPredict();
        return returnOutput();
    }

    //推論
    protected void modelPredict() {
        model.run(inputBuffer, output);
        model.close();//メモリの解放
    }

    //モデルに渡すデータを作成
    private void setByteBuffer(List<Integer>  targetList) {
        if (targetList == null || inputBuffer == null) {
            return;
        }
        inputBuffer.rewind();//最初から
        //long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < targetList.size(); ++i) {
            float putData = (float) targetList.get(i);
            inputBuffer.putFloat(putData);
        }
        //long endTime = SystemClock.uptimeMillis();
        //inputBuffer.flip();//書き込みを終了し、読み込み位置を0に戻す
        //Log.i("baron", "ByteBufferに値を入れるための時間コスト: " + Long.toString(endTime - startTime));
    }

    //推論結果を返す
    private int returnOutput() {
        ArrayList predictList = new ArrayList<>();
        for (int i = 0; i <  output[0].length; i++) {
            float value =  output[0][i];
            predictList.add(value);
        }
        //Log.i("baron", "predictList★ :  " + predictList);
        Float out = (Float) predictList.get(0);
        int index = 0;
        for (int i = 0; i < predictList.size(); i++) {
            if (out < (Float)predictList.get(i)) {
                out = (Float)predictList.get(i);
                index = i;
            }
        }
        this.finalList = new ArrayList<>();//最終確率リスト
        for (int i = 0; i < predictList.size(); i++) {
            float addData=  (Float)predictList.get(i);
            this.finalList.add((float) (Math.round(addData * 1000) / 1000.0));
        }
        //Math.round(predictList.get(i) * 1000) / 1000.0
        //Log.i("baron", "最終確率リスト： :  " + this.finalList);
        //Log.i("baron", "最大値★ :  " + out);
        //Log.i("baron", "最大INDEX★ :  " + index);
        return index;
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

    //最終確率リストを返す
    public List<Float> getFinalList() {
        return this.finalList;
    }
}
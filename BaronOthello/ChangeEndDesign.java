package com.baron.baronothello;

import android.graphics.Point;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

public class ChangeEndDesign {
    private Point screenSize;//画面サイズ
    private ImageView[] masuImageViewArray;
    private TableLayout board;
    private TextView boardTop;
    private TextView boardBottom;
    private LinearLayout playerArea;
    private TextView[] cpTextArray;
    private ImageView backImage;
    private LinearLayout kentouArea;
    private Button[] kentouButtonArray;
    private Spinner spinner;

    public ChangeEndDesign(Point screenSize, ImageView[] masuImageViewArray,
                           TableLayout board, TextView boardTop, TextView boardBottom,
                           LinearLayout playerArea, TextView[] cpTextArray,ImageView backImage,
                           LinearLayout kentouArea, Button[] kentouButtonArray, Spinner spinner) {
        this.screenSize = screenSize;//画面サイズの取得
        this.masuImageViewArray = masuImageViewArray;
        this.board = board;
        this.boardTop = boardTop;
        this.boardBottom = boardBottom;
        this.playerArea = playerArea;
        this.cpTextArray = cpTextArray;
        this.backImage=backImage;
        this.kentouArea = kentouArea;
        this.kentouButtonArray = kentouButtonArray;
        this.spinner = spinner;
        changeMasuSize(this.screenSize.x);//マスのサイズ変更(screenSize.ｙ：高さ,screenSize.x：横幅)
    }

    //マスのサイズ変更
    public void changeMasuSize(int screenX) {
        int masuSize = getMasuSize(screenX);//１マスのサイズ
        int boardMargin = getBoardMargin(screenX);//盤のマージン
        for (int i = 0; i < this.masuImageViewArray.length; i++) {
            ImageView targetImageView = (ImageView) this.masuImageViewArray[i];
            ViewGroup.LayoutParams targetParams = targetImageView.getLayoutParams();
            if ((i == 64) || (i == 65)) {
                //com,player画像
                targetParams.width = masuSize * 2 / 3;//横サイズ
                targetParams.height = masuSize * 2 / 3;//縦サイズ
            } else {
                targetParams.width = masuSize;//横サイズ
                targetParams.height = masuSize;//縦サイズ
            }
            targetImageView.setLayoutParams(targetParams);
        }

        ViewGroup.LayoutParams backImageParams = this.backImage.getLayoutParams();
        backImageParams.width = masuSize;//横サイズ
        backImageParams.height = masuSize;//縦サイズ
        this.backImage.setLayoutParams(backImageParams);

        //boardTop
        ViewGroup.LayoutParams boardTopParams = this.boardTop.getLayoutParams();
        boardTopParams.height = boardMargin;//高さをboardMarginにする
        this.boardTop.setLayoutParams(boardTopParams);

        //boardBottom
        ViewGroup.LayoutParams boardBottomParams = this.boardBottom.getLayoutParams();
        boardBottomParams.height = boardMargin;//高さをboardMarginにする
        this.boardBottom.setLayoutParams(boardBottomParams);

        //boardAreaのY座標の調整
        int boardAreaSetX = returnBoardAreaSetX(screenX, masuSize);
        this.board.setTranslationX(boardAreaSetX);//横位置変更

        //com,playerエリアの変更
        for (int i = 0; i < this.cpTextArray.length; i++) {
            TextView targetTextView = (TextView) this.cpTextArray[i];
            ViewGroup.LayoutParams targetParams = targetTextView.getLayoutParams();
            if ((i == 0) || (i == 1)) {
                targetParams.width = masuSize * 2 * 2 / 3;//横サイズをマスの2倍のサイズにする
                targetParams.height = masuSize * 2 / 3;//高さをマスと同じサイズにする
            } else if ((i == 2) || (i == 3)) {
                targetParams.width = masuSize * 2 / 3;//横サイズをマスと同じサイズにする
                targetParams.height = masuSize * 2 / 3;//高さをマスと同じサイズにする
            }
            targetTextView.setLayoutParams(targetParams);
            targetTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);//文字列の中央揃え
        }
        //playerAreaの調整
        int playerAreaSetX = returnPlayerAreaSetX(masuSize, boardMargin);//playerAreaのセットするX座標
        playerArea.setTranslationX(playerAreaSetX);//横位置変更
        //検討ボタンの調整
        for (int i = 0; i < this.kentouButtonArray.length; i++) {
            Button targetButton = (Button) this.kentouButtonArray[i];
            ViewGroup.LayoutParams targetParams = targetButton.getLayoutParams();
            targetParams.width = this.screenSize.x / 7;//横サイズをマスの2倍のサイズにする
            if (this.screenSize.x / 7 >= 190) {
                //タブレット考慮
                targetParams.height = 190;
            } else {
                targetParams.height = this.screenSize.x / 7;//高さをマスと同じサイズにする
            }
            targetButton.setLayoutParams(targetParams);
        }
        //プルダウンリストの調整
        ViewGroup.LayoutParams spinnerParams = this.spinner.getLayoutParams();
        spinnerParams.width = this.screenSize.x - ((this.screenSize.x / 7) * 4);//横サイズをマスの2倍のサイズにする
        if (this.screenSize.x / 7 >= 190) {
            //タブレット考慮
            spinnerParams.height = 190;
        } else {
            spinnerParams.height = this.screenSize.x / 7;//高さをマスと同じサイズにする
        }
        this.spinner.setLayoutParams(spinnerParams);

        //検討エリアの調整
        this.kentouArea.setTranslationY(boardMargin);//横位置変更

    }

    //１マスのサイズ
    public int getMasuSize(int screenX) {
        int masuSize = screenX / 9;
        if (masuSize >= 165) {
            masuSize = 165;
        }
        return masuSize;
    }

    //盤のマージン
    public int getBoardMargin(int screenX) {
        int masuSize = screenX / 9;
        if (masuSize >= 165) {
            masuSize = 165;
        }
        int boardMargin = masuSize / 2;
        return boardMargin;
    }

    //boardAreaのセットするX座標
    public int returnBoardAreaSetX(int screenX, int masuSize) {
        int boardWidth = masuSize * 8;//盤の横サイズ
        int setX = (screenX - boardWidth) / 2;
        return setX;
    }

    //playerAreaのセットするX座標
    public int returnPlayerAreaSetX(int masuSize, int boardMargin) {
        int boardX = this.screenSize.x;//盤の横サイズ
        int playerAreaWidth = masuSize * 2 / 3 * 4;
        int rX = boardX - playerAreaWidth;
        return rX;
    }

    //検討エリアのセットするY座標
    public int returnKentouAreaSetY(int masuSize, int mainAreaSetY) {
        int rY = mainAreaSetY + (masuSize / 2);
        return rY;
    }
}

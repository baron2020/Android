package com.baron.baronothello;

import android.graphics.Point;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class ChangeStartDesign {
    private Point screenSize;//画面サイズ
    private ImageView[] masuImageViewArray;
    private TableLayout board;
    private TextView boardTop;
    private TextView boardBottom;
    private TextView[] textViewArray;
    private LinearLayout playerArea;

    public ChangeStartDesign(Point screenSize, ImageView[] masuImageViewArray,
                              TableLayout board, TextView boardTop, TextView boardBottom,
                             TextView[] textViewArray,  LinearLayout playerArea) {
        this.screenSize = screenSize;//画面サイズの取得
        this.masuImageViewArray = masuImageViewArray;
        this.board = board;
        this.boardTop = boardTop;
        this.boardBottom = boardBottom;
        this.textViewArray = textViewArray;
        this.playerArea = playerArea;
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
        //盤周り(上下)の調整
        ViewGroup.LayoutParams boardTopParams = this.boardTop.getLayoutParams();
        ViewGroup.LayoutParams boardBottomParams = this.boardBottom.getLayoutParams();
        boardTopParams.height = boardMargin;//高さをboardMarginにする
        boardBottomParams.height = boardMargin;//高さをboardMarginにする
        this.boardTop.setLayoutParams(boardTopParams);
        this.boardBottom.setLayoutParams(boardBottomParams);

        //MainAreaのY座標の調整
        int mainAreaSetX = returnMainAreaSetX(screenX, masuSize);
        this.board.setTranslationX(mainAreaSetX);//横位置変更

        //com,playerエリアの変更
        for (int i = 0; i < this.textViewArray.length; i++) {
            TextView targetTextView = (TextView) this.textViewArray[i];
            ViewGroup.LayoutParams targetParams = targetTextView.getLayoutParams();
            if ((i == 2) || (i == 3)) {
                //石の数テキスト
                targetParams.width = masuSize * 2 / 3;//横サイズをマスと同じサイズにする
                targetParams.height = masuSize * 2 / 3;//高さをマスと同じサイズにする
            } else {
                targetParams.width = masuSize * 2 * 2 / 3;//横サイズをマスの2倍のサイズにする
                targetParams.height = masuSize * 2 / 3;//高さをマスと同じサイズにする
            }
            targetTextView.setLayoutParams(targetParams);
            targetTextView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);//文字列の中央揃え
        }
        //playerAreaの調整
        int playerAreaSetX = returnPlayerAreaSetX(masuSize);//playerAreaのセットするX座標
        this.playerArea.setTranslationX(playerAreaSetX);//横位置変更

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

    //mainAreaのセットするX座標
    public int returnMainAreaSetX(int screenX, int masuSize) {
        int boardWidth = masuSize * 8;//盤の横サイズ
        int setX = (screenX - boardWidth) / 2;
        return setX;
    }

    //playerAreaのセットするX座標
    public int returnPlayerAreaSetX(int masuSize) {
        int boardX = this.screenSize.x;//盤の横サイズ
        int playerAreaWidth = masuSize * 2 / 3 * 6;
        int rX = boardX - playerAreaWidth;
        return rX;
    }

}

package com.teamshiny.shogiai;

import android.graphics.Point;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

public class ChangeDesign {
    private Point screenSize;//画面サイズ
    private ImageView[] masuImageArray;
    private TableLayout board;
    private TextView boardTop;
    private TextView boardBottom;
    private LinearLayout numArea1;//駒台枚数1
    private LinearLayout numArea2;//駒台枚数2
    private LinearLayout komadaiArea1;//駒台1
    private LinearLayout komadaiArea2;//駒台2
    private ImageView[] numImageArray;


    public ChangeDesign(Point screenSize, ImageView[] masuImageArray,
                        TableLayout board, TextView boardTop, TextView boardBottom,
                        LinearLayout numArea1, LinearLayout numArea2, LinearLayout komadaiArea1, LinearLayout komadaiArea2,
                        ImageView[] numImageArray) {
        this.screenSize = screenSize;//画面サイズの取得
        this.masuImageArray = masuImageArray;
        this.board = board;
        this.boardTop = boardTop;
        this.boardBottom = boardBottom;
        this.numArea1 = numArea1;
        this.numArea2 = numArea2;
        this.komadaiArea1 = komadaiArea1;
        this.komadaiArea2 = komadaiArea2;
        this.numImageArray = numImageArray;


        changeMasuSize(this.screenSize.x);//マスのサイズ変更(screenSize.ｙ：高さ,screenSize.x：横幅)
    }

    //マスのサイズ変更
    public void changeMasuSize(int screenX) {
        int masuSize = getMasuSize(screenX);//１マスのサイズ
        int boardMargin = getBoardMargin(screenX);//盤のマージン
        for (int i = 0; i < this.masuImageArray.length; i++) {
            ImageView targetImageView = (ImageView) this.masuImageArray[i];
            ViewGroup.LayoutParams targetParams = targetImageView.getLayoutParams();
            targetParams.width = masuSize;//横サイズ
            targetParams.height = masuSize;//縦サイズ
            targetImageView.setLayoutParams(targetParams);
        }
        //盤周り(上下)の調整
        ViewGroup.LayoutParams boardTopParams = this.boardTop.getLayoutParams();
        ViewGroup.LayoutParams boardBottomParams = this.boardBottom.getLayoutParams();
        boardTopParams.height = boardMargin;//高さをboardMarginにする
        boardBottomParams.height = boardMargin;//高さをboardMarginにする
        this.boardTop.setLayoutParams(boardTopParams);
        this.boardBottom.setLayoutParams(boardBottomParams);
        //タブレット画面を考慮
        if (masuSize >= 150) {
            int changeX = (screenX - (masuSize * 9)) / 2;
            this.board.setTranslationX(changeX);//横位置変更
            this.numArea1.setTranslationX(changeX);//横位置変更
            this.numArea2.setTranslationX(changeX);//横位置変更
            this.komadaiArea1.setTranslationX(changeX);//横位置変更
            this.komadaiArea2.setTranslationX(changeX);//横位置変更
        } else {
            //将棋盤の中央揃え
            this.board.setTranslationX(boardMargin);//横位置変更
            //駒台の中央揃え
            this.numArea1.setTranslationX(boardMargin);//横位置変更
            this.numArea2.setTranslationX(boardMargin);//横位置変更
            this.komadaiArea1.setTranslationX(boardMargin);//横位置変更
            this.komadaiArea2.setTranslationX(boardMargin);//横位置変更
        }
        //駒台枚数の調整
        for (int i = 0; i < this.numImageArray.length; i++) {
            ImageView targetImageView = (ImageView) this.numImageArray[i];
            ViewGroup.LayoutParams targetParams = targetImageView.getLayoutParams();
            targetParams.width = masuSize;//横サイズ
            targetParams.height = boardMargin * 3 / 2;//縦サイズ
            targetImageView.setLayoutParams(targetParams);
        }


    }

    //１マスのサイズ
    public int getMasuSize(int screenX) {
        int wBoardMargin = screenX / 10 / 2;//左右のマージンの合計
        int masuSize = (screenX - wBoardMargin) / 9;
        if (masuSize >= 150) {
            masuSize = 150;
        }
        return masuSize;
    }

    //盤のマージン(1マスの半分)
    public int getBoardMargin(int screenX) {
        int wBoardMargin = screenX / 10 / 2;//左右のマージンの合計
        int boardMargin = wBoardMargin / 2;
        return boardMargin;
    }

}

package com.baron.minmusicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ListAdapter extends BaseAdapter {
    static class ViewHolder {
        TextView text;
        ImageView img;
    }
    private LayoutInflater inflater;
    private int itemLayoutId;
    private String[] namelist;//曲名
    private int[] photos;

    ListAdapter(
            Context context, int itemLayoutId, String[] names,  int[] photos ){
        this.inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//inflater = LayoutInflater.from(context);
        this.itemLayoutId = itemLayoutId;
        this.namelist = names;
        this.photos = photos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // 最初だけ View を inflate して、それを再利用する
            convertView = inflater.inflate(itemLayoutId, parent, false);// (layoutID, null)activity_main.xml に list.xml を inflate して convertView とする
            holder = new ViewHolder();// ViewHolder を生成
            holder.img = convertView.findViewById(R.id.imageView);
            holder.text = convertView.findViewById(R.id.textView);
            convertView.setTag(holder);
        } else {
            // holder を使って再利用
            holder = (ViewHolder) convertView.getTag();
        }
        // holder の imageView にセット
        holder.img.setImageResource(photos[position]);
        //holder.img.setImageBitmap(photolist[position]);
        // 現在の position にあるファイル名リストを holder の textView にセット
        holder.text.setText(namelist[position]);
        return convertView;
    }

    @Override
    public int getCount() {
        // データ配列の要素数
        return namelist.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
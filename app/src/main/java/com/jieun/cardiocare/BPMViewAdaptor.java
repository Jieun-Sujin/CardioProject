package com.jieun.cardiocare;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class BPMViewAdaptor extends BaseAdapter {
    private ArrayList<HeartUser>mItems = new ArrayList<>();
    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public HeartUser getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();

        // 커스텀 리스트뷰의 xml을 inflate
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_bpm, parent, false);
        }

        /* 커스텀 리스트뷰 xml에 있는 속성값들을 정의 */
        ImageView statusImage = (ImageView)convertView.findViewById(R.id.statusIcon);
        TextView dateText =(TextView)convertView.findViewById(R.id.dateText);
        TextView bpmText =(TextView)convertView.findViewById(R.id.bpmText);


        /* 데이터를 담는 그릇 정의 */
        HeartUser userItem = getItem(position);
        if(userItem.getStatus()==0){
            statusImage.setImageResource(R.drawable.ic_status_stable_on);
        }else if(userItem.getStatus()==1){
            statusImage.setImageResource(R.drawable.ic_status_excite_on);
        }else if(userItem.getStatus()==2){
            statusImage.setImageResource(R.drawable.ic_status_run_on);
        }else if(userItem.getStatus()==3){
            statusImage.setImageResource(R.drawable.ic_status_depress_on);
        }else if(userItem.getStatus()==4){
            statusImage.setImageResource(R.drawable.ic_status_slepp_on);
        }

        dateText.setText(userItem.getDate());
        bpmText.setText(String.valueOf((int)userItem.getBpm()) +"  BPM");
        return convertView;
    }

    public void addItem(float bpm, int status, String date ) {

        HeartUser userItem = new HeartUser();
        userItem.setBpm(bpm);
        userItem.setStatus(status);
        userItem.setDate(date);
        mItems.add(userItem);
    }

}

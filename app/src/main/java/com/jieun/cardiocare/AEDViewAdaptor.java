package com.jieun.cardiocare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AEDViewAdaptor extends BaseAdapter {
    private ArrayList<AEDItem>mItems = new ArrayList<>();
    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public AEDItem getItem(int position) {
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
            convertView = inflater.inflate(R.layout.listview_aed, parent, false);
        }

        /* 커스텀 리스트뷰 xml에 있는 속성값들을 정의 */
        TextView buildPlace = (TextView) convertView.findViewById(R.id.buildPlace);
        TextView buildAddress = (TextView) convertView.findViewById(R.id.buildAddress);
//        TextView clerkTel = (TextView) convertView.findViewById(R.id.clerkTel);
//        TextView distance = (TextView) convertView.findViewById(R.id.distance);


        /* 데이터를 담는 그릇 정의 */
        AEDItem aedItem = getItem(position);

        /* 해당 그릇에 담긴 정보들을 커스텀 리스트뷰 xml의 각 TextView에 뿌려줌 */
        buildAddress.setText(aedItem.getBuildAddress());
        buildPlace.setText(aedItem.getBuildPlace());
//        clerkTel.setText(aedItem.getClerkTel());
//        distance.setText(aedItem.getDistance());

        return convertView;
    }

    public void addItem(String buildAddress, String buildPlace, String clerkTel, String distance,String wgs84Lat, String wgs84Lon) {

        AEDItem mItem = new AEDItem();

        mItem.setBuildAddress(buildAddress);
        mItem.setBuildPlace(buildPlace);
        mItem.setClerkTel(clerkTel);
        mItem.setDistance(distance);
        mItem.setWgs84Lat(wgs84Lat);
        mItem.setWgs84Lon(wgs84Lon);

        mItems.add(mItem);

    }

}

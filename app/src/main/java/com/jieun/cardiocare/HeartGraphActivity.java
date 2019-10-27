package com.jieun.cardiocare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.hadiidbouk.charts.BarData;
import com.hadiidbouk.charts.ChartProgressBar;
import com.hadiidbouk.charts.OnBarClickedListener;

import java.util.ArrayList;

public class HeartGraphActivity extends AppCompatActivity implements OnBarClickedListener {

    private ChartProgressBar mChart;
    private Spinner spinner;
    private static final String[] paths
            = {"all", "stable", "running","depressed","excite","sleep"};

    private static final String[] today={};
    private static final String[] week={"MON","TUS","WEN","THR","FRI","SAT","SUN"};
    private static final String[] month
            ={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_graph);
        initBar();
        initSpinner();

    }

    private void initBar(){
        ArrayList<BarData> dataList = new ArrayList<>();

        BarData data = new BarData("Sep", 60.f, "60");
        dataList.add(data);

        data = new BarData("Oct", 110.0f, "110");
        dataList.add(data);

        data = new BarData("Nov", 1.8f, "1.8");
        dataList.add(data);

        data = new BarData("Dec", 7.3f, "7.3");
        dataList.add(data);

        data = new BarData("Jan", 6.2f, "6.2");
        dataList.add(data);

        data = new BarData("Feb", 3.3f, "3.3");
        dataList.add(data);


        data = new BarData("Mar", 4.0f, "4.0");
        dataList.add(data);

        mChart = (ChartProgressBar) findViewById(R.id.ChartProgressBar);

        mChart.setDataList(dataList);
        mChart.build();
        mChart.setOnBarClickedListener(this);
//        mChart.disableBar(dataList.size() - 1);
    }
    @Override public void onBarClicked(int index) {
//        Toast.makeText(this, String.valueOf(index), Toast.LENGTH_SHORT).show();
    }

    private void initSpinner(){
        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
//                Toast.makeText(getApplicationContext(), paths[position-1], Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }





}

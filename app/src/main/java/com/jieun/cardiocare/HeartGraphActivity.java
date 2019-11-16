package com.jieun.cardiocare;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.jieun.cardiocare.HeartActivity.mDatabase;

public class HeartGraphActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    final static int DAILY = 7;
    final static int WEEK = 5;
    final static int MONTH = 12;
    final static int STATUS =5;

    TextView dateText, timeText, bpmText, statusText;
    Spinner timeSpinner, statusSpinner;
    TextView max_v, min_v, avg_v;
    RelativeLayout current_layout;
    ListView listView;
    BPMViewAdaptor adaptor;


    static int clicked1 = 0, clicked2 = 0;

    int[][] daily = new int[7][5];
    int[][] daily_cnt = new int[7][5];
    float[][] daily_avg = new float[7][5];

    int[][] week = new int[5][5];
    int[][] week_cnt = new int[5][5]; //입력한 개수
    float[][] weekly_avg = new float[5][5];

    int[][] month = new int[12][5];
    int[][] month_cnt = new int[12][5];
    float[][] month_avg = new float[12][5];


    float max = 40, min = 120, avg = 0;
    Calendar calendar = Calendar.getInstance();
    LineChart mChart;

    //firebase

    private FirebaseAuth mAuth;
    private FirebaseUser fuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_graph);

        final int start = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());

        Toolbar toolbar = (androidx.appcompat.widget.Toolbar)findViewById(R.id.toolbar7);
        toolbar.setTitle("심박수 기록");
        toolbar.setTitleMarginStart(start);
        setSupportActionBar(toolbar);

        initFireBase();
        calculateWeekData();
        init();




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar, menu) ;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_home:
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initFireBase(){
        mAuth = FirebaseAuth.getInstance();
        fuser = mAuth.getCurrentUser();
    }

    public void init() {

        dateText = (TextView) findViewById(R.id.dateText);
        timeText = (TextView) findViewById(R.id.timeText);
        bpmText = (TextView) findViewById(R.id.bpmText);
        statusText = (TextView) findViewById(R.id.statusText);

        min_v = (TextView) findViewById(R.id.min_v);
        max_v = (TextView) findViewById(R.id.max_v);
        avg_v = (TextView) findViewById(R.id.avg_v);

        //chart
        mChart = (LineChart) findViewById(R.id.linechart);

        //spinner
        timeSpinner = (Spinner) findViewById(R.id.time_spinner);
        timeSpinner.setSelection(0);
        statusSpinner = (Spinner) findViewById(R.id.status_spinner);
        statusSpinner.setSelection(0);

        ArrayAdapter timeAdapter = ArrayAdapter.createFromResource(this, R.array.time_array, R.layout.spinner_text);
        ArrayAdapter statusAdapter = ArrayAdapter.createFromResource(this, R.array.status_array, R.layout.spinner_text);

        timeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        timeSpinner.setAdapter(timeAdapter);
        statusAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        statusSpinner.setAdapter(statusAdapter);

        setupGraph();
        getVal(clicked1, clicked2);

        statusSpinner.setOnItemSelectedListener(this);
        timeSpinner.setOnItemSelectedListener(this);

        initListView();


    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //TODO..
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        switch (adapterView.getId()) {
            case R.id.time_spinner:
                Toast.makeText(getApplicationContext(),"time",Toast.LENGTH_SHORT).show();
                clicked1 = pos;
                setupGraph();
                getVal(clicked1, clicked2);
                break;

            case R.id.status_spinner:
                Toast.makeText(getApplicationContext(),"status",Toast.LENGTH_SHORT).show();
                clicked2 = pos;
                setupGraph();
                getVal(clicked1, clicked2);
                break;
            default:
                setupGraph();
                getVal(clicked1, clicked2);
                break;
        }
    }

    private void calculateWeekData() {

        final int curWeek = calendar.get(Calendar.WEEK_OF_MONTH);
        //String id = fuser.getUid();
        String id = getString(R.string.firebase_key);

        mDatabase.child("Bpm").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    String dateString = data.getKey();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    long now = System.currentTimeMillis();
                    Date convertedDate = new Date(now);
                    try {
                        convertedDate = dateFormat.parse(dateString);
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    calendar.setTime(convertedDate);

                    //월별로
                    for (DataSnapshot timeData : data.getChildren()) {
                        int type = Integer.parseInt(timeData.child("status").getValue().toString());
                        float val = Float.parseFloat(timeData.child("bpm").getValue().toString());
                        week[calendar.get(Calendar.WEEK_OF_MONTH) - 1][type] += val;
                        week_cnt[calendar.get(Calendar.WEEK_OF_MONTH) - 1][type] += 1;
                    }

                    //오늘과 같은 주.
                    if (calendar.get(Calendar.WEEK_OF_MONTH) == curWeek) {

                        //시간 별로
                        for (DataSnapshot timeData : data.getChildren()) {

                            int type = Integer.parseInt(timeData.child("status").getValue().toString());
                            float val = Float.parseFloat(timeData.child("bpm").getValue().toString());
                            //해당 되는 주에 더하기.
                            daily[calendar.get(Calendar.DAY_OF_WEEK) - 1][type] += val;
                            daily_cnt[calendar.get(Calendar.DAY_OF_WEEK) - 1][type] += 1;
                        }
                    }


                    //시간 별로
                    for (DataSnapshot timeData : data.getChildren()) {
                        int type = Integer.parseInt(timeData.child("status").getValue().toString());
                        float val = Float.parseFloat(timeData.child("bpm").getValue().toString());
                        month[calendar.get(Calendar.MONTH)][type] += val;
                        month_cnt[calendar.get(Calendar.MONTH)][type] += 1;
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }


    private void initListView() {

        listView =(ListView)findViewById(R.id.listView);
        adaptor = new BPMViewAdaptor();
        //String id = fuser.getUid();
        String id = getString(R.string.firebase_key);

        mDatabase.child("Bpm").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    String dateString = data.getKey();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    long now = System.currentTimeMillis();
                    Date convertedDate = new Date(now);
                    try {
                        convertedDate = dateFormat.parse(dateString);
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    calendar.setTime(convertedDate);

                    //오늘 날짜
                    //Toast.makeText(getApplicationContext(),dateFormat.format(convertedDate),Toast.LENGTH_SHORT).show();
                    for (DataSnapshot timeData : data.getChildren()) {
                        String date =dateFormat.format(convertedDate) +"  "+timeData.getKey();
                        int status = Integer.parseInt(timeData.child("status").getValue().toString());
                        float bpm = Float.parseFloat(timeData.child("bpm").getValue().toString());
                        adaptor.addItem(bpm,status,date);
                    }

                    listView.setAdapter(adaptor);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    /*private void calculateWeekData() {

        final int curWeek = calendar.get(Calendar.WEEK_OF_MONTH);
        //String id = fuser.getUid();
        String id = getString(R.string.firebase_key);

        mDatabase.child("Bpm").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    String dateString = data.getKey();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date convertedDate = new Date();
                    try {
                        convertedDate = dateFormat.parse(dateString);
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    calendar.setTime(convertedDate);

                    for (DataSnapshot timeData : data.getChildren()) {
                        int type = Integer.parseInt(timeData.child("status").getValue().toString());
                        float val = Float.parseFloat(timeData.child("bpm").getValue().toString());
                        week[calendar.get(Calendar.WEEK_OF_MONTH) - 1][type] += val;
                        week_cnt[calendar.get(Calendar.WEEK_OF_MONTH) - 1][type] += 1;
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        mDatabase.child("Bpm").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    //이번주에 해당하는 날짜 가지고 오기
                    String dateString = data.getKey();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date convertedDate = new Date();
                    try {
                        convertedDate = dateFormat.parse(dateString);
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    calendar.setTime(convertedDate);

                    //오늘과 같은 주라면. 탐색
                    if (calendar.get(Calendar.WEEK_OF_MONTH) == curWeek) {

                        //시간 별로
                        for (DataSnapshot timeData : data.getChildren()) {

                            int type = Integer.parseInt(timeData.child("status").getValue().toString());
                            float val = Float.parseFloat(timeData.child("bpm").getValue().toString());
                            //해당 되는 주에 더하기.
                            daily[calendar.get(Calendar.DAY_OF_WEEK) - 1][type] += val;
                            daily_cnt[calendar.get(Calendar.DAY_OF_WEEK) - 1][type] += 1;
                        }
                    }


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        mDatabase.child("Bpm").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    String dateString = data.getKey();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date convertedDate = new Date();
                    try {
                        convertedDate = dateFormat.parse(dateString);
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    calendar.setTime(convertedDate);

                    //시간 별로
                    for (DataSnapshot timeData : data.getChildren()) {
                        int type = Integer.parseInt(timeData.child("status").getValue().toString());
                        float val = Float.parseFloat(timeData.child("bpm").getValue().toString());
                        month[calendar.get(Calendar.MONTH)][type] += val;
                        month_cnt[calendar.get(Calendar.MONTH)][type] += 1;
                    }


                }


            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }*/

    private void calculateB() {

        if (clicked1 == 0) {
            for (int i = 0; i < DAILY; i++) {
                for (int j = 0; j < STATUS; j++) {
                    if (daily[i][j] != 0) {
                        daily_avg[i][j] = daily[i][j] / daily_cnt[i][j];
                    }
                }
            }
        }

        if (clicked1 == 1) {
            for (int i = 0; i < WEEK; i++) {
                for (int j = 0; j < STATUS; j++) {
                    if (week[i][j] != 0) {
                        weekly_avg[i][j] = week[i][j] / week_cnt[i][j];

                    }
                }
            }

        }

        if (clicked1 == 2) {
            for (int i = 0; i < MONTH; i++) {
                for (int j = 0; j < STATUS; j++) {
                    if (month[i][j] != 0) {
                        month_avg[i][j] = month[i][j] / month_cnt[i][j];

                    }

                }
            }

        }


    }

    private void getVal(int period, int status) {

        calculateB();
        max = 40;
        min = 120;
        avg = 0;
        float sum = 0;
        int cnt = 0;

        if (period == 0) {
            for (int i = 0; i < DAILY; i++) {
                if (daily[i][status] != 0) {
                    min = min < daily_avg[i][status] ? min : daily_avg[i][status];
                    max = max > daily_avg[i][status] ? max : daily_avg[i][status];
                    sum += daily_avg[i][status];
                    cnt++;
                }
            }
        }

        if (period == 1) {
            for (int i = 0; i < WEEK; i++) {
                if (week[i][status] != 0) {
                    min = min < weekly_avg[i][status] ? min : weekly_avg[i][status];
                    max = max > weekly_avg[i][status] ? max : weekly_avg[i][status];
                    sum += weekly_avg[i][status];
                    cnt++;
                }
            }
        }

        if (period == 2) {
            for (int i = 0; i < MONTH; i++) {
                if (month[i][status] != 0) {
                    min = min < month_avg[i][status] ? min : month_avg[i][status];
                    max = max > month_avg[i][status] ? max : month_avg[i][status];
                    sum += month_avg[i][status];
                    cnt++;
                }
            }


        }

        if (cnt != 0) {
            avg = sum / cnt;
            min_v.setText("" + min);
            max_v.setText("" + max);
            avg_v.setText("" + avg);

        } else {
            max_v.setText("");
            min_v.setText("");
            avg_v.setText("");
        }


    }

    //graph setip
    private void setupGraph() {
        // add data
        Toast.makeText(getApplicationContext(),"setData",Toast.LENGTH_SHORT).show();
        setData();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        // enable touch gestures
        mChart.setTouchEnabled(false); //선 없애기

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);


        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        //leftAxis.addLimitLine(lower_limit);
        leftAxis.setAxisMaxValue(120f);
        leftAxis.setAxisMinValue(40f);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(10f, 5f, 0f);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);
        mChart.invalidate();
    }


    //------------------------- x좌표값 -----------------------------
    private ArrayList<String> setXAxisValues() {

        ArrayList<String> xVals = new ArrayList<String>();

        if (clicked1 == 0) {
            xVals.clear();
            xVals.add("SUN");
            xVals.add("MON");
            xVals.add("TUS");
            xVals.add("WEN");
            xVals.add("THR");
            xVals.add("FRI");
            xVals.add("SAT");
        }

        if (clicked1 == 1) {
            xVals.clear();
            for (int i = 0; i < WEEK; i++) {
                String input = (i + 1) + "주";
                xVals.add(input);
            }

        }

        if (clicked1 == 2) {
            xVals.clear();
            for (int i = 0; i < MONTH; i++) {
                String input = (i + 1) + "월";
                xVals.add(input);
            }
        }


        return xVals;
    }

    private ArrayList<Entry> setYAxisValues() {
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        if (clicked1 == 0) {
            yVals.clear();
            for (int i = 0; i < DAILY; i++) {
                if (daily[i][clicked2] != 0) {
                    yVals.add(new Entry(daily_avg[i][clicked2], i));
                }

            }
        }

        if (clicked1 == 1) {
            yVals.clear();
            for (int i = 0; i < WEEK; i++) {
                if (week[i][clicked2] != 0) {
                    yVals.add(new Entry(weekly_avg[i][clicked2], i));
                }

            }

        }

        if (clicked1 == 2) {
            yVals.clear();
            for (int i = 0; i < MONTH; i++) {
                if (month[i][clicked2] != 0) {
                    yVals.add(new Entry(month_avg[i][clicked2], i));
                }

            }
        }

        return yVals;
    }

    private void setData() {
        ArrayList<String> xVals = setXAxisValues();
        ArrayList<Entry> yVals = setYAxisValues();
        LineDataSet set1;


        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "BPM Value");

        set1.setFillAlpha(110);

        set1.setColor(getColor(R.color.colorGraph2));
        set1.setCircleColor(getColor(R.color.colorGraph2));
        set1.setLineWidth(3f);
        set1.setCircleRadius(5f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(13f);
        set1.setDrawFilled(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        mChart.setData(data);
        mChart.getAxisLeft().setDrawLabels(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setLabelsToSkip(0);
        xAxis.setTextSize(13f);

    }
}
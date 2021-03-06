package com.jieun.cardiocare;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    //private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    TextView msgText;
    TextView userNameTxt;
    TextView dateText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initToolbar();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        //mDatabase = FirebaseDatabase.getInstance().getReference();

        //final String userId = user.getUid();

        final String userName = user.getDisplayName();

        userNameTxt = (TextView) findViewById(R.id.userName);
        userNameTxt.setText(userName + "   님");
        initUI();

        final Button inputBtn = (Button) findViewById(R.id.dataInputBtn);
        Button predictBtn = (Button) findViewById(R.id.predictBtn);
        Button bpmBtn = (Button) findViewById(R.id.bpmBtn);
        Button aedBtn = (Button) findViewById(R.id.aedBtn);

        Button.OnTouchListener onTouchListener = new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                Button btn = (Button) view;
                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:
                        btn.setTextColor(getColor(R.color.white));
                        switch (btn.getId()) {
                            case R.id.dataInputBtn:
                                btn.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_noun_person_data2),null, getDrawable(R.drawable.ic_navigator2),null);
                                break;
                            case R.id.predictBtn:
                                btn.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_noun_neural_network2),null, getDrawable(R.drawable.ic_navigator2),null);
                                break;
                            case R.id.bpmBtn:
                                btn.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_noun_heart2),null, getDrawable(R.drawable.ic_navigator2),null);
                                break;
                            case R.id.aedBtn:
                                btn.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_noun_map2),null, getDrawable(R.drawable.ic_navigator2),null);
                                break;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        btn.setTextColor(getColor(R.color.home_navi_color));
                        switch (btn.getId()) {
                            case R.id.dataInputBtn:
                                btn.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_noun_person_data),null, getDrawable(R.drawable.ic_navigator),null);
                                break;
                            case R.id.predictBtn:
                                btn.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_noun_neural_network1),null, getDrawable(R.drawable.ic_navigator),null);
                                break;
                            case R.id.bpmBtn:
                                btn.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_noun_heart),null, getDrawable(R.drawable.ic_navigator),null);
                                break;
                            case R.id.aedBtn:
                                btn.setCompoundDrawablesWithIntrinsicBounds(getDrawable(R.drawable.ic_noun_map),null, getDrawable(R.drawable.ic_navigator),null);
                                break;
                        }
                        break;
                }
                return false;
            }
        };

        inputBtn.setOnTouchListener(onTouchListener);
        predictBtn.setOnTouchListener(onTouchListener);
        bpmBtn.setOnTouchListener(onTouchListener);
        aedBtn.setOnTouchListener(onTouchListener);

    }

    private void initToolbar() {
        final int cardict = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics());
        //final int logout = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());

        Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

    }

    private void initUI() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat hourSdf = new SimpleDateFormat("HH");
        SimpleDateFormat todaySdf = new SimpleDateFormat("yyyy-MM-dd");

        msgText = (TextView) findViewById(R.id.msgText);
        dateText =(TextView)findViewById(R.id.dateText);
        dateText.setText(todaySdf.format(date));

        int hour = Integer.parseInt(hourSdf.format(date));
        String[] msgList = getResources().getStringArray(R.array.msg_array);

        if (7 <= hour && 9 >= hour) {
            //아침
            msgText.setText(msgList[0]);
        } else if (9 < hour && 18 >= hour) {
            //점심
            msgText.setText(msgList[1]);
        } else if (18 < hour && 22 >= hour) {
            //저녁
            msgText.setText(msgList[2]);
        } else {
            //잠
            msgText.setText(msgList[3]);
        }

    }


    public void clickHeart(View view) {
        Intent intent = new Intent(getApplicationContext(), HeartActivity.class);
        startActivity(intent);
    }

    public void clickInputMydata(View view) {
        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
        startActivity(intent);
    }

    public void clickMyPredict(View view) {
        Intent intent = new Intent(getApplicationContext(), DataCheckActivity.class);
        startActivity(intent);
    }

    public void clickAed(View view) {

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            Intent intent = new Intent(getApplicationContext(), AEDActivity.class);
            startActivity(intent);
        }
    }

    public void clickLogout(View view) {
        mAuth.signOut();
        initToast("로그아웃 되었습니다");

        finish();
    }

    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.DialogStyle);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("이 기능을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 활성화 하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void initToast(String msg){

        LayoutInflater inflater = getLayoutInflater();
        View toastDesign = inflater.inflate(R.layout.toast_design2, (ViewGroup)findViewById(R.id.toast_design_root2)); //toast_design.xml 파일의 toast_design_root 속성을 로드

        TextView text = toastDesign.findViewById(R.id.TextView_toast_design);
        text.setText(msg);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 800); // CENTER를 기준으로 0, 0 위치에 메시지 출력
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastDesign);
        toast.show();

    }

}
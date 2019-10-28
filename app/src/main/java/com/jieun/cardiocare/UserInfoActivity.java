package com.jieun.cardiocare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.AppCompatActivity;

public class UserInfoActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    DatePicker birthday;
    RadioGroup genderSelect;
    NumberPicker heightPicker;
    NumberPicker weightPicker;
    NumberPicker aphiPicker;
    NumberPicker aploPicker;
    NumberPicker cholPicker;
    RadioGroup smokeSelect;
    RadioGroup alcoSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userdata_input);

        Intent intent = getIntent();
        final String userId = intent.getExtras().getString("userId");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("BodyInfo");

        birthday = (DatePicker) findViewById(R.id.datePicker);
        genderSelect = (RadioGroup) findViewById(R.id.genderSelect);
        heightPicker = (NumberPicker) findViewById(R.id.npkHeight);
        weightPicker = (NumberPicker) findViewById(R.id.npkWeight);
        aphiPicker = (NumberPicker) findViewById(R.id.npkAphi);
        aploPicker = (NumberPicker) findViewById(R.id.npkAplo);
        cholPicker = (NumberPicker) findViewById(R.id.npkChol);
        smokeSelect = (RadioGroup) findViewById(R.id.smokeSelect);
        alcoSelect = (RadioGroup) findViewById(R.id.alcoSelect);

        //생년월일 datepicker 설정
        Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH) + 1;
        final int mDay = c.get(Calendar.DAY_OF_MONTH);
        birthday.init(mYear,mMonth,mDay,null);

        //데이터 저장
        Button saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long age = getAge(mYear,mMonth,mDay);
                int gender = getGender();
                float height =  heightPicker.getValue();
                float weight = weightPicker.getValue();
                int aphi = aphiPicker.getValue();
                int aplo = aploPicker.getValue();
                int chol = getChol();
                int smoke = getSmoke();
                int alco = getAlco();

                mDatabase.child("age").setValue(age);
                mDatabase.child("gender").setValue(gender);
                mDatabase.child("height").setValue(height);
                mDatabase.child("weight").setValue(weight);
                mDatabase.child("ap_hi").setValue(aphi);
                mDatabase.child("ap_lo").setValue(aplo);
                mDatabase.child("cholesterol").setValue(chol);
                mDatabase.child("smoke").setValue(smoke);
                mDatabase.child("alco").setValue(alco);

            }
        });

        //heightpicker 설정
        heightPicker.setMinValue(0);
        heightPicker.setMaxValue(300);
        heightPicker.setValue(150);

        // weightpicker 설정
        weightPicker.setMinValue(0);
        weightPicker.setMaxValue(300);
        weightPicker.setValue(50);

        // aphipicker 설정
        aphiPicker.setMinValue(0);
        aphiPicker.setMaxValue(300);
        aphiPicker.setValue(120);

        // aplopicker 설정
        aploPicker.setMinValue(0);
        aploPicker.setMaxValue(200);
        aploPicker.setValue(80);

        // cholpicker 설정
        cholPicker.setMinValue(0);
        cholPicker.setMaxValue(300);
        cholPicker.setValue(200);


    }

    public long getAge(int mYear, int mMonth, int mDay){

        long age = 0;

        String birthY = birthday.getYear() + "";
        String birthM = "";
        if(birthday.getMonth() + 1 < 10)
            birthM = "0" + (birthday.getMonth() + 1);
        else
            birthM = (birthday.getMonth() + 1) + "";
        String birthD = birthday.getDayOfMonth() + "";

        String today = "";
        if(mMonth < 10)
            today = Integer.toString(mYear) + "0" + String.valueOf(mMonth) + String.valueOf(mDay);
        else
            today = Integer.toString(mYear) + String.valueOf(mMonth) + String.valueOf(mDay);

        String birthYMD = birthY + birthM + birthD;
        Log.i("today", today);
        Log.i("birthday",birthYMD);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        try{
            Date startDate = format.parse(today);
            Date endDate = format.parse(birthYMD);

            age = (startDate.getTime() - endDate.getTime()) / (24*60*60*1000);

        }catch(Exception e){
            e.printStackTrace();
        }

        return age;
    }

    public int getGender() {

        int gender;

        int genderId = genderSelect.getCheckedRadioButtonId();
        RadioButton rb = (RadioButton) findViewById(genderId);
        if("여".equals(rb.getText()))
            gender = 0; // 데이터 확인 해야함
        else //남자일때
            gender = 1;

        return gender;
    }

    public int getChol() {

        int chol;

        if(cholPicker.getValue() < 200) {
            chol = 1; //normal
        } else if(cholPicker.getValue() < 240) {
            chol = 2; //above normal
        } else {
            chol = 3; //well above normal
        }

        return chol;
    }

    public int getSmoke() {

        int smoke;

        int smokeId = smokeSelect.getCheckedRadioButtonId();
        RadioButton rb2 = (RadioButton) findViewById(smokeId);
        if("예".equals(rb2.getText()))
            smoke = 1;
        else // "아니요" 일 경우
            smoke = 0;

        return smoke;

        //음주여부

    }

    public int getAlco() {

        int alco;

        int alcoId = alcoSelect.getCheckedRadioButtonId();
        RadioButton rb3 = (RadioButton) findViewById(alcoId);
        if("예".equals(rb3.getText()))
            alco = 1;
        else // "아니요" 일 경우
            alco = 0;

        return alco;
    }

}

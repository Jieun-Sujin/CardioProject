package com.jieun.cardiocare;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import shortbread.Shortcut;

//@Shortcut(id = "PROFILE", icon = R.drawable.ic_noun_profile, shortLabelRes = R.string.label_profile, rank = 2)

public class UserInfoActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    DatePicker birthday;
    RadioGroup genderSelect, smokeSelect, alcoSelect;
    RadioButton man, woman, smoker, nonSmoker, drinking, nonDrink;
    Button heightBtn, weightBtn, aphiBtn, aploBtn, cholBtn;
    int mYear, mMonth, mDay;
    private Boolean join = false; // 가입되어있는지

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userdata_input);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String userId = user.getUid();
        final String userName = user.getDisplayName();
        getUser(userId);

        birthday = (DatePicker) findViewById(R.id.datePicker);
        genderSelect = (RadioGroup) findViewById(R.id.genderSelect);
        heightBtn = (Button) findViewById(R.id.btnHeight);
        weightBtn = (Button) findViewById(R.id.btnWeight);
        aphiBtn = (Button) findViewById(R.id.btnAphi);
        aploBtn = (Button) findViewById(R.id.btnAplo);
        cholBtn = (Button) findViewById(R.id.btnChol);
        smokeSelect = (RadioGroup) findViewById(R.id.smokeSelect);
        alcoSelect = (RadioGroup) findViewById(R.id.alcoSelect);
        woman = (RadioButton) findViewById(R.id.woman);
        man = (RadioButton) findViewById(R.id.man);
        smoker = (RadioButton) findViewById(R.id.smoker);
        nonSmoker = (RadioButton) findViewById(R.id.Nonsmoker);
        drinking = (RadioButton) findViewById(R.id.drinking);
        nonDrink= (RadioButton) findViewById(R.id.Nondrink);


        //생년월일 datepicker 설정
        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH) + 1;
        mDay = c.get(Calendar.DAY_OF_MONTH);
        birthday.init(mYear,mMonth,mDay,null);

        //데이터 저장
        Button saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String age = getBirth();
                int gender = getGender();

                String heightVal = String.valueOf(heightBtn.getText());
                float height = Float.parseFloat(heightVal.substring(0,heightVal.length() - 2));

                String weightVal = String.valueOf(weightBtn.getText());
                float weight = Float.parseFloat(weightVal.substring(0,weightVal.length() - 2));

                String aphiVal = String.valueOf(aphiBtn.getText());
                int aphi = Integer.parseInt(aphiVal.substring(0, aphiVal.length() - 4));

                String aploVal = String.valueOf(aploBtn.getText());
                int aplo = Integer.parseInt(aploVal.substring(0, aploVal.length() - 4));

                String cholVal = String.valueOf(cholBtn.getText());
                int chol = Integer.parseInt(cholVal.substring(0, cholVal.length() - 5));

                int smoke = getSmoke();
                int alco = getAlco();

                UserData user = new UserData(userName, gender, age, height, weight, chol, aphi, aplo, smoke, alco);
                mDatabase.child("BodyInfo").child(userId).setValue(user);
                Toast.makeText(getApplicationContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show();

                if(!join) { // 새로 가입하여 처음 저장하는 경우
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });

    }
    public String getBirth() {

        String birthY = birthday.getYear() + "";
        String birthM = "";
        String birthD = "";

        if(birthday.getMonth() + 1 < 10)
            birthM = "0" + (birthday.getMonth() + 1);
        else
            birthM = (birthday.getMonth() + 1) + "";

        if(birthday.getDayOfMonth() < 10)
            birthD = "0" + (birthday.getDayOfMonth());
        else
            birthD = (birthday.getDayOfMonth()) + "";

        String birthYMD = birthY + birthM + birthD;

        return birthYMD;
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

    public void getUser(String userId){

        mDatabase.child("BodyInfo").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        UserData user = dataSnapshot.getValue(UserData.class);
                        if(user != null){ // 이미 가입되어 있다면 저장되어 있던 데이터 보여주기
                            join = true;
                            int birhtY = Integer.parseInt(user.getAge().substring(0,4));
                            int birthM = Integer.parseInt(user.getAge().substring(4,6));
                            int birthD = Integer.parseInt(user.getAge().substring(6));
                            Log.i("월",birthM+"");
                            birthday.init(birhtY,birthM-1,birthD,null);
                            if(user.getGender() == 0)
                                genderSelect.check(woman.getId());
                            else
                                genderSelect.check(man.getId());

                            heightBtn.setText(user.getHeight() + "cm");
                            weightBtn.setText(user.getWeight() + "kg");
                            aphiBtn.setText(user.getAp_hi() + "mmHg");
                            aploBtn.setText(user.getAp_lo() + "mmHg");
                            cholBtn.setText(user.getCholesterol() + "mg/dl");

                            if(user.getSmoke() == 0)
                                smokeSelect.check(nonSmoker.getId());
                            else
                                smokeSelect.check(smoker.getId());

                            if(user.getAlco() == 0)
                                alcoSelect.check(nonDrink.getId());
                            else
                                alcoSelect.check(drinking.getId());
                        }
                        else{ // 가입안했다면 기본값으로 보여주기
                            join = false;
                            birthday.init(mYear,mMonth -1,mDay,null); //오늘날짜
                            heightBtn.setText(150 + "cm");
                            weightBtn.setText(50 + "kg");
                            aphiBtn.setText(120 + "mmHg");
                            aploBtn.setText(80 + "mmHg");
                            cholBtn.setText(20 + "mg/dl");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
                    }
                });
    }

    public void clickHeight(View view) {

        String height = String.valueOf(heightBtn.getText());
        String num = height.substring(0, height.length()-4);
        String dec = height.substring(height.length()-3,height.length()-2);
        Log.i("num", num + "");
        Log.i("dec", dec + "");

        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.double_picker_dialog, null);
        d.setTitle("신장 설정");
        d.setView(dialogView);
        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.integer_picker);
        final NumberPicker decimalPicker = (NumberPicker) dialogView.findViewById(R.id.decimal_picker);
        TextView unit = (TextView) dialogView.findViewById(R.id.unitTxt);
        unit.setText("cm");

        numberPicker.setMaxValue(300);
        numberPicker.setMinValue(0);

        decimalPicker.setMaxValue(9);
        decimalPicker.setMinValue(0);

        numberPicker.setValue(Integer.parseInt(num));
        decimalPicker.setValue(Integer.parseInt(dec));
        numberPicker.setWrapSelectorWheel(false);
        decimalPicker.setWrapSelectorWheel(true);

        d.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                heightBtn.setText(numberPicker.getValue() + "." + decimalPicker.getValue() + "cm");
                dialogInterface.dismiss();
            }
        });
        d.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }

    public void clickWeight(View view) {

        String weight = String.valueOf(weightBtn.getText());
        String num = weight.substring(0, weight.length()-4);
        String dec = weight.substring(weight.length()-3, weight.length()-2);

        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.double_picker_dialog, null);
        d.setTitle("체중 설정");
        d.setView(dialogView);
        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.integer_picker);
        final NumberPicker decimalPicker = (NumberPicker) dialogView.findViewById(R.id.decimal_picker);
        TextView unit = (TextView) dialogView.findViewById(R.id.unitTxt);
        unit.setText("kg");

        numberPicker.setMaxValue(300);
        numberPicker.setMinValue(0);

        decimalPicker.setMaxValue(9);
        decimalPicker.setMinValue(0);

        numberPicker.setValue(Integer.parseInt(num));
        decimalPicker.setValue(Integer.parseInt(dec));
        numberPicker.setWrapSelectorWheel(false);
        decimalPicker.setWrapSelectorWheel(true);

        d.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                weightBtn.setText(numberPicker.getValue() + "." + decimalPicker.getValue() + "kg");
                dialogInterface.dismiss();
            }
        });
        d.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }

    public void clickAphi(View view) {

        String aphi = String.valueOf(aphiBtn.getText());
        String setAphi = aphi.substring(0,aphi.length()-4);

        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.picker_dialog2, null);
        d.setTitle("최고혈압 설정");
        d.setView(dialogView);
        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.integer_picker);
        TextView unit = (TextView) dialogView.findViewById(R.id.unitTxt);
        unit.setText("mmHg");
        numberPicker.setMaxValue(300);
        numberPicker.setMinValue(0);
        numberPicker.setValue(Integer.parseInt(setAphi));
        numberPicker.setWrapSelectorWheel(false);

        d.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                aphiBtn.setText(numberPicker.getValue() + "mmHg");
                dialogInterface.dismiss();
            }
        });
        d.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }

    public void clickAplo(View view) {

        String aplo = String.valueOf(aploBtn.getText());
        String setAplo = aplo.substring(0,aplo.length()-4);

        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.picker_dialog2, null);
        d.setTitle("최저혈압 설정");
        d.setView(dialogView);
        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.integer_picker);
        TextView unit = (TextView) dialogView.findViewById(R.id.unitTxt);
        unit.setText("mmHg");
        numberPicker.setMaxValue(300);
        numberPicker.setMinValue(0);
        numberPicker.setValue(Integer.parseInt(setAplo));
        numberPicker.setWrapSelectorWheel(false);

        d.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                aploBtn.setText(numberPicker.getValue() + "mmHg");
                dialogInterface.dismiss();
            }
        });
        d.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }

    public void clickChol(View view) {

        String chol = String.valueOf(cholBtn.getText());
        String setChol = chol.substring(0,chol.length()-5);

        final AlertDialog.Builder d = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.picker_dialog2, null);
        d.setTitle("콜레스테롤 설정");
        d.setView(dialogView);
        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.integer_picker);
        TextView unit = (TextView) dialogView.findViewById(R.id.unitTxt);
        unit.setText("mg/dl");
        numberPicker.setMaxValue(300);
        numberPicker.setMinValue(0);
        numberPicker.setValue(Integer.parseInt(setChol));
        numberPicker.setWrapSelectorWheel(false);

        d.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                cholBtn.setText(numberPicker.getValue() + "mg/dl");
                dialogInterface.dismiss();
            }
        });
        d.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = d.create();
        alertDialog.show();
    }

}

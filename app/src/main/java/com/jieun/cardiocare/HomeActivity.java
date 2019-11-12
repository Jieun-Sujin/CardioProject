package com.jieun.cardiocare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    //private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    TextView msgText;
    TextView userNameTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        //mDatabase = FirebaseDatabase.getInstance().getReference();

        //final String userId = user.getUid();
        final String userName = "김지은";
        userNameTxt = (TextView) findViewById(R.id.userName);
        userNameTxt.setText(userName + "님");
        initUI();

    }


    private void initUI(){

        msgText =(TextView)findViewById(R.id.msgText);

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH");

        int hour = Integer.parseInt(sdfNow.format(date));
        String[] msgList = getResources().getStringArray(R.array.msg_array);

        if(7<=hour && 9>=hour){
            //아침
            msgText.setText(msgList[0]);
        }else if(9<hour && 18>=hour){
            //점심
            msgText.setText(msgList[1]);
        }else if(18<hour && 22>=hour){
            //저녁
            msgText.setText(msgList[2]);
        }else{
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
        Intent intent = new Intent(getApplicationContext(), AEDActivity.class);
        startActivity(intent);
    }

    public void clickLogout(View view) {
        mAuth.signOut();
        finish();
    }

}

package com.jieun.cardiocare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    //private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        //mDatabase = FirebaseDatabase.getInstance().getReference();

        final String userId = user.getUid();
        final String userName = user.getDisplayName();

        TextView userNameTxt = (TextView) findViewById(R.id.userName);
        userNameTxt.setText(userName + "님");

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

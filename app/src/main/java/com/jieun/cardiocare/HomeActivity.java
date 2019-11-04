package com.jieun.cardiocare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
        userNameTxt.setText(userName);

    }
}

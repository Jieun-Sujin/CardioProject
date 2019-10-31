package com.jieun.cardiocare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;

public class CardioPredictActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    TextView gender, age, height, weight, aphi, aplo, chol, smoke, alco;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_check);

        Intent intent = getIntent();
        final String userId = intent.getExtras().getString("userId");


    }

    public void getUser(String userId){

        mDatabase.child("users").child(userId).child("BodyInfo").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        UserData user = dataSnapshot.getValue(UserData.class);
                        if(user != null){

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
                    }
                });
    }

}

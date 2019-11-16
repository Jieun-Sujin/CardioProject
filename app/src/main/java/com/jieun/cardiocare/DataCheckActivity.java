package com.jieun.cardiocare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DataCheckActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    TextView gender, age, height, weight, aphi, aplo, chol, smoke, alco;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_check);

        final int start = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());

        Toolbar toolbar = (androidx.appcompat.widget.Toolbar)findViewById(R.id.toolbar2);
        toolbar.setTitle("심혈관질환 예측");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarText);
        toolbar.setTitleMarginStart(start);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        final String userId = user.getUid();
        final String userName = user.getDisplayName();
        getUser(userId);

        gender = (TextView) findViewById(R.id.gendertxt);
        age = (TextView) findViewById(R.id.agetxt);
        height = (TextView) findViewById(R.id.heighttxt);
        weight = (TextView) findViewById(R.id.weighttxt);
        aphi = (TextView) findViewById(R.id.aphitxt);
        aplo = (TextView) findViewById(R.id.aplotxt);
        chol = (TextView) findViewById(R.id.choltxt);
        smoke = (TextView) findViewById(R.id.smoketxt);
        alco = (TextView) findViewById(R.id.alcotxt);

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

    public void getUser(String userId){

        mDatabase.child("BodyInfo").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        UserData user = dataSnapshot.getValue(UserData.class);
                        if(user != null){
                            gender.setText(user.getGender() == 0 ? "여" : "남");
                            age.setText(user.getAge().substring(0,4) +"년 " + user.getAge().substring(4,6)+"월 " + user.getAge().substring(6) + "일");
                            //age.setText("만" + user.getAge()/365 + "세");
                            height.setText(String.valueOf(user.getHeight()));
                            weight.setText(String.valueOf(user.getWeight()));
                            aphi.setText(String.valueOf(user.getAp_hi()));
                            aplo.setText(String.valueOf(user.getAp_lo()));
                            chol.setText(String.valueOf(user.getCholesterol()));
                            smoke.setText(user.getSmoke() == 1 ? "예" : "아니요");
                            alco.setText(user.getAlco() == 1 ? "예" : "아니요");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
                    }
                });
    }

    public void clickPredict(View view) {

        Intent intent = new Intent(getApplicationContext(), CardioPredictActivity.class);
        startActivity(intent);
    }
    public void clickChange(View view) {

        Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
        startActivity(intent);
        finish();
    }
}

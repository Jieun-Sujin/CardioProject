package com.jieun.cardiocare;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
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
    TextView timeText;
    TextView userNameTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initToolbar();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        //mDatabase = FirebaseDatabase.getInstance().getReference();

        //final String userId = user.getUid();

        //final String userName = user.getDisplayName();
        final String userName = "김지은"; // 수진 테스트 할 때

        userNameTxt = (TextView) findViewById(R.id.userName);
        userNameTxt.setText(userName + "   님");
        initUI();

    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar, menu) ;
        return true;
    }
*/
   private void initToolbar(){
       final int cardict = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics());
       //final int logout = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics());

       Toolbar toolbar = (androidx.appcompat.widget.Toolbar)findViewById(R.id.toolbar);
       toolbar.setTitle("");
       /*toolbar.setTitle(R.string.appName);
       toolbar.setTitleTextColor(getColor(R.color.maincolor1));
       toolbar.setNavigationIcon(R.drawable.ic_logout);
       toolbar.setTitleMarginStart(cardict);
       */
       //toolbar.setContentInsetStartWithNavigation(logout);
       setSupportActionBar(toolbar);
       /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Toast.makeText(getApplicationContext(),"logout",Toast.LENGTH_SHORT).show();
               //mAuth.signOut();
               //finish();
           }
       });*/


   }

    private void initUI(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH");

        timeText =(TextView)findViewById(R.id.timeText);
        timeText.setText(sdf1.format(date));
        msgText =(TextView)findViewById(R.id.msgText);


        int hour = Integer.parseInt(sdf2.format(date));
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
        Toast.makeText(getApplicationContext(),"logout",Toast.LENGTH_SHORT).show();
        //mAuth.signOut();
        //finish();
    }

}

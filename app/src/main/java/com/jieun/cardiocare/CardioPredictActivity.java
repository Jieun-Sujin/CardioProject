package com.jieun.cardiocare;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import shortbread.Shortcut;

@Shortcut(id = "PREDICT", icon = R.drawable.ic_noun_neural_network1, shortLabelRes = R.string.label_predict, rank = 2)
public class CardioPredictActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private long age;
    private float height, weight;
    private int gender, aphi, aplo, cholesterol, smoke ,alco;
    TextView cardio_pst;

    int pStatus = 0;
    int ageStatus = 0;
    int cholStatus = 0;
    int aphiStatus = 0;
    int smokeStatus = 0;
    private Handler handler = new Handler();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cardio_predict);

        final int start = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());

        Toolbar toolbar = (androidx.appcompat.widget.Toolbar)findViewById(R.id.toolbar4);
        toolbar.setTitle("심혈관질환 예측");
        toolbar.setTitleTextAppearance(this, R.style.ToolbarText);
        toolbar.setTitleMarginStart(start);
        setSupportActionBar(toolbar);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        final String userId = user.getUid();
        final String userName = user.getDisplayName();

        TextView userNameTxt = (TextView) findViewById(R.id.user_name);
        final ProgressBar ageBar = (ProgressBar) findViewById(R.id.ageBar);
        final TextView agePct = (TextView) findViewById(R.id.agePct);
        final TextView comment = (TextView) findViewById(R.id.comment);

        final TextView cholPredictTxt = (TextView) findViewById(R.id.cholPredict);
        final ProgressBar cholBar = (ProgressBar) findViewById(R.id.cholBar);
        final TextView cholPct = (TextView) findViewById(R.id.cholPct);

        final TextView smokePredictTxt = (TextView) findViewById(R.id.smokePredict);
        final ProgressBar smokeBar = (ProgressBar) findViewById(R.id.smokeBar);
        final TextView smokePct = (TextView) findViewById(R.id.smokePct);

        final TextView aphiPredictTxt = (TextView) findViewById(R.id.aphiPredict);
        final ProgressBar aphiBar = (ProgressBar) findViewById(R.id.aphiBar);
        final TextView aphiPct = (TextView) findViewById(R.id.aphiPct);

        userNameTxt.setPaintFlags(userNameTxt.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        userNameTxt.setText(userName);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.circular);
        final ProgressBar mProgress = (ProgressBar) findViewById(R.id.circularProgressbar);
        mProgress.setProgress(0);   // Main Progress
        mProgress.setSecondaryProgress(100); // Secondary Progress
        mProgress.setMax(100); // Maximum Progress
        mProgress.setProgressDrawable(drawable);

        cardio_pst = (TextView) findViewById(R.id.cardio_pst);

        mDatabase.child("BodyInfo").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        UserData user = dataSnapshot.getValue(UserData.class);
                        if(user != null){

                            gender = user.getGender();
                            age = getAgeDay(user.getAge().substring(0,4), user.getAge().substring(4,6), user.getAge().substring(6));
                            height= user.getHeight();
                            weight = user.getWeight();
                            aphi = user.getAp_hi();
                            aplo = user.getAp_lo();
                            cholesterol = getChol(user.getCholesterol());
                            smoke = user.getSmoke();
                            alco = user.getAlco();

                            final float[] data = readCSV();
                            final float[] input = new float[]{age,gender,height,weight,aphi,aplo,cholesterol,smoke,alco}; //입력변수
                            final double percentage = predictCardio(data, input);

                            //발병률 색상 분류
                            if(percentage < 20) {
                                mProgress.setProgressTintList(ColorStateList.valueOf(Color.argb(255,124,252,0)));
                            } else if (percentage < 40) {
                                mProgress.setProgressTintList(ColorStateList.valueOf(Color.argb(255,255, 215, 0)));
                            } else if (percentage < 70) {
                                mProgress.setProgressTintList(ColorStateList.valueOf(Color.argb(255,255,69,0)));
                            } else {
                                mProgress.setProgressTintList(ColorStateList.valueOf(Color.RED));
                            }

                            final Thread progressThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while (pStatus < percentage - 1) {
                                        pStatus += 1;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mProgress.setProgress(pStatus);
                                                cardio_pst.setText(pStatus + "%");
                                            }
                                        });
                                        try {
                                            Thread.sleep(30);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            progressThread.start();


                            //10년 후 발병률
                            float[] input2 = new float[]{age + 3650,gender,height,weight,aphi,aplo,cholesterol,smoke,alco};
                            final double tenYearsPct = predictCardio(data, input2);

                            final Thread ageThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while (ageStatus < tenYearsPct - 1) {
                                        ageStatus += 1;
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                ageBar.setProgress(ageStatus);
                                                agePct.setText(ageStatus + "%");
                                            }
                                        });
                                        try {
                                            Thread.sleep(20);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                            ageThread.start();

                            //콜레스테롤 2또는 3일경우
                            float[] input5 = new float[]{age,gender,height,weight,aphi,aplo,cholesterol - 1,smoke,alco};
                            final double lowcholPct = predictCardio(data, input5);

                            if(cholesterol >= 2) {

                                Thread cholThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (cholStatus < lowcholPct - 1) {
                                            cholStatus += 1;
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cholBar.setProgress(cholStatus);
                                                    cholPct.setText(cholStatus + "%");
                                                }
                                            });
                                            try {
                                                Thread.sleep(20);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                                cholThread.start();

                            } else { // 1일때
                                cholPredictTxt.setVisibility(View.GONE);
                                cholBar.setVisibility(View.GONE);
                            }

                            //만약 흡연자라면 비흡연했을 경우
                            float[] input3 = new float[]{age,gender,height,weight,aphi,aplo,cholesterol, 0,alco};
                            final double nonSmokePct = predictCardio(data, input3);
                            if(smoke == 1) {

                                final Thread smokeThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (smokeStatus < nonSmokePct - 1) {
                                            smokeStatus += 1;
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    smokeBar.setProgress(smokeStatus);
                                                    smokePct.setText(smokeStatus + "%");
                                                }
                                            });
                                            try {
                                                Thread.sleep(20);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                                smokeThread.start();

                            } else {
                                smokePredictTxt.setVisibility(View.GONE);
                                smokeBar.setVisibility(View.GONE);
                            }

                            // 혈압이 높은 사람일 경우
                            float[] input4 = new float[]{age,gender,height,weight,aphi - 20 ,aplo,cholesterol,smoke, alco};
                            final double lowAphiPct = predictCardio(data, input4);
                            if(aphi > 140){

                                Thread aphiThread = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (aphiStatus < lowAphiPct - 1) {
                                            aphiStatus += 1;
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    aphiBar.setProgress(aphiStatus);
                                                    aphiPct.setText(aphiStatus + "%");
                                                }
                                            });
                                            try {
                                                Thread.sleep(20);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                                aphiThread.start();

                            } else {
                                aphiPredictTxt.setVisibility(View.GONE);
                                aphiBar.setVisibility(View.GONE);
                            }

                            new Thread((new Runnable() {
                                @Override
                                public void run() {
                                    while (true){
                                        try{
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Thread.State state = progressThread.getState();
                                                    Thread.State ageState = ageThread.getState();

                                                    if(state == Thread.State.TERMINATED) {
                                                        cardio_pst.setText(percentage + "%");

                                                        if(percentage < 20) {
                                                            comment.setText(R.string.Pct20);
                                                        } else if (percentage < 40) {
                                                            comment.setText(R.string.Pct40);
                                                        } else if (percentage < 70) {
                                                            comment.setText(R.string.Pct70);
                                                        } else {
                                                            comment.setText(R.string.Pct100);
                                                        }

                                                        if(cholesterol >= 2) {
                                                            //cholBar.setProgress((int) lowcholPct);
                                                            cholPct.setText(lowcholPct + "%");
                                                        }
                                                        if(smoke == 1) {
                                                            //smokeBar.setProgress((int)nonSmokePct);
                                                            smokePct.setText(nonSmokePct + "%");
                                                        }

                                                        if(aphi > 140) {
                                                            //aphiBar.setProgress((int)lowAphiPct);
                                                            aphiPct.setText(lowAphiPct + "%");
                                                        }
                                                    }

                                                    if(ageState == Thread.State.TERMINATED) {
                                                        agePct.setText(tenYearsPct + "%");
                                                    }

                                                }
                                            });
                                            Thread.sleep(100);
                                        } catch (Exception e) { e.printStackTrace(); } }
                                }
                            })).start();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
                        Log.e("Error","database error");
                    }
                });



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

    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private float[] readCSV(){

        float[][] data = new float[70000][13];

        try {

            InputStreamReader is = new InputStreamReader(getAssets().open("cardio_train.csv"));
            BufferedReader reader = new BufferedReader(is);

            String line = "";
            int row = 0;

            reader.readLine();
            while ((line= reader.readLine()) != null) {
                String[] token = line.split(";", -1);
                for (int i = 0; i < 13; i++)
                    data[row][i] = Float.parseFloat(token[i]);

                row++;
            }
            reader.close();

        }catch (FileNotFoundException e){
            e.printStackTrace();

        }catch(IOException e ){
            e.printStackTrace();
        }

        //min max구하기
        float maxAge = data[0][1];
        float minAge = maxAge;
        float maxHeight = data[0][3];
        float minHeight = maxHeight;
        float maxWeight = data[0][4];
        float minWeight = maxWeight;
        float maxAphi = data[0][5];
        float minAphi = maxAphi;
        float maxAplo = data[0][6];
        float minAplo = maxAplo;
        float maxChol = data[0][7];
        float minChol = maxChol;

        for(int i=0; i < 70000; i++){

            float currAge = data[i][1];
            float currHeight = data[i][3];
            float currWeight = data[i][4];
            float currAphi = data[i][5];
            float currAplo = data[i][6];
            float currChol = data[i][7];

            if( currAge > maxAge) maxAge = currAge;
            if(currAge < minAge) minAge = currAge;
            if(currHeight > maxHeight) maxHeight = currHeight;
            if(currHeight < minHeight) minHeight = currHeight;
            if(currWeight > maxWeight) maxWeight = currWeight;
            if(currWeight < minWeight) minWeight = currWeight;
            if(currAphi > maxAphi) maxAphi = currAphi;
            if(currAphi < minAphi) minAphi = currAphi;
            if(currAplo > maxAplo) maxAplo = currAplo;
            if(currAplo < minAplo) minAplo = currAplo;
            if(currChol > maxChol) maxChol = currChol;
            if(currChol < minChol) minChol = currChol;
        }

        float[] statisticData = new float[]{maxAge, minAge, maxHeight, minHeight, maxWeight, minWeight, maxAphi,
                minAphi, maxAplo, minAplo, maxChol, minChol};

        return statisticData;

    }

    private float[] scaling(@NotNull float[] data, @NotNull float[] input){

        float maxAge = data[0];
        float minAge = data[1];
        float maxHeight = data[2];
        float minHeight = data[3];
        float maxWeight = data[4];
        float minWeight = data[5];
        float maxAphi = data[6];
        float minAphi = data[7];
        float maxAplo = data[8];
        float minAplo = data[9];
        float maxChol = data[10];
        float minChol = data[11];

        input[0] = (input[0]-minAge)/(maxAge - minAge);
        input[2] = (input[2]-minHeight)/(maxHeight - minHeight);
        input[3] = (input[3]-minWeight)/(maxWeight - minWeight);
        input[4] = (input[4]-minAphi)/(maxAphi - minAphi);
        input[5] = (input[5]-minAplo)/(maxAplo - minAplo);
        input[6] = (input[6]-minChol)/(maxChol - minChol);

        return input;

    }

    public double predictCardio(float[]data, float[] input) {

        input = scaling(data, input);

        Interpreter tflite = getTfliteInterpreter("cardio_model.tflite");

        float[][] output = new float[1][1];
        tflite.run(input, output);

        float prediction = (output[0][0]) * 100;

        return Math.round(prediction*100)/100.0;

    }

    public long getAgeDay(String bYear, String bMonth, String bDay) {

        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH) + 1;
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        long age = 0;

        String today = "";
        if(mMonth < 10)
            today = Integer.toString(mYear) + "0" + String.valueOf(mMonth) + String.valueOf(mDay);
        else
            today = Integer.toString(mYear) + String.valueOf(mMonth) + String.valueOf(mDay);

        String birthday = bYear + bMonth + bDay;
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        try{
            Date todayDate = format.parse(today);
            Date birthDate = format.parse(birthday);

            age = (todayDate.getTime() - birthDate.getTime()) / (24*60*60*1000);

        }catch(Exception e){
            e.printStackTrace();
        }

        return age;
    }

    public int getChol(int cholesterol) {

        int chol;
        if(cholesterol <= 200) {
            chol = 1;
        } else if(cholesterol < 240) {
            chol = 2;
        } else {
            chol = 3;
        }

        return chol;
    }
}

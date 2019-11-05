package com.jieun.cardiocare;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import shortbread.Shortcut;

//@Shortcut(id = "BPM", icon = R.drawable.ic_noun_heart, shortLabelRes = R.string.label_bpm, rank = 2)

public class HeartActivity extends AppCompatActivity {

    //firebase
    public static DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser fuser;


    private String TAG = MainActivity.class.getName();
    private GoogleApiClient googleApiClient;
    private boolean authInProgress = false;
    private OnDataPointListener onDataPointListener;
    private static final int AUTH_REQUEST = 1;

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.BODY_SENSORS
    };

    private static final int REQUEST_PERMISSION_CODE = 12345;
    private List<String> missingPermission = new ArrayList<>();

    private boolean bCheckStarted = false;
    private boolean bGoogleConnected = false;

    private LinearLayout beatLayout;
    private Button btnStart;
    private ProgressBar spinner;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private TextView textMon;
    private ImageView beatanim;

    private Drawable beatdraw;
    private TextView bpmText;
    private SeekBar bpmseekBar;

    private TextView text_seekbar;


    private ImageView finger;

    //icon
    private TextView iconText;
    private RadioGroup icons;

    //end layout
    private LinearLayout endLayout;
    private LinearLayout btnLayout2;
    private Button againBtn;
    private Button storeBtn;

    private static HeartUser user;
    private static int status =-1;


    @SuppressWarnings("Convert2Lambda")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart);

        //심박패턴을 측정하는 동안 화면이 꺼지지 않도록 제어하기 위해 전원관리자를 얻어옵니다
        powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WAKELOCK");
        wakeLock.acquire(5000);

        initFireBase();
        initUI();
        initPrograssBar();
        initIcons();
        //필요한 권한을 얻었는지 확인하고, 얻지 않았다면 권한 요청을 하기 위한 코드를 호출합니다
        checkAndRequestPermissions();
    }
    private void initFireBase(){
        mAuth = FirebaseAuth.getInstance();
        fuser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void initUI() {
        //심박수를 측정하는 Google API의 호출을 위해 API 클라이언트를 초기화 합니다
        initGoogleApiClient();

        endLayout =(LinearLayout)findViewById(R.id.end_layout);
        btnLayout2 = (LinearLayout)findViewById(R.id.btnLayout2);
        //beat anim
        beatanim =(ImageView)findViewById(R.id.beatanim);
        beatdraw =beatanim.getDrawable();
        bpmText =(TextView)findViewById(R.id.bpmText);
        beatLayout =(LinearLayout)findViewById(R.id.beatLayout);

        againBtn=(Button)findViewById(R.id.againBtn);
        storeBtn=(Button)findViewById(R.id.storeBtn);
        finger =(ImageView)findViewById(R.id.fingerImage);

        textMon = findViewById(R.id.textMon);
        btnStart = findViewById(R.id.btnStart);
        btnStart.setText("Wait please ...");
        btnStart.setEnabled(false);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Measure(btnStart);
            }
        });
    }
    private void Measure(Button btn){

        if (bCheckStarted) {
            endLayout.setVisibility(View.GONE);
            btn.setText("Start");
            bCheckStarted = false;
            finger.setVisibility(View.VISIBLE);
            beatLayout.setVisibility(View.GONE);
            unregisterFitnessDataListener();
            wakeLock.release();
        }
        else {

            if (bGoogleConnected == true) {
                //심박수를 측정하기 위한 API를 설정합니다
                findDataSources();
                //심박수의 측정이 시작되면 심박수 정보를 얻을 콜백함수를 등록/설정하는 함수를 호출합니다
                registerDataSourceListener(DataType.TYPE_HEART_RATE_BPM);

                if ( beatdraw instanceof AnimatedVectorDrawable) {
                    AnimatedVectorDrawable avd = (AnimatedVectorDrawable) beatdraw;
                    avd.start();
                } else if ( beatdraw instanceof AnimatedVectorDrawableCompat) {
                    AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) beatdraw;
                    avd.start();
                }
                finger.setVisibility(View.GONE);
                beatLayout.setVisibility(View.VISIBLE);
                btn.setText("Waiting");
                bCheckStarted = true;
                //화면이 꺼지지 않도록 설정합니다
                wakeLock.acquire();

            }
            // Google API 클라이언트에 로그인이 되어 있지 않다면,
            else {
                //Google API 클라이언트에 로그인 합니다
                if (HeartActivity.this.googleApiClient != null)
                    HeartActivity.this.googleApiClient.connect();
            }
        }
    }
    private void initIcons(){
        iconText =(TextView)findViewById(R.id.statusText);
        icons =(RadioGroup)findViewById(R.id.statusIcons);
        icons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton)radioGroup.findViewById(i);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked)
                {
                    // Changes the textview's text to "Checked: example radiobutton text"
                    if(i ==R.id.stableBtn){ status =0;}
                    if(i ==R.id.exciteBtn){ status =1;}
                    if(i ==R.id.runningBtn){ status =2;}
                    if(i ==R.id.depressBtn){ status =3;}
                    if(i ==R.id.sleepBtn){ status =4;}
                    iconText.setText("Check : " + checkedRadioButton.getText());

                }
            }
        });

    }
    private void initPrograssBar(){
        bpmseekBar =(SeekBar)findViewById(R.id.bpmseekbar);
        text_seekbar =(TextView)findViewById(R.id.statusText);
/*        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                text_seekbar.setText("" + progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/



    }

    private void initGoogleApiClient() {
        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                //.addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            //Google API 클라이언트의 로그인에 성공하면 호출이 되는 콜백입니다
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.d(TAG, "initGoogleApiClient() onConnected good...");
                                bGoogleConnected = true;
                                btnStart.setText("Start");
                                btnStart.setEnabled(true);
                            }

                            @Override
                            public void onConnectionSuspended(int i) {

                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.d(TAG, "onConnectionSuspended() network_lost bad...");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.d(TAG, "onConnectionSuspended() service_disconnected bad...");

                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {

                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.d(TAG, "Connection failed. Cause: " + result.toString());

                                if (!result.hasResolution()) {
                                    HeartActivity.this.finish();
                                    return;
                                }

                                if (!authInProgress) {
                                    try {
                                        Log.d(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(HeartActivity.this,
                                                AUTH_REQUEST);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                        HeartActivity.this.finish();
                                    }
                                }
                                else {
                                    HeartActivity.this.finish();
                                }
                            }
                        }
                )
                .build();
    }

    /**
     * Checks if there is any missing permissions, and
     * requests runtime permission if needed.
     */
    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            if (HeartActivity.this.googleApiClient != null)
                HeartActivity.this.googleApiClient.connect();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        } else {
            if (HeartActivity.this.googleApiClient != null)
                HeartActivity.this.googleApiClient.connect();
        }

    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            initGoogleApiClient();
            if (HeartActivity.this.googleApiClient != null)
                HeartActivity.this.googleApiClient.connect();
        } else {
            Toast.makeText(getApplicationContext(), "Failed get permissions", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void findDataSources() {
        Fitness.SensorsApi.findDataSources(googleApiClient, new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_HEART_RATE_BPM)
                // .setDataTypes(DataType.TYPE_SPEED)
                // .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {

                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {

                            if (dataSource.getDataType().equals(DataType.TYPE_HEART_RATE_BPM)
                                    && onDataPointListener == null) {
                                registerDataSourceListener(DataType.TYPE_HEART_RATE_BPM);

                            }
                        }
                    }
                });

    }


    private void registerDataSourceListener(DataType dataType) {
        onDataPointListener = new OnDataPointListener() {
            // 심박수가 측정되면 심박수를 얻어올 수 있는 콜백입니다
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value aValue = dataPoint.getValue(field);
                    //Log.d(TAG, "Detected DataPoint field: " + field.getName());
                    //Log.d(TAG, "Detected DataPoint value: " + aValue);

                    //addContentToView("dataPoint=" + field.getName() + " " + aValue + "\n");
                    addContentToView(aValue.asFloat());
                }
            }
        };

        Fitness.SensorsApi.add(
                googleApiClient,
                new SensorRequest.Builder()
                        .setDataType(dataType)
                        .setSamplingRate(2, TimeUnit.SECONDS)
                        .setAccuracyMode(SensorRequest.ACCURACY_MODE_DEFAULT)
                        .build(),
                onDataPointListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "onDataPointListener  registered good");
                        } else {
                            Log.d(TAG, "onDataPointListener failed to register bad");
                        }
                    }
                });

    }

    private void unregisterFitnessDataListener() {
        if (this.onDataPointListener == null) {
            return;
        }

        if (this.googleApiClient == null) {
            return;
        }

        if (this.googleApiClient.isConnected() == false) {
            return;
        }

        Fitness.SensorsApi.remove(
                this.googleApiClient,
                this.onDataPointListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.d(TAG, "Listener was removed!");
                            Toast.makeText(getApplicationContext(),"Listener was removed",Toast.LENGTH_LONG).show();

                            if (beatdraw instanceof AnimatedVectorDrawable) {
                                AnimatedVectorDrawable avd = (AnimatedVectorDrawable) beatdraw;
                                avd.stop();
                            } else if (beatdraw instanceof AnimatedVectorDrawableCompat) {
                                AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) beatdraw;
                                avd.stop();
                            }

                        } else {
                            Toast.makeText(getApplicationContext(),"not removed",Toast.LENGTH_LONG).show();
                            Log.d(TAG, "Listener was not removed.");
                        }
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(getApplicationContext(),"onStart connect attempted",Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart connect attempted");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterFitnessDataListener();

        if (this.googleApiClient != null && this.googleApiClient.isConnected()) {
            this.googleApiClient.disconnect();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTH_REQUEST) {
            authInProgress = false;

            if (resultCode == RESULT_OK) {

                if (!this.googleApiClient.isConnecting() && !this.googleApiClient.isConnected()) {
                    this.googleApiClient.connect();
                    Log.d(TAG, "onActivityResult googleApiClient.connect() attempted in background");

                }
            }
        }
    }

    public String getDateStr(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd");
        return sdfNow.format(date);
    }

    public String getTimeStr(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH:mm");
        return sdfNow.format(date);
    }


    private synchronized void addContentToView(final float value) {

        endLayout.setVisibility(View.VISIBLE);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (beatdraw instanceof AnimatedVectorDrawable) {
                    AnimatedVectorDrawable avd = (AnimatedVectorDrawable) beatdraw;
                    avd.stop();
                } else if (beatdraw instanceof AnimatedVectorDrawableCompat) {
                    AnimatedVectorDrawableCompat avd = (AnimatedVectorDrawableCompat) beatdraw;
                    avd.stop();
                }
                SimpleDateFormat Sformat = new SimpleDateFormat ( "yyyy-MM-dd HH:mm");
                String format_time1 = Sformat.format (System.currentTimeMillis());
                textMon.setText(format_time1);
                bpmText.setText("BPM is " + value);
                bpmseekBar.setProgress((int)value);

                btnStart.setVisibility(View.GONE);
                btnLayout2.setVisibility(View.VISIBLE);
                user =new HeartUser();
                user.setDate(getDateStr()+"" +getTimeStr());
                user.setBpm(value);
                user.setStatus(status);

                againBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        textMon.setText("Please Finger over the camera sensor");
                        Measure(againBtn);
                    }
                });

                storeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(status<0){
                            storeBtn.setText("please check status");
                        }else{
                            String id = fuser.getUid();
                            mDatabase.child("users").child(id).child("Bpm").child(getDateStr()).child(getTimeStr()).setValue(user);
                            Toast.makeText(HeartActivity.this, "BPM 입력 완료", Toast.LENGTH_SHORT).show();

                            Intent intent =new Intent(getApplicationContext(),HeartGraphActivity.class);
                            intent.putExtra("user",user);
                            startActivity(intent);

                        }



                    }
                });
            }
        });
    }
}
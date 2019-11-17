package com.jieun.cardiocare;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import shortbread.Shortbread;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 구글로그인 result 상수
    private static final int RC_SIGN_IN = 900;

    // 구글api클라이언트
    private GoogleSignInClient mGoogleSignInClient;

    // Firebase 인증 객체 생성
    private FirebaseAuth mAuth;

    // Firebase DB
    public static DatabaseReference mDatabase;

    // 구글  로그인 버튼

    private SignInButton buttonGoogle;

    public static GoogleApiClient mGoogleApiClient;
    public static GoogleSignInOptions googleSignInOptions;

    //googleapi
    private boolean authInProgress = false;
    private static final int AUTH_REQUEST = 1;
    private static final int PERMISSION_CODE = 12345;
    //private List<String> missingPermission = new ArrayList<>();

    private String TAG = MainActivity.class.getName();

    public boolean bGoogleConnected = false;

    //gps
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private String[] REQUIRED_PERMISSIONS  = {Manifest.permission.BODY_SENSORS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Shortbread.create(this);


        checkRunTimePermission();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        buttonGoogle = findViewById(R.id.btn_googleSignIn);
        buttonGoogle.setSize(SignInButton.SIZE_WIDE);

        // Google 로그인을 앱에 통합
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        initGoogleApiClient();
        checkAndRequestPermissions();

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // 구글 로그인 성공
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    Log.w("tag", "Google sign in failed", e);
                }
                break;

            case AUTH_REQUEST:
                authInProgress = false;

                if (resultCode == RESULT_OK) {

                    if (!this.mGoogleApiClient.isConnecting() && !this.mGoogleApiClient.isConnected()) {
                        this.mGoogleApiClient.connect();
                        Log.d(TAG, "onActivityResult googleApiClient.connect() attempted in background");
                    }
                }
                break;

            case GPS_ENABLE_REQUEST_CODE:
                checkRunTimePermission();
                break;
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // 로그인 성공
                            FirebaseUser user = mAuth.getCurrentUser();
     
                            Log.i("username",user.getDisplayName());
                            Log.i("userid",user.getUid());

                            final String userId = user.getUid();

                            mDatabase.child("BodyInfo").child(userId).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            UserData user = dataSnapshot.getValue(UserData.class);
                                            Intent intent;
                                            if(user == null){ // 가입되어 있지 않다면
                                                intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                                            }
                                            else{ // 가입되어 있으면 -> 최종적으로는 홈화면
                                                intent = new Intent(getApplicationContext(), HomeActivity.class);
                                            }
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            // Getting Post failed, log a message
                                        }
                                    });
                            initToast("로그인 되었습니다");
                            //Toast.makeText(getApplicationContext(), R.string.success_login , Toast.LENGTH_SHORT).show();
                        } else { // 로그인 실패
                            initToast("로그인 실패");
                            //Toast.makeText(MainActivity.this, R.string.failed_login, Toast.LENGTH_SHORT).show();
                            Log.v("login","failed");
                        }

                    }
                });
    }

    private void initToast(String msg){

        LayoutInflater inflater = getLayoutInflater();
        View toastDesign = inflater.inflate(R.layout.toast_design2, (ViewGroup)findViewById(R.id.toast_design_root2)); //toast_design.xml 파일의 toast_design_root 속성을 로드

        TextView text = toastDesign.findViewById(R.id.TextView_toast_design);
        text.setText(msg);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 800); // CENTER를 기준으로 0, 0 위치에 메시지 출력
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastDesign);
        toast.show();

    }
    private void initGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            //Google API 클라이언트의 로그인에 성공하면 호출이 되는 콜백입니다
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.d(TAG, "initGoogleApiClient() onConnected good...");
                                bGoogleConnected = true;
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
                                    MainActivity.this.finish();
                                    return;
                                }

                                if (!authInProgress) {
                                    try {
                                        Log.d(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(MainActivity.this,
                                                AUTH_REQUEST);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                        MainActivity.this.finish();
                                    }
                                }
                                else {
                                    MainActivity.this.finish();
                                }
                            }
                        }
                )
                .build();

    }

    private void checkAndRequestPermissions() {
        // Check for permissions
        /*
        for (String eachPermission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            if (this.mGoogleApiClient != null)
                this.mGoogleApiClient.connect();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        } else {
            if (this.mGoogleApiClient != null)
                this.mGoogleApiClient.connect();
        }

         */
        if (this.mGoogleApiClient != null)
            this.mGoogleApiClient.connect();

    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            /*
            case REQUEST_PERMISSION_CODE:

                for (int i = grantResults.length - 1; i >= 0; i--) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        missingPermission.remove(permissions[i]);
                    }
                }
                if (missingPermission.isEmpty()) {
                    initGoogleApiClient();
                    if (this.mGoogleApiClient != null)
                        this.mGoogleApiClient.connect();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed get permissions", Toast.LENGTH_LONG).show();
                    Log.i("missingpermission", missingPermission.get(0) + "");
                    //finish();
                }

                break;
             */
            case PERMISSION_CODE:
                if(grantResults.length == REQUIRED_PERMISSIONS.length) {
                    // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
                    boolean check_result = true;

                    // 모든 퍼미션을 허용했는지 체크합니다.
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            check_result = false;
                            break;
                        }
                    }

                    if( !check_result) {
                        if ( ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2])) {

                            Toast.makeText(MainActivity.this, R.string.permission_failed1, Toast.LENGTH_LONG).show();
                            finish();
                        }else {
                            Toast.makeText(MainActivity.this, R.string.permission_failed2, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;
        }
    }

    public void checkRunTimePermission(){

        int hasBodySensorPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.BODY_SENSORS);

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if(hasBodySensorPermission != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this,"권한 승인이 필요합니다",Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BODY_SENSORS)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSION_CODE);
            }
        } else if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[1])) {
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSION_CODE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSION_CODE);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
    }

}

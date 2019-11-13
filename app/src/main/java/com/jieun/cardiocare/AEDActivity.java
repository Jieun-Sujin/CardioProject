package com.jieun.cardiocare;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import shortbread.Shortcut;

@Shortcut(id = "AED", icon = R.drawable.ic_noun_map, shortLabelRes = R.string.label_aed, rank = 4)
public class AEDActivity extends AppCompatActivity
        implements OnMapReadyCallback {


    private Button showLocationButton,popBtn;
    private GoogleMap mMap;
    private GpsTracker gpsTracker;
    private static MarkerOptions markerOptions;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    //private static final int PERMISSIONS_REQUEST_CODE = 100;
    //String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    static double latitude,longitude;
    static String address;
    LinearLayout location_layout;

    //listView
    ListView listView;
    ArrayList<String> buildAddress,buildPlace,clerkTel,distance,wgs84Lat,wgs84Lon;
    AEDViewAdaptor mMyAdapter;

    //select listView
    TextView buildAddressText,buildPlaceText,clerkTelText,distanceText;

    //map
    static SupportMapFragment mapFragment;
    LinearLayout end_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aed);


        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }
        /*
        else {
            checkRunTimePermission();
        }
        */

        init();
        initScreen();

        showLocationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0) {
                LayoutInflater inflater = getLayoutInflater();
                View toastDesign = inflater.inflate(R.layout.toast_design, (ViewGroup)findViewById(R.id.toast_design_root)); //toast_design.xml 파일의 toast_design_root 속성을 로드

                TextView text = toastDesign.findViewById(R.id.TextView_toast_design);
                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.CENTER, 0, 800); // CENTER를 기준으로 0, 0 위치에 메시지 출력
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(toastDesign);
                toast.show();


                initScreen();
            }
        });
        popBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AEDPopupActivity.class);
                intent.putExtra("data", "Test Popup");
                startActivityForResult(intent, 1);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long id) {
                final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
                final int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());

                LinearLayout.LayoutParams pm
                        = new LinearLayout.LayoutParams(width,height); //레이아웃파라미터 생성pm.gravity = Gravity.LEFT; //버튼의 Gravity를 지정
                showLocationButton.setLayoutParams(pm);
                showLocationButton.setBackground(getDrawable(R.drawable.navigator_left));

                listView.setVisibility(View.GONE);
                end_layout.setVisibility(View.VISIBLE);
                AEDItem selected_aed = (AEDItem)mMyAdapter.getItem(position);
                buildAddressText.setText(selected_aed.getBuildAddress());
                buildPlaceText.setText(selected_aed.getBuildPlace());
                clerkTelText.setText(selected_aed.getClerkTel());
                distanceText.setText(selected_aed.getDistance());

                longitude =Double.parseDouble(selected_aed.getWgs84Lon());
                latitude =Double.parseDouble(selected_aed.getWgs84Lat());
                mapFragment.getMapAsync(AEDActivity.this);
            }
        });
    }

    private void init(){


        /* toolbar.setTitleMargin(0,0,10,0);*/
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //toolbar.setNavigationIcon(R.drawable.navigator_left);



        showLocationButton = (Button) findViewById(R.id.button);
        popBtn =(Button)findViewById(R.id.popBtn);
        location_layout =(LinearLayout)findViewById(R.id.location_layout);
        listView =(ListView)findViewById(R.id.listView);
        end_layout =(LinearLayout)findViewById(R.id.end_layout);

        buildAddressText =(TextView)findViewById(R.id.buildAddress);
        buildPlaceText =(TextView)findViewById(R.id.buildPlace);
        clerkTelText =(TextView)findViewById(R.id.clerkTel);
        distanceText =(TextView)findViewById(R.id.distance);


    }

    private void initScreen(){

        final int top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
        final int right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
        final int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());



        LinearLayout.LayoutParams pm
                = new LinearLayout.LayoutParams(width,height); //레이아웃파라미터 생성
        pm.gravity = Gravity.RIGHT; //버튼의 Gravity를 지정
        pm.setMargins(0,top,right,0);
        showLocationButton.setLayoutParams(pm);
        showLocationButton.setBackground(getDrawable(R.drawable.ic_compass));
        showLocationButton.setText("");


        end_layout.setVisibility(View.GONE);
        location_layout.setVisibility(View.VISIBLE);
        listView.setVisibility(View.VISIBLE);
        gpsTracker = new GpsTracker(AEDActivity.this);

        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();

        address = getCurrentAddress(latitude, longitude);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(AEDActivity.this);

        new Thread(new Runnable() {

            @Override

            public void run() {
                getData(); // 하단의 getData 메소드를 통해 데이터를 파싱
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        listViewDataAdd();
                    }

                });
            }

        }).start();

    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        markerOptions = new MarkerOptions();
        mMap = googleMap;
        LatLng loc = new LatLng(latitude, longitude);
        markerOptions.position(loc);
        /*        markerOptions.title(address);*/
        mMap.clear();
        mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    /*
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                //위치 값을 가져올 수 있음
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(AEDActivity.this, R.string.permission_failed1, Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(AEDActivity.this, R.string.permission_failed2, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

     */

    /*
    public void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(AEDActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(AEDActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(AEDActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(AEDActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(AEDActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(AEDActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }
     */

    public String getCurrentAddress( double latitude, double longitude) {

        //GPS 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오스터 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(AEDActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 서비스를 활성화하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                    Intent callGPSSettingIntent
                            = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);

            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        //checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    */
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }




    public void getData(){

        String key ="@string/aed_api_key";

        String WGS84_LON =  Double.toString(longitude);
        String WGS84_LAT =  Double.toString(latitude);
        //String WGS84_LON =  Double.toString(127.1643387);
        //String WGS84_LAT =  Double.toString(37.6074135);

        String queryUrl="http://apis.data.go.kr/B552657/AEDInfoInqireService/getAedLcinfoInqire?"
                +"WGS84_LON="+WGS84_LON
                +"&WGS84_LAT="+WGS84_LAT
                +"&numOfRows=10"
                +"&serviceKey="+ "o6Ea0CAH7jCtTwXGrSM8WWWyEGtMDjmxQJBi7pUh%2Fn5Ec4tEPWIKzx%2Fe8YuOKCb2jwn5CqwAcxrqVD6rNRsFwg%3D%3D";

        //String queryUrl
        //       ="http://apis.data.go.kr/B552657/AEDInfoInqireService/getAedLcinfoInqire?WGS84_LON=127.0467336&WGS84_LAT=37.2852528&numOfRows=10&serviceKey=o6Ea0CAH7jCtTwXGrSM8WWWyEGtMDjmxQJBi7pUh%2Fn5Ec4tEPWIKzx%2Fe8YuOKCb2jwn5CqwAcxrqVD6rNRsFwg%3D%3D";

        try {

            URL url= new URL(queryUrl); // 문자열로 된 요청 url을 URL 객체로 생성.

            InputStream is= url.openStream(); // url 위치로 인풋스트림 연결


            XmlPullParserFactory factory= XmlPullParserFactory.newInstance();

            XmlPullParser xpp= factory.newPullParser();

            // inputstream 으로부터 xml 입력받기

            xpp.setInput( new InputStreamReader(is, "UTF-8") );

            String tag = "";

            xpp.next();

            int eventType= xpp.getEventType();


            buildAddress = new ArrayList<>();
            buildPlace = new ArrayList<>();
            clerkTel = new ArrayList<>();
            distance = new ArrayList<>();
            wgs84Lat =new ArrayList<>();
            wgs84Lon =new ArrayList<>();


            while( eventType != XmlPullParser.END_DOCUMENT ){
                Log.i("eventType",eventType + "");
                switch( eventType ){

                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:

                        tag= xpp.getName(); // 태그 이름 얻어오기

                        Log.i("tag", tag);

                        if(tag.equals("item")) ;

                        else if(tag.equals("buildPlace")){
                            xpp.next();
                            buildPlace.add(xpp.getText());
                            Log.i("buildPlace", xpp.getText());
                        }

                        else if(tag.equals("clerkTel")){
                            xpp.next();
                            clerkTel.add(xpp.getText());
                            Log.i("clerkTel", xpp.getText());

                        }
                        else if(tag.equals("buildAddress")){
                            xpp.next();
                            buildAddress.add(xpp.getText());
                            Log.i("buildAddress", xpp.getText());

                        }
                        else if(tag.equals("distance")){
                            xpp.next();
                            distance.add(xpp.getText());
                            Log.i("distance", xpp.getText());
                        }
                        else if(tag.equals("wgs84Lat")){
                            xpp.next();
                            wgs84Lat.add(xpp.getText());
                            Log.i("wgs84Lat", xpp.getText());
                        }
                        else if(tag.equals("wgs84Lon")){
                            xpp.next();
                            wgs84Lon.add(xpp.getText());
                            Log.i("wgs84Lon", xpp.getText());
                        }
                        break;

                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:

                        tag = xpp.getName(); // 태그 이름 얻어오기

                        break;
                }

                eventType= xpp.next();

            }

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    public void listViewDataAdd() {
        mMyAdapter = new AEDViewAdaptor();
        for (int i = 0; i < 10; i++) {
            mMyAdapter.addItem(buildAddress.get(i),
                    buildPlace.get(i),
                    clerkTel.get(i),
                    distance.get(i),
                    wgs84Lat.get(i),
                    wgs84Lon.get(i));
        }

        listView.setAdapter(mMyAdapter);

    }

}
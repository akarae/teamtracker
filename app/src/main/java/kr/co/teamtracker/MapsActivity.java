package kr.co.teamtracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import kr.co.teamtracker.gcm.QuickstartPreferences;
import kr.co.teamtracker.httpclient.GCMHttpClient;
import kr.co.teamtracker.utils.ReportingDTO;
import kr.co.teamtracker.utils.ReportingService;
import kr.co.teamtracker.utils.SQLiteHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;

    SQLiteHelper sqlHelper;

    MyReceiver myReceiver;

    LinearLayout llStatus;

    TextView mtvTitle;

    List<Marker> markerList = new ArrayList<Marker>();
    List<Polyline> polylineList = new ArrayList<Polyline>();

    private boolean isInitialPostion = true;

    String mTeamId;

    int markerPosition = 0;

    Button mBtnGoal;
    Button mBtnSetupGoal;
    Button mBtnToggleShow;
    Button mBtnSendMsg;

    EditText mEtMsg;

    boolean mIsShow = true;

    private double mGoallat;
    private double mGoallang;

    private double mGoallatOrg;
    private double mGoallangOrg;

    private Circle mCircleBig;
    private Circle mCircleSmall;

    private boolean mIsGoalIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        Bundle bundle = (Bundle) intent.getExtras();
        mTeamId = (String) bundle.get("teamid");

        mtvTitle = (TextView) findViewById(R.id.tv_map_title);

        llStatus = (LinearLayout) findViewById(R.id.ll_status);

        mBtnGoal = (Button) findViewById(R.id.btn_map_goal);
        mBtnGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mIsShow) {
                    try {
                        if (mGoallang != 0.0) {
                            LatLng latLng = new LatLng(mGoallat, mGoallang);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mBtnSetupGoal = (Button) findViewById(R.id.btn_map_setup);
        mBtnSetupGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mIsShow) {
                    try {
                        Intent intent = new Intent(MapsActivity.this, SetupActivity.class);
                        intent.putExtra("teamid", mTeamId);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mEtMsg = (EditText) findViewById(R.id.et_map_msg);

        mBtnSendMsg = (Button) findViewById(R.id.btn_map_sendmsg);
        mBtnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = mEtMsg.getText().toString();

                if (msg != null && msg.length() > 0) {

                    SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);

                    ReportingDTO reportingDTO = sqlHelper.getReporting(gMemberInfo.getString("uuid", null));

                    if (reportingDTO != null && reportingDTO.getLat() != null && reportingDTO.getLang() != null) {

                        // request parameter 설정
                        RequestParams params = new RequestParams();

//                    params.add("uuid",     gMemberInfo.getString("uuid", null));
//                    params.add("tokenid",  gMemberInfo.getString("tokenid", null));
//                    params.add("callsign", gMemberInfo.getString("callsign", null));
//                    params.add("status",   gMemberInfo.getString("status", null));
//                    params.add("color",    gMemberInfo.getString("color", null));
//                    params.add("msg",      msg);

                        SharedPreferences.Editor editor = gMemberInfo.edit();
                        editor.putString("msg", msg);
                        editor.commit();

                        params.add("uuid", gMemberInfo.getString("uuid", null));
//                        params.add("lat", reportingDTO.getLat().toString());
//                        params.add("lang", reportingDTO.getLang().toString());
//                        params.add("speed", reportingDTO.getSpeed().toString());
//                        params.add("direction", reportingDTO.getDirection());
//                        params.add("reporttime", reportingDTO.getReporttime());
                        params.add("msg", msg);

                        GCMHttpClient.get("/gcm", params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                                mEtMsg.setText("");
                                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputManager.hideSoftInputFromWindow(mEtMsg.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                                String responseMsg = new String(responseBody);

                                Log.i(TAG, "statusCode is : " + statusCode);
                                Log.i(TAG, "responseBody is : " + responseMsg);

                                if (responseMsg.equals(QuickstartPreferences.MEMBERS_SAVE_INSERT_SUCCESS)
                                        || responseMsg.equals(QuickstartPreferences.MEMBERS_SAVE_UPDATE_SUCCESS)) {

                                } else {

                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                String errorMsg = error.getMessage();
                                Throwable errorCause = error.getCause();
                                StackTraceElement stackTraceElement[] = error.getStackTrace();

                                Log.i(TAG, "errorMsg is : " + errorMsg);
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "There is no GPS reporting!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mBtnToggleShow = (Button) findViewById(R.id.btn_map_toggle);
        mBtnToggleShow.setText("<<");
        mBtnToggleShow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                setToggleShow();
            }
        });

        // SQLite test
        sqlHelper = new SQLiteHelper(MapsActivity.this, null, SQLiteHelper.dbVersion);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(false);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

                                                    @Override
                                                    public boolean onMyLocationButtonClick() {

                                                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                                        Criteria criteria = new Criteria();
                                                        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

                                                        return false;
                                                    }
                                                }
        );

        // 정보창 Customizing
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Context mContext = getApplicationContext();
                LinearLayout info = new LinearLayout(mContext);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(mContext);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(mContext);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

        updatePosition();

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub

        //Register BroadcastReceiver
        //to receive event from our service
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(QuickstartPreferences.REPORT_NOTIFICATION);
        registerReceiver(myReceiver, intentFilter);

        super.onStart();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (markerList != null && markerList.size() > 0) {
            updatePosition();
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        unregisterReceiver(myReceiver);

        sqlHelper.close();

        super.onStop();
    }

    // 브로드캐스트 리시버
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

//            String reportResult = arg1.getStringExtra("reportResult");
//
//            if (!reportResult.equals("success")) {
//                Toast.makeText(MapsActivity.this, reportResult, Toast.LENGTH_LONG).show();
//            }

            // 서버 전송여부와 무관하게 화면 UI는 갱신처리
            updatePosition();
        }
    }

    public void updatePosition() {

        if (markerList != null && markerList.size() > 0) {
            // 삭제 후 재설정
            for (int i = 0; i < markerList.size(); i++) {
                markerList.get(i).remove();
            }

            markerList.clear();
        }

        if (polylineList != null && polylineList.size() >0) {
            // 삭제 후 재설정
            for (int i = 0; i < polylineList.size(); i++) {
                polylineList.get(i).remove();
            }

            polylineList.clear();
        }

        // this is my sweet hometown!!
        Double dLat = 35.8041311;
        Double dLang = 128.6239454;
        LatLng positionTop = new LatLng(dLat, dLang);

        List<ReportingDTO> listDto = new ArrayList<ReportingDTO>();
        listDto = sqlHelper.getReportingByTeamid(mTeamId);

        String sTeamStatusView = new String();

        llStatus.removeAllViews();

        int imgId       = R.drawable.c_169fed;
        int textColorId = R.color.m_169fed;

        for (int i = 0; i < listDto.size(); i++) {

            ReportingDTO retDTO = listDto.get(i);

            mGoallat  = retDTO.getGoallat();
            mGoallang = retDTO.getGoallang();

            // icon / text color setting start
            if (retDTO.getColor().equals("06ff00") ) {
                imgId       = R.drawable.c_06ff00;
                textColorId = R.color.m_06ff00;
            }
            if (retDTO.getColor().equals("43bd00") ) {
                imgId       = R.drawable.c_43bd00;
                textColorId = R.color.m_43bd00;
            }
            if (retDTO.getColor().equals("169fed") ) {
                imgId       = R.drawable.c_169fed;
                textColorId = R.color.m_169fed;
            }
            if (retDTO.getColor().equals("1919ab") ) {
                imgId       = R.drawable.c_1919ab;
                textColorId = R.color.m_1919ab;
            }
            if (retDTO.getColor().equals("c720c9") ) {
                imgId       = R.drawable.c_c720c9;
                textColorId = R.color.m_c720c9;
            }
            if (retDTO.getColor().equals("ecd900") ) {
                imgId       = R.drawable.c_ecd900;
                textColorId = R.color.m_ecd900;
            }
            if (retDTO.getColor().equals("fc00ff") ) {
                imgId       = R.drawable.c_fc00ff;
                textColorId = R.color.m_fc00ff;
            }
            if (retDTO.getColor().equals("ff0000") ) {
                imgId       = R.drawable.c_ff0000;
                textColorId = R.color.m_ff0000;
            }
            if (retDTO.getColor().equals("ff9c00") ) {
                imgId       = R.drawable.c_ff9c00;
                textColorId = R.color.m_ff9c00;
            }
            // icon / text color setting end


            // location setting
            LatLng position = new LatLng(retDTO.getLat(), retDTO.getLang());

            View marker_root_view = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);



            TextView tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
            tv_marker.setText(retDTO.getCallsign());
            tv_marker.setShadowLayer(3, 1, 1, Color.argb(255, 255, 255, 255));

            TextView tv_marker_info = (TextView) marker_root_view.findViewById(R.id.tv_marker_info);
            tv_marker_info.setText(retDTO.getDirection() + "/" + retDTO.getSpeed());
            tv_marker_info.setShadowLayer(3, 1, 1, Color.argb(255, 255, 255, 255));

            TextView tv_marker_msg = (TextView) marker_root_view.findViewById(R.id.tv_marker_msg);
            String sMsg = retDTO.getMsg();
            if (sMsg == null || sMsg.isEmpty() || sMsg.length() < 1 || sMsg.equals("null")) {
                sMsg = "...";
            }
            tv_marker_msg.setText(sMsg);

            TextView tv_marker_img = (TextView) marker_root_view.findViewById(R.id.tv_marker_img);
            tv_marker_img.setBackgroundResource(imgId);

            Bitmap newBitmap = createDrawableFromView(this, marker_root_view);

            // 거리 계산
            double distance = 0;
            if (mGoallang != 0.0) {

                LatLng goalPosition = new LatLng(mGoallat, mGoallang);
                distance = CalculationByDistance(position, goalPosition);

                // 목표지점으로부터 특정거리 이내로 접근 시 서비스 종료처리
                SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
                String callsign = gMemberInfo.getString("callsign", null);
                if (retDTO.getCallsign().equals(callsign) && distance <= QuickstartPreferences.GOAL_END_DISTANCE) {
                    mIsGoalIn = true;
                } else {
                    mIsGoalIn = false;
                }

                // guideline 제공
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.LTGRAY);
                polylineOptions.width(2);
                polylineOptions.add(new LatLng(mGoallat, mGoallang));
                polylineOptions.add(position);

                Polyline polyline = mMap.addPolyline(polylineOptions);
                polylineList.add(polyline);

            }

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.fromBitmap(newBitmap))
                    .title(retDTO.getCallsign() + "'s reporting")
                    .snippet("report time : " + retDTO.getReporttime() + "\n"
                            + "direction : " + retDTO.getDirection() + "deg" + "\n"
                            + "speed : " + retDTO.getSpeed() + "km/h" + "\n"
                            + "status : " + retDTO.getStatus() + "\n"
                            + "distance : " + distance + "km" + "\n"
                            + "msg : " + retDTO.getMsg())
                    .anchor(0.155f, 0.25f));

            // 위치검증용
            //Marker marker2 = mMap.addMarker(new MarkerOptions().position(position));

            markerList.add(marker);


            if (i == 0) {
                positionTop = position;
            }

            if (retDTO.getReporttime() != null && !retDTO.getReporttime().isEmpty()
                    && !retDTO.getReporttime().equals("null") && retDTO.getReporttime().length() > 0) {

                String sReportTime = "?Min";

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());

                try {
                    Date dReportTime = df.parse(retDTO.getReporttime());
                    Date dCurretTime = df.parse(formattedDate);

                    long diff = dCurretTime.getTime() - dReportTime.getTime();
                    long seconds = diff / 1000;
                    long minutes = seconds / 60;

                    if (minutes > 60) {
                        sReportTime = "60M+";
                    } else {
                        sReportTime = minutes + "M";
                    }

                } catch (Exception e) {
                    sReportTime = "?M";
                }

                sTeamStatusView = "□" + retDTO.getCallsign() + " : " + sReportTime;

                final TextView tvStatus = new TextView(MapsActivity.this);
                tvStatus.setPadding(20, 2, 2, 5);
                tvStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), textColorId));
                tvStatus.setShadowLayer(1, 1, 1, Color.DKGRAY);
                tvStatus.setTextSize(12);
                tvStatus.setText(sTeamStatusView);
                llStatus.addView(tvStatus); // add TextView to LinearLayout
            }
        }

        llStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mIsShow) {

                    if (markerPosition >= markerList.size()) {
                        markerPosition = 0;
                    }
                    LinearLayout ll = (LinearLayout) v;

                    if (markerPosition == 0) {
                        TextView tv = (TextView) ll.getChildAt(markerList.size() - 1);
                        tv.setText(tv.getText().toString().replace("■", "□"));

                    } else {
                        TextView tv = (TextView) ll.getChildAt(markerPosition - 1);
                        tv.setText(tv.getText().toString().replace("■", "□"));
                    }

                    TextView tv = (TextView) ll.getChildAt(markerPosition);
                    tv.setText(tv.getText().toString().replace("□", "■"));


                    Marker marker = markerList.get(markerPosition);
                    LatLng latLng = marker.getPosition();
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                    markerPosition++;
                }
            }
        });

        if (mGoallang != 0.0) {

            // 최초생성이거나, 변경이 된 경우에만 처리
            if (mGoallat != mGoallatOrg || mGoallang != mGoallangOrg) {

                if (mCircleBig != null) {
                    mCircleBig.remove();
                }

                if (mCircleSmall != null) {
                    mCircleSmall.remove();
                }

                LatLng latLng = new LatLng(mGoallat, mGoallang);
                mCircleBig   = mMap.addCircle(new CircleOptions().center(latLng).fillColor(Color.argb(80, 55, 157, 243)).radius(100.0).strokeColor(Color.argb(100, 55, 157, 243)).strokeWidth(2f)); // 100meter
                mCircleSmall = mMap.addCircle(new CircleOptions().center(latLng).fillColor(Color.argb(80, 55, 157, 243)).radius(5.0).strokeColor(Color.argb(100, 55, 157, 243)).strokeWidth(2f)); // 5meter
            }

            mGoallatOrg  = mGoallat;
            mGoallangOrg = mGoallang;
        }

        if (isInitialPostion) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionTop, 14));
            isInitialPostion = false;
        } else {
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(positionTop));
        }

        // 목표지점으로부터 특정거리 이내로 접근 시 서비스 종료처리
        if (mIsGoalIn) {
            Intent intent = new Intent(MapsActivity.this, ReportingService.class);
            stopService(intent);
        }

    }

    protected int getImgResourceID(String color) {

        int imgResourceID = R.drawable.m_169fed;

        if (color.equals("m_06ff00")) imgResourceID = R.drawable.m_06ff00;
        if (color.equals("m_43bd00")) imgResourceID = R.drawable.m_43bd00;
        if (color.equals("m_169fed")) imgResourceID = R.drawable.m_169fed;
        if (color.equals("m_1919ab")) imgResourceID = R.drawable.m_1919ab;
        if (color.equals("m_c720c9")) imgResourceID = R.drawable.m_c720c9;
        if (color.equals("m_ecd900")) imgResourceID = R.drawable.m_ecd900;
        if (color.equals("m_fc00ff")) imgResourceID = R.drawable.m_fc00ff;
        if (color.equals("m_ff0000")) imgResourceID = R.drawable.m_ff0000;
        if (color.equals("m_ff9c00")) imgResourceID = R.drawable.m_ff9c00;

        return imgResourceID;
    }

    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));

        double rounded = (double) Math.round(meter * 100) / 100;

//        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec + " Meter   " + meterInDec + " MeterRounded   " + rounded);

        return rounded;
    }

    protected void setToggleShow() {
        if (mIsShow) {
            mtvTitle.setVisibility(View.GONE);
            TranslateAnimation animate = new TranslateAnimation(0,-mtvTitle.getWidth(),0,0);
            animate.setDuration(500);
            mtvTitle.startAnimation(animate);
            mtvTitle.invalidate();

            llStatus.setVisibility(View.GONE);
            TranslateAnimation animate2 = new TranslateAnimation(0,-llStatus.getWidth(),0,0);
            animate2.setDuration(500);
            llStatus.startAnimation(animate2);
            llStatus.invalidate();

            mBtnGoal.setVisibility(View.GONE);
            TranslateAnimation animate3 = new TranslateAnimation(0,-mBtnGoal.getWidth(),0,0);
            animate3.setDuration(500);
            mBtnGoal.startAnimation(animate3);
            mBtnGoal.invalidate();

            mBtnSetupGoal.setVisibility(View.GONE);
            TranslateAnimation animate4 = new TranslateAnimation(0,-mBtnSetupGoal.getWidth(),0,0);
            animate4.setDuration(500);
            mBtnSetupGoal.startAnimation(animate4);
            mBtnSetupGoal.invalidate();

            mBtnToggleShow.setText(">>");
            mIsShow = false;
        } else {
            mtvTitle.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(-mtvTitle.getWidth(),0,0,0);
            animate.setDuration(500);
            mtvTitle.startAnimation(animate);
            mtvTitle.invalidate();

            llStatus.setVisibility(View.VISIBLE);
            TranslateAnimation animate2 = new TranslateAnimation(-llStatus.getWidth(),0,0,0);
            animate2.setDuration(500);
            llStatus.startAnimation(animate2);
            llStatus.invalidate();

            mBtnGoal.setVisibility(View.VISIBLE);
            TranslateAnimation animate3 = new TranslateAnimation(-mBtnGoal.getWidth(),0,0,0);
            animate3.setDuration(500);
            mBtnGoal.startAnimation(animate3);
            mBtnGoal.invalidate();

            mBtnSetupGoal.setVisibility(View.VISIBLE);
            TranslateAnimation animate4 = new TranslateAnimation(-mBtnSetupGoal.getWidth(),0,0,0);
            animate4.setDuration(500);
            mBtnSetupGoal.startAnimation(animate4);
            mBtnSetupGoal.invalidate();

            mBtnToggleShow.setText("<<");
            mIsShow = true;
        }
    }
}

package kr.co.teamtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kr.co.teamtracker.utils.MemberInfo;
import kr.co.teamtracker.utils.ReportingDTO;
import kr.co.teamtracker.utils.ReportingService;
import kr.co.teamtracker.utils.RoundImage;
import kr.co.teamtracker.utils.SQLiteHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;

    SQLiteHelper sqlHelper;

    MyReceiver myReceiver;

    LinearLayout llStatus;

    List<Marker> markerList = new ArrayList<Marker>();

    private boolean isInitialPostion = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        llStatus = (LinearLayout) findViewById(R.id.ll_status);

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
        intentFilter.addAction(ReportingService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);

        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        unregisterReceiver(myReceiver);
        super.onStop();
    }

    // 브로드캐스트 리시버
    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            String reportResult = arg1.getStringExtra("reportResult");

            if (!reportResult.equals("success")) {
                Toast.makeText(MapsActivity.this, reportResult, Toast.LENGTH_LONG).show();
            }

            // 서버 전송여부와 무관하게 화면 UI는 갱신처리
            updatePosition();
        }
    }

    public void updatePosition() {

        //
        if (markerList != null && markerList.size() > 0) {
            // 삭제 후 재설정
            for (int i = 0; i < markerList.size(); i++) {
                markerList.get(i).remove();
            }

            markerList.clear();
        }

        // this is my sweet hometown!!
        Double dLat = 35.8041311;
        Double dLang = 128.6239454;
        LatLng positionTop = new LatLng(dLat, dLang);

        MemberInfo memberInfo = (MemberInfo) getApplicationContext();

        List<ReportingDTO> listDto = new ArrayList<ReportingDTO>();
        listDto = sqlHelper.getReportingByTeamid(memberInfo.getTeamid());

        String sTeamStatusView = new String();

        llStatus.removeAllViews();

        TextView tvStatusTitle = new TextView(MapsActivity.this);
        tvStatusTitle.setPadding(10, 2, 2, 10);
        tvStatusTitle.setTextSize(12);
        tvStatusTitle.setTextColor(Color.BLACK);
        tvStatusTitle.setText("** Unit Status View **");
        llStatus.addView(tvStatusTitle);

        int imgId       = R.drawable.m_169fed;
        int textColorId = R.color.m_169fed;

        for (int i = 0; i < listDto.size(); i++) {

            ReportingDTO retDTO = listDto.get(i);

            // icon / text color setting start
            if (retDTO.getColor().equals("06ff00") ) {
                imgId       = R.drawable.m_06ff00;
                textColorId = R.color.m_06ff00;
            }
            if (retDTO.getColor().equals("43bd00") ) {
                imgId       = R.drawable.m_43bd00;
                textColorId = R.color.m_43bd00;
            }
            if (retDTO.getColor().equals("169fed") ) {
                imgId       = R.drawable.m_169fed;
                textColorId = R.color.m_169fed;
            }
            if (retDTO.getColor().equals("1919ab") ) {
                imgId       = R.drawable.m_1919ab;
                textColorId = R.color.m_1919ab;
            }
            if (retDTO.getColor().equals("c720c9") ) {
                imgId       = R.drawable.m_c720c9;
                textColorId = R.color.m_c720c9;
            }
            if (retDTO.getColor().equals("ecd900") ) {
                imgId       = R.drawable.m_ecd900;
                textColorId = R.color.m_ecd900;
            }
            if (retDTO.getColor().equals("fc00ff") ) {
                imgId       = R.drawable.m_fc00ff;
                textColorId = R.color.m_fc00ff;
            }
            if (retDTO.getColor().equals("ff0000") ) {
                imgId       = R.drawable.m_ff0000;
                textColorId = R.color.m_ff0000;
            }
            if (retDTO.getColor().equals("ff9c00") ) {
                imgId       = R.drawable.m_ff9c00;
                textColorId = R.color.m_ff9c00;
            }
            // icon / text color setting end


            // location setting
            LatLng position = new LatLng(retDTO.getLat(), retDTO.getLang());

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgId).copy(Bitmap.Config.ARGB_8888, true);

            bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, false);

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(24);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setShadowLayer(2, 2, 2, Color.WHITE);
            canvas.drawText(retDTO.getCallsign(), canvas.getWidth() / 2, 24, paint);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                    .title(retDTO.getCallsign() + "님의 정보")
                    .snippet("report time : " + retDTO.getReporttime() + "\n"
                           + "speed : "       + retDTO.getSpeed() + "\n"
                           + "direction : "   + retDTO.getDirection() + "\n"
                           + "status : "      + retDTO.getStatus())
                    .anchor(0.5f, 0.83f));
            markerList.add(marker);



            if (i == 0) {
                positionTop = position;
            }

            if (retDTO.getReporttime() != null && !retDTO.getReporttime().equals("")
                    && !retDTO.getReporttime().equals("null") && retDTO.getReporttime().length() > 0) {
                sTeamStatusView += "■ " + retDTO.getCallsign() + " : " + retDTO.getReporttime().substring(11, retDTO.getReporttime().length()) + "\n";

                TextView tvStatus = new TextView(MapsActivity.this);
                tvStatus.setPadding(20, 2, 2, 5);
                tvStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), textColorId));
                tvStatus.setShadowLayer(1, 1, 1, Color.WHITE);
                tvStatus.setTextSize(12);
                tvStatus.setText(sTeamStatusView);
                llStatus.addView(tvStatus); // add TextView to LinearLayout
            }
        }

        if (isInitialPostion) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionTop, 14));
            isInitialPostion = false;
        } else {
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(positionTop));
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
}

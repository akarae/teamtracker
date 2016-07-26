package kr.co.teamtracker;

import android.content.Context;
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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kr.co.teamtracker.utils.ReportingDTO;
import kr.co.teamtracker.utils.ReportingService;
import kr.co.teamtracker.utils.RoundImage;
import kr.co.teamtracker.utils.SQLiteHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;

    SQLiteHelper sqlHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);

//        // SQLite test
        sqlHelper = new SQLiteHelper(MapsActivity.this, "table_reportings", null, SQLiteHelper.dbVersion);
//
//        ReportingDTO dto1 = new ReportingDTO();
//        // rae
//        dto1.setTokenid("00001");
//        dto1.setLat(35.7893002);
//        dto1.setLang(128.5985883);
//        dto1.setCallsign("akarae");
//
//        sqlHelper.insReporting(dto1);
//
//        // yaong
//        ReportingDTO dto2 = new ReportingDTO();
//        dto2.setTokenid("00002");
//        dto2.setLat(35.787925);
//        dto2.setLang(128.5936625);
//        dto2.setCallsign("yaong");
//        sqlHelper.insReporting(dto2);
//
//        // bbong
//        ReportingDTO dto3 = new ReportingDTO();
//        dto3.setTokenid("00003");
//        dto3.setLat(35.7978644);
//        dto3.setLang(128.6154815);
//        dto3.setCallsign("bbong");
//        sqlHelper.insReporting(dto3);


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

                                                        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                                                        Criteria criteria = new Criteria();
                                                        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

//                                                        if (location != null) {
//                                                            LatLng lastPosition = new LatLng(location.getLatitude(), location.getLongitude());
//                                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPosition, 14));
//                                                        }

                                                        return false;
                                                    }
                                                }
        );


        // this is my sweet hometown!!
        Double dLat = 35.8041311;
        Double dLang = 128.6239454;

        LatLng positionTop = new LatLng(dLat, dLang);

//        // 양지마을입구
//        LatLng position1 = new LatLng(35.787925, 128.5936625);
//        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.m_2e98f3);
//        bitmap1 = bitmap1.copy(Bitmap.Config.ARGB_8888,true);
//        Canvas canvas1 = new Canvas(bitmap1);
//        Paint paint1 = new Paint();
//        paint1.setColor(Color.BLACK);
//        paint1.setTextSize(24);
//        paint1.setTypeface(Typeface.DEFAULT_BOLD);
//        paint1.setTextAlign(Paint.Align.CENTER);
//        paint1.setShadowLayer(2,2,2,Color.WHITE);
//        canvas1.drawText("YAONG", canvas1.getWidth() / 2, 24, paint1);
//        mMap.addMarker(new MarkerOptions()
//                .position(position1)
//                .icon(BitmapDescriptorFactory.fromBitmap(bitmap1))
//                .title("this is my custom icon test")
//                .snippet("it was too difficult to me" + "\n" + "but i did it")
//                .anchor(0.5f, 0.83f));
//
//        // 가창댐
//        LatLng position2 = new LatLng(35.7978644, 128.6154815);
//        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.m_30f242);
////        bitmap2 = Bitmap.createScaledBitmap(bitmap2, bitmap2.getWidth()/2,bitmap2.getHeight()/2, false);
//        bitmap2 = bitmap2.copy(Bitmap.Config.ARGB_8888,true);
//        Canvas canvas2 = new Canvas(bitmap2);
//        Paint paint2 = new Paint();
//        paint2.setColor(Color.BLACK);
//        paint2.setTextSize(24);
//        paint2.setTypeface(Typeface.DEFAULT_BOLD);
//        paint2.setTextAlign(Paint.Align.CENTER);
//        paint2.setShadowLayer(2,2,2,Color.WHITE);
//        canvas2.drawText("BBONG", canvas2.getWidth() / 2, 24, paint2);
//        mMap.addMarker(new MarkerOptions()
//                .position(position2)
//                .icon(BitmapDescriptorFactory.fromBitmap(bitmap2))
//                .title("this is my custom icon test")
//                .snippet("it was too difficult to me" + "\n" + "but i did it")
//                .anchor(0.5f, 0.83f));

//        // 헐티로
//        ReportingDTO retDTO = new ReportingDTO();
//        retDTO = sqlHelper.getReporting("12345");
//
//        LatLng position3 = new LatLng(retDTO.getLat(), retDTO.getLang());
//        Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.m_f030f2);
//        bitmap3 = bitmap3.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas3 = new Canvas(bitmap3);
//        Paint paint3 = new Paint();
//        paint3.setColor(Color.BLACK);
//        paint3.setTextSize(24);
//        paint3.setTypeface(Typeface.DEFAULT_BOLD);
//        paint3.setTextAlign(Paint.Align.CENTER);
//        paint3.setShadowLayer(2,2,2,Color.WHITE);
//        canvas3.drawText(retDTO.getCallsign(), canvas3.getWidth() / 2, 24, paint3);
//        mMap.addMarker(new MarkerOptions()
//                .position(position3)
//                .icon(BitmapDescriptorFactory.fromBitmap(bitmap3))
//                .title("this is my custom icon test")
//                .snippet(retDTO.getTokenid())
//                .anchor(0.5f, 0.83f));
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position3, 14));

        // 헐티로
        List<ReportingDTO> listDto = new ArrayList<ReportingDTO>();
        listDto = sqlHelper.getReportingAll();

        List<Marker> markerList = new ArrayList<Marker>();

        String sTeamStatusView = new String();

        for (int i = 0; i < listDto.size(); i++) {

            ReportingDTO retDTO = listDto.get(i);
            LatLng position = new LatLng(retDTO.getLat(), retDTO.getLang());
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.m_f030f2).copy(Bitmap.Config.ARGB_8888, true);
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
                             + "speed : " + retDTO.getSpeed() + "\n"
                             + "direction : " + retDTO.getDirection() + "\n"
                             + "status : " + retDTO.getStatus())
                    .anchor(0.5f, 0.83f));
            markerList.add(marker);

            sTeamStatusView += retDTO.getCallsign() + " : " + retDTO.getReporttime() + "\n";

            if (i == 0) {
                positionTop = position;
            }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionTop, 14));


        RelativeLayout layout = (RelativeLayout) findViewById(R.id.map_layout);

        //TextView 생성
        TextView tvTeamStatus = (TextView) findViewById(R.id.tv_teamstatus);
        tvTeamStatus.setText("[Team Status View]\n" + sTeamStatusView);
        ;
        tvTeamStatus.setTextSize(12);
        tvTeamStatus.setTextColor(Color.BLACK);
        tvTeamStatus.setShadowLayer(2, 2, 2, Color.WHITE);

        //부모 뷰에 추가
//        layout.addView(tvTeamStatus);

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

    }
}

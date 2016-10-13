package kr.co.teamtracker;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import kr.co.teamtracker.gcm.QuickstartPreferences;
import kr.co.teamtracker.httpclient.GCMHttpClient;
import kr.co.teamtracker.utils.ReportingDTO;
import kr.co.teamtracker.utils.SQLiteHelper;

public class SetupActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "SetupActivity";

    SQLiteHelper sqlHelper;

    private String mTeamId;

    private GoogleMap mMap;

    private TextView tvGoal;

    private Button btnGoal;

    private LatLng mLatLng;
    private String mAddress;
    private String mCity;
    private String mState;
    private String mCountry;
    private String mKnownName;

    private Circle mCircleBig;
    private Circle mCircleSmall;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        Intent intent = getIntent();
        Bundle bundle = (Bundle) intent.getExtras();
        mTeamId = (String) bundle.get("teamid");

        tvGoal = (TextView) findViewById(R.id.tv_setup_goal);
        btnGoal = (Button) findViewById(R.id.btn_setup_goal);

        btnGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLatLng == null || mLatLng.latitude == 0.0) {
                    Toast.makeText(getApplicationContext(), "Please point your GOAL on this MAP", Toast.LENGTH_SHORT);
                } else {
                    tvGoal.setText(mAddress);

                    if (mCircleBig != null) {
                        mCircleBig.remove();
                    }

                    if (mCircleSmall != null) {
                        mCircleSmall.remove();
                    }

                    mCircleBig   = mMap.addCircle(new CircleOptions().center(mLatLng).fillColor(Color.argb(80, 55, 157, 243)).radius(100.0).strokeColor(Color.argb(100, 55, 157, 243)).strokeWidth(2f)); // 100meter
                    mCircleSmall = mMap.addCircle(new CircleOptions().center(mLatLng).fillColor(Color.argb(80, 55, 157, 243)).radius(5.0).strokeColor(Color.argb(100, 55, 157, 243)).strokeWidth(2f)); // 5meter

                    ReportingDTO reportingDTO = new ReportingDTO();
                    reportingDTO.setTeamid(mTeamId);
                    reportingDTO.setGoallat(mLatLng.latitude);
                    reportingDTO.setGoallang(mLatLng.longitude);

                    sqlHelper.setTeamGoal(reportingDTO);

                    // request parameter 설정
                    RequestParams params = new RequestParams();
                    params.add("teamid",   mTeamId);
                    params.add("goallat",  String.valueOf(mLatLng.latitude));
                    params.add("goallang", String.valueOf(mLatLng.longitude));
                    params.add("flag",     "G");

                    GCMHttpClient.get("/gcm/registteam", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String responseMsg = new String(responseBody);

                            Log.i(TAG, "statusCode is : " + statusCode);
                            Log.i(TAG, "responseBody is : " + responseMsg);

                            if (responseMsg.equals(QuickstartPreferences.TEAMS_GOAL_UPDATE_SUCCESS)
                                    || responseMsg.equals(QuickstartPreferences.TEAMS_GCM_SEND_SUCCESS)) {
                                Toast.makeText(getApplicationContext(), "team update success", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            String errorMsg = error.getMessage();
                            Throwable errorCause = error.getCause();
                            StackTraceElement stackTraceElement[] = error.getStackTrace();

                            Log.i(TAG, "errorMsg is : " + errorMsg);

                            Toast.makeText(getApplicationContext(), "team update failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        sqlHelper = new SQLiteHelper(SetupActivity.this, null, SQLiteHelper.dbVersion);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_setup);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setMyLocationEnabled(true);

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

        // 지도 클릭 시 해당 좌표로 이동 및 마커 설정
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                //Log.d(TAG, "clicked!!!");

                try {

                    mLatLng = latLng;

                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(SetupActivity.this, Locale.getDefault());

                    addresses = geocoder.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    mAddress   = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    mCity      = addresses.get(0).getLocality();
                    mState     = addresses.get(0).getAdminArea();
                    mCountry   = addresses.get(0).getCountryName();
                    mKnownName = addresses.get(0).getFeatureName();

                    //mMap.clear();

                    if (mMarker != null) {
                        mMarker.remove();
                    }

                    mMarker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("Position Information")
                                    .snippet(mCountry + "\n" + mState + "\n" + mCity + "\n" + mAddress + "\n" + mKnownName)
                    );

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        // Current GOAL
        List <ReportingDTO> ret = sqlHelper.getReportingByTeamid(mTeamId);

        Double dGoalLang = ret.get(0).getGoallang();
        Double dGoalLat  = ret.get(0).getGoallat();

        if (dGoalLat != 0.0) {

            mLatLng = new LatLng(dGoalLat, dGoalLang);

            if (mCircleBig != null) {
                mCircleBig.remove();
            }

            if (mCircleSmall != null) {
                mCircleSmall.remove();
            }

            mCircleBig = mMap.addCircle(new CircleOptions().center(mLatLng).fillColor(Color.argb(80, 55, 157, 243)).radius(100.0).strokeColor(Color.argb(100, 55, 157, 243)).strokeWidth(2f)); // 100meter
            mCircleSmall = mMap.addCircle(new CircleOptions().center(mLatLng).fillColor(Color.argb(80, 55, 157, 243)).radius(5.0).strokeColor(Color.argb(100, 55, 157, 243)).strokeWidth(2f)); // 5meter
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));

        Double dLat = 35.8041311;
        Double dLang = 128.6239454;
        LatLng position = new LatLng(dLat, dLang);

        if (location != null) {
            position = new LatLng(location.getLatitude(), location.getLongitude());
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14));

    }
}

package kr.co.teamtracker.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import kr.co.teamtracker.MainActivity;
import kr.co.teamtracker.R;
import kr.co.teamtracker.httpclient.GCMHttpClient;

public class ReportingService extends Service {

    private static final String TAG = "ReportingService";

    NotificationManager Notifi_M;
    ServiceThread thread;
    Notification Notifi ;

    public ReportingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // 최초 생성되었을때 한번 실행
    @Override
    public void onCreate() {
        super.onCreate();
    }

    // 백그라운드에서 실행되는 동작들이 들어가는 곳
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.start();
        return START_STICKY;
    }

    // 서비스가 종료될 때 실행되는 함수
    @Override
    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    // inner class
    class myServiceHandler extends Handler {

        LocationManager locationManager = null; // 전역변수로 처리되어야 함

        @Override
        public void handleMessage(android.os.Message msg) {

            try {

                locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, true);

                Location bestLocation = locationManager.getLastKnownLocation(provider);

                Double lat  = 0.0;
                Double lang = 0.0;

                if (bestLocation == null) {
                    lat  = 35.8041311;
                    lang = 128.6239454;
                } else {
                    lat  = bestLocation.getLatitude();
                    lang = bestLocation.getLongitude();
                }

//                Geocoder geocoder = new Geocoder(ReportingService.this, Locale.getDefault());
//                List<Address> addresses = geocoder.getFromLocation(lat, lang, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//
//                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                String city = addresses.get(0).getLocality();
//                String state = addresses.get(0).getAdminArea();
//                String country = addresses.get(0).getCountryName();
//                String postalCode = addresses.get(0).getPostalCode();
//                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
//
//                String fullAddress = new String();
//                fullAddress += "default lat  :  35.8041311" + "\n";
//                fullAddress += "default lang : 128.6239454" + "\n";
//                fullAddress += "lat  : " + lat + "\n";
//                fullAddress += "lang : " + lang + "\n";
//                fullAddress += "address : " + address + "\n";
//                fullAddress += city + "\n";
//                fullAddress += state + "\n";
//                fullAddress += country + "\n";
//                fullAddress += knownName + "\n";
//
//                Log.d(TAG, fullAddress);


                // request parameter 설정
                RequestParams params = new RequestParams();
                params.add("callsign", "akarae");
                params.add("teamid",   "ironman");
                params.add("lat", lat.toString());
                params.add("lang", lang.toString());
                params.add("tokenid", "fBSPYgGfX5s:APA91bGDFBOWZ9Jb_d9g993AKO_c56wdr9E3Rl-ckbJPQDOwQlpl7gQazJdGluYXnQyxvkb5UOgnSKbUTQg87phNPsvB_ICcxg_M2jPEEQM_9cTZJepXzsPeFbJRB1BXlG07uewScoeK");
                params.add("speed",   Float.toString(bestLocation.getSpeed() * (3600 / 1000)));
                params.add("direction", Float.toString(bestLocation.getBearing()));
                params.add("status", "NORMAL");

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String formattedDate = df.format(c.getTime());
                params.add("reporttime", formattedDate);

                GCMHttpClient.get("/gcm", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        String responseMsg = responseBody.toString();

                        String responseMsg2 = new String(responseBody);

                        Log.i(TAG, "i got a response");
                        Log.i(TAG, "statusCode is : " + statusCode);
                        Log.i(TAG, "responseBody is : " + responseMsg2);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        String responseMsg = responseBody.toString();

                        String errorMsg = error.getMessage();
                        Throwable errorCause = error.getCause();
                        StackTraceElement stackTraceElement[] = error.getStackTrace();

                        Log.i(TAG, "errorMsg is : " + errorMsg);

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "nodejs connection error " + e.getMessage());
            }

        }
    };
}

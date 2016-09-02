package kr.co.teamtracker.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import android.location.LocationListener;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;
import kr.co.teamtracker.httpclient.GCMHttpClient;

public class ReportingService extends Service {

    private static final String TAG = "ReportingService";

    public final static String MY_ACTION = "MY_ACTION";

    NotificationManager Notifi_M;
    ServiceThread thread;
    SQLiteHelper sqlHelper;

    // location service
    private static int LOCATION_UPDATE_TERM = 1000 * 1 * 1; // 10sec
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;

    /**
     minTime     = long: minimum time interval between location updates, in milliseconds
     minDistance = float: minimum distance between location updates, in meters
     */
    long minTime = 1000;
    float minDistance = 0;

    public ReportingService() { }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    // 최초 생성되었을때 한번 실행
    @Override
    public void onCreate() {

        sqlHelper = new SQLiteHelper(ReportingService.this, null, SQLiteHelper.dbVersion);

        super.onCreate();
    }

    // 백그라운드에서 실행되는 동작들이 들어가는 곳
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "☆☆☆☆☆ service onStartCommand ☆☆☆☆☆");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, listener);

        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.start();
        return START_STICKY;
    }

    // 서비스가 종료될 때 실행되는 함수
    @Override
    public void onDestroy() {

        Log.d(TAG, "☆☆☆☆☆ service destroyed ☆☆☆☆☆");

        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.

        locationManager.removeUpdates(listener);
    }

    // inner class
    class myServiceHandler extends Handler {

        LocationManager locationManager = null; // 전역변수로 처리되어야 함

        String reportResult = "reporing failed";

        @Override
        public void handleMessage(android.os.Message msg) {

            try {

                ReportingDTO reportingDTO = sqlHelper.getReporting(((MemberInfo)getApplicationContext()).getUuid());

                if (reportingDTO != null && reportingDTO.getLat() != null && reportingDTO.getLang() != null) {

                    MemberInfo memberInfo = (MemberInfo) getApplicationContext();

                    // request parameter 설정
                    RequestParams params = new RequestParams();

                    params.add("uuid",     memberInfo.getUuid());
//                    params.add("callsign", memberInfo.getCallsign());
//                    params.add("status",   memberInfo.getStatus());
//                    params.add("color",    memberInfo.getColor());

                    params.add("lat", reportingDTO.getLat().toString());
                    params.add("lang", reportingDTO.getLang().toString());
                    params.add("speed", reportingDTO.getSpeed().toString());
                    params.add("direction", reportingDTO.getDirection());
                    params.add("reporttime", reportingDTO.getReporttime());



                    GCMHttpClient.get("/gcm", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String responseMsg = responseBody.toString();

                            String responseMsg2 = new String(responseBody);

                            Log.i(TAG, "i got a response");
                            Log.i(TAG, "statusCode is : " + statusCode);
                            Log.i(TAG, "responseBody is : " + responseMsg2);

                            reportResult = "success";
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            // nullable
//                        String responseMsg = responseBody.toString();

                            String errorMsg = error.getMessage();
                            Throwable errorCause = error.getCause();
                            StackTraceElement stackTraceElement[] = error.getStackTrace();

                            Log.i(TAG, "errorMsg is : " + errorMsg);

                            reportResult = "reporing failed";

                        }
                    });
                } else {
                    reportResult = "no location";
                }

                Intent intent = new Intent();
                intent.setAction(MY_ACTION);
                intent.putExtra("reportResult", reportResult);
                sendBroadcast(intent);

            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "nodejs connection error " + e.getMessage());
            }

        }
    };

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > LOCATION_UPDATE_TERM;
        boolean isSignificantlyOlder = timeDelta < -LOCATION_UPDATE_TERM;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {

            if (isBetterLocation(loc, previousBestLocation)) {

                //Toast.makeText(getApplicationContext(), "location changed", Toast.LENGTH_SHORT).show();

                ReportingDTO reportingDTO = sqlHelper.getReporting(((MemberInfo)getApplicationContext()).getUuid());

                if (reportingDTO == null || reportingDTO.getLat() == null || reportingDTO.getLat() == null) {

                    ReportingDTO dto = new ReportingDTO();
                    dto.setUuid(((MemberInfo) getApplicationContext()).getUuid());
                    dto.setCallsign(((MemberInfo) getApplicationContext()).getCallsign());
                    dto.setTeamid(((MemberInfo) getApplicationContext()).getTeamid());
                    dto.setStatus(((MemberInfo) getApplicationContext()).getStatus());

                    dto.setLat(loc.getLatitude());
                    dto.setLang(loc.getLongitude());
                    dto.setSpeed(Float.toString(loc.getSpeed()));
                    dto.setDirection(Float.toString(loc.getBearing()));

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());
                    dto.setReporttime(formattedDate);

                    sqlHelper.insReporting(dto);

                } else {

                    // SQLite 저장
                    // SQLite Update 처리
                    SQLiteHelper sqlHelper = new SQLiteHelper(ReportingService.this, null, SQLiteHelper.dbVersion);

                    ReportingDTO dto = new ReportingDTO();
                    dto.setUuid(((MemberInfo) getApplicationContext()).getUuid());
                    dto.setLat(loc.getLatitude());
                    dto.setLang(loc.getLongitude());
                    dto.setSpeed(Float.toString(loc.getSpeed()));
                    dto.setDirection(Float.toString(loc.getBearing()));

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String formattedDate = df.format(c.getTime());
                    dto.setReporttime(formattedDate);

                    sqlHelper.setReporting(dto);
                }
            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }
}

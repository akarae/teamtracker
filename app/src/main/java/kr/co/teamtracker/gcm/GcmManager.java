package kr.co.teamtracker.gcm;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import kr.co.teamtracker.httpclient.GCMHttpClient;

/**
 * Created by akarae on 2016-07-26.
 */
public class GcmManager {

    private final String TAG = this.getClass().getName();

    // Position Request
    private void sendMyReporting() {

//        try {
//
//            LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//
//            List<String> providers = locationManager.getProviders(true);
//
//            Location bestLocation = null;
//
//            for (String provider : providers) {
//                Location l = locationManager.getLastKnownLocation(provider);
//                if (l == null) {
//                    continue;
//                }
//                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
//                    // Found best last known location: %s", l);
//                    bestLocation = l;
//                }
//            }
//
//            Double lat  = 0.0;
//            Double lang = 0.0;
//
//            bestLocation = mBestReading;
//
//            if (bestLocation == null) {
//                lat  = 35.8041311;
//                lang = 128.6239454;
//            } else {
//                lat  = bestLocation.getLatitude();
//                lang = bestLocation.getLongitude();
//            }
//
//            Geocoder geocoder = new Geocoder(LocationHelper.this, Locale.getDefault());
//            List<Address> addresses = geocoder.getFromLocation(lat, lang, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
//
//            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//            String city = addresses.get(0).getLocality();
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
//
//            String fullAddress = new String();
//            fullAddress += "default lat  :  35.8041311" + "\n";
//            fullAddress += "default lang : 128.6239454" + "\n";
//            fullAddress += "lat  : " + lat + "\n";
//            fullAddress += "lang : " + lang + "\n";
//            fullAddress += "address : " + address + "\n";
//            fullAddress += city + "\n";
//            fullAddress += state + "\n";
//            fullAddress += country + "\n";
//            fullAddress += knownName + "\n";
//
//            mPositionTextView.setText(fullAddress);
//
//            float speed = bestLocation.getSpeed();
//
//            // request parameter 설정
//            RequestParams params = new RequestParams();
//            params.add("callsign", "akarae");
//            params.add("teamid",   "ironman");
//            params.add("lat", lat.toString());
//            params.add("lang", lang.toString());
//            params.add("tokenid",  "fBSPYgGfX5s:APA91bGDFBOWZ9Jb_d9g993AKO_c56wdr9E3Rl-ckbJPQDOwQlpl7gQazJdGluYXnQyxvkb5UOgnSKbUTQg87phNPsvB_ICcxg_M2jPEEQM_9cTZJepXzsPeFbJRB1BXlG07uewScoeK");
//            params.add("speed",   Float.toString(bestLocation.getSpeed() * (3600 / 1000)));
//            params.add("direction", Float.toString(bestLocation.getBearing()));
//            params.add("status", "NORMAL");
//
//            Calendar c = Calendar.getInstance();
//            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//            String formattedDate = df.format(c.getTime());
//            params.add("reporttime", formattedDate);
//
//            GCMHttpClient.get("/gcm", params, new AsyncHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//
//                    String responseMsg = responseBody.toString();
//
//                    String responseMsg2 = new String(responseBody);
//
//                    Log.i(TAG, "i got a response");
//                    Log.i(TAG, "statusCode is : " + statusCode);
//                    Log.i(TAG, "responseBody is : " + responseMsg2);
//                    mNodejsTextView.setVisibility(View.VISIBLE);
//                    mNodejsTextView.setText("nodejs request is success : " + responseMsg2);
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//
//                    String responseMsg = responseBody.toString();
//
//                    String errorMsg = error.getMessage();
//                    Throwable errorCause = error.getCause();
//                    StackTraceElement stackTraceElement[] = error.getStackTrace();
//
//                    Log.i(TAG, "errorMsg is : " + errorMsg);
//
//                    mNodejsTextView.setText("nodejs request is failure : " + errorMsg);
//
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.i(TAG, "nodejs connection error " + e.getMessage());
//        }

    }

}

package kr.co.teamtracker.httpclient;

import android.nfc.Tag;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

/**
 * Created by akarae on 2016-03-14.
 */
public class GCMHttpClient {
//    private static final String BASE_URL = "http://211.110.165.187";
    private static final String BASE_URL = "http://googooeyes.cafe24.com:3000";
//    private static final String BASE_URL = "http://192.168.0.3";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static AsyncHttpClient getInstance() {
        return client;
    }

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        //Log.i("HttpClient", "HttpClient URL is : " + getAbsoluteUrl(url));
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }
    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
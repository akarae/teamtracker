package kr.co.teamtracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.loopj.android.http.AsyncHttpClient;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import kr.co.teamtracker.gcm.QuickstartPreferences;
import kr.co.teamtracker.gcm.RegistrationIntentService;
import kr.co.teamtracker.httpclient.GCMHttpClient;
import kr.co.teamtracker.utils.ItemData;
import kr.co.teamtracker.utils.MemberInfo;
import kr.co.teamtracker.utils.ReportingDTO;
import kr.co.teamtracker.utils.ReportingService;
import kr.co.teamtracker.utils.SQLiteHelper;
import kr.co.teamtracker.utils.SpinnerAdapter;

public class MainActivity extends AppCompatActivity  {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private Button mRegistrationButton;
    private Button mTeamRegistrationButton;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Spinner mSpinner;
    ArrayList<ItemData> listSpinnerItem = new ArrayList<>();

    private EditText mCallsignEditText;
    private EditText mTeamidEditText;

    private Button mTeamlistButton;
    private ListView mTeamListView;

    private long mLastTimeBackPressed = 0;


    SQLiteHelper sqlHelper;



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;

    /**
     * Instance ID를 이용하여 디바이스 토큰을 가져오는 RegistrationIntentService를 실행한다.
     */
    public void getInstanceIdToken() {

        Log.i(TAG, "getInstanceIdToken");

        // GooglePlayService를 사용 가능한 환경인지 검증
        if (checkPlayServices()) {

            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);

            Log.i(TAG, "intent is : " + intent.toString());

            ComponentName startServiceResult = startService(intent);

            Log.i(TAG, "startServiceResult is : " + startServiceResult.toString());
        }
    }

    /**
     * LocalBroadcast 리시버를 정의한다. 토큰을 획득하기 위한 READY, GENERATING, COMPLETE 액션에 따라 UI에 변화를 준다.
     */
    public void registBroadcastReceiver() {

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(QuickstartPreferences.REGISTRATION_COMPLETE)) {
                    String token = intent.getStringExtra("token");

                    MemberInfo memberInfo = (MemberInfo) getApplicationContext();

                    // request parameter 설정
                    RequestParams params = new RequestParams();

                    params.add("tokenid",  memberInfo.getTokeinid());
                    params.add("callsign", memberInfo.getCallsign());
//                    params.add("teamid",   memberInfo.getTeamid());
                    params.add("status",   memberInfo.getStatus());
                    params.add("color",    memberInfo.getColor());

                    GCMHttpClient.get("/gcm/regist", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String responseMsg = responseBody.toString();

                            String responseMsg2 = new String(responseBody);

                            Log.i(TAG, "i got a response");
                            Log.i(TAG, "statusCode is : " + statusCode);
                            Log.i(TAG, "responseBody is : " + responseMsg2);

                            Toast.makeText(getApplicationContext(), "registration success", Toast.LENGTH_SHORT).show();

//                            // team 등록
//                            MemberInfo memberInfo = (MemberInfo) getApplicationContext();
//
//                            ReportingDTO reportingDTO = new ReportingDTO();
//                            reportingDTO.setTokenid(memberInfo.getTokeinid());
//                            reportingDTO.setTeamid(memberInfo.getTeamid());
//                            sqlHelper.insTeam(reportingDTO);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            // nullable
//                        String responseMsg = responseBody.toString();

                            String errorMsg = error.getMessage();
                            Throwable errorCause = error.getCause();
                            StackTraceElement stackTraceElement[] = error.getStackTrace();

                            Log.i(TAG, "errorMsg is : " + errorMsg);

                            Toast.makeText(getApplicationContext(), "registration failed", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registBroadcastReceiver();

        setContentView(R.layout.activity_main);

        sqlHelper = new SQLiteHelper(MainActivity.this, null, SQLiteHelper.dbVersion);

        // spinner start
        listSpinnerItem.add(new ItemData("06ff00", R.drawable.c_06ff00));
        listSpinnerItem.add(new ItemData("43bd00", R.drawable.c_43bd00));
        listSpinnerItem.add(new ItemData("169fed", R.drawable.c_169fed));
        listSpinnerItem.add(new ItemData("1919ab", R.drawable.c_1919ab));
        listSpinnerItem.add(new ItemData("c720c9", R.drawable.c_c720c9));
        listSpinnerItem.add(new ItemData("ecd900", R.drawable.c_ecd900));
        listSpinnerItem.add(new ItemData("fc00ff", R.drawable.c_fc00ff));
        listSpinnerItem.add(new ItemData("ff0000", R.drawable.c_ff0000));
        listSpinnerItem.add(new ItemData("ff9c00", R.drawable.c_ff9c00));

        mSpinner = (Spinner)findViewById(R.id.sp_color);
        SpinnerAdapter adapter=new SpinnerAdapter(this, R.layout.spinner_layout,R.id.tv_sp_txt,listSpinnerItem);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // An item was selected. You can retrieve the selected item using
                // parent.getItemAtPosition(pos)

                Log.d(TAG, "onItemSelected - pos : " + pos + " id : " + id);
                MemberInfo memberInfo = (MemberInfo) getApplicationContext();
                memberInfo.setColor(listSpinnerItem.get(pos).getText());
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }

        });
        // spinner end

        mCallsignEditText = (EditText) findViewById(R.id.et_callsign);
        mTeamidEditText   = (EditText) findViewById(R.id.et_teamid);
        mTeamListView     = (ListView) findViewById(R.id.lv_teamlist);

        // 전역변수 선언
        MemberInfo memberInfo = (MemberInfo) getApplicationContext();
        String sCallsign = memberInfo.getCallsign();
        if (sCallsign != null && sCallsign.length() > 0) {
            mCallsignEditText.setText(sCallsign);
            mTeamidEditText.setText(memberInfo.getTeamid());
            mSpinner.setSelection(listSpinnerItem.indexOf(memberInfo.getColor()));
            setTeamListView();
        }

//        // 토큰을 가져오는 동안 인디케이터를 보여줄 ProgressBar를 정의
//        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
//        mRegistrationProgressBar.setVisibility(ProgressBar.GONE);

        // 토큰을 가져오는 Button을 정의
        mRegistrationButton = (Button) findViewById(R.id.btn_registration);
        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            /**
             * 버튼을 클릭하면 토큰을 가져오는 getInstanceIdToken() 메소드를 실행한다.
             *
             * @param view
             */
            @Override
            public void onClick(View view) {

                Editable etCallsign = mCallsignEditText.getText();
                Editable etTeamid   = mTeamidEditText.getText();

                if (etCallsign == null || etCallsign.toString().equals("") || etCallsign.toString().length() == 0) {

                    Toast.makeText(MainActivity.this, "input your CallSign", Toast.LENGTH_LONG).show();

                } else if (etTeamid == null || etTeamid.toString().equals("") || etTeamid.toString().length() == 0) {

                    Toast.makeText(MainActivity.this, "input your TeamId", Toast.LENGTH_LONG).show();

                } else {

                    // 전역변수 선언
                    MemberInfo memberInfo = (MemberInfo) getApplicationContext();
                    memberInfo.setCallsign(etCallsign.toString());
                    memberInfo.setTeamid(etTeamid.toString());
                    memberInfo.setStatus("NORMAL");

                    // 토큰 획득
                    getInstanceIdToken();

                }
            }
        });

        mTeamRegistrationButton = (Button) findViewById(R.id.btn_teamregistration);
        mTeamRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mTeamidEditText.getText() != null && mTeamidEditText.getText().toString().length() > 0) {
                    MemberInfo memberInfo = (MemberInfo) getApplicationContext();
                    String sTeamid = mTeamidEditText.getText().toString();

                    // request parameter 설정
                    RequestParams params = new RequestParams();

                    params.add("tokenid",  memberInfo.getTokeinid());
                    params.add("teamid",   sTeamid);
                    params.add("flag",     "N");

                    GCMHttpClient.get("/gcm/registteam", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String responseMsg = responseBody.toString();

                            String responseMsg2 = new String(responseBody);

                            Log.i(TAG, "statusCode is : " + statusCode);
                            Log.i(TAG, "responseBody is : " + responseMsg2);

                            Toast.makeText(getApplicationContext(), "team registration success", Toast.LENGTH_SHORT).show();

                            // team 등록
                            MemberInfo memberInfo = (MemberInfo) getApplicationContext();

                            ReportingDTO reportingDTO = new ReportingDTO();
                            reportingDTO.setTokenid(memberInfo.getTokeinid());
                            reportingDTO.setTeamid(memberInfo.getTeamid());
                            sqlHelper.insTeam(reportingDTO);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            // nullable
//                        String responseMsg = responseBody.toString();

                            String errorMsg = error.getMessage();
                            Throwable errorCause = error.getCause();
                            StackTraceElement stackTraceElement[] = error.getStackTrace();

                            Log.i(TAG, "errorMsg is : " + errorMsg);

                            Toast.makeText(getApplicationContext(), "team registration failed", Toast.LENGTH_SHORT).show();

                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), "Teamid is empty, Please input Teamid", Toast.LENGTH_SHORT);
                }

            }
        });

        // MAP
        Button btnMap = (Button) findViewById(R.id.btn_map);
        btnMap.setOnClickListener(new View.OnClickListener() {
            /**
             * 버튼을 클릭하면 토큰을 가져오는 getInstanceIdToken() 메소드를 실행한다.
             *
             * @param view
             */
            @Override
            public void onClick(View view) {
                //@todo it will be deleted
                startMapActivity("ironman");
            }

        });

        // Teamlist
        mTeamlistButton = (Button) findViewById(R.id.btn_teamlist);
        mTeamlistButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                setTeamListView();
            }
        });

        // START SERVICE
        Button btnStartService = (Button) findViewById(R.id.btn_startservice);
        btnStartService.setOnClickListener(new View.OnClickListener() {
            /**
             * 버튼을 클릭하면 토큰을 가져오는 getInstanceIdToken() 메소드를 실행한다.
             *
             * @param view
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ReportingService.class);
                startService(intent);
            }
        });

        // STOP SERVICE
        Button btnStopService = (Button) findViewById(R.id.btn_stopservice);
        btnStopService.setOnClickListener(new View.OnClickListener() {
            /**
             * 버튼을 클릭하면 토큰을 가져오는 getInstanceIdToken() 메소드를 실행한다.
             *
             * @param view
             */
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ReportingService.class);
                stopService(intent);
            }
        });

        // TeamListView



        // 쿠키정보 저장(앱을 종료시켰다가 재실행해도 로긴유지
        AsyncHttpClient client = GCMHttpClient.getInstance();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);

                // ATTENTION: This was auto-generated to implement the App Indexing API.
                // See https://g.co/AppIndexing/AndroidStudio for more information.
                client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * 앱이 실행되어 화면에 나타날때 LocalBoardcastManager에 액션을 정의하여 등록한다.
     */
    @Override
    protected void onResume() {


        super.onResume();

        Log.d(TAG, "★★★★★ onResume ★★★★★");

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

    }

    /**
     * 앱이 화면에서 사라지면 등록된 LocalBoardcast를 모두 삭제한다.
     */
    @Override
    protected void onPause() {

        Log.d(TAG, "★★★★★ onPause ★★★★★");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onPause();
    }


    /**
     * Google Play Service를 사용할 수 있는 환경이지를 체크한다.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        Log.i(TAG, "resultCode is : " + resultCode);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), "Sorry, this Device is not support", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {

        super.onStart();

        Log.d(TAG, "★★★★★ onStart ★★★★★");

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://kr.co.teamtracker/http/host/path")
        );
        AppIndex.AppIndexApi.start(client2, viewAction);
    }

    @Override
    public void onStop() {

        super.onStop();

        Log.d(TAG, "★★★★★ onStop ★★★★★");

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://kr.co.teamtracker/http/host/path")
        );
        AppIndex.AppIndexApi.end(client2, viewAction);
        client2.disconnect();
    }

    @Override
    public void onBackPressed() {

        if (System.currentTimeMillis() - mLastTimeBackPressed < 1500) {

            // Tracking Service 종료
            Intent intent = new Intent(MainActivity.this, ReportingService.class);
            stopService(intent);

            // application 종료

            finish();
            return;
        }

        Toast.makeText(getApplicationContext(), "Press 'Back' again to Quit", Toast.LENGTH_SHORT).show();

        mLastTimeBackPressed = System.currentTimeMillis();

    }

    protected void setTeamListView() {

        MemberInfo memberInfo = (MemberInfo) getApplicationContext();
        String sTokenId = memberInfo.getTokeinid();

        if (sTokenId != null && !sTokenId.equals("") && sTokenId.length() > 0) {

            List<ReportingDTO> retDto = sqlHelper.getTeamList(sTokenId);

            if (retDto != null && retDto.size() > 0) {

                String[] teamList = new String[retDto.size()];

                for (int i = 0; i < retDto.size(); i++) {

                    teamList[i] = retDto.get(i).getTeamid();
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.listview_layout,
                        R.id.text1, teamList);
                mTeamListView.setAdapter(adapter);
                mTeamListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        int itemPosition     = position;
                        String  itemValue    = (String) mTeamListView.getItemAtPosition(position);

                        startMapActivity(itemValue);
                    }
                });

            } else {
                Toast.makeText(getApplicationContext(), "Can't get teamid list", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ListView에서 선택한 teamid로 MapActivity 호출
    protected void startMapActivity(String teamid) {
        Log.i(TAG, "button clicked!!!");

        try {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("teamid", teamid);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "nodejs connection error " + e.getMessage());
        }
    }
}
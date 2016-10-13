package kr.co.teamtracker;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import kr.co.teamtracker.gcm.QuickstartPreferences;
import kr.co.teamtracker.gcm.RegistrationIntentService;
import kr.co.teamtracker.httpclient.GCMHttpClient;
import kr.co.teamtracker.utils.ItemData;
import kr.co.teamtracker.utils.ReportingDTO;
import kr.co.teamtracker.utils.ReportingService;
import kr.co.teamtracker.utils.SQLiteHelper;
import kr.co.teamtracker.utils.SpinnerAdapter;

public class MainActivity extends AppCompatActivity  {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private Button mRegistrationButton;
    private Button mTeamRegistrationButton;
    private Button mTeamJoin;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Spinner mSpinner;
    private Spinner mSpinnerStatus;
    private Spinner mSpinnerInterval;
    ArrayList<ItemData> listSpinnerItem = new ArrayList<>();

    private EditText mCallsignEditText;
    private EditText mTeamidEditText;
    private TextView mStatusText;

    private Button mTeamlistButton;
    private ListView mTeamListView;

    private long mLastTimeBackPressed = 0;

    String listItemValue;
    int listItemPosition;

    String inputValue;

    SQLiteHelper sqlHelper;

    String[] teamList;
    ArrayList<String> items;

    ArrayAdapter<String> adapter;

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

                    SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
                    // request parameter 설정
                    RequestParams params = new RequestParams();

                    params.add("uuid",     gMemberInfo.getString("uuid", null));
                    params.add("tokenid",  gMemberInfo.getString("tokenid", null));
                    params.add("callsign", gMemberInfo.getString("callsign", null));
                    params.add("status",   gMemberInfo.getString("status", null));
                    params.add("color",    gMemberInfo.getString("color", null));
                    params.add("msg",      gMemberInfo.getString("msg", null));

                    GCMHttpClient.get("/gcm/regist", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String responseMsg = new String(responseBody);

                            Log.i(TAG, "statusCode is : " + statusCode);
                            Log.i(TAG, "responseBody is : " + responseMsg);

                            if (responseMsg.equals(QuickstartPreferences.MEMBERS_SAVE_INSERT_SUCCESS)
                                    || responseMsg.equals(QuickstartPreferences.MEMBERS_SAVE_UPDATE_SUCCESS)) {
                                Toast.makeText(getApplicationContext(), "registration success", Toast.LENGTH_SHORT).show();
                            } else {
                                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();     //닫기
                                    }
                                });
                                alert.setMessage("please try again later!!\n(or check your Network)");
                                alert.show();
                            }
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

        GetDevicesUUID(getApplicationContext());

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

                //Log.d(TAG, "onItemSelected - pos : " + pos + " id : " + id);
                SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = gMemberInfo.edit();
                editor.putString("color", listSpinnerItem.get(pos).getText());
                editor.commit();
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }

        });

//        mSpinnerStatus = (Spinner) findViewById(R.id.sp_status);
//        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.status_array, R.layout.spinner_item);
//        statusAdapter.setDropDownViewResource(R.layout.spinner_item);
//        mSpinnerStatus.setAdapter(statusAdapter);
//        mSpinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
//                // An item was selected. You can retrieve the selected item using
//                // parent.getItemAtPosition(pos)
//
//                String item = (String) parent.getItemAtPosition(pos);
//
//                Log.d(TAG, "onItemSelected - pos : " + pos + " item : " + item);
//                SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
//                SharedPreferences.Editor editor = gMemberInfo.edit();
//                editor.putString("status", item);
//                editor.commit();
//            }
//
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Another interface callback
//            }
//
//        });

//        mSpinnerInterval = (Spinner) findViewById(R.id.sp_interval);
//        ArrayAdapter<CharSequence> intervalAdapter = ArrayAdapter.createFromResource(this, R.array.interval_array, R.layout.spinner_item);
//        intervalAdapter.setDropDownViewResource(R.layout.spinner_item);
//        mSpinnerInterval.setAdapter(intervalAdapter);
//        mSpinnerInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//
//            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
//                // An item was selected. You can retrieve the selected item using
//                // parent.getItemAtPosition(pos)
//
//                String item = (String) parent.getItemAtPosition(pos);
//
//                Log.d(TAG, "onItemSelected - pos : " + pos + " item : " + item);
//
//                //todo interval 설정 후 TrackingService Stop AND Start
//
//            }
//
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Another interface callback
//            }
//
//        });

        // spinner end

        mCallsignEditText = (EditText) findViewById(R.id.et_callsign);
        mCallsignEditText.setPrivateImeOptions("defaultInputmode=english;");
        mTeamidEditText   = (EditText) findViewById(R.id.et_teamid);
        mTeamidEditText.setPrivateImeOptions("defaultInputmode=english;");
        mTeamListView     = (ListView) findViewById(R.id.lv_teamlist);

        // 전역변수 선언
        SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
        String sCallsign = gMemberInfo.getString("callsign", null);

        if (sCallsign != null && sCallsign.length() > 0) {
            mCallsignEditText.setText(sCallsign);
            //mTeamidEditText.setText(gMemberInfo.getString("teamid", null));

            int idx = 0;

            for (int i = 0; i < listSpinnerItem.size(); i++) {
                if (listSpinnerItem.get(i).getText().equals(gMemberInfo.getString("color", null))) {
                    idx = i;
                    break;
                }
            }

            //Log.d(TAG, "index is ::::::::: " + idx);

            mSpinner.setSelection(idx);

            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(mCallsignEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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

                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(mCallsignEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                Editable etCallsign = mCallsignEditText.getText();
                Editable etTeamid   = mTeamidEditText.getText();

                if (etCallsign == null || etCallsign.toString().equals("") || etCallsign.toString().length() == 0) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });
                    alert.setMessage("input your CallSign");
                    alert.show();

                } else {

                    // 전역변수 선언
                    SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
                    SharedPreferences.Editor editor = gMemberInfo.edit();
                    editor.putString("callsign", etCallsign.toString());
                    editor.commit();

                    if (gMemberInfo.getString("status", null) == null || gMemberInfo.getString("status", null).length() == 0) {
                        editor.putString("status", "NORMAL");
                        editor.commit();
                    }

                    // 토큰 획득
                    getInstanceIdToken();
                }
            }
        });

        // TEAM JOIN
        mTeamJoin = (Button) findViewById(R.id.btn_teamjoin);
        mTeamJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(mTeamidEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                Editable etCallsign = mCallsignEditText.getText();

                if (etCallsign == null || etCallsign.toString().equals("") || etCallsign.toString().length() == 0) {

                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });
                    alert.setMessage("regist your CallSign first");
                    alert.show();

                }


                if (mTeamidEditText.getText() != null && mTeamidEditText.getText().toString().length() > 0) {

                    // request parameter 설정
                    RequestParams params = new RequestParams();
                    SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
                    params.add("uuid",   gMemberInfo.getString("uuid", null));
                    params.add("teamid", mTeamidEditText.getText().toString());
                    params.add("flag", "J");

                    GCMHttpClient.get("/gcm/registteam", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String responseMsg = new String(responseBody);
                            JSONObject jsonObj = new JSONObject();
                            JSONArray membersObjArr = new JSONArray();

                            // json parsing
                            try {
                                jsonObj = new JSONObject(responseMsg);
                                responseMsg = jsonObj.getString("resultMsg");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Log.i(TAG, "statusCode is : " + statusCode);
                            Log.i(TAG, "responseBody is : " + responseMsg);

                            if (responseMsg.equals(QuickstartPreferences.TEAMS_TEAMID_NOT_EXISTS)) {

                                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();     //닫기
                                    }
                                });
                                alert.setMessage("TeamID not exists!!");
                                alert.show();
                            }

                            if (responseMsg.equals(QuickstartPreferences.TEAMS_MEMBERS_UPDATE_SUCCESS)
                                    || responseMsg.equals(QuickstartPreferences.TEAMS_GCM_SEND_SUCCESS)
                                    || responseMsg.equals(QuickstartPreferences.TEAMS_GCM_SEND_ERROR)) {

                                Toast.makeText(getApplicationContext(), "team registration success", Toast.LENGTH_SHORT).show();

                                // team 등록
                                SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
                                SharedPreferences.Editor editor = gMemberInfo.edit();
                                editor.putString("teamid", mTeamidEditText.getText().toString());
                                editor.commit();

//                                ReportingDTO reportingDTO = new ReportingDTO();
//                                reportingDTO.setUuid(memberInfo.getUuid());
//                                reportingDTO.setTeamid(memberInfo.getTeamid());
//                                sqlHelper.insTeam(reportingDTO);

                                try {
                                    membersObjArr = jsonObj.getJSONArray("membersObjArr");

                                    // 데이터 insert
                                    for (int i = 0; i < membersObjArr.length(); i++) {

                                        // reporting 정보
                                        JSONObject memberJSON = (JSONObject) membersObjArr.get(i);
                                        ReportingDTO memberDto = new ReportingDTO();
                                        memberDto.setUuid(memberJSON.getString("uuid"));
                                        memberDto.setCallsign(memberJSON.getString("callsign"));
                                        memberDto.setStatus(memberJSON.getString("status"));
                                        memberDto.setLat(memberJSON.getDouble("lat"));
                                        memberDto.setLang(memberJSON.getDouble("lang"));
                                        memberDto.setReporttime(memberJSON.getString("reporttime"));
                                        memberDto.setDirection(memberJSON.getString("direction"));
                                        memberDto.setSpeed(memberJSON.getString("speed"));
                                        memberDto.setColor(memberJSON.getString("color"));
                                        memberDto.setMsg(memberJSON.getString("msg"));
                                        sqlHelper.insReporting(memberDto);

                                        // team 정보
                                        memberDto.setTeamid(gMemberInfo.getString("teamid", null));
                                        sqlHelper.insTeam(memberDto);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                // teamid 삭제 후 리스트 재조회
                                mTeamidEditText.setText("");
                                setTeamListView();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            String errorMsg = error.getMessage();
                            Throwable errorCause = error.getCause();
                            StackTraceElement stackTraceElement[] = error.getStackTrace();

                            Log.i(TAG, "errorMsg is : " + errorMsg);

                            Toast.makeText(getApplicationContext(), "team registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });
                    alert.setMessage("input your TeamId");
                    alert.show();
                }
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

                Toast.makeText(getApplicationContext(), "Reporting Start!!!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, ReportingService.class);
                startService(intent);
                checkServiceStatus();
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

                Toast.makeText(getApplicationContext(), "Reporting Stop!!!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, ReportingService.class);
                stopService(intent);
                checkServiceStatus();
            }
        });

        mStatusText = (TextView) findViewById(R.id.tv_status);
        checkServiceStatus();

        // TeamListView


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

        //Log.d(TAG, "★★★★★ onResume ★★★★★");

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));

        checkServiceStatus();

    }

    /**
     * 앱이 화면에서 사라지면 등록된 LocalBoardcast를 모두 삭제한다.
     */
    @Override
    protected void onPause() {

        //Log.d(TAG, "★★★★★ onPause ★★★★★");

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

        //Log.d(TAG, "★★★★★ onStart ★★★★★");

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

        //Log.d(TAG, "★★★★★ onStop ★★★★★");

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
    public void finish() {

        // Tracking Service 종료
        Intent intent = new Intent(MainActivity.this, ReportingService.class);
        stopService(intent);

        super.finish();
    }

    @Override
    public void onBackPressed() {

        if (System.currentTimeMillis() - mLastTimeBackPressed < 1500) {

            finish();
            return;
        }

//        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
//        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();     //닫기
//            }
//        });
//        alert.setMessage("please try again later!!\n(or check your Network)");
//        alert.show();

        Toast.makeText(getApplicationContext(), "Press 'Back' again to Quit!!\r\n(Tracking Service will be stopped)", Toast.LENGTH_SHORT).show();

        mLastTimeBackPressed = System.currentTimeMillis();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            // 신규Team을 생성
            case R.id.mn_maketeam:

                final EditText etEdit = new EditText(this);
                etEdit.setTextColor(Color.WHITE);
                etEdit.setHintTextColor(Color.GRAY);
                etEdit.setTextSize(16);
                etEdit.setPrivateImeOptions("defaultInputmode=english;");
                etEdit.setHint("input your TeadID");

                etEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);
                InputFilter maxLengthFilter = new InputFilter.LengthFilter(10);
                InputFilter alphanumFilter = new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {

                        Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
                        if (!ps.matcher(source).matches()) {
                            return "";
                        }
                        return null;
                    }
                };
                etEdit.setFilters(new InputFilter[] {alphanumFilter, maxLengthFilter});

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this, R.style.custom_alert);
                dialog.setTitle("Create a new Team");
                dialog.setView(etEdit);

                // OK 버튼 이벤트
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Editable etCallsign = mCallsignEditText.getText();

                        if (etCallsign == null || etCallsign.toString().equals("") || etCallsign.toString().length() == 0) {

                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();     //닫기
                                }
                            });
                            alert.setMessage("regist your CallSign first");
                            alert.show();
                            return;
                        }

                        inputValue = etEdit.getText().toString();
                        if (inputValue != null && inputValue.length() > 0) {

                            final SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);

                            // request parameter 설정
                            RequestParams params = new RequestParams();

                            params.add("uuid",     gMemberInfo.getString("uuid", null));
                            params.add("teamid",   inputValue);
                            params.add("flag",     "N");

                            GCMHttpClient.get("/gcm/registteam", params, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                                    String responseMsg = new String(responseBody);

                                    Log.i(TAG, "statusCode is : " + statusCode);
                                    Log.i(TAG, "responseBody is : " + responseMsg);

                                    if (responseMsg.equals(QuickstartPreferences.TEAMS_TEAMID_ALEADY_EXISTS)) {

                                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();     //닫기
                                            }
                                        });
                                        alert.setMessage("TeamID aleady exists!!");
                                        alert.show();

                                    }
                                    if (responseMsg.equals(QuickstartPreferences.TEAMS_GCM_SEND_SUCCESS)
                                            || responseMsg.equals(QuickstartPreferences.TEAMS_MEMBERS_UPDATE_SUCCESS)) {
                                        Toast.makeText(getApplicationContext(), "team registration success", Toast.LENGTH_SHORT).show();

                                        // team 등록
                                        SharedPreferences.Editor editor = gMemberInfo.edit();
                                        editor.putString("teamid", inputValue);
                                        editor.commit();

                                        ReportingDTO reportingDTO = new ReportingDTO();
                                        reportingDTO.setUuid(gMemberInfo.getString("uuid", null));
                                        reportingDTO.setTeamid(gMemberInfo.getString("teamid", null));
                                        sqlHelper.insTeam(reportingDTO);
                                    }

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
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();     //닫기
                                }
                            });
                            alert.setMessage("input your TeamId");
                            alert.show();
                        }


                    }
                });
                // Cancel 버튼 이벤트
                dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });



                        dialog.show();

                return true;

            // Exit from this Application
            case R.id.mn_exit:

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setMessage("Press 'OK' to EXIT.\n(Tracking Service will be stopped)");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                        finish();
                    }
                });
                alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();     //닫기
                    }
                });
                alert.show();

                return true;

            // Close this Menu
            case R.id.mn_close:
                //Log.d(TAG, "onOptionsItemSelected");
                return true;
        }
        return true;
    }

    // TeamList 조회
    protected void setTeamListView() {

        SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
        final String sUuid = gMemberInfo.getString("uuid", null);

        if (sUuid != null && !sUuid.equals("") && sUuid.length() > 0) {

            List<ReportingDTO> retDto = sqlHelper.getTeamList(sUuid);

            if (retDto != null && retDto.size() > 0) {

                teamList = new String[retDto.size()];

                items = new ArrayList<String>() ;

                //Log.d(TAG, "myTeamList size is : " + retDto.size());

                for (int i = 0; i < retDto.size(); i++) {

//                    teamList[i] = retDto.get(i).getTeamid();
                    items.add(i, retDto.get(i).getTeamid());
                }

                adapter = new ArrayAdapter<String>(this, R.layout.listview_layout, R.id.text1, items);
                mTeamListView.setAdapter(adapter);
                mTeamListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                            String  itemValue    = (String) mTeamListView.getItemAtPosition(position);

                            startMapActivity(itemValue);
                        }
                });

                mTeamListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        listItemValue = (String) mTeamListView.getItemAtPosition(position);
                        listItemPosition = position;

                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.this);
                        alert_confirm.setMessage("Do you really want to delete my team : " + listItemValue).setCancelable(false).setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        // request parameter 설정
                                        RequestParams params = new RequestParams();

                                        params.add("uuid",   sUuid);
                                        params.add("teamid", listItemValue);
                                        params.add("flag",   "D");

                                        GCMHttpClient.get("/gcm/registteam", params, new AsyncHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                                                String responseMsg = new String(responseBody);

                                                Log.i(TAG, "statusCode is : " + statusCode);
                                                Log.i(TAG, "responseBody is : " + responseMsg);

                                                if (responseMsg.equals(QuickstartPreferences.TEAMS_MEMBERS_UPDATE_SUCCESS)
                                                        || responseMsg.equals(QuickstartPreferences.TEAMS_GCM_SEND_SUCCESS)
                                                        || responseMsg.equals(QuickstartPreferences.TEAMS_REVOCE_SUCCESS)) {

                                                    Toast.makeText(getApplicationContext(), "team delete success", Toast.LENGTH_SHORT).show();

                                                    int count, checked ;
                                                    count = adapter.getCount() ;

                                                    if (count > 0) {

                                                        // team 삭제
                                                        sqlHelper.delTeamAll(listItemValue);
                                                        setTeamListView(); // team삭제후 ListView 재조회

                                                        // 아이템 삭제
                                                        //items.remove(listItemPosition) ;

                                                        // listview 갱신.
                                                        //adapter.notifyDataSetChanged();
                                                    }

                                                }
                                            }

                                            @Override
                                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                                String errorMsg = error.getMessage();
                                                Throwable errorCause = error.getCause();
                                                StackTraceElement stackTraceElement[] = error.getStackTrace();

                                                Log.i(TAG, "errorMsg is : " + errorMsg);

                                                Toast.makeText(getApplicationContext(), "team delete failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }).setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 'No'
                                        return;
                                    }
                                });
                        AlertDialog alert = alert_confirm.create();
                        alert.show();

                        return true; // true:LongClick 이후 이벤트 수행하지 않음
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

    /**
     * get Universal Unique Number
     * @param mContext
     * @return
     */
    private void GetDevicesUUID(Context mContext){
        final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();

        SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = gMemberInfo.edit();
        editor.putString("uuid", deviceId);
        editor.commit();
    }


    private void checkServiceStatus() {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        boolean isInService = false;

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ReportingService.class.getName().equals(service.service.getClassName())) {

                isInService = true;
                break;
            }
        }

        if (isInService) {
            mStatusText.setTextColor(Color.RED);
        } else {
            mStatusText.setTextColor(Color.WHITE);
        }
    }


}
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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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

public class SetupActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "SetupActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private Button mRegistrationButton;
    private Button mTeamJoinButton;
    private Button mTeamMakeButton;
    private Spinner mSpinner;
    ArrayList<ItemData> listSpinnerItem = new ArrayList<>();

    private EditText mCallsignEditText;
    private EditText mTeamidEditText;
    private EditText mMakeTeamidEditText;

    SQLiteHelper sqlHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registBroadcastReceiver();

        setContentView(R.layout.activity_setup);

        sqlHelper = new SQLiteHelper(SetupActivity.this, null, SQLiteHelper.dbVersion);

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

        // Callsign
        mCallsignEditText = (EditText) findViewById(R.id.et_callsign);
        mCallsignEditText.setPrivateImeOptions("defaultInputmode=english;");
        mCallsignEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Team make
        mMakeTeamidEditText = (EditText) findViewById(R.id.et_maketeamid);
        mMakeTeamidEditText.setPrivateImeOptions("defaultInputmode=english;");
        mMakeTeamidEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Team Join
        mTeamidEditText   = (EditText) findViewById(R.id.et_teamid);
        mTeamidEditText.setPrivateImeOptions("defaultInputmode=english;");
        mTeamidEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // 입력문자 필터 생성
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

        // 필터 적용
        mCallsignEditText.setFilters(new InputFilter[]{alphanumFilter, maxLengthFilter});
        mMakeTeamidEditText.setFilters(new InputFilter[]{alphanumFilter, maxLengthFilter});
        mTeamidEditText.setFilters(new InputFilter[]{alphanumFilter, maxLengthFilter});

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

        }

        // CallSign 적용
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

                    AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });
                    alert.setMessage("CallSign을 입력하세요.");
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

        // Team Make 생성
        mTeamMakeButton = (Button) findViewById(R.id.btn_teammake);
        mTeamMakeButton.setOnClickListener(new View.OnClickListener() {
            /**
             * 버튼을 클릭하면 토큰을 가져오는 getInstanceIdToken() 메소드를 실행한다.
             *
             * @param view
             */
            @Override
            public void onClick(View view) {

                // 키보드 숨기기
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(mMakeTeamidEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (!isCallSignRegisted()) {
                    return;
                }

                if (mMakeTeamidEditText.getText() != null && mMakeTeamidEditText.getText().toString().length() > 0) {

                    final SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);

                    // request parameter 설정
                    RequestParams params = new RequestParams();

                    params.add("uuid",   gMemberInfo.getString("uuid", null));
                    params.add("teamid", mMakeTeamidEditText.getText().toString());
                    params.add("flag",   "N");

                    GCMHttpClient.get("/gcm/registteam", params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            String responseMsg = new String(responseBody);

                            Log.i(TAG, "statusCode is : " + statusCode);
                            Log.i(TAG, "responseBody is : " + responseMsg);

                            if (responseMsg.equals(QuickstartPreferences.TEAMS_TEAMID_ALEADY_EXISTS)) {

                                AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();     //닫기
                                    }
                                });
                                alert.setMessage("동일한 팀ID가 존재합니다!");
                                alert.show();

                            }
                            if (responseMsg.equals(QuickstartPreferences.TEAMS_GCM_SEND_SUCCESS)
                                    || responseMsg.equals(QuickstartPreferences.TEAMS_MEMBERS_UPDATE_SUCCESS)) {
                                Toast.makeText(getApplicationContext(), "팀 생성 성공", Toast.LENGTH_SHORT).show();

                                // team 등록
                                SharedPreferences.Editor editor = gMemberInfo.edit();
                                editor.putString("teamid", mMakeTeamidEditText.getText().toString());
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

                            Toast.makeText(getApplicationContext(), "팀 생성 실패(오류발생)", Toast.LENGTH_SHORT).show();

                        }
                    });

                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });
                    alert.setMessage("팀ID를 입력하세요.");
                    alert.show();
                }
            }
        });

        // TEAM JOIN
        mTeamJoinButton = (Button) findViewById(R.id.btn_teamjoin);
        mTeamJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 키보드 숨기기
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(mTeamidEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                if (!isCallSignRegisted()) {
                    return;
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

                                AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();     //닫기
                                    }
                                });
                                alert.setMessage("팀ID가 존재하지 않습니다.!!");
                                alert.show();
                            }

                            if (responseMsg.equals(QuickstartPreferences.TEAMS_MEMBERS_UPDATE_SUCCESS)
                                    || responseMsg.equals(QuickstartPreferences.TEAMS_GCM_SEND_SUCCESS)
                                    || responseMsg.equals(QuickstartPreferences.TEAMS_GCM_SEND_ERROR)) {

                                Toast.makeText(getApplicationContext(), "팀 참가 성공", Toast.LENGTH_SHORT).show();

                                // team 등록
//                                SharedPreferences gMemberInfo = getSharedPreferences("gMemberInfo", MODE_PRIVATE);
//                                SharedPreferences.Editor editor = gMemberInfo.edit();
//                                editor.putString("teamid", mTeamidEditText.getText().toString());
//                                editor.commit();

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
                                        memberDto.setTeamid(mTeamidEditText.getText().toString());
                                        sqlHelper.insTeam(memberDto);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                // teamid 삭제 후 리스트 재조회
                                mTeamidEditText.setText("");
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                            String errorMsg = error.getMessage();
                            Throwable errorCause = error.getCause();
                            StackTraceElement stackTraceElement[] = error.getStackTrace();

                            Log.i(TAG, "errorMsg is : " + errorMsg);

                            Toast.makeText(getApplicationContext(), "팀 참가 실패", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();     //닫기
                        }
                    });
                    alert.setMessage("팀ID를 입력하세요.");
                    alert.show();
                }
            }
        });
    }

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
                                Toast.makeText(getApplicationContext(), "CallSign 등록 성공", Toast.LENGTH_SHORT).show();
                            } else {
                                AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
                                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();     //닫기
                                    }
                                });
                                alert.setMessage("나중에 다시 시도하세요!!\n(통신상태 확인)");
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

                            Toast.makeText(getApplicationContext(), "CallSign 등록 실패", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

            }
        };
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

    private boolean isCallSignRegisted() {

        boolean isCallSignRegisted = false;

        Editable etCallsign = mCallsignEditText.getText();

        if (etCallsign == null || etCallsign.toString().equals("") || etCallsign.toString().length() == 0) {

            AlertDialog.Builder alert = new AlertDialog.Builder(SetupActivity.this);
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();     //닫기
                }
            });
            alert.setMessage("CallSign을 먼저 등록하세요.");
            alert.show();

            isCallSignRegisted = false;

        } else {

            isCallSignRegisted = true;
        }

        return isCallSignRegisted;
    }

}

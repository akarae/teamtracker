package kr.co.teamtracker.gcm;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import kr.co.teamtracker.R;
import kr.co.teamtracker.utils.MemberInfo;
import kr.co.teamtracker.utils.ReportingDTO;
import kr.co.teamtracker.utils.SQLiteHelper;

/**
 * Created by saltfactory on 6/8/15.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegistrationIntentService";

    SQLiteHelper sqlHelper;

    public RegistrationIntentService() {
        super(TAG);
    }

    /**
     * GCM을 위한 Instance ID의 토큰을 생성하여 가져온다.
     * @param intent
     */
    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {

        // SQLite test
        sqlHelper = new SQLiteHelper(RegistrationIntentService.this, null, SQLiteHelper.dbVersion);

        Log.i(TAG, "ResitrationIntentService called!!!");

        // GCM Instance ID의 토큰을 가져오는 작업이 시작되면 LocalBoardcast로 GENERATING 액션을 알려 ProgressBar가 동작하도록 한다.
//        LocalBroadcastManager.getInstance(this)
//                .sendBroadcast(new Intent(QuickstartPreferences.REGISTRATION_GENERATING));

        // GCM을 위한 Instance ID를 가져온다.
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            synchronized (TAG) {
                // GCM 앱을 등록하고 획득한 설정파일인 google-services.json을 기반으로 SenderID를 자동으로 가져온다.
                String default_senderId = getString(R.string.gcm_defaultSenderId);
                // GCM 기본 scope는 "GCM"이다.
                String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                // Instance ID에 해당하는 토큰을 생성하여 가져온다.
                token = instanceID.getToken(default_senderId, scope, null);

                Log.i(TAG, "GCM Registration Token: " + token);

                // 전역변수 선언
                MemberInfo memberInfo = (MemberInfo) getApplicationContext();
                memberInfo.setTokenid(token);

                // SQLite 등록처리 및 WebSerrver 등록
                ReportingDTO reportingDTO = new ReportingDTO();
                reportingDTO.setUuid(memberInfo.getUuid());
                reportingDTO.setCallsign(memberInfo.getCallsign());
                reportingDTO.setTeamid(memberInfo.getTeamid());
                reportingDTO.setStatus(memberInfo.getStatus());
                reportingDTO.setColor(memberInfo.getColor());

                // 조회결과가 존재하지 않는 경우에만 insert
                ReportingDTO retDTO = sqlHelper.getReporting(memberInfo.getUuid());
                if (retDTO == null || retDTO.getCallsign() == null) {

                    sqlHelper.insReporting(reportingDTO);

                // 조회결과가 존재하는 경우에는 update
                } else {

                    sqlHelper.setReporting(reportingDTO);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // GCM Instance ID에 해당하는 토큰을 획득하면 LocalBoardcast에 COMPLETE 액션을 알린다.
        // 이때 토큰을 함께 넘겨주어서 UI에 토큰 정보를 활용할 수 있도록 했다.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
//        registrationComplete.putExtra("token", token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}


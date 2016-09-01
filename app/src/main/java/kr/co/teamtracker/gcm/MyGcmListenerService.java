package kr.co.teamtracker.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import kr.co.teamtracker.MainActivity;
import kr.co.teamtracker.MapsActivity;
import kr.co.teamtracker.R;
import kr.co.teamtracker.utils.ReportingDTO;
import kr.co.teamtracker.utils.SQLiteHelper;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     *
     * @param from SenderID 값을 받아온다.
     * @param data Set형태로 GCM으로 받은 데이터 payload이다.
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {

        String title   = data.getString("title");
        String message = data.getString("message");

        String sTokenid    = new String();
        String sCallsign   = new String();
        String sTeamid     = new String();
        String sStatus     = new String();
        Double sLat        = 0.0;
        Double sLang       = 0.0;
        String sReporttime = new String();
        String sDirection  = new String();
        String sSpeed      = new String();
        String sColor      = "169fed";
        String sFlag       = new String();

        try {

            JSONObject jsonObj = new JSONObject(data.getString("custom_key1"));

            sFlag       = jsonObj.getString("flag"); // R:Reporting, T:TeamRegist, D:TeamDelete

            if (sFlag.equals("R")) { // R:Reporting, T:TeamRegist, D:TeamDelete
                sTokenid    = jsonObj.getString("tokenid");
                sCallsign   = jsonObj.getString("callsign");
                sStatus     = jsonObj.getString("status");
                sLat        = jsonObj.getDouble("lat");
                sLang       = jsonObj.getDouble("lang");
                sReporttime = jsonObj.getString("reporttime");
                sDirection  = jsonObj.getString("direction");
                sSpeed      = jsonObj.getString("speed");
                sColor      = jsonObj.getString("color");
            }

            if (sFlag.equals("T") || sFlag.equals("D")) { // R:Reporting, T:TeamRegist, D:TeamDelete
                sTokenid    = jsonObj.getString("tokenid");
                sTeamid     = jsonObj.getString("teamid");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "From: "        + from);
        Log.d(TAG, "Title: "       + title);
        Log.d(TAG, "Message: "     + message);
        Log.d(TAG, "callsign: "    + sCallsign);
        Log.d(TAG, "teamid: "      + sTeamid);
        Log.d(TAG, "sTokenid: "    + sTokenid);
        Log.d(TAG, "sLat: "        + sLat);
        Log.d(TAG, "sLang: "       + sLang);
        Log.d(TAG, "sReporttime: " + sReporttime);
        Log.d(TAG, "sDirection: "  + sDirection);
        Log.d(TAG, "sSpeed: "      + sSpeed);
        Log.d(TAG, "sStatus: "     + sStatus);
        Log.d(TAG, "sColor:"       + sColor);

        // GCM으로 받은 메세지를 디바이스에 알려주는 sendNotification()을 호출한다.
        // todo
        // 신규멤버가 들어올 때만 noti 처리하도록 분기
//        sendNotification(title, message);

        // SQLite Update 처리
        SQLiteHelper sqlHelper = new SQLiteHelper(MyGcmListenerService.this, null, SQLiteHelper.dbVersion);

        // Reporting
        ReportingDTO dto = new ReportingDTO();

        if (sFlag.equals("R")) { // R:Reporting, T:TeamRegist, D:TeamDelete
            dto.setTokenid(sTokenid);
            dto.setTeamid(sTeamid);
            dto.setCallsign(sCallsign);
            dto.setLat(sLat);
            dto.setLang(sLang);
            dto.setReporttime(sReporttime);
            dto.setSpeed(sSpeed);
            dto.setDirection(sDirection);
            dto.setStatus(sStatus);
            dto.setColor(sColor);

            sqlHelper.insReporting(dto);
        }

        if (sFlag.equals("T")) { // R:Reporting, T:TeamRegist, D:TeamDelete
            dto.setTokenid(sTokenid);
            dto.setTeamid(sTeamid);

            sqlHelper.insTeam(dto);
        }

        if (sFlag.equals("D")) {
            dto.setTokenid(sTokenid);
            dto.setTeamid(sTeamid);

            sqlHelper.delTeamOne(dto);
        }
    }


    /**
     * 실제 디바에스에 GCM으로부터 받은 메세지를 알려주는 함수이다. 디바이스 Notification Center에 나타난다.
     * @param title
     * @param message
     */
    private void sendNotification(String title, String message) {

        Intent intent = new Intent(this, MapsActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] vibratePattern =  new long[] { -1 };

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.m_169fed)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(vibratePattern)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // noti 처리
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}

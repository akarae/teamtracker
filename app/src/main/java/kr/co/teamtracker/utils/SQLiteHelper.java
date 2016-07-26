package kr.co.teamtracker.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akarae on 2016-07-12.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLiteHelper";

    public static final int dbVersion = 5;

    // 안드로이드에서 SQLite 데이터 베이스를 쉽게 사용할 수 있도록 도와주는 클래스
    public SQLiteHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // 최초에 데이터베이스가 없을경우, 데이터베이스 생성을 위해 호출됨
        // 테이블 생성하는 코드를 작성한다
        String sql = "CREATE TABLE table_reportings("
                                                    + "seq integer primary key autoincrement, "
                                                    + "tokenid text,"
                                                    + "lat double, "
                                                    + "lang double, "
                                                    + "callsign text, "
                                                    + "direction text, "
                                                    + "speed text,"
                                                    + "reporttime text,"
                                                    + "status text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스의 버전이 바뀌었을 때 호출되는 콜백 메서드
        // 버전 바뀌었을 때 기존데이터베이스를 어떻게 변경할 것인지 작성한다
        // 각 버전의 변경 내용들을 버전마다 작성해야함
        String sql = "DROP TABLE IF EXISTS table_reportings"; // 테이블 드랍
        db.execSQL(sql);
        onCreate(db); // 다시 테이블 생성
    }

    // drop table
    public void dropTable(String arg) {
        String sql = "DROP TABLE " + arg; // 테이블 드랍
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);
    }

    // insert
    public void insReporting(ReportingDTO reportingDTO) {

        ContentValues values = new ContentValues();
        values.put("tokenid",    reportingDTO.getTokenid());
        values.put("callsign",   reportingDTO.getCallsign());
        values.put("lat",        reportingDTO.getLat());
        values.put("lang",       reportingDTO.getLang());
        values.put("reporttime", reportingDTO.getReporttime());
        values.put("speed",      reportingDTO.getSpeed());
        values.put("direction",  reportingDTO.getDirection());
        values.put("status",     reportingDTO.getStatus());

        SQLiteDatabase db = this.getWritableDatabase();

        long lRet = db.insert("table_reportings", null, values);
        Log.d(TAG, "lRet is : " + lRet);
        db.close();
    }

    // update
    public void setReporting(ReportingDTO reportingDTO) {

        ContentValues values = new ContentValues();
        values.put("lat",        reportingDTO.getLat());
        values.put("lang",       reportingDTO.getLang());
        values.put("reporttime", reportingDTO.getReporttime());
        values.put("speed",      reportingDTO.getSpeed());
        values.put("direction",  reportingDTO.getDirection());
        values.put("status",     reportingDTO.getStatus());

        SQLiteDatabase db = this.getWritableDatabase();

        db.update("table_reportings", values, "tokenid=?", new String[]{reportingDTO.getTokenid()});
    }


    // delete
    public boolean delReporting(String tokenID) {

        boolean result = false;

        String query = "SELECT * FROM table_reportings WHERE tokenid = \"" + tokenID + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            cursor.getString(0);

            db.delete("table_reportings", "tokenid = ?", new String[]{"1234"});
            cursor.close();;
            result = true;
        }

        db.close();
        return result;
    }

    // delete
    public void delReportingAll() {

        boolean result = false;

        String query = "DELETE FROM table_reportings";

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(query);
        db.close();
    }

    // select
    public ReportingDTO getReporting(String tokenid) {

        ReportingDTO retDTO = new ReportingDTO();

        String query = "SELECT tokenid, lat, lang, callsign, reporttime, speed, direction, status FROM table_reportings WHERE tokenid = \"" + tokenid + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            retDTO.setTokenid(cursor.getString(0));
            retDTO.setLat(cursor.getDouble(1));
            retDTO.setLang(cursor.getDouble(2));
            retDTO.setCallsign(cursor.getString(3));
            retDTO.setReporttime(cursor.getString(4));
            retDTO.setSpeed(cursor.getString(5));
            retDTO.setDirection(cursor.getString(6));
            retDTO.setStatus(cursor.getString(7));

            cursor.close();
        } else {


        }

        db.close();

        return retDTO;
    }

    // select
    public List<ReportingDTO> getReportingAll() {

        List<ReportingDTO> dtoList = new ArrayList<ReportingDTO>();

        String query = "SELECT tokenid, lat, lang, callsign, reporttime, speed, direction, status FROM table_reportings ORDER BY tokenid DESC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        Log.d(TAG, "getReportingAll result count is : " + cursor.getCount());


        while( cursor.moveToNext() ) {
            ReportingDTO retDTO = new ReportingDTO();
            retDTO.setTokenid(cursor.getString(0));
            retDTO.setLat(cursor.getDouble(1));
            retDTO.setLang(cursor.getDouble(2));
            retDTO.setCallsign(cursor.getString(3));
            retDTO.setReporttime(cursor.getString(4));
            retDTO.setSpeed(cursor.getString(5));
            retDTO.setDirection(cursor.getString(6));
            retDTO.setStatus(cursor.getString(7));
            dtoList.add(retDTO);
        }

        db.close();

        return dtoList;
    }
}
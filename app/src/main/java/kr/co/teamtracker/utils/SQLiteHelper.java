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

    private static final String DBFilename = "teamtrackerdb";

    public static final int dbVersion = 18;

    // 안드로이드에서 SQLite 데이터 베이스를 쉽게 사용할 수 있도록 도와주는 클래스
    public SQLiteHelper(Context context, CursorFactory factory, int version) {

        super(context, DBFilename, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // 최초에 데이터베이스가 없을경우, 데이터베이스 생성을 위해 호출됨
        // 테이블 생성하는 코드를 작성한다
        String sql1 = "CREATE TABLE table_reportings ("
                //+ "seq integer primary key autoincrement, "
                + "uuid       text primary key,"
                + "lat        double, "
                + "lang       double, "
                + "callsign   text, "
                + "direction  text, "
                + "speed      text,"
                + "reporttime text,"
                + "status     text, "
                //+ "teamid     text, "
                + "color      text, "
                + "msg        text)";
        db.execSQL(sql1);

        String sql2 = "CREATE UNIQUE INDEX idx_reportings ON table_reportings (uuid)";
        db.execSQL(sql2);

        String sql3 = "CREATE TABLE table_team("
                //+ "seq integer primary key autoincrement, "
                + "teamkey  text primarykey,"
                + "teamid   text,"
                + "uuid     text,"
                + "goallat  double,"
                + "goallang double )";
        db.execSQL(sql3);

        String sql4 = "CREATE UNIQUE INDEX idx_team ON table_team (teamkey)";
        db.execSQL(sql4);
        //db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스의 버전이 바뀌었을 때 호출되는 콜백 메서드
        // 버전 바뀌었을 때 기존데이터베이스를 어떻게 변경할 것인지 작성한다
        // 각 버전의 변경 내용들을 버전마다 작성해야함
        String sql = "DROP TABLE IF EXISTS table_reportings"; // 테이블 드랍
        db.execSQL(sql);

//        String sql2 = "DROP INDEX table_reportings.idx_reportings";
//        db.execSQL(sql2);

        String sql3 = "DROP TABLE IF EXISTS table_team"; // 테이블 드랍
        db.execSQL(sql3);

//        String sql4 = "DROP INDEX table_team.idx_team";
//        db.execSQL(sql4);

        onCreate(db); // 다시 테이블 생성
        //db.close();
    }

    // drop table
    public void dropTable(String arg) {
        String sql = "DROP TABLE " + arg; // 테이블 드랍
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);
        //db.close();
    }

    // insert
    public void insReporting(ReportingDTO reportingDTO) {

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "INSERT OR REPLACE INTO table_reportings "
                + "(uuid, callsign, lat, lang, reporttime, speed, direction, status, color, msg) "
                + "VALUES "
                + "(\""
                + reportingDTO.getUuid() + "\", \""
                + reportingDTO.getCallsign() + "\", \""
                + reportingDTO.getLat() + "\", \""
                + reportingDTO.getLang() + "\", \""
                + reportingDTO.getReporttime() + "\", \""
                + reportingDTO.getSpeed() + "\", \""
                + reportingDTO.getDirection() + "\", \""
                + reportingDTO.getStatus() + "\", \""
                + reportingDTO.getColor()  + "\", \""
                + reportingDTO.getMsg() + "\")";

        db.execSQL(query);

        //db.close();
    }

    // update
    public void setReporting(ReportingDTO reportingDTO) {

        ContentValues values = new ContentValues();
        if (reportingDTO.getCallsign() != null)   values.put("callsign",   reportingDTO.getCallsign());
        if (reportingDTO.getLat() != null)        values.put("lat",        reportingDTO.getLat());
        if (reportingDTO.getLang() != null)       values.put("lang",       reportingDTO.getLang());
        if (reportingDTO.getReporttime() != null) values.put("reporttime", reportingDTO.getReporttime());
        if (reportingDTO.getSpeed() != null)      values.put("speed",      reportingDTO.getSpeed());
        if (reportingDTO.getDirection() != null)  values.put("direction",  reportingDTO.getDirection());
        if (reportingDTO.getStatus() != null)     values.put("status",     reportingDTO.getStatus());
        if (reportingDTO.getColor() != null)      values.put("color",      reportingDTO.getColor());
        if (reportingDTO.getMsg() != null)        values.put("msg",        reportingDTO.getMsg());

        SQLiteDatabase db = this.getWritableDatabase();

        db.update("table_reportings", values, "uuid=?", new String[]{reportingDTO.getUuid()});
        //db.close();
    }


    // delete
    public boolean delReporting(String uuid) {

        boolean result = false;

        String query = "SELECT * FROM table_reportings WHERE uuid = \"" + uuid + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            cursor.getString(0);

            db.delete("table_reportings", "uuid = ?", new String[]{uuid});
            cursor.close();;
            result = true;
        }

        //db.close();
        return result;
    }

    // delete
    public void delReportingAll() {

        boolean result = false;

        String query = "DELETE FROM table_reportings";

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(query);
        //db.close();
    }

    // select
    public ReportingDTO getReporting(String uuid) {

        ReportingDTO retDTO = new ReportingDTO();

        String query = "SELECT uuid, lat, lang, callsign, reporttime, speed, direction, status, color, msg FROM table_reportings WHERE uuid = \"" + uuid + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            retDTO.setUuid(cursor.getString(0));
            retDTO.setLat(cursor.getDouble(1));
            retDTO.setLang(cursor.getDouble(2));
            retDTO.setCallsign(cursor.getString(3));
            retDTO.setReporttime(cursor.getString(4));
            retDTO.setSpeed(cursor.getString(5));
            retDTO.setDirection(cursor.getString(6));
            retDTO.setStatus(cursor.getString(7));
            retDTO.setColor(cursor.getString(8));
            retDTO.setMsg(cursor.getString(9));

            cursor.close();
        } else {


        }

        //db.close();

        return retDTO;
    }

    // select
    public List<ReportingDTO> getReportingByTeamid(String teamid) {

        List<ReportingDTO> dtoList = new ArrayList<ReportingDTO>();

        String query = "SELECT " +
                "    a.uuid, a.lat, a.lang, a.callsign, a.reporttime, a.speed, a.direction, a.status, b.teamid, a.color, b.goallat, b.goallang, a.msg " +
                "FROM " +
                "    table_reportings a, " +
                "    table_team b " +
                "WHERE " +
                "    b.uuid    = a.uuid " +
                "AND b.teamid  = \"" + teamid + "\" " +
                "ORDER BY a.callsign ASC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        while( cursor.moveToNext() ) {

            //Log.d(TAG, cursor.getString(0));

            ReportingDTO retDTO = new ReportingDTO();
            retDTO.setUuid(cursor.getString(0));
            retDTO.setLat(cursor.getDouble(1));
            retDTO.setLang(cursor.getDouble(2));
            retDTO.setCallsign(cursor.getString(3));
            retDTO.setReporttime(cursor.getString(4));
            retDTO.setSpeed(cursor.getString(5));
            retDTO.setDirection(cursor.getString(6));
            retDTO.setStatus(cursor.getString(7));
            retDTO.setTeamid(cursor.getString(8));
            retDTO.setColor(cursor.getString(9));
            retDTO.setGoallat(cursor.getDouble(10));
            retDTO.setGoallang(cursor.getDouble(11));
            retDTO.setMsg(cursor.getString(12));
            dtoList.add(retDTO);
        }
        cursor.close();

        //db.close();

        return dtoList;
    }

    // select
    public List<ReportingDTO> getTeamList(String uuid) {

        List<ReportingDTO> dtoList = new ArrayList<ReportingDTO>();

        String query = "SELECT uuid, teamid FROM table_team WHERE uuid = \"" + uuid + "\" ORDER BY teamid ASC";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);

        while( cursor.moveToNext() ) {
            ReportingDTO retDTO = new ReportingDTO();
            retDTO.setUuid(cursor.getString(0));
            retDTO.setTeamid(cursor.getString(1));
            dtoList.add(retDTO);
        }
        cursor.close();

        //db.close();

        return dtoList;
    }

    /**
     * insert before select
     * @param reportingDTO
     */
    public void insTeam(ReportingDTO reportingDTO) {

        // select before insert
        String uuid     = reportingDTO.getUuid();
        String teamid   = reportingDTO.getTeamid();
        Double goallat  = reportingDTO.getGoallat();
        Double goallang = reportingDTO.getGoallang();

        SQLiteDatabase db = this.getWritableDatabase();

        String query = "INSERT OR REPLACE INTO table_team "
                + "(teamkey, teamid, uuid, goallat, goallang) "
                + "VALUES "
                + "(\"" + teamid + uuid + "\", \"" + teamid + "\", \"" + uuid + "\", \"" + goallat + "\", \"" + goallang + "\")";


        db.execSQL(query);

        //db.close();
    }

    // update
    public void setTeamGoal(ReportingDTO reportingDTO) {

        ContentValues values = new ContentValues();
        values.put("goallat",        reportingDTO.getGoallat());
        values.put("goallang",       reportingDTO.getGoallang());

        SQLiteDatabase db = this.getWritableDatabase();

        db.update("table_team", values, "teamid=?", new String[]{reportingDTO.getTeamid()});
        //db.close();
    }

    // deleteAll
    public void delTeamAll(String teamId) {

        String query = "DELETE FROM table_team WHERE teamid = \"" + teamId + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(query);
        //db.close();
    }

    // deleteOne
    public void delTeamOne(ReportingDTO dto) {

        String query = "DELETE FROM table_team WHERE teamid = \"" + dto.getTeamid() + "\" " + "AND uuid = \"" + dto.getUuid() + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(query);
        //db.close();
    }
}

package kr.co.teamtracker.utils;

import android.app.Application;

/**
 * Created by akarae on 2016-08-03.
 */
public class MemberInfo extends Application {

    private String tokeinid;

    private String callsign;

    private String reporttime;

    private String color;

    private String teamid;

    private String status;

    public String getStatus() {
        return status;
    }

    public String getTokeinid() {
        return tokeinid;
    }

    public void setTokeinid(String tokeinid) {
        this.tokeinid = tokeinid;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getReporttime() {
        return reporttime;
    }

    public void setReporttime(String reporttime) {
        this.reporttime = reporttime;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTeamid() {
        return teamid;
    }

    public void setTeamid(String teamid) {
        this.teamid = teamid;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

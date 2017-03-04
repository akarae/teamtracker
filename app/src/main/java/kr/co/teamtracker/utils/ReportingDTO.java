package kr.co.teamtracker.utils;

/**
 * Created by akarae on 2016-07-14.
 */
public class ReportingDTO {

    private int seq;

    private String uuid;

    private Double lat;

    private Double lang;

    private String callsign;

    private String reporttime;

    private String speed;

    private String direction;

    private String status;

    private String color;

    private Double goallat;

    private Double goallang;

    private String msg;

    private int teamcnt;

    public int getTeamcnt() {
        return teamcnt;
    }

    public void setTeamcnt(int teamcnt) {
        this.teamcnt = teamcnt;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getColor() {
        return color;
    }

    public Double getGoallat() {
        return goallat;
    }

    public void setGoallat(Double goallat) {
        this.goallat = goallat;
    }

    public Double getGoallang() {
        return goallang;
    }

    public void setGoallang(Double goallang) {
        this.goallang = goallang;
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

    private String teamid;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLang() {
        return lang;
    }

    public void setLang(Double lang) {
        this.lang = lang;
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

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

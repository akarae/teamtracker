package kr.co.teamtracker.gcm;

/**
 * Created by akarae on 2016-03-04.
 */
public class QuickstartPreferences {
    public static final String REGISTRATION_COMPLETE = "registrationComplete";

    // RerportingService 내에서 처리되는 결과값
    //reporting MONGODB members.findOne err
    public static final String GCM_MEMBERS_FINDONE_ERROR = "GCM_MEMBERS_FINDONE_ERROR";

    //reporting MONGODB members.save err
    public static final String GCM_MEMBERS_SAVE_ERROR = "GCM_MEMBERS_SAVE_ERROR";

    //reporting MONGODB members.find err
    public static final String GCM_REPORTING_FIND_ERROR = "GCM_REPORTING_FIND_ERROR";

    //reporting MONGODB members.find end
    public static final String GCM_REPORTING_FIND_SUCCESS = "GCM_REPORTING_FIND_SUCCESS";

    //gcm.send err
    public static final String GCM_REPORTING_SEND_ERROR = "GCM_REPORTING_SEND_ERROR";

    //gcm.send successs
    public static final String GCM_REPORTING_SEND_SUCCESS = "GCM_REPORTING_SEND_SUCCESS";
    // RerportingService 내에서 처리되는 결과값


    // CallSign Apply 시 처리되는 결과값
    //members.findOne err
    public static final String MEMBERS_FINDONE_ERROR = "MEMBERS_FINDONE_ERROR";

    //members.save err
    public static final String MEMBERS_SAVE_UPDATE_ERROR = "MEMBERS_SAVE(UPDATE)_ERROR";

    //members.save end
    public static final String MEMBERS_SAVE_UPDATE_SUCCESS = "MEMBERS_SAVE(UPDATE)_SUCCESS";

    //members.save err
    public static final String MEMBERS_SAVE_INSERT_ERROR = "MEMBERS_SAVE(INSERT)_ERROR";

    //members.save end
    public static final String MEMBERS_SAVE_INSERT_SUCCESS = "MEMBERS_SAVE(INSERT)_SUCCESS";
    // CallSign Apply 시 처리되는 결과값


    // TeamId Apply시 처리되는 결과값
    //teams.findOne err
    public static final String TEAMS_FINDONE_ERROR = "TEAMS_FINDONE_ERROR";

    //-- exists
    //teams teamid aleady exists
    public static final String TEAMS_TEAMID_ALEADY_EXISTS = "TEAMS_TEAMID_ALEADY_EXISTS";

    //teams members.update err
    public static final String TEAMS_MEMBERS_UPDATE_ERROR = "TEAMS_MEMBERS_UPDATE_ERROR";

    //teams members.update end
    public static final String TEAMS_MEMBERS_UPDATE_SUCCESS = "TEAMS_MEMBERS_UPDATE_SUCCESS";

    //teams gcm members.find err
    public static final String TEAMS_GCM_MEMBERS_FIND_ERROR = "TEAMS_GCM_MEMBERS_FIND_ERROR";

    //teams gcm send err
    public static final String TEAMS_GCM_SEND_ERROR = "TEAMS_GCM_SEND_ERROR";

    //teams gcm send successs
    public static final String TEAMS_GCM_SEND_SUCCESS = "TEAMS_GCM_SEND_SUCCESS";

    // teams remove err
    public static final String TEAMS_REMOVE_ERROR = "TEAMS_REMOVE_ERROR";

    // temas remove success
    public static final String TEAMS_REVOCE_SUCCESS = "TEAMS_REVOCE_SUCCESS";

    //-- not exists
    //teams teams.save err
    public static final String TEAMS_SAVE_INSERT__ERROR = "TEAMS_SAVE(INSERT)_ERROR";

    //teams members.update err
    //public static final String TEAMS_MEMBERS_UPDATE_ERROR = "TEAMS_MEMBERS_UPDATE_ERROR";

    //teams members.update success
    //public static final String TEAMS_MEMBERS_UPDATE_SUCCESS = "TEAMS_MEMBERS_UPDATE_SUCCESS";

    //teams teams.findOne noResult
    public static final String TEAMS_TEAMID_NOT_EXISTS = "TEAMS_TEAMID_NOT_EXISTS";
    // TeamId Apply시 처리되는 결과값
}

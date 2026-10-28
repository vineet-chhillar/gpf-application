package com.example.backend.constants;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface Constants {
    int STATUS_INT_ERROR = 0;
    int STATUS_INT_SUCCESS = 1;


    String ALERT_DANGER = "alert alert-danger";
    String ALERT_SUCCESS = "alert alert-success";
    String ALERT_INFO = "alert alert-info";

    String ACTION_UPDATE_RO_CO_GROUP = "Update GroupWise";
    String ACTION_UPDATE_EMP_INFO = "Update Individually";

    String SIGN_REQUEST_TYPE_SINGLE = "SINGLE";
    String SIGN_REQUEST_TYPE_MULTI = "MULTI";

    String ESIGN_INITIATED = "ESIGN_INITIATED";
    String ESIGN_REQUEST_DOCIDGEN = "ESIGN_REQUEST_DOCIDGEN";
    String ESIGN_RESPONSE_FROM_SERVICE = "ESIGN_RESPONSE_FROM_SERVICE";
    String ESIGN_REQUEST_GETSIGNEDDOC = "ESIGN_REQUEST_GETSIGNEDDOC";
    String ESIGN_COMPLETED = "ESIGN_COMPLETED";

    String STATUS_OK = "OKAY";
    String STATUS_ERROR = "ERROR";

    int DOCIDGEN_FILE_ERROR = -6;
    int DOCIDGEN_AWRSTATUSINVALID = -5;
    int DOCIDGEN_ALREADYSIGNED = -4;
    int DOCIDGEN_INVALIDREQUEST = -3;
    int DOCIDGEN_FILENOTFOUND = -2;
    int DOCIDGEN_EXCEPTION = -1;

    String DOC_TYPE_IND_UNSIGNED = "IND_UNSIGNED";
    String DOC_TYPE_IND_SIGNED = "IND_SIGNED";
    String DOC_TYPE_NOTIFICATION = "NOTIFICATION";
    Long INDIA =1L;

    long LEAVE_APP_SERVICE_ID = 19l;
    long LEAVE_CANCEL_SERVICE_ID = 31l;
    long LEAVE_EXT_SERVICE_ID = 30l;
    long LEAVE_JOIN_SERVICE_ID = 32l;
    long STATION_LEAVE_SERVICE_ID = 47l;
    long LEAVE_STATION_JOINING = 84L;
    long LEAVE_NOTIFICATION_SERVICE_ID = 85L;

    long PENDING_APPLICATION_PDF_GENERATION_ID = 16L;

    int EARNED_LEAVE_TYPE = 1;
    int HALF_PAY_LEAVE_TYPE = 2;
    int CASUAL_LEAVE_TYPE = 3;
    int RH_LEAVE_TYPE = 4;
    int COMMUTED_LEAVE_TYPE = 5;
    int MATERNITY_LEAVE_TYPE = 9;
    int STUDY_LEAVE_TYPE = 13;
    int SPECIAL_CASUAL_LEAVE_TYPE = 14;

    long MAX_CL_NUM = 8l;
    long GLOBAL_ORG_ID = 2l;
    long EMP_LEVEL_ID = 1l;

    String DATE_FORMAT = "dd-MM-yyyy";
    String LEAVE_GEN_PDF = "pages/gen_leave_pdf";
    String PENDING_APPLICATION_PDF = "pages/gen_pending_application_pdf";
    String SUBO_LEAVE_HIS_GEN = "pages/gen_subordinate_leave_history_pdf";

    Integer SIO_FUNCTIONAL_POST_ID = 6;
    Integer DG_FUNCTIONAL_POST_ID = 9;

    LocalDateTime APP_START_DATE = LocalDateTime.of(2019, 1, 1, 0, 0, 1);

    LocalDateTime CURRENT_YEAR_START = LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 00, 00, 01);
    LocalDateTime CURRENT_YEAR_END = LocalDateTime.of(LocalDate.now().getYear(), 12, 31, 23, 59, 59);

    LocalDate CURRENT_YEAR_START_DATE = LocalDate.of(LocalDate.now().getYear(), 1, 1);
    LocalDate CURRENT_YEAR_END_DATE = LocalDate.of(LocalDate.now().getYear(), 12, 31);

}

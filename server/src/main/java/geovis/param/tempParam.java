package geovis.param;

import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;


/**
 * 传参
 */

@Data
public class tempParam {

    private String station;
    private double year;
    private double month;
    private double day;
    private double hour;
    private double latitude;
    private double longitude;
    private double qc_ind;
    private double elapsetime;
    private double press;
    private double hgt;
    private double at;
    private double rh;
    private double td;
    private double wd;
    private double ws;
    private double leveltype;
    private String oq_et;
    private String oq_p;
    private String oq_hgt;
    private String oq_at;
    private String oq_rh;
    private String oq_td;
    private String oq_wind;
    private String reserved;
    private double q_wd;
    private double q_ws;
    private double minute;
    private Timestamp odate;
    private Date ldate;
    private double q_mean;
    private double ltime;
    private double mean;
    private double type;





}

package geovis.param;

import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * 传参
 */

@Data
public class dianCiParam {

    @ApiParam(name = "类型")
    private String requestType;
    @ApiParam(name = "最小经度")
    private String lonMin;
    @ApiParam(name = "最大经度")
    private String lonMax;
    @ApiParam(name = "最小纬度")
    private String latMin;
    @ApiParam(name = "最大纬度")
    private String latMax;
    @ApiParam(name = "最小freq")
    private String freqMin;
    @ApiParam(name = "最大freq")
    private String freqMax;
    @ApiParam(name = "低Rate")
    private String lowRate;
    @ApiParam(name = "高Rate")
    private String highRate;
    @ApiParam(name = "起始日期")
    private String beginDt;
    @ApiParam(name = "结束日期")
    private String endDt;
    //短波无线电仿真
    @ApiParam(name = "起始纬度")
    private String startLat;
    @ApiParam(name = "起始经度")
    private String startLon;
    @ApiParam(name = "结束纬度")
    private String endLat;
    @ApiParam(name = "结束经度")
    private String endLon;
    @ApiParam(name = "pt")
    private String pt;
    @ApiParam(name = "年")
    private String year;
    @ApiParam(name = "月")
    private String month;
    @ApiParam(name = "小时")
    private String hour;
    @ApiParam(name = "R12")
    private String R12;
    @ApiParam(name = "BW")
    private String BW;
    @ApiParam(name = "SNRwanted")
    private String SNRwanted;
    @ApiParam(name = "EC")
    private String EC;
    //信道占用率仿真
    @ApiParam(name = "freqUnion")
    private String freqUnion;
    //电磁场仿真
    @ApiParam(name = "txtPath")
    private String txtPath;
    @ApiParam(name = "pngPath")
    private String pngPath;


}

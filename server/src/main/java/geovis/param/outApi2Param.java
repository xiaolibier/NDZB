package geovis.param;

import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * 对外使用的参数
 */

@Data
public class outApi2Param {


    //对想定平台接口
    @ApiParam(name = "数据类型 DaQi/HaiYang/KongJian")
    private String data_type="";
    @ApiParam(name = "日期时间")
    private String date_time="";
    @ApiParam(name = "经纬度坐标")
    private String lat_lon="";

    //想定平台查询接口
    @ApiParam(name = "类型 1或2")
    private String type="";
    @ApiParam(name = "日期")
    private String dt="";
    @ApiParam(name = "纬度")
    private String lat="";
    @ApiParam(name = "经度")
    private String lon="";
    @ApiParam(name = "高度")
    private String thgt="";
    @ApiParam(name = "高度")
    private String xIndex="";
    @ApiParam(name = "高度")
    private String yIndex="";
    @ApiParam(name = "高度")
    private String depth="";
    @ApiParam(name = "距离台风中心距离")
    private String distance="";
    @ApiParam(name = "距离台风中心方位角")
    private String degree="";





}

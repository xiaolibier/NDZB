package geovis.param;

import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * 传参
 */

@Data
public class chengGuoParam {

    @ApiParam(name = "当前页数")
    private Integer current = 1;
    @ApiParam(name = "每页显示的数量")
    private Integer pageSize = 10;
    @ApiParam(name = "id")
    private String id;
    @ApiParam(name = "文件名")
    private String name;
    @ApiParam(name = "文件类型 1 高空 2 地面")
    private String type;
    @ApiParam(name = "文件类型：doc")
    private String file_type;
    @ApiParam(name = "文件大小")
    private String file_size;
    @ApiParam(name = "开始时间")
    private String start_time;
    @ApiParam(name = "结束时间")
    private String end_time;
    //
    @ApiParam(name = "目标纬度")
    private String t_lat;
    @ApiParam(name = "目标名称")
    private String t_name;
    @ApiParam(name = "目标经度")
    private String t_long;
    @ApiParam(name = "地面代站站号")
    private String s_station;
    @ApiParam(name = "地面代站站名")
    private String s_name;
    @ApiParam(name = "地面代站纬度")
    private String s_lat;
    @ApiParam(name = "地面代站经度")
    private String s_long;
    @ApiParam(name = "高空代站名")
    private String u_name;
    @ApiParam(name = "高空代站号")
    private String u_station;
    @ApiParam(name = "高空代站纬度")
    private String u_lat;
    @ApiParam(name = "高空代站经度")
    private String u_long;
    @ApiParam(name = "成果制作单位")
    private String unit;
    @ApiParam(name = "制作人")
    private String person;
    @ApiParam(name = "制作上传时间")
    private String upload_time;
    @ApiParam(name = "状态")
    private String stat;
    @ApiParam(name = "文件路径")
    private String file_url;
    @ApiParam(name = "成果富文本")
    private String t_doc;



}

package geovis.param;

import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * 传参
 */

@Data
public class dataSourceParam {

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
    @ApiParam(name = "上传时间")
    private String time;
    @ApiParam(name = "文件类型：doc")
    private String file_type;
    @ApiParam(name = "开始时间")
    private String start_time;
    @ApiParam(name = "结束时间")
    private String end_time;
    @ApiParam(name = "存储路径")
    private String url;
    @ApiParam(name = "状态")
    private String stat;
    @ApiParam(name = "文件大小")
    private String file_size;




}

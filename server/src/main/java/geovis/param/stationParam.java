package geovis.param;

import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * 传参
 */

@Data
public class stationParam {

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
    @ApiParam(name = "站点编号")
    private String station_number;




}

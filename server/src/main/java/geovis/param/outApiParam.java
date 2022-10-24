package geovis.param;

import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * 对外使用的参数
 */

@Data
public class outApiParam {

    //查询三维球使用的图片和json
    @ApiParam(name = "数据类型 DaQi HaiYang")
    private String dataType="";
    @ApiParam(name = "日期时间 20210101")
    private String dateTime="";
    @ApiParam(name = "日期时间_起")
    private String date_time_start="";
    @ApiParam(name = "日期时间_止")
    private String date_time_end="";
    @ApiParam(name = "要素名称 wind")
    private String yaoSu="";
    @ApiParam(name = "高度/深度 100 1000")
    private String height="";
    @ApiParam(name = "资源文件二级目录")
    private String filePath="L2"; //默认是解报后的目录
    //调用python 处理nc文件
    @ApiParam(name = "文件名")
    private String sourceFile="";
     @ApiParam(name = "源文件路径L1.2")
    private String sourceFilePath="";
    //对外接口 对外服务
    @ApiParam(name = "数据类型 DaQi/HaiYang")
    private String data_type="";
    @ApiParam(name = "存传过来的要素列表 yaosu,yaosu")
    private String yaoSu_array="";
    @ApiParam(name = "存传过来的高度列表 gaodu,gaodu")
    private String layer_array="";
    @ApiParam(name = "要素名称 uwnd/vwnd")
    private String factor="";
    @ApiParam(name = "日期时间 20210903")
    private String date_time="";
    @ApiParam(name = "经纬度坐标 180_0_-180_90")
    private String lot_lat="";
    @ApiParam(name = "区域范围 全球或区域")
    private String area="";
    @ApiParam(name = "资源文件二级目录")
    private String file_path="L3"; //默认是解报后NC的目录
    //电磁数据下载
    @ApiParam(name = "电磁要素名")
    private String varName="";
    @ApiParam(name = "经度范围")
    private String lonlim="";
    @ApiParam(name = "纬度范围")
    private String latlim="";
    @ApiParam(name = "频率上下限")
    private String freqlim="";





}

package geovis.param;

import io.swagger.annotations.ApiParam;
import lombok.Data;

/**
 * 传参
 */

@Data
public class userParam {

    @ApiParam(name = "id")
    private String id;
    @ApiParam(name = "用户名")
    private String name;
    @ApiParam(name = "手机号")
    private String phone;
    @ApiParam(name = "邮箱")
    private String email;
    @ApiParam(name = "地址")
    private String address;
    @ApiParam(name = "单位")
    private String company;
    @ApiParam(name = "部门")
    private String part;
    @ApiParam(name = "创建时间")
    private String create_time;
    @ApiParam(name = "登录用户名")
    private String login_name;
    @ApiParam(name = "密码")
    private String password;
    @ApiParam(name = "junzhong 存序号")
    private String junzhong;
    @ApiParam(name = "zhanqu 存序号")
    private String zhanqu;
    @ApiParam(name = "用户层级")
    private String cengji;
    @ApiParam(name = "用户激活状态")
    private String user_static;
    @ApiParam(name = "经度")
    private String lon;
    @ApiParam(name = "纬度")
    private String lat;
    //搜索项
    @ApiParam(name = "时间_起")
    private String start_time;
    @ApiParam(name = "时间_止")
    private String end_time;
    @ApiParam(name = "查询对应的数据类型 预报或实况")
    private String data_type;



}

package geovis.mapper;


import geovis.param.*;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;


@Mapper
public interface dataSourceMapper {







    //获取所有站点列表
    @Select("<script>" +
            " SELECT  * FROM ndzb.t_static_station  " +
            "    where 1=1  " +
            "    <if test=\"param.name!=null and param.name!=''\"> " +
            "      and station_number  like '%${param.name}%' " +
            "    </if> " +
            "  ORDER BY data_type desc " +
            "</script>")
    List<Map<String,String>> getStaticStation(@Param("param") stationParam param);

    //修改成果数据
    @Update("update ndzb.cg_upload_record set type=#{param.type}" +
            ",t_name=#{param.t_name},s_station=#{param.s_station},s_name=#{param.s_name},s_lat=#{param.s_lat}," +
            "s_long=#{param.s_long},u_name=#{param.u_name},u_station=#{param.u_station}," +
            "u_lat=#{param.u_lat},u_long=#{param.u_long},unit=#{param.unit},person=#{param.person}" +
            ",file_url=#{param.file_url},t_doc=#{param.t_doc},t_lat=#{param.t_lat},t_long=#{param.t_long} " +
            "where id=#{param.id}")
    void saveChengGuo(@Param("param") chengGuoParam param);
    //上传成果数据
    @Insert("insert into ndzb.cg_upload_record (id,type,stat,t_name,s_station,s_name,s_lat,s_long," +
            "u_name,u_station,u_lat,u_long,unit,person,upload_time,file_url,t_doc,t_lat,t_long) " +
            " values (#{param.id},#{param.type},#{param.stat},#{param.t_name},#{param.s_station}," +
            "#{param.s_name},#{param.s_lat},#{param.s_long},#{param.u_name}," +
            "#{param.u_station},#{param.u_lat},#{param.u_long}," +
            "#{param.unit},#{param.person},#{param.upload_time},#{param.file_url},#{param.t_doc},#{param.t_lat},#{param.t_long})")
    void uploadChengGuo(@Param("param") chengGuoParam param);

    //删除一个成果记录
    @Delete(" <script> delete from ndzb.cg_upload_record " +
            " WHERE 1=1" +
            "    <if test=\"param.id!=null and param.id!=''\"> " +
            "       and id = #{param.id}  " +
            "    </if> " +
            " </script> ")
    Integer deleteChengGuoRecordById(@Param("param") chengGuoParam param);

    //删除一个资料记录
    @Delete(" <script> delete from ndzb.zl_upload_record " +
            " WHERE 1=1" +
            "    <if test=\"param.id!=null and param.id!=''\"> " +
            "       and id = #{param.id}  " +
            "    </if> " +
            " </script> ")
    Integer deleteZiLiaoRecordById(@Param("param") dataSourceParam param);

    //条件查询文件列表
    @Select("<script>" +
            " SELECT * FROM ndzb.cg_upload_record  " +
            "    where 1=1  " +
            "    <if test=\"param.t_name!=null and param.t_name!=''\"> " +
            "      and t_name like '%${param.t_name}%' " +
            "    </if> " +
            "    <if test=\"param.unit!=null and param.unit!=''\"> " +
            "      and unit like '%${param.unit}%' " +
            "    </if> " +
            "    <if test=\"param.upload_time!=null and param.upload_time!=''\"> " +
            "      and upload_time like '%${param.upload_time}%' " +
            "    </if> " +
            "     " +
            "</script>")
    List<Map<String,String>> getChengGuoList(@Param("param") chengGuoParam param);

    //条件查询文件列表
    @Select("<script>" +
            " SELECT * FROM ndzb.zl_upload_record  " +
            "    where 1=1  " +
            "    <if test=\"param.type!=null and param.type!=''\"> " +
            "      and type = #{param.type} " +
            "    </if> " +
            "    <if test=\"param.name!=null and param.name!=''\"> " +
            "      and name like '%${param.name}%' " +
            "    </if> " +
            "    <if test=\"param.time!=null and param.time!=''\"> " +
            "      and time like '%${param.time}%' " +
            "    </if> " +
            "     " +
            "</script>")
    List<Map<String,String>> getFileList(@Param("param") dataSourceParam param);

    //文件记录入库
    @Insert("insert into ndzb.zl_upload_record (id,name,type,time,file_type,stat,file_size,url) " +
            " values (#{param.id},#{param.name},#{param.type},#{param.time},#{param.file_type},#{param.stat}," +
            "#{param.file_size},#{param.url})")
    void fileToDB(@Param("param") dataSourceParam param);

    //地面数据入库
    @Insert("insert into ndzb.ldb_his_obs_surf (year,month,day,latitude,longitude,station," +
            "hour,unitid,recievetime,markr,markw,markx,type,evelation,onlyland,tranhour,slp," +
            "lp,wd,ws,at,td,max_at,min_at,oq_slp,oq_lp,oq_wd,oq_at,w2,vis,ww,w1,n,nh,cl,h," +
            "cm,ch,dp,dp3,dp24,rain24,area,q_slp,q_lp,q_wd,q_ws,q_at,q_td,q_max_at,q_min_at," +
            "q_oq_slp,q_oq_lp,q_oq_wd,q_oq_at,q_w2,q_vis,q_ww,q_w1,q_n,q_nh,q_cl,q_h,q_cm," +
            "q_ch,q_dp,q_dp3,q_dp24,q_rain24,remark,cccc,stn_type,rh,spl,a3,dp_idx,n1,c1,h1," +
            "n2,c2,h2,n3,c3,h3,n4,c4,h4,cbn,cbt,cbh,rain01,rain02,rain03,rain06,rain09,rain12," +
            "rain15,rain18,dt24,max_at12,min_at12,snowh,eva,alr,sunp,gs,sw1,sw2,sw3,sw4,sw5,sw6," +
            "rain032,rain062,rain122,rain242,q_rh,q_spl,q_a3,q_dp03,q_dp_idx,q_n1,q_c1,q_h1,q_n2," +
            "q_c2,q_h2,q_n3,q_c3,q_h3,q_n4,q_c4,q_h4,q_cbn,q_cbt,q_cbh,q_rain01,q_rain02,q_rain03," +
            "q_rain06,q_rain09,q_rain12,q_rain15,q_rain18,q_dt24,q_max_at24,q_min_at24,q_max_at12," +
            "q_min_at12,q_snowh,q_eva,q_alr,q_sunp,q_gs,q_sw1,q_sw2,q_sw3,q_sw4,q_sw5,q_sw6,stamp) " +
            " values (#{p.year},#{p.month},#{p.day},#{p.latitude},#{p.longitude},#{p.station},#{p.hour}," +
            "#{p.unitid},#{p.recievetime},#{p.markr},#{p.markw},#{p.markx},#{p.type},#{p.evelation}," +
            "#{p.onlyland},#{p.tranhour},#{p.slp},#{p.lp},#{p.wd},#{p.ws},#{p.at},#{p.td},#{p.max_at}," +
            "#{p.min_at},#{p.oq_slp},#{p.oq_lp},#{p.oq_wd},#{p.oq_at},#{p.w2},#{p.vis},#{p.ww},#{p.w1}," +
            "#{p.n},#{p.nh},#{p.cl},#{p.h},#{p.cm},#{p.ch},#{p.dp},#{p.dp3},#{p.dp24},#{p.rain24}," +
            "#{p.area},#{p.q_slp},#{p.q_lp},#{p.q_wd},#{p.q_ws},#{p.q_at},#{p.q_td},#{p.q_max_at}," +
            "#{p.q_min_at},#{p.q_oq_slp},#{p.q_oq_lp},#{p.q_oq_wd},#{p.q_oq_at},#{p.q_w2},#{p.q_vis}," +
            "#{p.q_ww},#{p.q_w1},#{p.q_n},#{p.q_nh},#{p.q_cl},#{p.q_h},#{p.q_cm},#{p.q_ch},#{p.q_dp}," +
            "#{p.q_dp3},#{p.q_dp24},#{p.q_rain24},#{p.remark},#{p.cccc},#{p.stn_type},#{p.rh},#{p.spl}," +
            "#{p.a3},#{p.dp_idx},#{p.n1},#{p.c1},#{p.h1},#{p.n2},#{p.c2},#{p.h2},#{p.n3},#{p.c3},#{p.h3}," +
            "#{p.n4},#{p.c4},#{p.h4},#{p.cbn},#{p.cbt},#{p.cbh},#{p.rain01},#{p.rain02},#{p.rain03}," +
            "#{p.rain06},#{p.rain09},#{p.rain12},#{p.rain15},#{p.rain18},#{p.dt24},#{p.max_at12}," +
            "#{p.min_at12},#{p.snowh},#{p.eva},#{p.alr},#{p.sunp},#{p.gs},#{p.sw1},#{p.sw2},#{p.sw3}," +
            "#{p.sw4},#{p.sw5},#{p.sw6},#{p.rain032},#{p.rain062},#{p.rain122},#{p.rain242},#{p.q_rh}," +
            "#{p.q_spl},#{p.q_a3},#{p.q_dp03},#{p.q_dp_idx},#{p.q_n1},#{p.q_c1},#{p.q_h1},#{p.q_n2}," +
            "#{p.q_c2},#{p.q_h2},#{p.q_n3},#{p.q_c3},#{p.q_h3},#{p.q_n4},#{p.q_c4},#{p.q_h4},#{p.q_cbn}," +
            "#{p.q_cbt},#{p.q_cbh},#{p.q_rain01},#{p.q_rain02},#{p.q_rain03},#{p.q_rain06},#{p.q_rain09}," +
            "#{p.q_rain12},#{p.q_rain15},#{p.q_rain18},#{p.q_dt24},#{p.q_max_at24},#{p.q_min_at24}," +
            "#{p.q_max_at12},#{p.q_min_at12},#{p.q_snowh},#{p.q_eva},#{p.q_alr},#{p.q_sunp},#{p.q_gs}," +
            "#{p.q_sw1},#{p.q_sw2},#{p.q_sw3},#{p.q_sw4},#{p.q_sw5},#{p.q_sw6},#{p.stamp})")
    void surfInToDB(@Param("p") surfParam param);
    //高空数据入库
    @Insert("insert into ndzb.ldb_his_obs_up (station,year,month,day,hour,latitude,longitude," +
            "qc_ind,elapsetime,press,hgt,at,rh,td,wd,ws,leveltype,oq_et,oq_p,oq_hgt,oq_at,oq_rh," +
            "oq_td,oq_wind,reserved,q_wd,q_ws,minute,odate,ldate,q_mean,ltime,mean,type) " +
            " values (#{p.station},#{p.year},#{p.month},#{p.day},#{p.hour},#{p.latitude}," +
            "#{p.longitude},#{p.qc_ind},#{p.elapsetime},#{p.press},#{p.hgt},#{p.at},#{p.rh}," +
            "#{p.td},#{p.wd},#{p.ws},#{p.leveltype},#{p.oq_et},#{p.oq_p},#{p.oq_hgt},#{p.oq_at}," +
            "#{p.oq_rh},#{p.oq_td},#{p.oq_wind},#{p.reserved},#{p.q_wd},#{p.q_ws},#{p.minute}," +
            "#{p.odate},#{p.ldate},#{p.q_mean},#{p.ltime},#{p.mean},#{p.type})")
    void tempInToDB(@Param("p") tempParam param);


    //查询记录是否已经存在
    @Select("<script>" +
            " SELECT count(*) FROM ndzb.zl_upload_record  " +
            "    where 1=1  " +
            "    <if test=\"fileInfo.data_type!=null and fileInfo.data_type!=''\"> " +
            "      and data_type = #{fileInfo.data_type} " +
            "    </if> " +
            "    <if test=\"fileInfo.file_name!=null and fileInfo.file_name!=''\"> " +
            "      and file_name = #{fileInfo.file_name} " +
            "    </if> " +
            "    <if test=\"fileInfo.file_path!=null and fileInfo.file_path!=''\"> " +
            "      and file_path = #{fileInfo.file_path} " +
            "    </if> " +
            "</script>")
    Integer checkRecordExist(@Param("fileInfo") Map<String,String> fileInfo);



    //登录校验
    @Select("<script>" +
            " SELECT  * FROM ndzb.user  " +
            "    where 1=1  " +
            "    <if test=\"param.login_name!=null and param.login_name!=''\"> " +
            "      and login_name = #{param.login_name} " +
            "    </if> " +
            "    <if test=\"param.password!=null and param.password!=''\"> " +
            "      and password = #{param.password} " +
            "    </if> " +
            "</script>")
    List<Map<String,String>> login(@Param("param") userParam param);

//用户管理接口

    //添加用户
    @Insert("insert into ndzb.user (id,name,phone,email,address," +
            "company,part,login_name,password,junzhong,create_time,zhanqu,cengji,user_static,lon,lat) " +
            " values (#{param.id},#{param.name},#{param.phone},#{param.email}," +
            "#{param.address},#{param.company},#{param.part},#{param.login_name},#{param.password}," +
            "#{param.junzhong},#{param.create_time},#{param.zhanqu},#{param.cengji},#{param.user_static},#{param.lon},#{param.lat})")
    void addUser(@Param("param") userParam param);

    //删除一个用户
    @Delete("delete from ndzb.user where id = #{param.id}")
    Integer deleteUserById(@Param("param") userParam param);

    //修改用户信息
    @Update("update ndzb.user set name=#{param.name},phone=#{param.phone}" +
            ",email=#{param.email},address=#{param.address},company=#{param.company},part=#{param.part}," +
            "login_name=#{param.login_name},password=#{param.password}," +
            "junzhong=#{param.junzhong},zhanqu=#{param.zhanqu},cengji=#{param.cengji},user_static=#{param.user_static}" +
            ",lon=#{param.lon},lat=#{param.lat}" +
            "where id=#{param.id}")
    void saveUser(@Param("param") userParam param);

    //获取用户列表
    @Select("<script>" +
            " SELECT  * FROM ndzb.user  " +
            "    where 1=1  " +
            "    <if test=\"param.start_time!=null and param.start_time!=''\"> " +
            "       and to_timestamp(create_time, 'YYYY-MM-DD hh24:mi:ss') &gt;= to_timestamp(#{param.start_time}, 'YYYY-MM-DD hh24:mi:ss')  " +
            "    </if> " +
            "    <if test=\"param.end_time!=null and param.end_time!=''\"> " +
            "       and to_timestamp(create_time, 'YYYY-MM-DD hh24:mi:ss') &lt;= to_timestamp(#{param.end_time}, 'YYYY-MM-DD hh24:mi:ss') " +
            "    </if> " +
            "    <if test=\"param.login_name!=null and param.login_name!=''\"> " +
            "      and login_name = #{param.login_name} " +
            "    </if> " +
            "    <if test=\"param.name!=null and param.name!=''\"> " +
            "      and name  like '%${param.name}%' " +
            "    </if> " +
            "  ORDER BY create_time desc " +
            "</script>")
    List<Map<String,String>> getUserList(@Param("param") userParam param);



}

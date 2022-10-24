package geovis.mapper;


import geovis.param.*;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;


@Mapper
public interface tongJiMapper {









    //高空统计表循环插入月统计数据
    @Insert("insert into ndzb.ldb_stat_over_up_mn_isb (station,year,month,latitude,longitude,press,m_hgt,m_atk,m_d,m_u,m_v,m_aste) " +
            " values (#{param.station},#{param.year},#{param.month},#{param.latitude},#{param.longitude},#{param.press},#{param.m_hgt}," +
            "#{param.m_atk},#{param.m_d},#{param.m_u},#{param.m_v},#{param.m_aste})")
    void insertUpYueTJ(@Param("param") upTongJiParam param);

 //地面统计表循环插入月统计数据
    @Insert("insert into ndzb.ldb_stat_over_surf_dkm (station,year,month,dekad,m_slp,m_d,m_ws,m_at,m_max_at,m_min_at,m_rh,m_n,m_nh,max_slp," +
            "min_slp,max_ws,max_wd,max_at,min_at,min_rh,sd_slp,sd_ws,sd_at,sd_slp_qr,sd_ws_qr,sd_at_qr,c_sd_slp,c_sd_ws,c_sd_at," +
            "mcount,cloudy,fine,c_h_all,c_nh_all,c_vis_all,c_wd_all,c_h_100,c_h_200,c_h_300,c_h_600,c_h_1500,c_h_2500,c_h_2500p," +
            "c_nh_1,c_nh_3,c_nh_6,c_nh_8,c_nh_10,c_vis_1,c_vis_4,c_vis_10,c_vis_10p,c_wd_1,c_wd_2,c_wd_3,c_wd_4,c_wd_5,c_wd_6," +
            "c_wd_7,c_wd_8,c_wd_9,c_wd_10,c_wd_11,c_wd_12,c_wd_13,c_wd_14,c_wd_15,c_wd_16,c_wd_17,fog,thunder) " +
            " values (#{param.station},#{param.year},#{param.month},#{param.dekad},#{param.m_slp},#{param.m_d},#{param.m_ws}," +
            "#{param.m_at},#{param.m_max_at},#{param.m_min_at},#{param.m_rh},#{param.m_n},#{param.m_nh},#{param.max_slp}," +
            "#{param.min_slp},#{param.max_ws},#{param.max_wd},#{param.max_at},#{param.min_at},#{param.min_rh},#{param.sd_slp}," +
            "#{param.sd_ws},#{param.sd_at},#{param.sd_slp_qr},#{param.sd_ws_qr},#{param.sd_at_qr},#{param.c_sd_slp},#{param.c_sd_ws}," +
            "#{param.c_sd_at},#{param.mcount},#{param.cloudy},#{param.fine},#{param.c_h_all},#{param.c_nh_all},#{param.c_vis_all}," +
            "#{param.c_wd_all},#{param.c_h_100},#{param.c_h_200},#{param.c_h_300},#{param.c_h_600},#{param.c_h_1500},#{param.c_h_2500}," +
            "#{param.c_h_2500p},#{param.c_nh_1},#{param.c_nh_3},#{param.c_nh_6},#{param.c_nh_8},#{param.c_nh_10},#{param.c_vis_1}," +
            "#{param.c_vis_4},#{param.c_vis_10},#{param.c_vis_10p},#{param.c_wd_1},#{param.c_wd_2},#{param.c_wd_3},#{param.c_wd_4}," +
            "#{param.c_wd_5},#{param.c_wd_6},#{param.c_wd_7},#{param.c_wd_8},#{param.c_wd_9},#{param.c_wd_10},#{param.c_wd_11}," +
            "#{param.c_wd_12},#{param.c_wd_13},#{param.c_wd_14},#{param.c_wd_15},#{param.c_wd_16},#{param.c_wd_17},#{param.fog}," +
            "#{param.thunder})")
    void insertSurfYueTJ(@Param("param") surfTongJiParam param);


    //循环插入旬统计数据
    @Insert("insert into ndzb.ldb_stat_over_surf_dkm (station,year,month,dekad,m_slp,m_d,m_ws,m_at,m_max_at,m_min_at,m_rh,m_n,m_nh," +
            "max_slp,min_slp,max_ws,max_wd,max_at,min_at,min_rh,sd_slp,sd_ws,sd_at,sd_slp_qr,sd_ws_qr,sd_at_qr,c_sd_slp," +
            "c_sd_ws,c_sd_at,mcount,cloudy,fine) " +
            " values (#{param.station},#{param.year},#{param.month},#{param.dekad},#{param.m_slp},#{param.m_d},#{param.m_ws}," +
            "#{param.m_at},#{param.m_max_at},#{param.m_min_at},#{param.m_rh},#{param.m_n},#{param.m_nh},#{param.max_slp}," +
            "#{param.min_slp},#{param.max_ws},#{param.max_wd},#{param.max_at},#{param.min_at},#{param.min_rh},#{param.sd_slp}," +
            "#{param.sd_ws},#{param.sd_at},#{param.sd_slp_qr},#{param.sd_ws_qr},#{param.sd_at_qr},#{param.c_sd_slp}," +
            "#{param.c_sd_ws},#{param.c_sd_at},#{param.mcount},#{param.cloudy},#{param.fine})")
    void insertXunTJ(@Param("param") surfTongJiParam param);



    //高空厚度合成风累年统计 删除旧的记录
    @Delete("delete from ndzb.ldb_stat_else_all_up_wind_thc where station = #{param.station}  and syear = #{param.syear} and eyear = #{param.eyear} ")
    Integer deleteAllUpThcHisRecord(@Param("param") upAllThcParam param);
    //高空厚度合成风累年统计 获取历年统计表中最大年份
    @Select("<script>" +
            " SELECT max(year) FROM ndzb.ldb_stat_over_up_wind_thc  where station=#{station} " +
            "</script>")
    Integer getUpTHCOverMaxYear(@Param("station") Integer station);
    //高高空厚度合成风累年统计 获取历年统计表中最小年份
    @Select("<script>" +
            " SELECT min(year) FROM ndzb.ldb_stat_over_up_wind_thc  where station=#{station} " +
            "</script>")
    Integer getUpTHCOverMinYear(@Param("station") Integer station);
    //高空厚度合成风累年统计 查询经纬度
    @Select("<script>" +
            " SELECT sum(latitude)/count(latitude),sum(longitude)/count(longitude) " +
            " FROM ndzb.ldb_stat_over_up_wind_thc  where station = #{station}  " +
            "</script>")
    List<Object> getUpTHClatlon(@Param("station") Integer station);
    //高空厚度合成风历年统计 统计结果入库
    @Insert(" insert into ndzb.ldb_stat_over_up_wind_thc ( station,year,month,latitude,longitude,max_ws_3_1,max_ws_3_2," +
            "max_ws_3_3,max_ws_3_4,max_ws_3_5,max_ws_3_6,max_ws_3_7,max_ws_3_8,max_ws_6_1,max_ws_6_2,max_ws_6_3," +
            "max_ws_6_4,max_ws_6_5,max_ws_6_6,max_ws_6_7,max_ws_6_8,max_ws_9_1,max_ws_9_2,max_ws_9_3,max_ws_9_4," +
            "max_ws_9_5,max_ws_9_6,max_ws_9_7,max_ws_9_8,max_ws_12_1,max_ws_12_2,max_ws_12_3,max_ws_12_4,max_ws_12_5," +
            "max_ws_12_6,max_ws_12_7,max_ws_12_8,max_ws_16_1,max_ws_16_2,max_ws_16_3,max_ws_16_4,max_ws_16_5,max_ws_16_6," +
            "max_ws_16_7,max_ws_16_8,max_ws_20_1,max_ws_20_2,max_ws_20_3,max_ws_20_4,max_ws_20_5,max_ws_20_6,max_ws_20_7," +
            "max_ws_20_8,min_ws_3_1,min_ws_3_2,min_ws_3_3,min_ws_3_4,min_ws_3_5,min_ws_3_6,min_ws_3_7,min_ws_3_8," +
            "min_ws_6_1,min_ws_6_2,min_ws_6_3,min_ws_6_4,min_ws_6_5,min_ws_6_6,min_ws_6_7,min_ws_6_8,min_ws_9_1," +
            "min_ws_9_2,min_ws_9_3,min_ws_9_4,min_ws_9_5,min_ws_9_6,min_ws_9_7,min_ws_9_8,min_ws_12_1,min_ws_12_2," +
            "min_ws_12_3,min_ws_12_4,min_ws_12_5,min_ws_12_6,min_ws_12_7,min_ws_12_8,min_ws_16_1,min_ws_16_2," +
            "min_ws_16_3,min_ws_16_4,min_ws_16_5,min_ws_16_6,min_ws_16_7,min_ws_16_8,min_ws_20_1,min_ws_20_2," +
            "min_ws_20_3,min_ws_20_4,min_ws_20_5,min_ws_20_6,min_ws_20_7,min_ws_20_8,m_ws_3_1,m_ws_3_2,m_ws_3_3," +
            "m_ws_3_4,m_ws_3_5,m_ws_3_6,m_ws_3_7,m_ws_3_8,m_ws_6_1,m_ws_6_2,m_ws_6_3,m_ws_6_4,m_ws_6_5,m_ws_6_6," +
            "m_ws_6_7,m_ws_6_8,m_ws_9_1,m_ws_9_2,m_ws_9_3,m_ws_9_4,m_ws_9_5,m_ws_9_6,m_ws_9_7,m_ws_9_8,m_ws_12_1," +
            "m_ws_12_2,m_ws_12_3,m_ws_12_4,m_ws_12_5,m_ws_12_6,m_ws_12_7,m_ws_12_8,m_ws_16_1,m_ws_16_2,m_ws_16_3," +
            "m_ws_16_4,m_ws_16_5,m_ws_16_6,m_ws_16_7,m_ws_16_8,m_ws_20_1,m_ws_20_2,m_ws_20_3,m_ws_20_4,m_ws_20_5," +
            "m_ws_20_6,m_ws_20_7,m_ws_20_8,c_ws_3_1,c_ws_3_2,c_ws_3_3,c_ws_3_4,c_ws_3_5,c_ws_3_6,c_ws_3_7,c_ws_3_8," +
            "c_ws_6_1,c_ws_6_2,c_ws_6_3,c_ws_6_4,c_ws_6_5,c_ws_6_6,c_ws_6_7,c_ws_6_8,c_ws_9_1,c_ws_9_2,c_ws_9_3," +
            "c_ws_9_4,c_ws_9_5,c_ws_9_6,c_ws_9_7,c_ws_9_8,c_ws_12_1,c_ws_12_2,c_ws_12_3,c_ws_12_4,c_ws_12_5,c_ws_12_6," +
            "c_ws_12_7,c_ws_12_8,c_ws_16_1,c_ws_16_2,c_ws_16_3,c_ws_16_4,c_ws_16_5,c_ws_16_6,c_ws_16_7,c_ws_16_8," +
            "c_ws_20_1,c_ws_20_2,c_ws_20_3,c_ws_20_4,c_ws_20_5,c_ws_20_6,c_ws_20_7,c_ws_20_8,max_ws_0_3_1,max_ws_0_3_2," +
            "max_ws_0_3_3,max_ws_0_3_4,max_ws_0_3_5,max_ws_0_3_6,max_ws_0_3_7,max_ws_0_3_8,max_ws_0_6_1,max_ws_0_6_2," +
            "max_ws_0_6_3,max_ws_0_6_4,max_ws_0_6_5,max_ws_0_6_6,max_ws_0_6_7,max_ws_0_6_8,max_ws_0_9_1,max_ws_0_9_2," +
            "max_ws_0_9_3,max_ws_0_9_4,max_ws_0_9_5,max_ws_0_9_6,max_ws_0_9_7,max_ws_0_9_8,max_ws_0_12_1,max_ws_0_12_2," +
            "max_ws_0_12_3,max_ws_0_12_4,max_ws_0_12_5,max_ws_0_12_6,max_ws_0_12_7,max_ws_0_12_8,max_ws_0_16_1," +
            "max_ws_0_16_2,max_ws_0_16_3,max_ws_0_16_4,max_ws_0_16_5,max_ws_0_16_6,max_ws_0_16_7,max_ws_0_16_8," +
            "max_ws_0_20_1,max_ws_0_20_2,max_ws_0_20_3,max_ws_0_20_4,max_ws_0_20_5,max_ws_0_20_6,max_ws_0_20_7," +
            "max_ws_0_20_8,min_ws_0_3_1,min_ws_0_3_2,min_ws_0_3_3,min_ws_0_3_4,min_ws_0_3_5,min_ws_0_3_6,min_ws_0_3_7," +
            "min_ws_0_3_8,min_ws_0_6_1,min_ws_0_6_2,min_ws_0_6_3,min_ws_0_6_4,min_ws_0_6_5,min_ws_0_6_6,min_ws_0_6_7," +
            "min_ws_0_6_8,min_ws_0_9_1,min_ws_0_9_2,min_ws_0_9_3,min_ws_0_9_4,min_ws_0_9_5,min_ws_0_9_6,min_ws_0_9_7," +
            "min_ws_0_9_8,min_ws_0_12_1,min_ws_0_12_2,min_ws_0_12_3,min_ws_0_12_4,min_ws_0_12_5,min_ws_0_12_6," +
            "min_ws_0_12_7,min_ws_0_12_8,min_ws_0_16_1,min_ws_0_16_2,min_ws_0_16_3,min_ws_0_16_4,min_ws_0_16_5," +
            "min_ws_0_16_6,min_ws_0_16_7,min_ws_0_16_8,min_ws_0_20_1,min_ws_0_20_2,min_ws_0_20_3,min_ws_0_20_4," +
            "min_ws_0_20_5,min_ws_0_20_6,min_ws_0_20_7,min_ws_0_20_8,m_ws_0_3_1,m_ws_0_3_2,m_ws_0_3_3,m_ws_0_3_4," +
            "m_ws_0_3_5,m_ws_0_3_6,m_ws_0_3_7,m_ws_0_3_8,m_ws_0_6_1,m_ws_0_6_2,m_ws_0_6_3,m_ws_0_6_4,m_ws_0_6_5," +
            "m_ws_0_6_6,m_ws_0_6_7,m_ws_0_6_8,m_ws_0_9_1,m_ws_0_9_2,m_ws_0_9_3,m_ws_0_9_4,m_ws_0_9_5,m_ws_0_9_6," +
            "m_ws_0_9_7,m_ws_0_9_8,m_ws_0_12_1,m_ws_0_12_2,m_ws_0_12_3,m_ws_0_12_4,m_ws_0_12_5,m_ws_0_12_6,m_ws_0_12_7," +
            "m_ws_0_12_8,m_ws_0_16_1,m_ws_0_16_2,m_ws_0_16_3,m_ws_0_16_4,m_ws_0_16_5,m_ws_0_16_6,m_ws_0_16_7,m_ws_0_16_8," +
            "m_ws_0_20_1,m_ws_0_20_2,m_ws_0_20_3,m_ws_0_20_4,m_ws_0_20_5,m_ws_0_20_6,m_ws_0_20_7,m_ws_0_20_8,c_ws_0_3_1," +
            "c_ws_0_3_2,c_ws_0_3_3,c_ws_0_3_4,c_ws_0_3_5,c_ws_0_3_6,c_ws_0_3_7,c_ws_0_3_8,c_ws_0_6_1,c_ws_0_6_2," +
            "c_ws_0_6_3,c_ws_0_6_4,c_ws_0_6_5,c_ws_0_6_6,c_ws_0_6_7,c_ws_0_6_8,c_ws_0_9_1,c_ws_0_9_2,c_ws_0_9_3," +
            "c_ws_0_9_4,c_ws_0_9_5,c_ws_0_9_6,c_ws_0_9_7,c_ws_0_9_8,c_ws_0_12_1,c_ws_0_12_2,c_ws_0_12_3,c_ws_0_12_4," +
            "c_ws_0_12_5,c_ws_0_12_6,c_ws_0_12_7,c_ws_0_12_8,c_ws_0_16_1,c_ws_0_16_2,c_ws_0_16_3,c_ws_0_16_4,c_ws_0_16_5," +
            "c_ws_0_16_6,c_ws_0_16_7,c_ws_0_16_8,c_ws_0_20_1,c_ws_0_20_2,c_ws_0_20_3,c_ws_0_20_4,c_ws_0_20_5,c_ws_0_20_6," +
            "c_ws_0_20_7,c_ws_0_20_8 ) " +
            " values (#{param.station},#{param.year},#{param.month},#{param.latitude},#{param.longitude},#{param.max_ws_3_1},#{param.max_ws_3_2},#{param.max_ws_3_3},#{param.max_ws_3_4},#{param.max_ws_3_5},#{param.max_ws_3_6},#{param.max_ws_3_7},#{param.max_ws_3_8},#{param.max_ws_6_1},#{param.max_ws_6_2},#{param.max_ws_6_3},#{param.max_ws_6_4},#{param.max_ws_6_5},#{param.max_ws_6_6},#{param.max_ws_6_7},#{param.max_ws_6_8},#{param.max_ws_9_1},#{param.max_ws_9_2},#{param.max_ws_9_3},#{param.max_ws_9_4},#{param.max_ws_9_5},#{param.max_ws_9_6},#{param.max_ws_9_7},#{param.max_ws_9_8},#{param.max_ws_12_1},#{param.max_ws_12_2},#{param.max_ws_12_3},#{param.max_ws_12_4},#{param.max_ws_12_5},#{param.max_ws_12_6},#{param.max_ws_12_7},#{param.max_ws_12_8},#{param.max_ws_16_1},#{param.max_ws_16_2},#{param.max_ws_16_3},#{param.max_ws_16_4},#{param.max_ws_16_5},#{param.max_ws_16_6},#{param.max_ws_16_7},#{param.max_ws_16_8},#{param.max_ws_20_1},#{param.max_ws_20_2},#{param.max_ws_20_3},#{param.max_ws_20_4},#{param.max_ws_20_5},#{param.max_ws_20_6},#{param.max_ws_20_7},#{param.max_ws_20_8},#{param.min_ws_3_1},#{param.min_ws_3_2},#{param.min_ws_3_3},#{param.min_ws_3_4},#{param.min_ws_3_5},#{param.min_ws_3_6},#{param.min_ws_3_7},#{param.min_ws_3_8},#{param.min_ws_6_1},#{param.min_ws_6_2},#{param.min_ws_6_3},#{param.min_ws_6_4},#{param.min_ws_6_5},#{param.min_ws_6_6},#{param.min_ws_6_7},#{param.min_ws_6_8},#{param.min_ws_9_1},#{param.min_ws_9_2},#{param.min_ws_9_3},#{param.min_ws_9_4},#{param.min_ws_9_5},#{param.min_ws_9_6},#{param.min_ws_9_7},#{param.min_ws_9_8},#{param.min_ws_12_1},#{param.min_ws_12_2},#{param.min_ws_12_3},#{param.min_ws_12_4},#{param.min_ws_12_5},#{param.min_ws_12_6},#{param.min_ws_12_7},#{param.min_ws_12_8},#{param.min_ws_16_1},#{param.min_ws_16_2},#{param.min_ws_16_3},#{param.min_ws_16_4},#{param.min_ws_16_5},#{param.min_ws_16_6},#{param.min_ws_16_7},#{param.min_ws_16_8},#{param.min_ws_20_1},#{param.min_ws_20_2},#{param.min_ws_20_3},#{param.min_ws_20_4},#{param.min_ws_20_5},#{param.min_ws_20_6},#{param.min_ws_20_7},#{param.min_ws_20_8},#{param.m_ws_3_1},#{param.m_ws_3_2},#{param.m_ws_3_3},#{param.m_ws_3_4},#{param.m_ws_3_5},#{param.m_ws_3_6},#{param.m_ws_3_7},#{param.m_ws_3_8},#{param.m_ws_6_1},#{param.m_ws_6_2},#{param.m_ws_6_3},#{param.m_ws_6_4},#{param.m_ws_6_5},#{param.m_ws_6_6},#{param.m_ws_6_7},#{param.m_ws_6_8},#{param.m_ws_9_1},#{param.m_ws_9_2},#{param.m_ws_9_3},#{param.m_ws_9_4},#{param.m_ws_9_5},#{param.m_ws_9_6},#{param.m_ws_9_7},#{param.m_ws_9_8},#{param.m_ws_12_1},#{param.m_ws_12_2},#{param.m_ws_12_3},#{param.m_ws_12_4},#{param.m_ws_12_5},#{param.m_ws_12_6},#{param.m_ws_12_7},#{param.m_ws_12_8},#{param.m_ws_16_1},#{param.m_ws_16_2},#{param.m_ws_16_3},#{param.m_ws_16_4},#{param.m_ws_16_5},#{param.m_ws_16_6},#{param.m_ws_16_7},#{param.m_ws_16_8},#{param.m_ws_20_1},#{param.m_ws_20_2},#{param.m_ws_20_3},#{param.m_ws_20_4},#{param.m_ws_20_5},#{param.m_ws_20_6},#{param.m_ws_20_7},#{param.m_ws_20_8},#{param.c_ws_3_1},#{param.c_ws_3_2},#{param.c_ws_3_3},#{param.c_ws_3_4},#{param.c_ws_3_5},#{param.c_ws_3_6},#{param.c_ws_3_7},#{param.c_ws_3_8},#{param.c_ws_6_1},#{param.c_ws_6_2},#{param.c_ws_6_3},#{param.c_ws_6_4},#{param.c_ws_6_5},#{param.c_ws_6_6},#{param.c_ws_6_7},#{param.c_ws_6_8},#{param.c_ws_9_1},#{param.c_ws_9_2},#{param.c_ws_9_3},#{param.c_ws_9_4},#{param.c_ws_9_5},#{param.c_ws_9_6},#{param.c_ws_9_7},#{param.c_ws_9_8},#{param.c_ws_12_1},#{param.c_ws_12_2},#{param.c_ws_12_3},#{param.c_ws_12_4},#{param.c_ws_12_5},#{param.c_ws_12_6},#{param.c_ws_12_7},#{param.c_ws_12_8},#{param.c_ws_16_1},#{param.c_ws_16_2},#{param.c_ws_16_3},#{param.c_ws_16_4},#{param.c_ws_16_5},#{param.c_ws_16_6},#{param.c_ws_16_7},#{param.c_ws_16_8},#{param.c_ws_20_1},#{param.c_ws_20_2},#{param.c_ws_20_3},#{param.c_ws_20_4},#{param.c_ws_20_5},#{param.c_ws_20_6},#{param.c_ws_20_7},#{param.c_ws_20_8},#{param.max_ws_0_3_1},#{param.max_ws_0_3_2},#{param.max_ws_0_3_3},#{param.max_ws_0_3_4},#{param.max_ws_0_3_5},#{param.max_ws_0_3_6},#{param.max_ws_0_3_7},#{param.max_ws_0_3_8},#{param.max_ws_0_6_1},#{param.max_ws_0_6_2},#{param.max_ws_0_6_3},#{param.max_ws_0_6_4},#{param.max_ws_0_6_5},#{param.max_ws_0_6_6},#{param.max_ws_0_6_7},#{param.max_ws_0_6_8},#{param.max_ws_0_9_1},#{param.max_ws_0_9_2},#{param.max_ws_0_9_3},#{param.max_ws_0_9_4},#{param.max_ws_0_9_5},#{param.max_ws_0_9_6},#{param.max_ws_0_9_7},#{param.max_ws_0_9_8},#{param.max_ws_0_12_1},#{param.max_ws_0_12_2},#{param.max_ws_0_12_3},#{param.max_ws_0_12_4},#{param.max_ws_0_12_5},#{param.max_ws_0_12_6},#{param.max_ws_0_12_7},#{param.max_ws_0_12_8},#{param.max_ws_0_16_1},#{param.max_ws_0_16_2},#{param.max_ws_0_16_3},#{param.max_ws_0_16_4},#{param.max_ws_0_16_5},#{param.max_ws_0_16_6},#{param.max_ws_0_16_7},#{param.max_ws_0_16_8},#{param.max_ws_0_20_1},#{param.max_ws_0_20_2},#{param.max_ws_0_20_3},#{param.max_ws_0_20_4},#{param.max_ws_0_20_5},#{param.max_ws_0_20_6},#{param.max_ws_0_20_7},#{param.max_ws_0_20_8},#{param.min_ws_0_3_1},#{param.min_ws_0_3_2},#{param.min_ws_0_3_3},#{param.min_ws_0_3_4},#{param.min_ws_0_3_5},#{param.min_ws_0_3_6},#{param.min_ws_0_3_7},#{param.min_ws_0_3_8},#{param.min_ws_0_6_1},#{param.min_ws_0_6_2},#{param.min_ws_0_6_3},#{param.min_ws_0_6_4},#{param.min_ws_0_6_5},#{param.min_ws_0_6_6},#{param.min_ws_0_6_7},#{param.min_ws_0_6_8},#{param.min_ws_0_9_1},#{param.min_ws_0_9_2},#{param.min_ws_0_9_3},#{param.min_ws_0_9_4},#{param.min_ws_0_9_5},#{param.min_ws_0_9_6},#{param.min_ws_0_9_7},#{param.min_ws_0_9_8},#{param.min_ws_0_12_1},#{param.min_ws_0_12_2},#{param.min_ws_0_12_3},#{param.min_ws_0_12_4},#{param.min_ws_0_12_5},#{param.min_ws_0_12_6},#{param.min_ws_0_12_7},#{param.min_ws_0_12_8},#{param.min_ws_0_16_1},#{param.min_ws_0_16_2},#{param.min_ws_0_16_3},#{param.min_ws_0_16_4},#{param.min_ws_0_16_5},#{param.min_ws_0_16_6},#{param.min_ws_0_16_7},#{param.min_ws_0_16_8},#{param.min_ws_0_20_1},#{param.min_ws_0_20_2},#{param.min_ws_0_20_3},#{param.min_ws_0_20_4},#{param.min_ws_0_20_5},#{param.min_ws_0_20_6},#{param.min_ws_0_20_7},#{param.min_ws_0_20_8},#{param.m_ws_0_3_1},#{param.m_ws_0_3_2},#{param.m_ws_0_3_3},#{param.m_ws_0_3_4},#{param.m_ws_0_3_5},#{param.m_ws_0_3_6},#{param.m_ws_0_3_7},#{param.m_ws_0_3_8},#{param.m_ws_0_6_1},#{param.m_ws_0_6_2},#{param.m_ws_0_6_3},#{param.m_ws_0_6_4},#{param.m_ws_0_6_5},#{param.m_ws_0_6_6},#{param.m_ws_0_6_7},#{param.m_ws_0_6_8},#{param.m_ws_0_9_1},#{param.m_ws_0_9_2},#{param.m_ws_0_9_3},#{param.m_ws_0_9_4},#{param.m_ws_0_9_5},#{param.m_ws_0_9_6},#{param.m_ws_0_9_7},#{param.m_ws_0_9_8},#{param.m_ws_0_12_1},#{param.m_ws_0_12_2},#{param.m_ws_0_12_3},#{param.m_ws_0_12_4},#{param.m_ws_0_12_5},#{param.m_ws_0_12_6},#{param.m_ws_0_12_7},#{param.m_ws_0_12_8},#{param.m_ws_0_16_1},#{param.m_ws_0_16_2},#{param.m_ws_0_16_3},#{param.m_ws_0_16_4},#{param.m_ws_0_16_5},#{param.m_ws_0_16_6},#{param.m_ws_0_16_7},#{param.m_ws_0_16_8},#{param.m_ws_0_20_1},#{param.m_ws_0_20_2},#{param.m_ws_0_20_3},#{param.m_ws_0_20_4},#{param.m_ws_0_20_5},#{param.m_ws_0_20_6},#{param.m_ws_0_20_7},#{param.m_ws_0_20_8},#{param.c_ws_0_3_1},#{param.c_ws_0_3_2},#{param.c_ws_0_3_3},#{param.c_ws_0_3_4},#{param.c_ws_0_3_5},#{param.c_ws_0_3_6},#{param.c_ws_0_3_7},#{param.c_ws_0_3_8},#{param.c_ws_0_6_1},#{param.c_ws_0_6_2},#{param.c_ws_0_6_3},#{param.c_ws_0_6_4},#{param.c_ws_0_6_5},#{param.c_ws_0_6_6},#{param.c_ws_0_6_7},#{param.c_ws_0_6_8},#{param.c_ws_0_9_1},#{param.c_ws_0_9_2},#{param.c_ws_0_9_3},#{param.c_ws_0_9_4},#{param.c_ws_0_9_5},#{param.c_ws_0_9_6},#{param.c_ws_0_9_7},#{param.c_ws_0_9_8},#{param.c_ws_0_12_1},#{param.c_ws_0_12_2},#{param.c_ws_0_12_3},#{param.c_ws_0_12_4},#{param.c_ws_0_12_5},#{param.c_ws_0_12_6},#{param.c_ws_0_12_7},#{param.c_ws_0_12_8},#{param.c_ws_0_16_1},#{param.c_ws_0_16_2},#{param.c_ws_0_16_3},#{param.c_ws_0_16_4},#{param.c_ws_0_16_5},#{param.c_ws_0_16_6},#{param.c_ws_0_16_7},#{param.c_ws_0_16_8},#{param.c_ws_0_20_1},#{param.c_ws_0_20_2},#{param.c_ws_0_20_3},#{param.c_ws_0_20_4},#{param.c_ws_0_20_5},#{param.c_ws_0_20_6},#{param.c_ws_0_20_7},#{param.c_ws_0_20_8}) ")
    void insertOverUpThcTJ(@Param("param") upThcParam param);
    //高空厚度合成风历年统计 查询一个站点一年的数据
    @Select("<script>" +
            " SELECT  * FROM ndzb.ldb_his_obs_up  " +
            "    where 1=1  " +
            "    <if test=\"param.year!=null and param.year!=''\"> " +
            "      and year  = #{param.year} " +
            "    </if> " +
            "    <if test=\"param.station!=null and param.station!=''\"> " +
            "      and station  = #{param.station} " +
            "    </if> " +
            "  and qc_ind in (0,1,8)   " +
            "  and  oq_p in ('01','04','05')   " +
            "  and (hour=0 or hour=12)   " +
            "  ORDER BY month,day,hour, press desc " +
            "</script>")
    List<Map<String,Object>> getUpThcTongJiData(@Param("param") tempParam param);
    //高空厚度合成风历年统计 删除旧的记录
    @Delete("delete from ndzb.ldb_stat_over_up_wind_thc where station = #{param.station}  and syear = #{param.syear} and eyear = #{param.eyear} ")
    Integer deleteOverUpThcHisRecord(@Param("param") upThcParam param);
    //战略高空累年统计 统计结果入库
    @Insert(" insert into ndzb.ldb_stat_all_he_mn_hgt ( station,syear,eyear,month,latitude,longitude,hgtjihe,m_atk,m_d ) " +
            " values ( #{param.station},#{param.syear},#{param.eyear},#{param.month},#{param.latitude},#{param.longitude},#{param.hgtjihe}, " +
            " #{param.m_atk},#{param.m_d} ) ")
    void insertAllUpHeTJ(@Param("param") upHeParam param);
    //战略高空累年统计 删除旧的记录
    @Delete("delete from ndzb.ldb_stat_all_he_mn_hgt where station = #{param.station}  and syear = #{param.syear} and eyear = #{param.eyear} ")
    Integer deleteAllUpHeHisRecord(@Param("param") upHeParam param);

    //常规高空累年统计 统计结果入库
    @Insert(" insert into ndzb.ldb_stat_all_up_mn_isb ( station,syear,eyear,month,latitude,longitude,hgtdadi,m_press,m_atk,m_d,m_u,m_v,m_aste ) " +
            " values (#{param.station},#{param.syear},#{param.eyear},#{param.month},#{param.latitude},#{param.longitude},#{param.hgtdadi}," +
            "#{param.m_press},#{param.m_atk},#{param.m_d},#{param.m_u},#{param.m_v},#{param.m_aste}) ")
    void insertAllUpCgxhTJ(@Param("param") upCgxhParam param);
    //常规高空累年统计 将各气压层的要素值读入要素数组
    @Select("<script>" +
            " SELECT m_hgt,m_atk,m_d,m_u,m_v,m_aste " +
            " FROM ndzb.ldb_stat_all_up_mn_isb  where station = #{param.station} and syear = #{param.syear}  and eyear = #{param.eyear}  " +
            " and month = #{param.month}  and press = #{param.press}  " +
            "</script>")
    List<Map<String,Object>> getUpFetch(@Param("param") upAllParam param);
    //常规高空累年统计 删除旧的记录
    @Delete("delete from ndzb.ldb_stat_all_cgxh_mn_hgt where station = #{param.station}  and syear = #{param.syear} and eyear = #{param.eyear} ")
    Integer deleteAllUpCgxhHisRecord(@Param("param") upCgxhParam param);
    //常规高空累年统计 查询经纬度
    @Select("<script>" +
            " SELECT sum(latitude)/count(latitude),sum(longitude)/count(longitude) " +
            " FROM ndzb.ldb_stat_over_up_mn_isb  where station = #{station}  " +
            "</script>")
    List<Object> getUplatlon(@Param("station") Integer station);
    //常规高空累年统计 读取差异高记录
    @Select("<script>" +
            " SELECT cyhgt " +
            " FROM ndzb.ldb_dic_station  where station = #{station}  " +
            "</script>")
    List<Object> getUpCyhgt(@Param("station") String station);
    //常规高空累年统计 读取测站的海拔高
    @Select("<script>" +
            " SELECT elevation " +
            " FROM ndzb.ldb_dic_station  where station = #{station}  " +
            "</script>")
    List<Object> getUpElevation(@Param("station") String station);

    //高空累年统计 插入统计结果
    @Insert(" insert into ndzb.ldb_stat_all_up_mn_isb ( station,syear,eyear,month,latitude,longitude,press,m_hgt,m_atk,m_d,m_u,m_v,m_aste ) " +
            " values (#{param.station},#{param.syear},#{param.eyear},#{param.month},#{param.latitude},#{param.longitude},#{param.press},#{param.m_hgt}, " +
            " #{param.m_atk},#{param.m_d},#{param.m_u},#{param.m_v},#{param.m_aste}) ")
    void insertAllUpTJ(@Param("param") upAllParam param);
    //高空累年统计 从历年统计表查询并统计
    @Select("<script>" +
            " SELECT sum(latitude)/count(latitude),sum(longitude)/count(longitude),sum(m_hgt)/count(m_hgt),sum(m_atk)/count(m_atk),sum(m_d)/count(m_d)," +
            " sum(m_u)/count(m_u),sum(m_v)/count(m_v),sum(m_aste)/count(m_aste) " +
            " FROM ndzb.ldb_stat_over_up_mn_isb  where station = #{param.station} and  year &gt;= #{param.syear} and " +
            " year &lt;= #{param.eyear} and  month = #{param.month} and  press = #{param.press}  " +
            "</script>")
    List<Object> getAllUpTJData(@Param("param") upAllParam param);

    //高空累年统计 删除累年统计表中历史数据
    @Delete("delete from ndzb.ldb_stat_all_up_mn_isb where syear = #{param.syear} and station = #{param.station}  and eyear = #{param.eyear} ")
    Integer deleteAllUpHisRecord(@Param("param") upAllParam param);
    //高空累年统计 获取历年统计表中最大年份
    @Select("<script>" +
            " SELECT max(year) FROM ndzb.ldb_stat_over_up_mn_isb  where station=#{station} " +
            "</script>")
    Integer getUpOverMaxYear(@Param("station") Integer station);
    //高空累年统计 获取历年统计表中最小年份
    @Select("<script>" +
            " SELECT min(year) FROM ndzb.ldb_stat_over_up_mn_isb  where station=#{station} " +
            "</script>")
    Integer getUpOverMinYear(@Param("station") Integer station);
    //删除高空统计表之前的历史统计数据
    @Delete("delete from ndzb.ldb_stat_over_up_mn_isb where year = #{param.year} and station = #{param.station} ")
    Integer deleteUpHisRecord(@Param("param") upTongJiParam param);

    //删除地面统计表之前的历史统计数据
    @Delete("delete from ndzb.ldb_stat_over_surf_dkm where year = #{param.year} and station = #{param.station} ")
    Integer deleteSurfHisRecord(@Param("param") surfTongJiParam param);

    //高空历年统计  获取库中所有年份
    @Select("<script>" +
            " SELECT year,count(station) FROM ndzb.ldb_his_obs_up  " +
            "  GROUP BY year " +
            "</script>")
    List<Map<String,Object>> getUpYears();
    //高空历年统计  获取库中所有站号
    @Select("<script>" +
            " SELECT station,count(year) FROM ndzb.ldb_his_obs_up  " +
            "  GROUP BY station " +
            "</script>")
    List<Map<String,Object>> getUpStations();
    //高空历年各站数据统计  查询一个站点一年的数据
    @Select("<script>" +
            " SELECT  * FROM ndzb.ldb_his_obs_up  " +
            "    where 1=1  " +
            "    <if test=\"param.year!=null and param.year!=''\"> " +
            "      and year  = #{param.year} " +
            "    </if> " +
            "    <if test=\"param.station!=null and param.station!=''\"> " +
            "      and station  = #{param.station} " +
            "    </if> " +
            "  and qc_ind in (0,1,8)   " +
            "  and  oq_p in ('01','04','05')   " +
            "  and (hour=0 or hour=12)   " +
            "  ORDER BY month,day,hour " +
            "</script>")
    List<Map<String,Object>> getUpTongJiData(@Param("param") tempParam param);


//==========================================================================


    //地面累年统计 从历年统计表查询并统计
    @Select("<script>" +
            " SELECT sum(m_slp)/count(m_slp),sum(m_ws)/count(m_ws),sum(m_at)/count(m_at), " +
            " sum(m_rh)/count(m_rh),sum(m_n)/count(m_n),sum(m_nh)/count(m_nh),sum(m_d)/count(m_d),sum(m_max_at)/count(m_max_at), " +
            " sum(m_min_at)/count(m_min_at),max(max_slp),max(max_ws),max(max_at),max(max_wd),min(min_slp),min(min_at),min(min_rh), " +
            " sqrt((sum(sd_slp_qr)-sum(sd_slp)*sum(sd_slp)/sum(c_sd_slp))/sum(c_sd_slp)), " +
            " sqrt((sum(sd_ws_qr)-sum(sd_ws)*sum(sd_ws)/sum(c_sd_ws))/sum(c_sd_ws)), " +
            " sqrt((sum(sd_at_qr)-sum(sd_at)*sum(sd_at)/sum(c_sd_at))/sum(c_sd_at)),sum(cloudy)/count(cloudy),sum(fine)/count(fine), " +
            " sum(fog)/count(fog),sum(thunder)/count(thunder),100*sum(c_h_100)/sum(c_h_all),100*sum(c_h_100+c_h_200)/sum(c_h_all), " +
            " 100*sum(c_h_100+c_h_200+c_h_300)/sum(c_h_all),100*sum(c_h_100+c_h_200+c_h_300+c_h_600)/sum(c_h_all), " +
            " 100*sum(c_h_100+c_h_200+c_h_300+c_h_600+c_h_1500)/sum(c_h_all),100*sum(c_h_100+c_h_200+c_h_300+c_h_600+c_h_1500+c_h_2500)/sum(c_h_all), " +
            " 100*sum(c_h_2500p)/sum(c_h_all),100*sum(c_nh_1)/sum(c_nh_all),100*sum(c_nh_3)/sum(c_nh_all),100*sum(c_nh_6)/sum(c_nh_all), " +
            " 100*sum(c_nh_8)/sum(c_nh_all),100*sum(c_nh_10)/sum(c_nh_all),100*sum(c_vis_1)/sum(c_vis_all),100*sum(c_vis_4)/sum(c_vis_all), " +
            " 100*sum(c_vis_10)/sum(c_vis_all),100*sum(c_vis_10p)/sum(c_vis_all) " +
            " FROM ndzb.ldb_stat_over_surf_dkm  where station = #{param.station} and  year &gt;= #{param.vrsyear} and " +
            " year &lt;= #{param.vreyear} and  dekad = #{param.dekad}  " +
            "</script>")
    List<Object> getAllTJData(@Param("param") surfAllParam param);



    //地面累年统计 插入统计结果
    @Insert(" insert into ndzb.ldb_stat_all_surf_dkmn (station,syear,eyear,month,dekad,m_slp,m_ws,m_at,m_rh,m_n,m_nh,m_d,m_max_at, " +
            " m_min_at,max_slp,max_ws,max_at,max_wd,min_slp,min_at,min_rh,sd_slp,sd_ws,sd_at,cloudy,fine,fog,thunder,f_h_100,f_h_200, " +
            " f_h_300,f_h_600,f_h_1500,f_h_2500,f_h_2500p,f_nh_1,f_nh_3,f_nh_6,f_nh_8,f_nh_10,f_vis_1,f_vis_4,f_vis_10,f_vis_10p, " +
            " most_wd,f_most_wd) " +
            " values (#{param.station},#{param.syear},#{param.eyear},#{param.month},#{param.dekad},#{param.m_slp},#{param.m_ws},#{param.m_at}, " +
            " #{param.m_rh},#{param.m_n},#{param.m_nh},#{param.m_d},#{param.m_max_at},#{param.m_min_at},#{param.max_slp},#{param.max_ws}, " +
            "  #{param.max_at},#{param.max_wd},#{param.min_slp},#{param.min_at},#{param.min_rh},#{param.sd_slp},#{param.sd_ws},#{param.sd_at}, " +
            " #{param.cloudy},#{param.fine},#{param.fog},#{param.thunder},#{param.f_h_100},#{param.f_h_200},#{param.f_h_300},#{param.f_h_600}, " +
            " #{param.f_h_1500},#{param.f_h_2500},#{param.f_h_2500p},#{param.f_nh_1},#{param.f_nh_3},#{param.f_nh_6},#{param.f_nh_8},#{param.f_nh_10}, " +
            " #{param.f_vis_1},#{param.f_vis_4},#{param.f_vis_10},#{param.f_vis_10p},#{param.most_wd},#{param.f_most_wd},) ")
    void insertAllSurfTJ(@Param("param") surfAllParam param);


   //地面累年统计 删除累年统计表中历史数据
   @Delete("delete from ndzb.ldb_stat_all_surf_dkmn where syear = #{param.syear} and station = #{param.station}  and eyear = #{param.eyear} ")
   Integer deleteAllSurfHisRecord(@Param("param") surfAllParam param);

   //地面累年统计 历年统计表风向频率计数
   @Select("<script>" +
           " SELECT sum(c_wd_all),sum(c_wd_1),sum(c_wd_2),sum(c_wd_3),sum(c_wd_4),sum(c_wd_5), " +
           " sum(c_wd_6),sum(c_wd_7),sum(c_wd_8),sum(c_wd_9),sum(c_wd_10),sum(c_wd_11),sum(c_wd_12), " +
           " sum(c_wd_13),sum(c_wd_14),sum(c_wd_15),sum(c_wd_16),sum(c_wd_17) " +
           " FROM ndzb.ldb_stat_over_surf_dkm  where station = #{param.station} and  year &gt;= #{param.vrsyear} and " +
           " year &lt;= #{param.vreyear} and  dekad = #{param.dekad}  " +
           "</script>")
   List<Integer> getVCWD(@Param("param") surfAllParam param);

    //地面累年统计 获取历年统计表中最大年份
   @Select("<script>" +
           " SELECT max(year) FROM ndzb.ldb_stat_over_surf_dkm  where station=#{station} " +
           "</script>")
   Integer getSurfOverMaxYear(@Param("station") Integer station);
   //地面累年统计 获取历年统计表中最小年份
   @Select("<script>" +
           " SELECT min(year) FROM ndzb.ldb_stat_over_surf_dkm  where station=#{station} " +
           "</script>")
   Integer getSurfOverMinYear(@Param("station") Integer station);
    //地面历年统计  获取库中所有年份
   @Select("<script>" +
           " SELECT year,count(station) FROM ndzb.ldb_his_obs_surf  " +
           "  GROUP BY year " +
           "</script>")
   List<Map<String,Object>> getSurfYears();
   //地面历年统计  获取库中所有站号
   @Select("<script>" +
           " SELECT station,count(year) FROM ndzb.ldb_his_obs_surf  " +
           "  GROUP BY station " +
           "</script>")
   List<Map<String,Object>> getSurfStations();

    //地面历年各站数据统计  查询一个站点一年的数据
    @Select("<script>" +
            " SELECT  * FROM ndzb.ldb_his_obs_surf  " +
            "    where 1=1  " +
            "    <if test=\"param.year!=null and param.year!=''\"> " +
            "      and year  = #{param.year} " +
            "    </if> " +
            "    <if test=\"param.station!=null and param.station!=''\"> " +
            "      and station  = #{param.station} " +
            "    </if> " +
            "  ORDER BY month,day,hour " +
            "</script>")
    List<Map<String,Object>> getSurfTongJiData(@Param("param") surfParam param);





}

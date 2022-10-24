package geovis.service;


import com.google.common.collect.Lists;
import geovis.mapper.dataSourceMapper;
import geovis.mapper.tongJiMapper;
import geovis.param.*;
import geovis.tools.Result;
import geovis.tools.selfTools;
import jnr.ffi.annotations.In;
import org.apache.poi.util.Internal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.*;


@Service
public class tongJiService {


    @Resource
    private tongJiMapper tongjimapper;
    //@Autowired
    private selfTools selftools = new selfTools();





    //自动统计所有地面数据
    public Result doSumAllSurfData(surfParam param) {

        Result res;
        //地面历年各月旬统计
        //res = doOverSurfTongJi();//地面所有历年统计
        //res = doOverUpTongJi();//高空所有历年统计
        res = doOverUpTHCTongJi();//厚度合成风历年统计
        //res = doAllSurfTongJi(58968,2011,2013);//地面累年统计
        //res = doAllUpTongJi(58968,2011,2013);//高空累年统计
        //res = doAllCGXHUpTongJi(58968,2011,2013);//常规高空累年统计
        //doSurfTongJi(58968,2011,1,4,4,30);
        //doUpTongJi(58968,2013);

        return res;
    }


    /*
     *  统计累年厚度合成风高空 从历年厚度合成风统计表ldb_stat_over_up_wind_thc统计累年数据。如果起止年份在实际存在的资料年份之外，则以最小、最大年份代替
     *  vstation 站号
     *  vsyear 年 起
     *  veyear 年 止
     */
    public Result doAllUpTHCTongJi(Integer vstation,Integer vsyear,Integer veyear) {


        //用于记录实际的最大最小年份
        Integer vrsyear;
        Integer vreyear;

        //用于记录累年各厚度层各风向下的最大风速、最小风速、平均风速、和风次数
        //各风向下要素数组
        //为了在没有找到某层最多风向时（如该层所有风向的风次数都为0时）向zp_all_cards_wind_thc插入null值，所以增加一列存null
        //type wd_array is varray(9) of number;
        //各厚度层下各风向下要素数组
        //type wind_array is varray(6) of wd_array;

        //各厚度层下各风向下要素
        Integer[][] vmax_ws;
        Integer[][] vmin_ws;
        Integer[][] vm_ws;
        Integer[][] vc_wd;
        //从0高度开始的各厚度层下各风向下要素
        Integer[][] vmax_ws0;
        Integer[][] vmin_ws0;
        Integer[][] vm_ws0;
        Integer[][] vc_wd0;


        //记录各厚度层最多风向的序号
        //type maxwd_array is varray(6) of integer;
        Integer[] vmaxwd;
        Integer[] vmaxwd0;
        //记录各层的最多风向风次数(使用maxwd_array数组)
        Integer[] vcmaxwd;
        Integer[] vcmaxwd0;

        //记录各层的最多风向
        //type winddirect_array is varray(8) of char(7);
        String[] vmaxwdchar;
        String[] vmaxwdchar0;

        //经纬度
        Integer vlatitude;
        Integer vlongitude;

        //循环变量
        Integer vmonth;
        Integer i;


        //begin
        //初始化变量//////////////////////////////////////////////////////

        //用于记录累年各厚度层各风向下的最大风速、最小风速、平均风速、和风次数
        //各厚度层下各风向下要素数组
        vmax_ws = new Integer[][]{null, null, null, null, null, null};
        vmin_ws = new Integer[][]{null, null, null, null, null, null};
        vm_ws = new Integer[][]{null, null, null, null, null, null};
        vc_wd = new Integer[][]{null, null, null, null, null, null};

        for (i = 0; i < 6; i++) {
            vmax_ws[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vmin_ws[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vm_ws[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vc_wd[i] = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, null};
        } //for i
        //从0高度开始的各厚度层下各风向下要素数组
        vmax_ws0 = new Integer[][]{null, null, null, null, null, null};
        vmin_ws0 = new Integer[][]{null, null, null, null, null, null};
        vm_ws0 = new Integer[][]{null, null, null, null, null, null};
        vc_wd0 = new Integer[][]{null, null, null, null, null, null};

        for (i = 0; i < 6; i++) {
            vmax_ws0[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vmin_ws0[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vm_ws0[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vc_wd0[i] = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, null};
        } //for i

        //记录各厚度层最多风向的序号
        vmaxwd = new Integer[]{-1, -1, -1, -1, -1, -1};
        vmaxwd0 = new Integer[]{-1, -1, -1, -1, -1, -1};
        //记录各层的最多风向风次数(使用maxwd_array数组)
        vcmaxwd = new Integer[]{0, 0, 0, 0, 0, 0};
        vcmaxwd0 = new Integer[]{0, 0, 0, 0, 0, 0};
        //记录各层的最多风向
        vmaxwdchar = new String[]{null, null, null, null, null, null, null, null};
        vmaxwdchar0 = new String[]{null, null, null, null, null, null, null, null};

        //经纬度
        List<Object> vlatLon = tongjimapper.getUpTHClatlon(vstation);
        vlatitude = Integer.valueOf(vlatLon.get(0).toString());
        vlongitude = Integer.valueOf(vlatLon.get(1).toString());

        //计算起止年份//////////////////////////////////////////////////////////////////
        //找出现有数据的实际最大最小年份
        vrsyear = tongjimapper.getUpTHCOverMinYear(vstation);
        vreyear = tongjimapper.getUpTHCOverMaxYear(vstation);
        //select min(year) into vrsyear from zp_over_cards_wind_thc where station=vstation;
        //select max(year) into vreyear from zp_over_cards_wind_thc where station=vstation;
        //如果指定的起止年不在资料的最小和最大年份范围内，则作相应调整
        if (vsyear >= vrsyear && vsyear <= vreyear){
            vrsyear = vsyear;
        }
        if (veyear >= vrsyear && veyear <= vreyear){
            vreyear = veyear;
        }


        //删除已有的记录-------------------------
        upAllThcParam thcParam = new upAllThcParam();
        thcParam.setStation(vstation);
        thcParam.setSyear(vrsyear);
        thcParam.setEyear(vreyear);
        tongjimapper.deleteAllUpThcHisRecord(thcParam);
        //delete from zp_all_cards_wind_thc where station = vstation and syear = vrsyear and eyear = vreyear;

        //进行累年统计////////////////////////////////////////////////////////-
        for (vmonth=0;vmonth<12;vmonth++){

         //计算最多各厚度层上的最多风向，以及最多风向的平均风速和最大风速

                select
        max(max_ws_3_1),

                vc_wd0(6)(8)


        from zp_over_cards_wind_thc
        where station = vstation && year >= vrsyear and year <=vreyear and month = vmonth;

        //确定各厚度层的最多风向序号
        for (i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (vc_wd[i][j] > vcmaxwd[i]) {
                    vcmaxwd[i] = vc_wd[i][j];
                    vmaxwd[i] = j;
                }
                if (vc_wd0[i][j] > vcmaxwd0[i]) {
                    vcmaxwd0[i] = vc_wd0[i][j];
                    vmaxwd0[i] = j;
                }
            }
            //如果没有找到该厚度层的最多风向，就将最多风向序号置为7
            if (vmaxwd[i] == -1) {
                vmaxwd[i] = 9;
            }
            if (vmaxwd0[i] == -1) {
                vmaxwd0[i] = 9;
            }
        }

        //将各厚度层德最多风向序号翻译为最多风向字符串
        for (i = 0; i < 6; i++) {
            switch (vmaxwd[i]) {
                case 1:
                    vmaxwdchar[i] = "0-45";
                    break;
                case 2:
                    vmaxwdchar[i] = "45-90";
                    break;
                case 3:
                    vmaxwdchar[i] = "90-135";
                    break;
                case 4:
                    vmaxwdchar[i] = "135-180";
                    break;
                case 5:
                    vmaxwdchar[i] = "180-225";
                    break;
                case 6:
                    vmaxwdchar[i] = "225-270";
                    break;
                case 7:
                    vmaxwdchar[i] = "270-315";
                    break;
                case 8:
                    vmaxwdchar[i] = "315-360";
                    break;
            }

            switch (vmaxwd0[i]) {
                case 1:
                    vmaxwdchar0[i] = "0-45";
                    break;
                case 2:
                    vmaxwdchar0[i] = "45-90";
                    break;
                case 3:
                    vmaxwdchar0[i] = "90-135";
                    break;
                case 4:
                    vmaxwdchar0[i] = "135-180";
                    break;
                case 5:
                    vmaxwdchar0[i] = "180-225";
                    break;
                case 6:
                    vmaxwdchar0[i] = "225-270";
                    break;
                case 7:
                    vmaxwdchar0[i] = "270-315";
                    break;
                case 8:
                    vmaxwdchar0[i] = "315-360";
                    break;
            }
        }


        //向zp_all_cards_wind_thc表插入给定区站、年代区间的各月统计结果//////////////////////////////
        //如果确实从zp_over_cards_wind_thc表中读出了数据则进行统计并插入到zp_all_cards_wind_thc中
        if (vrsyear != null and vreyear !=null ){
            insert into zp_all_cards_wind_thc(
                    station,
                    syear,
                    eyear,
                    month,
                    latitude,
                    longitude,
                    max_wd_3,
                    m_ws_3,
                    max_ws_3,
                    max_wd_6,
                    m_ws_6,
                    max_ws_6,
                    max_wd_9,
                    m_ws_9,
                    max_ws_9,
                    max_wd_12,
                    m_ws_12,
                    max_ws_12,
                    max_wd_16,
                    m_ws_16,
                    max_ws_16,
                    max_wd_20,
                    m_ws_20,
                    max_ws_20,
                    max_wd0_3,
                    m_ws0_3,
                    max_ws0_3,
                    max_wd0_6,
                    m_ws0_6,
                    max_ws0_6,
                    max_wd0_9,
                    m_ws0_9,
                    max_ws0_9,
                    max_wd0_12,
                    m_ws0_12,
                    max_ws0_12,
                    max_wd0_16,
                    m_ws0_16,
                    max_ws0_16,
                    max_wd0_20,
                    m_ws0_20,
                    max_ws0_20
            )
            values(
                    vstation,
                    vrsyear,
                    vreyear,
                    vmonth,
                    vlatitude,
                    vlongitude,
                    vmaxwdchar(1),
                    vm_ws(1) (vmaxwd(1)),
                    vmax_ws(1) (vmaxwd(1)),
                    vmaxwdchar(2),
                    vm_ws(2) (vmaxwd(2)),
                    vmax_ws(2) (vmaxwd(2)),
                    vmaxwdchar(3),
                    vm_ws(3) (vmaxwd(3)),
                    vmax_ws(3) (vmaxwd(3)),
                    vmaxwdchar(4),
                    vm_ws(4) (vmaxwd(4)),
                    vmax_ws(4) (vmaxwd(4)),
                    vmaxwdchar(5),
                    vm_ws(5) (vmaxwd(5)),
                    vmax_ws(5) (vmaxwd(5)),
                    vmaxwdchar(6),
                    vm_ws(6) (vmaxwd(6)),
                    vmax_ws(6) (vmaxwd(6)),

                    vmaxwdchar0(1),
                    vm_ws0(1) (vmaxwd0(1)),
                    vmax_ws0(1) (vmaxwd0(1)),
                    vmaxwdchar0(2),
                    vm_ws0(2) (vmaxwd0(2)),
                    vmax_ws0(2) (vmaxwd0(2)),
                    vmaxwdchar0(3),
                    vm_ws0(3) (vmaxwd0(3)),
                    vmax_ws0(3) (vmaxwd0(3)),
                    vmaxwdchar0(4),
                    vm_ws0(4) (vmaxwd0(4)),
                    vmax_ws0(4) (vmaxwd0(4)),
                    vmaxwdchar0(5),
                    vm_ws0(5) (vmaxwd0(5)),
                    vmax_ws0(5) (vmaxwd0(5)),
                    vmaxwdchar0(6),
                    vm_ws0(6) (vmaxwd0(6)),
                    vmax_ws0(6) (vmaxwd0(6))

      );
        }


        //重新初始化变量//////////////////////////////////////////////////////

        //用于记录累年各厚度层各风向下的最大风速、最小风速、平均风速、和风次数

        //各厚度层下各风向下要素数组
        for (i = 0; i < 6; i++) {
            vmax_ws[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vmin_ws[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vm_ws[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vc_wd[i] = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, null};
        } //for i

        //从0高度开始的各厚度层下各风向下要素数组
        for (i = 0; i < 6; i++) {
            vmax_ws0[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vmin_ws0[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vm_ws0[i] = new Integer[]{null, null, null, null, null, null, null, null, null};
            vc_wd0[i] = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, null};
        } //for i

        //记录各厚度层最多风向的序号
        vmaxwd = new Integer[]{-1, -1, -1, -1, -1, -1};
        vmaxwd0 = new Integer[]{-1, -1, -1, -1, -1, -1};

        //记录各厚度层最多风向的风次数
        vcmaxwd = new Integer[]{0, 0, 0, 0, 0, 0};
        vcmaxwd0 = new Integer[]{0, 0, 0, 0, 0, 0};

        //记录各层的最多风向
        vmaxwdchar = new String[]{null, null, null, null, null, null, null, null};
        vmaxwdchar0 = new String[]{null, null, null, null, null, null, null, null};


    } //for vmonth
        //commit;



        return Result.success("高空厚度合成风累年统计完成");
    }

    //统计历年厚度合成风高空 循环调用站点和年份
    public Result doOverUpTHCTongJi(){

        List<Map<String,Object>> years = tongjimapper.getUpYears();
        List<Map<String,Object>> stations = tongjimapper.getUpStations();
        if(years.size() > 0 && stations.size() > 0 ){
            //遍历所有年份
            for(int i = 0 ,len=years.size(); i<len;i++) {
                //遍历所有站号
                for(int j = 0 ,lenj=years.size(); j<lenj;j++) {

                    Object year_o = years.get(i).get("year");
                    Object station_o = stations.get(j).get("station");
                    if(year_o == null || station_o == null)continue;//跳过空的
                    Integer year = Integer.parseInt(year_o.toString());//年
                    Integer station = Integer.parseInt(station_o.toString());//年
                    doOverUpTHCTongJi(station,year);//调用统计程序
                }
            }
        }else{
            return Result.error("数据年份或站点号全为空");
        }

        return Result.success("高空厚度合成风历年统计完成");
    }

    /*
     *  统计历年厚度合成风高空 一年一个站的统计 由zh_cards_ele，对核目标统计历年厚度合成风。即标准厚度层上各风向的最大风速、最小风速、平均风速和风次数，以便进行累年统计而得到各厚度层的 最多风向、最多风向上的平均和极大、极小风速。
     *  vstation 所要统计的站点
     *  viyear 所要统计的年份
     */
    public Result doOverUpTHCTongJi(Integer vstation,Integer viyear){


       //主循环所用变量，用于取出记录集和判断月、日、时次交替------------------------------------------

       //新旧记录的月、日、时 和新记录的气压、经纬度
        Integer vmonth;Integer imonth;Integer vday;Integer iday;Integer vhour;Integer ihour;Integer vpress;
        Double vlatitude;
        Double vlongitude;

       //fetch into变量
        Integer vhgt;Integer vwd;Integer vws;
        String voq_hgt;String voq_wind;

       //一时次内有效的气压层计数和一月内有效的时次计数
        Integer inum_press;
        Integer inum_hour;


       //用于将每个时次的数据从气压层向高度层插值所用的变量--------------------------------
        //标准气压层数组
        Integer[] press = new Integer[16];
       //气压层变量
        Integer ip;

       //标准几何高度层数组
        Integer[] hgtjihe = new Integer[35];
       //高层变量
        Integer ih;

       //该站点的海拔高
       Integer vhgthaiba;
       //该站点的无量纲高度
       Integer vhgth0;


       //计算所得各要素在各气压层上的值
        Integer[] chgt = new Integer[16];
        Integer[] cwindu = new Integer[16];
        Integer[] cwindv = new Integer[16];


       //插值所得要素
        Double cwinduins;
        Double cwindvins;

       //时次的厚度合成风计算所用变量------------------
               //统计要素在各厚度层上的值
        Integer[] wsthcu = new Integer[6];
        Integer[] wsthcv = new Integer[6];
        Integer[] wsthc = new Integer[6];
        Integer[] wdthc = new Integer[6];
       //从0高度开始的各厚度层上的值
        Integer[] wsthcu0 = new Integer[6];
        Integer[] wsthcv0 = new Integer[6];
        Integer[] wsthc0 = new Integer[6];
        Integer[] wdthc0 = new Integer[6];

       //目前时次，各要素在各厚度层有效高度层的计数
        Integer[] cwsthcu = new Integer[6];
        Integer[] cwsthcv = new Integer[6];

       //从0高度开始的各厚度层上的值
        Integer[] cwsthcu0 = new Integer[6];
        Integer[] cwsthcv0 = new Integer[6];


       //用于向zp_over_cards_wind_thc插入记录的计算所得最终变量---------------
        //各风向下要素数组
        //type wd_array is varray(8) of number;
       //各厚度层下各风向下要素数组
        //type wind_array is varray(6) of wd_array;

       //各厚度层下各风向下要素
        Integer[][] vmax_ws = new Integer[6][8];
        Integer[][] vmin_ws = new Integer[6][8];
        Integer[][] vm_ws = new Integer[6][8];
        Integer[][] vc_wd = new Integer[6][8];

       //从0高度开始的各厚度层下各风向下要素
        Integer[][] vmax_ws0 = new Integer[6][8];
        Integer[][] vmin_ws0 = new Integer[6][8];
        Integer[][] vm_ws0 = new Integer[6][8];
        Integer[][] vc_wd0 = new Integer[6][8];

       //记录统计相关计数的变量-----------------
       //插入的月记录数
        Integer icnt;
       //从原有统计结果中删除的旧记录数
        Integer idel;

       //统计用到的总的记录数，含非标准4时次的记录
        Integer iscount;


        //循环变量-------------------------------------
        Integer i;

        //判断是否是处理第一条记录-----------
        Integer iisfirst;

        //判断是否达到了最后一条记录-----------
        Integer iislast;

        //变量初始化---------------------------------------------------------------------------------

        //主循环所用变量，用于取出记录集和判断月、日、时次交替------------------------------------------

        //新旧记录的月、日、时 和新记录的气压
        vmonth =-1;imonth =-1 ; vday =-1;iday =-1;vhour =-1; ihour =-1; vpress =-1;
        //因为同一站的经纬度在原始表zh_cards_ele中不唯一，所以此处取平均值
        List<Object> vlatLon = tongjimapper.getUplatlon(vstation);
        vlatitude = Double.valueOf(vlatLon.get(0).toString());
        vlongitude = Double.valueOf(vlatLon.get(1).toString());

       //fetch into变量
        vhgt =-1;   vwd =-1; vws =-1;
        voq_hgt ="";  voq_wind ="";

       //一时次内有效的气压层计数和一月内有效的时次计数
        inum_press = 0;
        inum_hour = 0;


         //用于将每个时次的数据从气压层向高度层插值所用的变量--------------------------------

        //标准气压层数组
        press = new Integer[]{1000, 925, 850, 700, 500, 400, 300, 250, 200, 150, 100, 70, 50, 30, 20, 10};

       //标准几何高度数组--0到16000米每隔1000米，0-10000米每隔500米
        hgtjihe = new Integer[]{0, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000, 5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9500, 10000, 11000, 12000, 13000, 14000, 15000, 16000, 16500, 17000, 17500, 18000, 18500, 19000, 19500, 20000};

       //该站点的海拔高
        //从base.base_station表中读取测站的海拔高
        List<Object> elevationObj = tongjimapper.getUpElevation(vstation.toString());
        vhgthaiba = Integer.parseInt(elevationObj.get(0).toString());

        //对没有海拔高的站点，将vhgthaiba置为0
        if (vhgthaiba == null || vhgthaiba == 9999){
            vhgthaiba = 0;
        }

       //该站点的无量纲高度
        if (vhgthaiba<=500){
            vhgth0 = 0;
        }else{
            vhgth0 = vhgthaiba/500;
        }


       //各要素在各气压层上的值
        chgt     =  new Integer[]{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
        cwindu   =  new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        cwindv   =  new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};


       //插值所得要素
        cwinduins = null;
        cwindvins = null;

        //时次的厚度合成风计算所用变量------------------
        //统计要素在各厚度层上的值
        wsthcu = new Integer[]{0, 0, 0, 0, 0, 0};
        wsthcv = new Integer[]{0,0,0,0,0,0};
        wsthc  = new Integer[]{0,0,0,0,0,0};
        wdthc  = new Integer[]{0,0,0,0,0,0};
       //统计要素在从0高度开始的各厚度层上的值
        wsthcu0 = new Integer[]{0,0,0,0,0,0};
        wsthcv0 = new Integer[]{0,0,0,0,0,0};
        wsthc0  = new Integer[]{0,0,0,0,0,0};
        wdthc0  = new Integer[]{0,0,0,0,0,0};

       //目前时次，各要素在各厚度层有效高度层的计数
        cwsthcu = new Integer[]{0,0,0,0,0,0};
        cwsthcv = new Integer[]{0,0,0,0,0,0};
       //目前时次，各要素在从0高度开始的各厚度层有效高度层的计数
        cwsthcu0 = new Integer[]{0,0,0,0,0,0};
        cwsthcv0 = new Integer[]{0,0,0,0,0,0};

       //用于向zp_over_cards_wind_thc插入记录的计算所得最终变量---------------
        //各厚度层下各风向下要素数组
        vmax_ws= new Integer[][]{null,null,null,null,null,null};
        vmin_ws= new Integer[][]{null,null,null,null,null,null};
        vm_ws= new Integer[][]{null,null,null,null,null,null};
        vc_wd= new Integer[][]{null,null,null,null,null,null};

        for(i=0;i<6;i++) {
            vmax_ws[i] = new Integer[]{-999, -999, -999, -999, -999, -999, -999, -999};
            vmin_ws[i] = new Integer[]{999, 999, 999, 999, 999, 999, 999, 999};
            vm_ws[i] = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0};
            vc_wd[i] = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0};
        }
        //从0高度开始的各厚度层下各风向下要素数组
        vmax_ws0= new Integer[][]{null,null,null,null,null,null};
        vmin_ws0= new Integer[][]{null,null,null,null,null,null};
        vm_ws0= new Integer[][]{null,null,null,null,null,null};
        vc_wd0= new Integer[][]{null,null,null,null,null,null};

        for(i=0;i<6;i++) {
            vmax_ws0[i] = new Integer[]{-999, -999, -999, -999, -999, -999, -999, -999};
            vmin_ws0[i] = new Integer[]{999, 999, 999, 999, 999, 999, 999, 999};
            vm_ws0[i] = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0};
            vc_wd0[i] = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0};
        }


       //记录统计相关计数的变量-----------------
       //插入的月记录数
        icnt =0;
       //从原有统计结果中删除的旧记录数
        idel =0;

       //统计用到的总的记录数，含非标准4时次的记录
        iscount =0;

       //判断是否是处理第一条记录----------
        iisfirst =1;



        //删除旧记录-----------------------------------------------------------
        //关于该年旧的统计结果
        upThcParam thcParam = new upThcParam();
        thcParam.setYear(viyear);
        thcParam.setStation(vstation);
        tongjimapper.deleteOverUpThcHisRecord(thcParam);
        //idel = sql%rowcount;

       //判断是否是处理此次气压循环的第一条记录
        iisfirst =1;

       //判断是否达到了最后一条记录-----------
        iislast =0;

       //打开光标-------------------------------------------------------------
        tempParam param = new tempParam();
        param.setStation(String.valueOf(vstation));
        param.setYear(viyear);
        //查询一个站点的一年的数据
        List<Map<String,Object>> mapList = tongjimapper.getUpThcTongJiData(param);//获取统计数据
        //遍历所有数据开始统计
        for(int a = 0 ,len=mapList.size(); a<len;a++) {

            try {

                vmonth = Integer.parseInt(mapList.get(a).get("month").toString());//
                vday = Integer.parseInt(mapList.get(a).get("day").toString());//
                vhour = Integer.parseInt(mapList.get(a).get("hour").toString());//
                vpress = Integer.parseInt(mapList.get(a).get("press").toString());//
                vhgt = Integer.parseInt(mapList.get(a).get("hgt").toString());//
                vwd = Integer.parseInt(mapList.get(a).get("wd").toString());//
                vws = Double.valueOf(mapList.get(a).get("ws").toString()).intValue();//
                voq_hgt = mapList.get(a).get("oq_hgt").toString();//
                voq_wind = mapList.get(a).get("oq_wind").toString();//

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        //open sqlsel;
        //loop
        //cursor sqlsel is select month,day,hour,press, hgt, wd, ws,oq_hgt,oq_wind from base.zh_cards_ele
        //where year=viyear && station=vstation && qc_ind in (0,1,8) && oq_p in ('01','04','05') && (hour=0 || hour=12) order by month,day,hour, press desc;
        //fetch sqlsel into vmonth,vday,vhour,vpress, vhgt, vwd, vws,voq_hgt,voq_wind;


       //如果到达最后一条，则退出
       //if(sqlsel%notfound){iislast=1 ; }
        if(a == len-1){iislast = 1;}

       //处理的总记录数加1
        iscount = iscount +1 ;

       //旧日期的初次赋值
        if (iisfirst == 1){
            imonth = vmonth ;
            iday = vday ;
            ihour= vhour;
            iisfirst=0;
        }





       //时次判断--------------------------------------------------------------------
        if ((ihour != vhour || iday != vday || imonth != vmonth || iislast==1) &&  inum_press != 0) {


            //对各个高度层进行插值-------------------------------------------------
            for (ih = 0; ih < 35; ih++) {
                //for ih in 1..35 loop

                //对经向风、纬向风用拉格朗日插值------------------------------------------------------------------------------------------

                //如果当前是高度在2000米以下则用1000，850和700三个气压层数据进行插值
                if (hgtjihe[ih] <= 2000 && chgt[1] != null && chgt[3] != null && chgt[4] != null) {

                    //纬向风
                    if (cwindu[1] != null && cwindu[3] != null && cwindu[4] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[1], chgt[1], cwindu[3], chgt[3], cwindu[4], chgt[4]);
                    }
                    //经向风
                    if (cwindv[1] != null && cwindv[3] != null && cwindv[4] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[1], chgt[1], cwindv[3], chgt[3], cwindv[4], chgt[4]);
                    }

                }

                //如果当前是高度在2000米以上，4500（含）以下则用850、700和500三个气压层数据进行插值
                if (hgtjihe[ih] > 2000 && hgtjihe[ih] <= 4500 && chgt[3] != null && chgt[4] != null && chgt[5] != null) {

                    //纬向风
                    if (cwindu[3] != null && cwindu[4] != null && cwindu[5] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[3], chgt[3], cwindu[4], chgt[4], cwindu[5], chgt[5]);
                    }
                    //经向风
                    if (cwindv[3] != null && cwindv[4] != null && cwindv[5] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[3], chgt[3], cwindv[4], chgt[4], cwindv[5], chgt[5]);
                    }

                }

                //如果当前是高度在4500米以上，6000（含）以下则用700、500和400三个气压层数据进行插值
                if (hgtjihe[ih] > 4500 && hgtjihe[ih] <= 6000 && chgt[4] != null && chgt[5] != null && chgt[6] != null) {

                    //纬向风
                    if (cwindu[4] != null && cwindu[5] != null && cwindu[6] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[4], chgt[4], cwindu[5], chgt[5], cwindu[6], chgt[6]);
                    }
                    //经向风
                    if (cwindv[4] != null && cwindv[5] != null && cwindv[6] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[4], chgt[4], cwindv[5], chgt[5], cwindv[6], chgt[6]);
                    }

                }

                //如果当前是高度在6000米以上，8500（含）以下则用500、400和300三个气压层数据进行插值
                if (hgtjihe[ih] > 6000 && hgtjihe[ih] <= 8500 && chgt[5] != null && chgt[6] != null && chgt[7] != null) {

                    //纬向风
                    if (cwindu[5] != null && cwindu[6] != null && cwindu[7] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[5], chgt[5], cwindu[6], chgt[6], cwindu[7], chgt[7]);
                    }
                    //经向风
                    if (cwindv[5] != null && cwindv[6] != null && cwindv[7] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[5], chgt[5], cwindv[6], chgt[6], cwindv[7], chgt[7]);
                    }

                }

                //如果当前是高度在8500米以上，9500米（含）以下则用400、300和250三个气压层数据进行插值
                if (hgtjihe[ih] > 8500 && hgtjihe[ih] <= 9500 && chgt[6] != null && chgt[7] != null && chgt[8] != null) {


                    //纬向风
                    if (cwindu[6] != null && cwindu[7] != null && cwindu[8] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[6], chgt[6], cwindu[7], chgt[7], cwindu[8], chgt[8]);
                    }
                    //经向风
                    if (cwindv[6] != null && cwindv[7] != null && cwindv[8] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[6], chgt[6], cwindv[7], chgt[7], cwindv[8], chgt[8]);
                    }

                }

                //如果当前是高度在9500米以上，10500米（含）以下则用300、250和200三个气压层数据进行插值
                if (hgtjihe[ih] > 9500 && hgtjihe[ih] <= 10500 && chgt[7] != null && chgt[8] != null && chgt[9] != null) {


                    //纬向风
                    if (cwindu[7] != null && cwindu[8] != null && cwindu[9] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[7], chgt[7], cwindu[8], chgt[8], cwindu[9], chgt[9]);
                    }
                    //经向风
                    if (cwindv[7] != null && cwindv[8] != null && cwindv[9] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[7], chgt[7], cwindv[8], chgt[8], cwindv[9], chgt[9]);
                    }

                }


                //如果当前是高度在10500米以上，12500米（含）以下则用250、200和150三个气压层数据进行插值
                if (hgtjihe[ih] > 10500 && hgtjihe[ih] <= 12500 && chgt[8] != null && chgt[9] != null && chgt[10] != null) {

                    //纬向风
                    if (cwindu[8] != null && cwindu[9] != null && cwindu[10] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[8], chgt[8], cwindu[9], chgt[9], cwindu[10], chgt[10]);
                    }
                    //经向风
                    if (cwindv[8] != null && cwindv[9] != null && cwindv[10] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[8], chgt[8], cwindv[9], chgt[9], cwindv[10], chgt[10]);
                    }

                }

                //如果当前是高度在12500米以上，14500米（含）以下则用200、150和100三个气压层数据进行插值
                if (hgtjihe[ih] > 12500 && hgtjihe[ih] <= 14500 && chgt[9] != null && chgt[10] != null && chgt[11] != null) {

                    //纬向风
                    if (cwindu[9] != null && cwindu[10] != null && cwindu[11] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[9], chgt[9], cwindu[10], chgt[10], cwindu[11], chgt[11]);
                    }
                    //经向风
                    if (cwindv[9] != null && cwindv[10] != null && cwindv[11] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[9], chgt[9], cwindv[10], chgt[10], cwindv[11], chgt[11]);
                    }

                }

                //如果当前是高度在14500米以上，16500米（含）以下则用150、100和70三个气压层数据进行插值
                if (hgtjihe[ih] > 14500 && hgtjihe[ih] <= 16500 && chgt[10] != null && chgt[11] != null && chgt[12] != null) {

                    //纬向风
                    if (cwindu[10] != null && cwindu[11] != null && cwindu[12] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[10], chgt[10], cwindu[11], chgt[11], cwindu[12], chgt[12]);
                    }
                    //经向风
                    if (cwindv[10] != null && cwindv[11] != null && cwindv[12] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[10], chgt[10], cwindv[11], chgt[11], cwindv[12], chgt[12]);
                    }

                }

                //如果当前是高度在16500米以上，19500米（含）以下则用100、70和50三个气压层数据进行插值
                if (hgtjihe[ih] > 16500 && hgtjihe[ih] <= 19500 && chgt[11] != null && chgt[12] != null && chgt[13] != null) {

                    //纬向风
                    if (cwindu[11] != null && cwindu[12] != null && cwindu[13] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[11], chgt[11], cwindu[12], chgt[12], cwindu[13], chgt[13]);
                    }
                    //经向风
                    if (cwindv[11] != null && cwindv[12] != null && cwindv[13] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[11], chgt[11], cwindv[12], chgt[12], cwindv[13], chgt[13]);
                    }

                }


                //如果当前是高度在19500米以上，22500米（含）以下则用70、50和30三个气压层数据进行插值
                if (hgtjihe[ih] > 19500 && hgtjihe[ih] <= 22500 && chgt[12] != null && chgt[13] != null && chgt[14] != null) {

                    //纬向风
                    if (cwindu[12] != null && cwindu[13] != null && cwindu[14] != null) {
                        cwinduins = selftools.lagrange(hgtjihe[ih], cwindu[12], chgt[12], cwindu[13], chgt[13], cwindu[14], chgt[14]);
                    }
                    //经向风
                    if (cwindv[12] != null && cwindv[13] != null && cwindv[14] != null) {
                        cwindvins = selftools.lagrange(hgtjihe[ih], cwindv[12], chgt[12], cwindv[13], chgt[13], cwindv[14], chgt[14]);
                    }

                }

                //计算各厚度层的平均纬向风和经向风
                if (cwinduins != null && cwindvins != null) {
                    //将该高度的纬向风和经向风计入相应厚度层的合成风-------------
                    //纬向风--------------------
                    //0-3厚度层0
                    if (hgtjihe[ih] == 0) {
                        wsthcu[1] = wsthcu[1] + (int) (cwinduins / 2);
                        cwsthcu[1] = (int) (cwsthcu[1] + 0.5);
                    }
                    //0-3厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 3000) {
                        wsthcu[1] = (int) (wsthcu[1] + cwinduins);
                        cwsthcu[1] = cwsthcu[1] + 1;
                    }
                    //0-3、3-6厚度层
                    if (hgtjihe[ih] == 3000) {
                        wsthcu[1] = (int) (wsthcu[1] + cwinduins / 2);
                        wsthcu[2] = (int) (wsthcu[2] + cwinduins / 2);
                        cwsthcu[1] = (int) (cwsthcu[1] + 0.5);
                        cwsthcu[2] = (int) (cwsthcu[2] + 0.5);
                    }
                    //3-6厚度层
                    if (hgtjihe[ih] > 3000 && hgtjihe[ih] < 6000) {
                        wsthcu[2] = (int) (wsthcu[2] + cwinduins);
                        cwsthcu[2] = cwsthcu[2] + 1;
                    }
                    //3-6、6-9厚度层
                    if (hgtjihe[ih] == 6000) {
                        wsthcu[2] = (int) (wsthcu[2] + cwinduins / 2);
                        wsthcu[3] = (int) (wsthcu[3] + cwinduins / 2);
                        cwsthcu[2] = (int) (cwsthcu[2] + 0.5);
                        cwsthcu[3] = (int) (cwsthcu[3] + 0.5);
                    }
                    //6-9厚度层
                    if (hgtjihe[ih] > 6000 && hgtjihe[ih] < 9000) {
                        wsthcu[3] = (int) (wsthcu[3] + cwinduins);
                        cwsthcu[3] = cwsthcu[3] + 1;
                    }
                    //6-9、9-12厚度层
                    if (hgtjihe[ih] == 9000) {
                        wsthcu[3] = (int) (wsthcu[3] + cwinduins / 2);
                        wsthcu[4] = (int) (wsthcu[4] + cwinduins / 2);
                        cwsthcu[3] = (int) (cwsthcu[3] + 0.5);
                        cwsthcu[4] = (int) (cwsthcu[4] + 0.5);
                    }
                    //9-12厚度层
                    if (hgtjihe[ih] > 9000 && hgtjihe[ih] < 12000) {
                        wsthcu[4] = (int) (wsthcu[4] + cwinduins);
                        cwsthcu[4] = cwsthcu[4] + 1;
                    }
                    //9-12、12-16厚度层
                    if (hgtjihe[ih] == 12000) {
                        wsthcu[4] = (int) (wsthcu[4] + cwinduins / 2);
                        wsthcu[5] = (int) (wsthcu[5] + cwinduins / 2);
                        cwsthcu[4] = (int) (cwsthcu[4] + 0.5);
                        cwsthcu[5] = (int) (cwsthcu[5] + 0.5);
                    }
                    //12-16厚度层
                    if (hgtjihe[ih] > 12000 && hgtjihe[ih] < 16000) {
                        wsthcu[5] = (int) (wsthcu[5] + cwinduins);
                        cwsthcu[5] = cwsthcu[5] + 1;
                    }
                    //12-16、16-20厚度层
                    if (hgtjihe[ih] == 16000) {
                        wsthcu[5] = (int) (wsthcu[5] + cwinduins / 2);
                        wsthcu[6] = (int) (wsthcu[6] + cwinduins / 2);
                        cwsthcu[5] = (int) (cwsthcu[5] + 0.5);
                        cwsthcu[6] = (int) (cwsthcu[6] + 0.5);
                    }
                    //16-20厚度层
                    if (hgtjihe[ih] > 16000 && hgtjihe[ih] < 20000) {
                        wsthcu[6] = (int) (wsthcu[6] + cwinduins);
                        cwsthcu[6] = (int) (cwsthcu[6] + 1);
                    }
                    //16-20厚度层20000
                    if (hgtjihe[ih] == 20000) {
                        wsthcu[6] = (int) (wsthcu[6] + cwinduins / 2);
                        cwsthcu[6] = (int) (cwsthcu[6] + 0.5);
                    }


                    //经向风--------------------
                    //0-3厚度层0
                    if (hgtjihe[ih] == 0) {
                        wsthcv[1] = (int) (wsthcv[1] + cwindvins / 2);
                        cwsthcv[1] = (int) (cwsthcv[1] + 0.5);
                    }
                    //0-3厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 3000) {
                        wsthcv[1] = (int) (wsthcv[1] + cwindvins);
                        cwsthcv[1] = (int) (cwsthcv[1] + 1);
                    }
                    //0-3、3-6厚度层
                    if (hgtjihe[ih] == 3000) {
                        wsthcv[1] = (int) (wsthcv[1] + cwindvins / 2);
                        wsthcv[2] = (int) (wsthcv[2] + cwindvins / 2);
                        cwsthcv[1] = (int) (cwsthcv[1] + 0.5);
                        cwsthcv[2] = (int) (cwsthcv[2] + 0.5);
                    }
                    //3-6厚度层
                    if (hgtjihe[ih] > 3000 && hgtjihe[ih] < 6000) {
                        wsthcv[2] = (int) (wsthcv[2] + cwindvins);
                        cwsthcv[2] = (int) (cwsthcv[2] + 1);
                    }
                    //3-6、6-9厚度层
                    if (hgtjihe[ih] == 6000) {
                        wsthcv[2] = (int) (wsthcv[2] + cwindvins / 2);
                        wsthcv[3] = (int) (wsthcv[3] + cwindvins / 2);
                        cwsthcv[2] = (int) (cwsthcv[2] + 0.5);
                        cwsthcv[3] = (int) (cwsthcv[3] + 0.5);
                    }
                    //6-9厚度层
                    if (hgtjihe[ih] > 6000 && hgtjihe[ih] < 9000) {
                        wsthcv[3] = (int) (wsthcv[3] + cwindvins);
                        cwsthcv[3] = (int) (cwsthcv[3] + 1);
                    }
                    //6-9、9-12厚度层
                    if (hgtjihe[ih] == 9000) {
                        wsthcv[3] = (int) (wsthcv[3] + cwindvins / 2);
                        wsthcv[4] = (int) (wsthcv[4] + cwindvins / 2);
                        cwsthcv[3] = (int) (cwsthcv[3] + 0.5);
                        cwsthcv[4] = (int) (cwsthcv[4] + 0.5);
                    }
                    //9-12厚度层
                    if (hgtjihe[ih] > 9000 && hgtjihe[ih] < 12000) {
                        wsthcv[4] = (int) (wsthcv[4] + cwindvins);
                        cwsthcv[4] = (int) (cwsthcv[4] + 1);
                    }
                    //9-12、12-16厚度层
                    if (hgtjihe[ih] == 12000) {
                        wsthcv[4] = (int) (wsthcv[4] + cwindvins / 2);
                        wsthcv[5] = (int) (wsthcv[5] + cwindvins / 2);
                        cwsthcv[4] = (int) (cwsthcv[4] + 0.5);
                        cwsthcv[5] = (int) (cwsthcv[5] + 0.5);
                    }
                    //12-16厚度层
                    if (hgtjihe[ih] > 12000 && hgtjihe[ih] < 16000) {
                        wsthcv[5] = (int) (wsthcv[5] + cwindvins);
                        cwsthcv[5] = (int) (cwsthcv[5] + 1);
                    }
                    //12-16、16-20厚度层
                    if (hgtjihe[ih] == 16000) {
                        wsthcv[5] = (int) (wsthcv[5] + cwindvins / 2);
                        wsthcv[6] = (int) (wsthcv[6] + cwindvins / 2);
                        cwsthcv[5] = (int) (cwsthcv[5] + 0.5);
                        cwsthcv[6] = (int) (cwsthcv[6] + 0.5);
                    }
                    //16-20厚度层
                    if (hgtjihe[ih] > 16000 && hgtjihe[ih] < 20000) {
                        wsthcv[6] = (int) (wsthcv[6] + cwindvins);
                        cwsthcv[6] = (int) (cwsthcv[6] + 1);
                    }
                    //16-20厚度层20000
                    if (hgtjihe[ih] == 20000) {
                        wsthcv[6] = (int) (wsthcv[6] + cwindvins / 2);
                        cwsthcv[6] = (int) (cwsthcv[6] + 0.5);
                    }

                    //将该高度的纬向风和经向风计入相应从0高度开始的厚度层的合成风-------------
                    //纬向风--------------------
                    //各厚度层0
                    if (hgtjihe[ih] == 0) {
                        wsthcu0[1] = (int) (wsthcu0[1] + cwinduins / 2);
                        cwsthcu0[1] = (int) (cwsthcu0[1] + 0.5);
                        wsthcu0[2] = (int) (wsthcu0[2] + cwinduins / 2);
                        cwsthcu0[2] = (int) (cwsthcu0[2] + 0.5);
                        wsthcu0[3] = (int) (wsthcu0[3] + cwinduins / 2);
                        cwsthcu0[3] = (int) (cwsthcu0[3] + 0.5);
                        wsthcu0[4] = (int) (wsthcu0[4] + cwinduins / 2);
                        cwsthcu0[4] = (int) (cwsthcu0[4] + 0.5);
                        wsthcu0[5] = (int) (wsthcu0[5] + cwinduins / 2);
                        cwsthcu0[5] = (int) (cwsthcu0[5] + 0.5);
                        wsthcu0[6] = (int) (wsthcu0[6] + cwinduins / 2);
                        cwsthcu0[6] = (int) (cwsthcu0[6] + 0.5);
                    }
                    //0-3厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 3000) {
                        wsthcu0[1] = (int) (wsthcu0[1] + cwinduins);
                        cwsthcu0[1] = (int) (cwsthcu0[1] + 1);
                    }
                    //0-3厚度层3000
                    if (hgtjihe[ih] == 3000) {
                        wsthcu0[1] = (int) (wsthcu0[1] + cwinduins / 2);
                        cwsthcu0[1] = (int) (cwsthcu0[1] + 0.5);
                    }
                    //0-6厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 6000) {
                        wsthcu0[2] = (int) (wsthcu0[2] + cwinduins);
                        cwsthcu0[2] = (int) (cwsthcu0[2] + 1);
                    }
                    //0-6厚度层6000
                    if (hgtjihe[ih] == 6000) {
                        wsthcu0[2] = (int) (wsthcu0[2] + cwinduins / 2);
                        cwsthcu0[2] = (int) (cwsthcu0[2] + 0.5);
                    }
                    //0-9厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 9000) {
                        wsthcu0[3] = (int) (wsthcu0[3] + cwinduins);
                        cwsthcu0[3] = (int) (cwsthcu0[3] + 1);
                    }
                    //0-9厚度层9000
                    if (hgtjihe[ih] == 9000) {
                        wsthcu0[3] = (int) (wsthcu0[3] + cwinduins / 2);
                        cwsthcu0[3] = (int) (cwsthcu0[3] + 0.5);
                    }
                    //0-12厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 12000) {
                        wsthcu0[4] = (int) (wsthcu0[4] + cwinduins);
                        cwsthcu0[4] = (int) (cwsthcu0[4] + 1);
                    }
                    //0-12厚度层12000
                    if (hgtjihe[ih] == 12000) {
                        wsthcu0[4] = (int) (wsthcu0[4] + cwinduins / 2);
                        cwsthcu0[4] = (int) (cwsthcu0[4] + 0.5);
                    }
                    //0-16厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 16000) {
                        wsthcu0[5] = (int) (wsthcu0[5] + cwinduins);
                        cwsthcu0[5] = (int) (cwsthcu0[5] + 1);
                    }
                    //0-16厚度层16000
                    if (hgtjihe[ih] == 16000) {
                        wsthcu0[5] = (int) (wsthcu0[5] + cwinduins / 2);
                        cwsthcu0[5] = (int) (cwsthcu0[5] + 0.5);
                    }
                    //0-20厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 20000) {
                        wsthcu0[6] = (int) (wsthcu0[6] + cwinduins);
                        cwsthcu0[6] = (int) (cwsthcu0[6] + 1);
                    }
                    //0-20厚度层20000
                    if (hgtjihe[ih] == 20000) {
                        wsthcu0[6] = (int) (wsthcu0[6] + cwinduins / 2);
                        cwsthcu0[6] = (int) (cwsthcu0[6] + 0.5);
                    }

                    //经向风--------------------
                    //各厚度层0
                    if (hgtjihe[ih] == 0) {
                        wsthcv0[1] = (int) (wsthcv0[1] + cwindvins / 2);
                        cwsthcv0[1] = (int) (cwsthcv0[1] + 0.5);
                        wsthcv0[2] = (int) (wsthcv0[2] + cwindvins / 2);
                        cwsthcv0[2] = (int) (cwsthcv0[2] + 0.5);
                        wsthcv0[3] = (int) (wsthcv0[3] + cwindvins / 2);
                        cwsthcv0[3] = (int) (cwsthcv0[3] + 0.5);
                        wsthcv0[4] = (int) (wsthcv0[4] + cwindvins / 2);
                        cwsthcv0[4] = (int) (cwsthcv0[4] + 0.5);
                        wsthcv0[5] = (int) (wsthcv0[5] + cwindvins / 2);
                        cwsthcv0[5] = (int) (cwsthcv0[5] + 0.5);
                        wsthcv0[6] = (int) (wsthcv0[6] + cwindvins / 2);
                        cwsthcv0[6] = (int) (cwsthcv0[6] + 0.5);
                    }
                    //0-3厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 3000) {
                        wsthcv0[1] = (int) (wsthcv0[1] + cwindvins);
                        cwsthcv0[1] = (int) (cwsthcv0[1] + 1);
                    }
                    //0-3厚度层3000
                    if (hgtjihe[ih] == 3000) {
                        wsthcv0[1] = (int) (wsthcv0[1] + cwindvins / 2);
                        cwsthcv0[1] = (int) (cwsthcv0[1] + 0.5);
                    }
                    //0-6厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 6000) {
                        wsthcv0[2] = (int) (wsthcv0[2] + cwindvins);
                        cwsthcv0[2] = (int) (cwsthcv0[2] + 1);
                    }
                    //0-6厚度层6000
                    if (hgtjihe[ih] == 6000) {
                        wsthcv0[2] = (int) (wsthcv0[2] + cwindvins / 2);
                        cwsthcv0[2] = (int) (cwsthcv0[2] + 0.5);
                    }
                    //0-9厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 9000) {
                        wsthcv0[3] = (int) (wsthcv0[3] + cwindvins);
                        cwsthcv0[3] = (int) (cwsthcv0[3] + 1);
                    }
                    //0-9厚度层9000
                    if (hgtjihe[ih] == 9000) {
                        wsthcv0[3] = (int) (wsthcv0[3] + cwindvins / 2);
                        cwsthcv0[3] = (int) (cwsthcv0[3] + 0.5);
                    }
                    //0-12厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 12000) {
                        wsthcv0[4] = (int) (wsthcv0[4] + cwindvins);
                        cwsthcv0[4] = (int) (cwsthcv0[4] + 1);
                    }
                    //0-12厚度层12000
                    if (hgtjihe[ih] == 12000) {
                        wsthcv0[4] = (int) (wsthcv0[4] + cwindvins / 2);
                        cwsthcv0[4] = (int) (cwsthcv0[4] + 0.5);
                    }
                    //0-16厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 16000) {
                        wsthcv0[5] = (int) (wsthcv0[5] + cwindvins);
                        cwsthcv0[5] = (int) (cwsthcv0[5] + 1);
                    }
                    //0-16厚度层16000
                    if (hgtjihe[ih] == 16000) {
                        wsthcv0[5] = (int) (wsthcv0[5] + cwindvins / 2);
                        cwsthcv0[5] = (int) (cwsthcv0[5] + 0.5);
                    }
                    //0-20厚度层
                    if (hgtjihe[ih] > 0 && hgtjihe[ih] < 20000) {
                        wsthcv0[6] = (int) (wsthcv0[6] + cwindvins);
                        cwsthcv0[6] = (int) (cwsthcv0[6] + 1);
                    }
                    //0-20厚度层20000
                    if (hgtjihe[ih] == 20000) {
                        wsthcv0[6] = (int) (wsthcv0[6] + cwindvins / 2);
                        cwsthcv0[6] = (int) (cwsthcv0[6] + 0.5);
                    }
                }


                //变量重新初始化---------------------------------
                //插值所得要素
                cwinduins = null;
                cwindvins = null;

            }//各层

            //计算该时次合成风并进行统计-------------
            for (i = 0; i < 6; i++) {
                //for i in 1..6 loop
                if (cwsthcu[i] != 0 && cwsthcv[i] != 0) {
                    //计算各厚度层内矢量风的平均值
                    if (i == 1) {
                        wsthcu[i] = wsthcu[i] / (cwsthcu[i] - 2 * vhgth0);
                        wsthcv[i] = wsthcv[i] / (cwsthcv[i] - 2 * vhgth0);
                    } else {
                        wsthcu[i] = wsthcu[i] / cwsthcu[i];
                        wsthcv[i] = wsthcv[i] / cwsthcv[i];
                    }
                    //计算合成风向
                    if (wsthcu[i] == 0 && wsthcv[i] == 0) {
                        wdthc[i] = 0;
                    }//风向为0表示静稳风
                    if (wsthcu[i] == 0 && wsthcv[i] > 0) {
                        wdthc[i] = 180;
                    }
                    if (wsthcu[i] == 0 && wsthcv[i] < 0) {
                        wdthc[i] = 360;
                    }
                    if (wsthcu[i] > 0) {
                        wdthc[i] = (int) (270 - 180 * atan(wsthcv[i] / wsthcu[i]) / 3.1415927);
                    }
                    if (wsthcu[i] < 0) {
                        wdthc[i] = (int) (180 - 180 * atan(wsthcv[i] / wsthcu[i]) / 3.1415927);
                    }
                    //计算合成风速
                    wsthc[i] = (int) sqrt(wsthcu[i] * wsthcu[i] + wsthcv[i] * wsthcv[i]);

                    //进行统计-------------
                    //风向1
                    if (wdthc[i] > 0 && wdthc[i] <= 45) {
                        vc_wd[i][1] = vc_wd[i][1] + 1;
                        vm_ws[i][1] = vm_ws[i][1] + wsthc[i];
                        if (vmax_ws[i][1] < wsthc[i]) {
                            vmax_ws[i][1] = wsthc[i];
                        }
                        if (vmin_ws[i][1] > wsthc[i]) {
                            vmin_ws[i][1] = wsthc[i];
                        }
                    }
                    //风向2
                    if (wdthc[i] > 45 && wdthc[i] <= 90) {
                        vc_wd[i][2] = vc_wd[i][2] + 1;
                        vm_ws[i][2] = vm_ws[i][2] + wsthc[i];
                        if (vmax_ws[i][2] < wsthc[i]) {
                            vmax_ws[i][2] = wsthc[i];
                        }
                        if (vmin_ws[i][2] > wsthc[i]) {
                            vmin_ws[i][2] = wsthc[i];
                        }
                    }
                    //风向3
                    if (wdthc[i] > 90 && wdthc[i] <= 135) {
                        vc_wd[i][3] = vc_wd[i][3] + 1;
                        vm_ws[i][3] = vm_ws[i][3] + wsthc[i];
                        if (vmax_ws[i][3] < wsthc[i]) {
                            vmax_ws[i][3] = wsthc[i];
                        }
                        if (vmin_ws[i][3] > wsthc[i]) {
                            vmin_ws[i][3] = wsthc[i];
                        }
                    }
                    //风向4
                    if (wdthc[i] > 135 && wdthc[i] <= 180) {
                        vc_wd[i][4] = vc_wd[i][4] + 1;
                        vm_ws[i][4] = vm_ws[i][4] + wsthc[i];
                        if (vmax_ws[i][4] < wsthc[i]) {
                            vmax_ws[i][4] = wsthc[i];
                        }
                        if (vmin_ws[i][4] > wsthc[i]) {
                            vmin_ws[i][4] = wsthc[i];
                        }
                    }
                    //风向5
                    if (wdthc[i] > 180 && wdthc[i] <= 225) {
                        vc_wd[i][5] = vc_wd[i][5] + 1;
                        vm_ws[i][5] = vm_ws[i][5] + wsthc[i];
                        if (vmax_ws[i][5] < wsthc[i]) {
                            vmax_ws[i][5] = wsthc[i];
                        }
                        if (vmin_ws[i][5] > wsthc[i]) {
                            vmin_ws[i][5] = wsthc[i];
                        }
                    }
                    //风向6
                    if (wdthc[i] > 225 && wdthc[i] <= 270) {
                        vc_wd[i][6] = vc_wd[i][6] + 1;
                        vm_ws[i][6] = vm_ws[i][6] + wsthc[i];
                        if (vmax_ws[i][6] < wsthc[i]) {
                            vmax_ws[i][6] = wsthc[i];
                        }
                        if (vmin_ws[i][6] > wsthc[i]) {
                            vmin_ws[i][6] = wsthc[i];
                        }
                    }
                    //风向7
                    if (wdthc[i] > 270 && wdthc[i] <= 315) {
                        vc_wd[i][7] = vc_wd[i][7] + 1;
                        vm_ws[i][7] = vm_ws[i][7] + wsthc[i];
                        if (vmax_ws[i][7] < wsthc[i]) {
                            vmax_ws[i][7] = wsthc[i];
                        }
                        if (vmin_ws[i][7] > wsthc[i]) {
                            vmin_ws[i][7] = wsthc[i];
                        }
                    }
                    //风向8
                    if (wdthc[i] > 315 && wdthc[i] <= 360) {
                        vc_wd[i][8] = vc_wd[i][8] + 1;
                        vm_ws[i][8] = vm_ws[i][8] + wsthc[i];
                        if (vmax_ws[i][8] < wsthc[i]) {
                            vmax_ws[i][8] = wsthc[i];
                        }
                        if (vmin_ws[i][8] > wsthc[i]) {
                            vmin_ws[i][8] = wsthc[i];
                        }
                    }
                }
            }//for

            //计算该时次从0高度开始的各厚度层合成风并进行统计-------------
            for (i = 0; i < 6; i++) {
                //for i in 1..6 loop
                if (cwsthcu0[i] != 0 && cwsthcv0[i] != 0) {
                    //计算各厚度层内矢量风的平均值
                    if (i == 1) {
                        wsthcu0[i] = wsthcu0[i] /(cwsthcu0[i] - 2 * vhgth0);
                        wsthcv0[i] = wsthcv0[i] /(cwsthcv0[i] - 2 * vhgth0);
                    } else {
                        wsthcu0[i] = wsthcu0[i] / cwsthcu0[i];
                        wsthcv0[i] = wsthcv0[i] / cwsthcv0[i];
                    }
                    //计算合成风向
                    if (wsthcu0[i] == 0 && wsthcv0[i] == 0) {
                        wdthc0[i] = 0;
                    }//风向为0表示静稳风
                    if (wsthcu0[i] == 0 && wsthcv0[i] > 0) {
                        wdthc0[i] = 180;
                    }
                    if (wsthcu0[i] == 0 && wsthcv0[i] < 0) {
                        wdthc0[i] = 360;
                    }
                    if (wsthcu0[i] > 0) {
                        wdthc0[i] = (int) (270 - 180 * atan(wsthcv0[i] / wsthcu0[i]) / 3.1415927);
                    }
                    if (wsthcu0[i] < 0) {
                        wdthc0[i] = (int) (180 - 180 * atan(wsthcv0[i] / wsthcu0[i]) / 3.1415927);
                    }
                    //计算合成风速
                    wsthc0[i] = (int) (sqrt(wsthcu0[i] * wsthcu0[i] + wsthcv0[i] * wsthcv0[i]));

                    //进行统计-------------
                    //风向1
                    if (wdthc0[i] > 0 && wdthc0[i] <= 45) {
                        vc_wd0[i][1] = vc_wd0[i][1] + 1;
                        vm_ws0[i][1] = vm_ws0[i][1] + wsthc0[i];
                        if (vmax_ws0[i][1] < wsthc0[i]) {
                            vmax_ws0[i][1] = wsthc0[i];
                        }
                        if (vmin_ws0[i][1] > wsthc0[i]) {
                            vmin_ws0[i][1] = wsthc0[i];
                        }
                    }
                    //风向2
                    if (wdthc0[i] > 45 && wdthc0[i] <= 90) {
                        vc_wd0[i][2] = vc_wd0[i][2] + 1;
                        vm_ws0[i][2] = vm_ws0[i][2] + wsthc0[i];
                        if (vmax_ws0[i][2] < wsthc0[i]) {
                            vmax_ws0[i][2] = wsthc0[i];
                        }
                        if (vmin_ws0[i][2] > wsthc0[i]) {
                            vmin_ws0[i][2] = wsthc0[i];
                        }
                    }
                    //风向3
                    if (wdthc0[i] > 90 && wdthc0[i] <= 135) {
                        vc_wd0[i][3] = vc_wd0[i][3] + 1;
                        vm_ws0[i][3] = vm_ws0[i][3] + wsthc0[i];
                        if (vmax_ws0[i][3] < wsthc0[i]) {
                            vmax_ws0[i][3] = wsthc0[i];
                        }
                        if (vmin_ws0[i][3] > wsthc0[i]) {
                            vmin_ws0[i][3] = wsthc0[i];
                        }
                    }
                    //风向4
                    if (wdthc0[i] > 135 && wdthc0[i] <= 180) {
                        vc_wd0[i][4] = vc_wd0[i][4] + 1;
                        vm_ws0[i][4] = vm_ws0[i][4] + wsthc0[i];
                        if (vmax_ws0[i][4] < wsthc0[i]) {
                            vmax_ws0[i][4] = wsthc0[i];
                        }
                        if (vmin_ws0[i][4] > wsthc0[i]) {
                            vmin_ws0[i][4] = wsthc0[i];
                        }
                    }
                    //风向5
                    if (wdthc0[i] > 180 && wdthc0[i] <= 225) {
                        vc_wd0[i][5] = vc_wd0[i][5] + 1;
                        vm_ws0[i][5] = vm_ws0[i][5] + wsthc0[i];
                        if (vmax_ws0[i][5] < wsthc0[i]) {
                            vmax_ws0[i][5] = wsthc0[i];
                        }
                        if (vmin_ws0[i][5] > wsthc0[i]) {
                            vmin_ws0[i][5] = wsthc0[i];
                        }
                    }
                    //风向6
                    if (wdthc0[i] > 225 && wdthc0[i] <= 270) {
                        vc_wd0[i][6] = vc_wd0[i][6] + 1;
                        vm_ws0[i][6] = vm_ws0[i][6] + wsthc0[i];
                        if (vmax_ws0[i][6] < wsthc0[i]) {
                            vmax_ws0[i][6] = wsthc0[i];
                        }
                        if (vmin_ws0[i][6] > wsthc0[i]) {
                            vmin_ws0[i][6] = wsthc0[i];
                        }
                    }
                    //风向7
                    if (wdthc0[i] > 270 && wdthc0[i] <= 315) {
                        vc_wd0[i][7] = vc_wd0[i][7] + 1;
                        vm_ws0[i][7] = vm_ws0[i][7] + wsthc0[i];
                        if (vmax_ws0[i][7] < wsthc0[i]) {
                            vmax_ws0[i][7] = wsthc0[i];
                        }
                        if (vmin_ws0[i][7] > wsthc0[i]) {
                            vmin_ws0[i][7] = wsthc0[i];
                        }
                    }
                    //风向8
                    if (wdthc0[i] > 315 && wdthc0[i] <= 360) {
                        vc_wd0[i][8] = vc_wd0[i][8] + 1;
                        vm_ws0[i][8] = vm_ws0[i][8] + wsthc0[i];
                        if (vmax_ws0[i][8] < wsthc0[i]) {
                            vmax_ws0[i][8] = wsthc0[i];
                        }
                        if (vmin_ws0[i][8] > wsthc0[i]) {
                            vmin_ws0[i][8] = wsthc0[i];
                        }
                    }
                }
            }//for


            //时次变量重新初始化----------

            //当站、年代区间、月，各要素在各气压层上的值
            chgt = new Integer[]{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
            cwindu = new Integer[]{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};
            cwindv = new Integer[]{null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null};

            //时次的厚度合成风计算所用变量------------------
            //统计要素在各厚度层上的值
            wsthcu = new Integer[]{0, 0, 0, 0, 0, 0};
            wsthcv = new Integer[]{0, 0, 0, 0, 0, 0};
            wsthc = new Integer[]{0, 0, 0, 0, 0, 0};
            wdthc = new Integer[]{0, 0, 0, 0, 0, 0};
            //目前时次，各要素在各厚度层有效高度层的计数
            cwsthcu = new Integer[]{0, 0, 0, 0, 0, 0};
            cwsthcv = new Integer[]{0, 0, 0, 0, 0, 0};

            //统计要素在从0高度开始的各厚度层上的值
            wsthcu0 = new Integer[]{0, 0, 0, 0, 0, 0};
            wsthcv0 = new Integer[]{0, 0, 0, 0, 0, 0};
            wsthc0 = new Integer[]{0, 0, 0, 0, 0, 0};
            wdthc0 = new Integer[]{0, 0, 0, 0, 0, 0};
            //目前时次，各要素在从0高度开始的各厚度层有效高度层的计数
            cwsthcu0 = new Integer[]{0, 0, 0, 0, 0, 0};
            cwsthcv0 = new Integer[]{0, 0, 0, 0, 0, 0};

            //更新旧日、时次变量-----------
            ihour = vhour;
            iday = vday;

            //更新一月内的有效时次计数-------
            inum_hour = inum_hour + 1;

            //有效气压层计数清零
            inum_press = 0;

        } //时次判断结束



        //月判断---------------------------------------------------------------------------------------
        if ((imonth!=vmonth || iislast==1) && inum_hour!=0){
        //处理月各厚度层各风向下的平均风速
            for(i=0;i<6;i++) {
                for (int j = 0; j < 8; j++) {
                    if (vc_wd[i][j] != 0){
                        vm_ws[i][j] = vm_ws[i][j] / vc_wd[i][j];
                    }
                }
            }

       //将默认值的要素置空(除风计数)
        for(i=0;i<6;i++){
            for(int j=0;j<8;j++){
                if (vc_wd[i][j]==0){
                vm_ws[i][j]=null;
                }
                if (vmax_ws[i][j]==-999){vmax_ws[i][j]=null; }
                if (vmin_ws[i][j]==999){vmin_ws[i][j]=null; }
            }
        }

       //处理月从0高度开始的各厚度层各风向下的平均风速
        for(i=0;i<6;i++){
            for(int j=0;j<8;j++){
                if (vc_wd0[i][j]!=0){
                vm_ws0[i][j]=vm_ws0[i][j]/vc_wd0[i][j];
                }
            }
        }

       //将默认值的要素置空(除风计数)
        for(i=0;i<6;i++){
            for(int j=0;j<8;j++){
            if (vc_wd0[i][j]==0){
            vm_ws0[i][j]=null;
            }
            if (vmax_ws0[i][j]==-999){vmax_ws0[i][j]=null; }
            if (vmin_ws0[i][j]==999){vmin_ws0[i][j]=null; }
            }
        }

       //insert ldb_stat_over_up_wind_thc 历年月统计数据。如果各个字段不都为空就插入 。


            upThcParam thcP = new upThcParam();
            thcP.setStation(vstation);
            thcP.setYear(viyear);
            thcP.setMonth(imonth);
            thcP.setLatitude(vlatitude.intValue());
            thcP.setLongitude(vlongitude.intValue());
            thcP.setMax_ws_3_1(vmax_ws[1][1]);
            thcP.setMax_ws_3_2(vmax_ws[1][2]);
            thcP.setMax_ws_3_3(vmax_ws[1][3]);
            thcP.setMax_ws_3_4(vmax_ws[1][4]);
            thcP.setMax_ws_3_5(vmax_ws[1][5]);
            thcP.setMax_ws_3_6(vmax_ws[1][6]);
            thcP.setMax_ws_3_7(vmax_ws[1][7]);
            thcP.setMax_ws_3_8(vmax_ws[1][8]);
            thcP.setMax_ws_6_1(vmax_ws[2][1]);
            thcP.setMax_ws_6_2(vmax_ws[2][2]);
            thcP.setMax_ws_6_3(vmax_ws[2][3]);
            thcP.setMax_ws_6_4(vmax_ws[2][4]);
            thcP.setMax_ws_6_5(vmax_ws[2][5]);
            thcP.setMax_ws_6_6(vmax_ws[2][6]);
            thcP.setMax_ws_6_7(vmax_ws[2][7]);
            thcP.setMax_ws_6_8(vmax_ws[2][8]);
            thcP.setMax_ws_9_1(vmax_ws[3][1]);
            thcP.setMax_ws_9_2(vmax_ws[3][2]);
            thcP.setMax_ws_9_3(vmax_ws[3][3]);
            thcP.setMax_ws_9_4(vmax_ws[3][4]);
            thcP.setMax_ws_9_5(vmax_ws[3][5]);
            thcP.setMax_ws_9_6(vmax_ws[3][6]);
            thcP.setMax_ws_9_7(vmax_ws[3][7]);
            thcP.setMax_ws_9_8(vmax_ws[3][8]);
            thcP.setMax_ws_12_1(vmax_ws[4][1]);
            thcP.setMax_ws_12_2(vmax_ws[4][2]);
            thcP.setMax_ws_12_3(vmax_ws[4][3]);
            thcP.setMax_ws_12_4(vmax_ws[4][4]);
            thcP.setMax_ws_12_5(vmax_ws[4][5]);
            thcP.setMax_ws_12_6(vmax_ws[4][6]);
            thcP.setMax_ws_12_7(vmax_ws[4][7]);
            thcP.setMax_ws_12_8(vmax_ws[4][8]);
            thcP.setMax_ws_16_1(vmax_ws[5][1]);
            thcP.setMax_ws_16_2(vmax_ws[5][2]);
            thcP.setMax_ws_16_3(vmax_ws[5][3]);
            thcP.setMax_ws_16_4(vmax_ws[5][4]);
            thcP.setMax_ws_16_5(vmax_ws[5][5]);
            thcP.setMax_ws_16_6(vmax_ws[5][6]);
            thcP.setMax_ws_16_7(vmax_ws[5][7]);
            thcP.setMax_ws_16_8(vmax_ws[5][8]);
            thcP.setMax_ws_20_1(vmax_ws[6][1]);
            thcP.setMax_ws_20_2(vmax_ws[6][2]);
            thcP.setMax_ws_20_3(vmax_ws[6][3]);
            thcP.setMax_ws_20_4(vmax_ws[6][4]);
            thcP.setMax_ws_20_5(vmax_ws[6][5]);
            thcP.setMax_ws_20_6(vmax_ws[6][6]);
            thcP.setMax_ws_20_7(vmax_ws[6][7]);
            thcP.setMax_ws_20_8(vmax_ws[6][8]);
            thcP.setMin_ws_3_1(vmin_ws[1][1]);
            thcP.setMin_ws_3_2(vmin_ws[1][2]);
            thcP.setMin_ws_3_3(vmin_ws[1][3]);
            thcP.setMin_ws_3_4(vmin_ws[1][4]);
            thcP.setMin_ws_3_5(vmin_ws[1][5]);
            thcP.setMin_ws_3_6(vmin_ws[1][6]);
            thcP.setMin_ws_3_7(vmin_ws[1][7]);
            thcP.setMin_ws_3_8(vmin_ws[1][8]);
            thcP.setMin_ws_6_1(vmin_ws[2][1]);
            thcP.setMin_ws_6_2(vmin_ws[2][2]);
            thcP.setMin_ws_6_3(vmin_ws[2][3]);
            thcP.setMin_ws_6_4(vmin_ws[2][4]);
            thcP.setMin_ws_6_5(vmin_ws[2][5]);
            thcP.setMin_ws_6_6(vmin_ws[2][6]);
            thcP.setMin_ws_6_7(vmin_ws[2][7]);
            thcP.setMin_ws_6_8(vmin_ws[2][8]);
            thcP.setMin_ws_9_1(vmin_ws[3][1]);
            thcP.setMin_ws_9_2(vmin_ws[3][2]);
            thcP.setMin_ws_9_3(vmin_ws[3][3]);
            thcP.setMin_ws_9_4(vmin_ws[3][4]);
            thcP.setMin_ws_9_5(vmin_ws[3][5]);
            thcP.setMin_ws_9_6(vmin_ws[3][6]);
            thcP.setMin_ws_9_7(vmin_ws[3][7]);
            thcP.setMin_ws_9_8(vmin_ws[3][8]);
            thcP.setMin_ws_12_1(vmin_ws[4][1]);
            thcP.setMin_ws_12_2(vmin_ws[4][2]);
            thcP.setMin_ws_12_3(vmin_ws[4][3]);
            thcP.setMin_ws_12_4(vmin_ws[4][4]);
            thcP.setMin_ws_12_5(vmin_ws[4][5]);
            thcP.setMin_ws_12_6(vmin_ws[4][6]);
            thcP.setMin_ws_12_7(vmin_ws[4][7]);
            thcP.setMin_ws_12_8(vmin_ws[4][8]);
            thcP.setMin_ws_16_1(vmin_ws[5][1]);
            thcP.setMin_ws_16_2(vmin_ws[5][2]);
            thcP.setMin_ws_16_3(vmin_ws[5][3]);
            thcP.setMin_ws_16_4(vmin_ws[5][4]);
            thcP.setMin_ws_16_5(vmin_ws[5][5]);
            thcP.setMin_ws_16_6(vmin_ws[5][6]);
            thcP.setMin_ws_16_7(vmin_ws[5][7]);
            thcP.setMin_ws_16_8(vmin_ws[5][8]);
            thcP.setMin_ws_20_1(vmin_ws[6][1]);
            thcP.setMin_ws_20_2(vmin_ws[6][2]);
            thcP.setMin_ws_20_3(vmin_ws[6][3]);
            thcP.setMin_ws_20_4(vmin_ws[6][4]);
            thcP.setMin_ws_20_5(vmin_ws[6][5]);
            thcP.setMin_ws_20_6(vmin_ws[6][6]);
            thcP.setMin_ws_20_7(vmin_ws[6][7]);
            thcP.setMin_ws_20_8(vmin_ws[6][8]);
            thcP.setM_ws_3_1(vm_ws[1][1]);
            thcP.setM_ws_3_2(vm_ws[1][2]);
            thcP.setM_ws_3_3(vm_ws[1][3]);
            thcP.setM_ws_3_4(vm_ws[1][4]);
            thcP.setM_ws_3_5(vm_ws[1][5]);
            thcP.setM_ws_3_6(vm_ws[1][6]);
            thcP.setM_ws_3_7(vm_ws[1][7]);
            thcP.setM_ws_3_8(vm_ws[1][8]);
            thcP.setM_ws_6_1(vm_ws[2][1]);
            thcP.setM_ws_6_2(vm_ws[2][2]);
            thcP.setM_ws_6_3(vm_ws[2][3]);
            thcP.setM_ws_6_4(vm_ws[2][4]);
            thcP.setM_ws_6_5(vm_ws[2][5]);
            thcP.setM_ws_6_6(vm_ws[2][6]);
            thcP.setM_ws_6_7(vm_ws[2][7]);
            thcP.setM_ws_6_8(vm_ws[2][8]);
            thcP.setM_ws_9_1(vm_ws[3][1]);
            thcP.setM_ws_9_2(vm_ws[3][2]);
            thcP.setM_ws_9_3(vm_ws[3][3]);
            thcP.setM_ws_9_4(vm_ws[3][4]);
            thcP.setM_ws_9_5(vm_ws[3][5]);
            thcP.setM_ws_9_6(vm_ws[3][6]);
            thcP.setM_ws_9_7(vm_ws[3][7]);
            thcP.setM_ws_9_8(vm_ws[3][8]);
            thcP.setM_ws_12_1(vm_ws[4][1]);
            thcP.setM_ws_12_2(vm_ws[4][2]);
            thcP.setM_ws_12_3(vm_ws[4][3]);
            thcP.setM_ws_12_4(vm_ws[4][4]);
            thcP.setM_ws_12_5(vm_ws[4][5]);
            thcP.setM_ws_12_6(vm_ws[4][6]);
            thcP.setM_ws_12_7(vm_ws[4][7]);
            thcP.setM_ws_12_8(vm_ws[4][8]);
            thcP.setM_ws_16_1(vm_ws[5][1]);
            thcP.setM_ws_16_2(vm_ws[5][2]);
            thcP.setM_ws_16_3(vm_ws[5][3]);
            thcP.setM_ws_16_4(vm_ws[5][4]);
            thcP.setM_ws_16_5(vm_ws[5][5]);
            thcP.setM_ws_16_6(vm_ws[5][6]);
            thcP.setM_ws_16_7(vm_ws[5][7]);
            thcP.setM_ws_16_8(vm_ws[5][8]);
            thcP.setM_ws_20_1(vm_ws[6][1]);
            thcP.setM_ws_20_2(vm_ws[6][2]);
            thcP.setM_ws_20_3(vm_ws[6][3]);
            thcP.setM_ws_20_4(vm_ws[6][4]);
            thcP.setM_ws_20_5(vm_ws[6][5]);
            thcP.setM_ws_20_6(vm_ws[6][6]);
            thcP.setM_ws_20_7(vm_ws[6][7]);
            thcP.setM_ws_20_8(vm_ws[6][8]);
            thcP.setC_ws_3_1(vc_wd[1][1]);
            thcP.setC_ws_3_2(vc_wd[1][2]);
            thcP.setC_ws_3_3(vc_wd[1][3]);
            thcP.setC_ws_3_4(vc_wd[1][4]);
            thcP.setC_ws_3_5(vc_wd[1][5]);
            thcP.setC_ws_3_6(vc_wd[1][6]);
            thcP.setC_ws_3_7(vc_wd[1][7]);
            thcP.setC_ws_3_8(vc_wd[1][8]);
            thcP.setC_ws_6_1(vc_wd[2][1]);
            thcP.setC_ws_6_2(vc_wd[2][2]);
            thcP.setC_ws_6_3(vc_wd[2][3]);
            thcP.setC_ws_6_4(vc_wd[2][4]);
            thcP.setC_ws_6_5(vc_wd[2][5]);
            thcP.setC_ws_6_6(vc_wd[2][6]);
            thcP.setC_ws_6_7(vc_wd[2][7]);
            thcP.setC_ws_6_8(vc_wd[2][8]);
            thcP.setC_ws_9_1(vc_wd[3][1]);
            thcP.setC_ws_9_2(vc_wd[3][2]);
            thcP.setC_ws_9_3(vc_wd[3][3]);
            thcP.setC_ws_9_4(vc_wd[3][4]);
            thcP.setC_ws_9_5(vc_wd[3][5]);
            thcP.setC_ws_9_6(vc_wd[3][6]);
            thcP.setC_ws_9_7(vc_wd[3][7]);
            thcP.setC_ws_9_8(vc_wd[3][8]);
            thcP.setC_ws_12_1(vc_wd[4][1]);
            thcP.setC_ws_12_2(vc_wd[4][2]);
            thcP.setC_ws_12_3(vc_wd[4][3]);
            thcP.setC_ws_12_4(vc_wd[4][4]);
            thcP.setC_ws_12_5(vc_wd[4][5]);
            thcP.setC_ws_12_6(vc_wd[4][6]);
            thcP.setC_ws_12_7(vc_wd[4][7]);
            thcP.setC_ws_12_8(vc_wd[4][8]);
            thcP.setC_ws_16_1(vc_wd[5][1]);
            thcP.setC_ws_16_2(vc_wd[5][2]);
            thcP.setC_ws_16_3(vc_wd[5][3]);
            thcP.setC_ws_16_4(vc_wd[5][4]);
            thcP.setC_ws_16_5(vc_wd[5][5]);
            thcP.setC_ws_16_6(vc_wd[5][6]);
            thcP.setC_ws_16_7(vc_wd[5][7]);
            thcP.setC_ws_16_8(vc_wd[5][8]);
            thcP.setC_ws_20_1(vc_wd[6][1]);
            thcP.setC_ws_20_2(vc_wd[6][2]);
            thcP.setC_ws_20_3(vc_wd[6][3]);
            thcP.setC_ws_20_4(vc_wd[6][4]);
            thcP.setC_ws_20_5(vc_wd[6][5]);
            thcP.setC_ws_20_6(vc_wd[6][6]);
            thcP.setC_ws_20_7(vc_wd[6][7]);
            thcP.setC_ws_20_8(vc_wd[6][8]);
            thcP.setMax_ws_0_3_1(vmax_ws0[1][1]);
            thcP.setMax_ws_0_3_2(vmax_ws0[1][2]);
            thcP.setMax_ws_0_3_3(vmax_ws0[1][3]);
            thcP.setMax_ws_0_3_4(vmax_ws0[1][4]);
            thcP.setMax_ws_0_3_5(vmax_ws0[1][5]);
            thcP.setMax_ws_0_3_6(vmax_ws0[1][6]);
            thcP.setMax_ws_0_3_7(vmax_ws0[1][7]);
            thcP.setMax_ws_0_3_8(vmax_ws0[1][8]);
            thcP.setMax_ws_0_6_1(vmax_ws0[2][1]);
            thcP.setMax_ws_0_6_2(vmax_ws0[2][2]);
            thcP.setMax_ws_0_6_3(vmax_ws0[2][3]);
            thcP.setMax_ws_0_6_4(vmax_ws0[2][4]);
            thcP.setMax_ws_0_6_5(vmax_ws0[2][5]);
            thcP.setMax_ws_0_6_6(vmax_ws0[2][6]);
            thcP.setMax_ws_0_6_7(vmax_ws0[2][7]);
            thcP.setMax_ws_0_6_8(vmax_ws0[2][8]);
            thcP.setMax_ws_0_9_1(vmax_ws0[3][1]);
            thcP.setMax_ws_0_9_2(vmax_ws0[3][2]);
            thcP.setMax_ws_0_9_3(vmax_ws0[3][3]);
            thcP.setMax_ws_0_9_4(vmax_ws0[3][4]);
            thcP.setMax_ws_0_9_5(vmax_ws0[3][5]);
            thcP.setMax_ws_0_9_6(vmax_ws0[3][6]);
            thcP.setMax_ws_0_9_7(vmax_ws0[3][7]);
            thcP.setMax_ws_0_9_8(vmax_ws0[3][8]);
            thcP.setMax_ws_0_12_1(vmax_ws0[4][1]);
            thcP.setMax_ws_0_12_2(vmax_ws0[4][2]);
            thcP.setMax_ws_0_12_3(vmax_ws0[4][3]);
            thcP.setMax_ws_0_12_4(vmax_ws0[4][4]);
            thcP.setMax_ws_0_12_5(vmax_ws0[4][5]);
            thcP.setMax_ws_0_12_6(vmax_ws0[4][6]);
            thcP.setMax_ws_0_12_7(vmax_ws0[4][7]);
            thcP.setMax_ws_0_12_8(vmax_ws0[4][8]);
            thcP.setMax_ws_0_16_1(vmax_ws0[5][1]);
            thcP.setMax_ws_0_16_2(vmax_ws0[5][2]);
            thcP.setMax_ws_0_16_3(vmax_ws0[5][3]);
            thcP.setMax_ws_0_16_4(vmax_ws0[5][4]);
            thcP.setMax_ws_0_16_5(vmax_ws0[5][5]);
            thcP.setMax_ws_0_16_6(vmax_ws0[5][6]);
            thcP.setMax_ws_0_16_7(vmax_ws0[5][7]);
            thcP.setMax_ws_0_16_8(vmax_ws0[5][8]);
            thcP.setMax_ws_0_20_1(vmax_ws0[6][1]);
            thcP.setMax_ws_0_20_2(vmax_ws0[6][2]);
            thcP.setMax_ws_0_20_3(vmax_ws0[6][3]);
            thcP.setMax_ws_0_20_4(vmax_ws0[6][4]);
            thcP.setMax_ws_0_20_5(vmax_ws0[6][5]);
            thcP.setMax_ws_0_20_6(vmax_ws0[6][6]);
            thcP.setMax_ws_0_20_7(vmax_ws0[6][7]);
            thcP.setMax_ws_0_20_8(vmax_ws0[6][8]);
            thcP.setMin_ws_0_3_1(vmin_ws0[1][1]);
            thcP.setMin_ws_0_3_2(vmin_ws0[1][2]);
            thcP.setMin_ws_0_3_3(vmin_ws0[1][3]);
            thcP.setMin_ws_0_3_4(vmin_ws0[1][4]);
            thcP.setMin_ws_0_3_5(vmin_ws0[1][5]);
            thcP.setMin_ws_0_3_6(vmin_ws0[1][6]);
            thcP.setMin_ws_0_3_7(vmin_ws0[1][7]);
            thcP.setMin_ws_0_3_8(vmin_ws0[1][8]);
            thcP.setMin_ws_0_6_1(vmin_ws0[2][1]);
            thcP.setMin_ws_0_6_2(vmin_ws0[2][2]);
            thcP.setMin_ws_0_6_3(vmin_ws0[2][3]);
            thcP.setMin_ws_0_6_4(vmin_ws0[2][4]);
            thcP.setMin_ws_0_6_5(vmin_ws0[2][5]);
            thcP.setMin_ws_0_6_6(vmin_ws0[2][6]);
            thcP.setMin_ws_0_6_7(vmin_ws0[2][7]);
            thcP.setMin_ws_0_6_8(vmin_ws0[2][8]);
            thcP.setMin_ws_0_9_1(vmin_ws0[3][1]);
            thcP.setMin_ws_0_9_2(vmin_ws0[3][2]);
            thcP.setMin_ws_0_9_3(vmin_ws0[3][3]);
            thcP.setMin_ws_0_9_4(vmin_ws0[3][4]);
            thcP.setMin_ws_0_9_5(vmin_ws0[3][5]);
            thcP.setMin_ws_0_9_6(vmin_ws0[3][6]);
            thcP.setMin_ws_0_9_7(vmin_ws0[3][7]);
            thcP.setMin_ws_0_9_8(vmin_ws0[3][8]);
            thcP.setMin_ws_0_12_1(vmin_ws0[4][1]);
            thcP.setMin_ws_0_12_2(vmin_ws0[4][2]);
            thcP.setMin_ws_0_12_3(vmin_ws0[4][3]);
            thcP.setMin_ws_0_12_4(vmin_ws0[4][4]);
            thcP.setMin_ws_0_12_5(vmin_ws0[4][5]);
            thcP.setMin_ws_0_12_6(vmin_ws0[4][6]);
            thcP.setMin_ws_0_12_7(vmin_ws0[4][7]);
            thcP.setMin_ws_0_12_8(vmin_ws0[4][8]);
            thcP.setMin_ws_0_16_1(vmin_ws0[5][1]);
            thcP.setMin_ws_0_16_2(vmin_ws0[5][2]);
            thcP.setMin_ws_0_16_3(vmin_ws0[5][3]);
            thcP.setMin_ws_0_16_4(vmin_ws0[5][4]);
            thcP.setMin_ws_0_16_5(vmin_ws0[5][5]);
            thcP.setMin_ws_0_16_6(vmin_ws0[5][6]);
            thcP.setMin_ws_0_16_7(vmin_ws0[5][7]);
            thcP.setMin_ws_0_16_8(vmin_ws0[5][8]);
            thcP.setMin_ws_0_20_1(vmin_ws0[6][1]);
            thcP.setMin_ws_0_20_2(vmin_ws0[6][2]);
            thcP.setMin_ws_0_20_3(vmin_ws0[6][3]);
            thcP.setMin_ws_0_20_4(vmin_ws0[6][4]);
            thcP.setMin_ws_0_20_5(vmin_ws0[6][5]);
            thcP.setMin_ws_0_20_6(vmin_ws0[6][6]);
            thcP.setMin_ws_0_20_7(vmin_ws0[6][7]);
            thcP.setMin_ws_0_20_8(vmin_ws0[6][8]);
            thcP.setM_ws_0_3_1(vm_ws0[1][1]);
            thcP.setM_ws_0_3_2(vm_ws0[1][2]);
            thcP.setM_ws_0_3_3(vm_ws0[1][3]);
            thcP.setM_ws_0_3_4(vm_ws0[1][4]);
            thcP.setM_ws_0_3_5(vm_ws0[1][5]);
            thcP.setM_ws_0_3_6(vm_ws0[1][6]);
            thcP.setM_ws_0_3_7(vm_ws0[1][7]);
            thcP.setM_ws_0_3_8(vm_ws0[1][8]);
            thcP.setM_ws_0_6_1(vm_ws0[2][1]);
            thcP.setM_ws_0_6_2(vm_ws0[2][2]);
            thcP.setM_ws_0_6_3(vm_ws0[2][3]);
            thcP.setM_ws_0_6_4(vm_ws0[2][4]);
            thcP.setM_ws_0_6_5(vm_ws0[2][5]);
            thcP.setM_ws_0_6_6(vm_ws0[2][6]);
            thcP.setM_ws_0_6_7(vm_ws0[2][7]);
            thcP.setM_ws_0_6_8(vm_ws0[2][8]);
            thcP.setM_ws_0_9_1(vm_ws0[3][1]);
            thcP.setM_ws_0_9_2(vm_ws0[3][2]);
            thcP.setM_ws_0_9_3(vm_ws0[3][3]);
            thcP.setM_ws_0_9_4(vm_ws0[3][4]);
            thcP.setM_ws_0_9_5(vm_ws0[3][5]);
            thcP.setM_ws_0_9_6(vm_ws0[3][6]);
            thcP.setM_ws_0_9_7(vm_ws0[3][7]);
            thcP.setM_ws_0_9_8(vm_ws0[3][8]);
            thcP.setM_ws_0_12_1(vm_ws0[4][1]);
            thcP.setM_ws_0_12_2(vm_ws0[4][2]);
            thcP.setM_ws_0_12_3(vm_ws0[4][3]);
            thcP.setM_ws_0_12_4(vm_ws0[4][4]);
            thcP.setM_ws_0_12_5(vm_ws0[4][5]);
            thcP.setM_ws_0_12_6(vm_ws0[4][6]);
            thcP.setM_ws_0_12_7(vm_ws0[4][7]);
            thcP.setM_ws_0_12_8(vm_ws0[4][8]);
            thcP.setM_ws_0_16_1(vm_ws0[5][1]);
            thcP.setM_ws_0_16_2(vm_ws0[5][2]);
            thcP.setM_ws_0_16_3(vm_ws0[5][3]);
            thcP.setM_ws_0_16_4(vm_ws0[5][4]);
            thcP.setM_ws_0_16_5(vm_ws0[5][5]);
            thcP.setM_ws_0_16_6(vm_ws0[5][6]);
            thcP.setM_ws_0_16_7(vm_ws0[5][7]);
            thcP.setM_ws_0_16_8(vm_ws0[5][8]);
            thcP.setM_ws_0_20_1(vm_ws0[6][1]);
            thcP.setM_ws_0_20_2(vm_ws0[6][2]);
            thcP.setM_ws_0_20_3(vm_ws0[6][3]);
            thcP.setM_ws_0_20_4(vm_ws0[6][4]);
            thcP.setM_ws_0_20_5(vm_ws0[6][5]);
            thcP.setM_ws_0_20_6(vm_ws0[6][6]);
            thcP.setM_ws_0_20_7(vm_ws0[6][7]);
            thcP.setM_ws_0_20_8(vm_ws0[6][8]);
            thcP.setC_ws_0_3_1(vc_wd0[1][1]);
            thcP.setC_ws_0_3_2(vc_wd0[1][2]);
            thcP.setC_ws_0_3_3(vc_wd0[1][3]);
            thcP.setC_ws_0_3_4(vc_wd0[1][4]);
            thcP.setC_ws_0_3_5(vc_wd0[1][5]);
            thcP.setC_ws_0_3_6(vc_wd0[1][6]);
            thcP.setC_ws_0_3_7(vc_wd0[1][7]);
            thcP.setC_ws_0_3_8(vc_wd0[1][8]);
            thcP.setC_ws_0_6_1(vc_wd0[2][1]);
            thcP.setC_ws_0_6_2(vc_wd0[2][2]);
            thcP.setC_ws_0_6_3(vc_wd0[2][3]);
            thcP.setC_ws_0_6_4(vc_wd0[2][4]);
            thcP.setC_ws_0_6_5(vc_wd0[2][5]);
            thcP.setC_ws_0_6_6(vc_wd0[2][6]);
            thcP.setC_ws_0_6_7(vc_wd0[2][7]);
            thcP.setC_ws_0_6_8(vc_wd0[2][8]);
            thcP.setC_ws_0_9_1(vc_wd0[3][1]);
            thcP.setC_ws_0_9_2(vc_wd0[3][2]);
            thcP.setC_ws_0_9_3(vc_wd0[3][3]);
            thcP.setC_ws_0_9_4(vc_wd0[3][4]);
            thcP.setC_ws_0_9_5(vc_wd0[3][5]);
            thcP.setC_ws_0_9_6(vc_wd0[3][6]);
            thcP.setC_ws_0_9_7(vc_wd0[3][7]);
            thcP.setC_ws_0_9_8(vc_wd0[3][8]);
            thcP.setC_ws_0_12_1(vc_wd0[4][1]);
            thcP.setC_ws_0_12_2(vc_wd0[4][2]);
            thcP.setC_ws_0_12_3(vc_wd0[4][3]);
            thcP.setC_ws_0_12_4(vc_wd0[4][4]);
            thcP.setC_ws_0_12_5(vc_wd0[4][5]);
            thcP.setC_ws_0_12_6(vc_wd0[4][6]);
            thcP.setC_ws_0_12_7(vc_wd0[4][7]);
            thcP.setC_ws_0_12_8(vc_wd0[4][8]);
            thcP.setC_ws_0_16_1(vc_wd0[5][1]);
            thcP.setC_ws_0_16_2(vc_wd0[5][2]);
            thcP.setC_ws_0_16_3(vc_wd0[5][3]);
            thcP.setC_ws_0_16_4(vc_wd0[5][4]);
            thcP.setC_ws_0_16_5(vc_wd0[5][5]);
            thcP.setC_ws_0_16_6(vc_wd0[5][6]);
            thcP.setC_ws_0_16_7(vc_wd0[5][7]);
            thcP.setC_ws_0_16_8(vc_wd0[5][8]);
            thcP.setC_ws_0_20_1(vc_wd0[6][1]);
            thcP.setC_ws_0_20_2(vc_wd0[6][2]);
            thcP.setC_ws_0_20_3(vc_wd0[6][3]);
            thcP.setC_ws_0_20_4(vc_wd0[6][4]);
            thcP.setC_ws_0_20_5(vc_wd0[6][5]);
            thcP.setC_ws_0_20_6(vc_wd0[6][6]);
            thcP.setC_ws_0_20_7(vc_wd0[6][7]);
            thcP.setC_ws_0_20_8(vc_wd0[6][8] );


            tongjimapper.insertOverUpThcTJ(thcP);


        //icnt= icnt + sql%rowcount ;


       //重新初始化------------

               //各厚度层下各风向下要素数组
        for(i=0;i<6;i++){
            vmax_ws[i]=new Integer[]{-999,-999,-999,-999,-999,-999,-999,-999};
            vmin_ws[i]=new Integer[]{999,999,999,999,999,999,999,999};
            vm_ws[i]  =new Integer[]{0,0,0,0,0,0,0,0};
            vc_wd[i]  =new Integer[]{0,0,0,0,0,0,0,0};
        }//for i

               //从0高度开始的各厚度层下各风向下要素数组
        for(i=0;i<6;i++){
            vmax_ws0[i]=new Integer[]{-999,-999,-999,-999,-999,-999,-999,-999};
            vmin_ws0[i]=new Integer[]{999,999,999,999,999,999,999,999};
            vm_ws0[i]  =new Integer[]{0,0,0,0,0,0,0,0};
            vc_wd0[i]  =new Integer[]{0,0,0,0,0,0,0,0};
        }//for i


               //更新旧月值
        imonth=vmonth;
       //有效气压层计数清零
        inum_hour=0;
        } //月判断结束


       //如果达到最后一条记录，则退出--
        //if (iislast==1){exit; }

       //气压层-----------------------------------------------------------------------


        //确定当前的气压层
        i=1;
        ip=-1;
        while( i<=16 ){
            if (press[i]==vpress/100){
            ip=i;
            break;
          }else{
            i=i+1;
            }
        }

        if (ip!=-1){
         //计算几何高度
        if (voq_hgt == "01" || voq_hgt == "04" || voq_hgt == "05"){
            chgt[ip] = selftools.tojihehgt(vhgt,(int)(vlatitude/100000));
        }

       //计算纬向风、经向风
        if ( (voq_wind == "01" || voq_hgt == "04" || voq_hgt == "05") && vws != null && vwd != null){
        cwindu[ip]=(int)(vws*sin((vwd/180-1)*3.1415927)/10);
        cwindv[ip]=(int)(vws*cos((vwd/180-1)*3.1415927)/10);
        }

       //更新一时次内的有效气压层计数-------
        inum_press=inum_press+1;

        }

       //气压层统计结束

        }

       //关闭光标
       //close sqlsel;

        return Result.success("统计高空历年厚度合成风完成！");
    }


    /*
     *  统计战略高空累年数据 由ldb_stat_all_up_mn_isb，对核目标统计各几何高度层上的平均绝对温度、大气密度
     *  vstation 站号
     *  vsyear 年 起
     *  veyear 年 止
     */
    public Result doAllHEUpTongJi(Integer vstation,Integer vsyear,Integer veyear){

        //几何高度数组
        Integer[] hgtjihe = new Integer[91];
        //大地高层变量
        Integer ih;
        //标准气压层数组
        Integer[] press = new Integer[16];
        //气压层变量
        Integer ip;

        //各要素在各气压层上的值（用于fetch）
        Integer[] hgt = new Integer[16];
        Integer[] atk = new Integer[16];
        Integer[] density = new Integer[16];

        //月变量
        Integer vmonth;

        //插值所得要素
        Double pressins;
        Double atkins;
        Double densityins;

        //经纬度
        Double vlatitude;
        Double vlongitude;

        //用于在等压面数据不足的情况下，用最后一组可用数据进行插值。目前只用于对树插值----
        //记录最后两个可用的等压面标号
        Integer vokpress1;
        Integer vokpress2;

        //该站的大地高数组的起始位置
        Integer hgtNo;

        //循环变量
        Integer i;

        //测站海拔（几何）高
        Integer haiba_hgt_station;

        //测站大地高
        Integer dadi_hgt_station;

        //该站点的几何高度和大地高之差
        Integer vhgtdelta;


        //初始化------------------------------------------------------------------------------

        //(已要求改为大地高)
        // 标准几何高度数组--0到30000米每隔1000米，0-10000米每隔500米
        hgtjihe = new Integer[]{0,200,400,600,800,1000,1200,1400,1600,1800,2000,2200,2400,2600,2800,3000,3200,3400,3600,3800,4000,4200,4400,4600,4800,5000,5200,5400,5600,5800,6000,6200,6400,6600,6800,7000,7200,7400,7600,7800,8000,8200,8400,8600,8800,9000,9200,9400,9600,9800,10000,10500,11000,11500,12000,12500,13000,13500,14000,14500,15000,15500,16000,16500,17000,17500,18000,18500,19000,19500,20000,20500,21000,21500,22000,22500,23000,23500,24000,24500,25000,25500,26000,26500,27000,27500,28000,28500,29000,29500,30000};
        //标准几何高数组长度
        hgtNo = 1;

        //因为当前没有各站点大地高和海拔高之差，所以先将vhgtdelta置0
        //vhgtdelta:=0;

        //从base.base_station表中读取差异高记录
        List<Object> cyhgtObj = tongjimapper.getUpCyhgt(vstation.toString());
        vhgtdelta = Integer.valueOf(cyhgtObj.get(0).toString());

        //从base.base_station表中读取测站的海拔高
        List<Object> elevationObj = tongjimapper.getUpElevation(vstation.toString());
        haiba_hgt_station = Integer.valueOf(elevationObj.get(0).toString());

        //如果测站海拔高为9999，则为缺测，将测站大地高置为0，使用标准大地高数组，否则生成非标准大地高数组及数组长度
        //根据测站海拔高和差异高计算测站大地高
        if (haiba_hgt_station.equals(9999)) {
            dadi_hgt_station=0;
        }else {
            dadi_hgt_station=haiba_hgt_station + vhgtdelta;
        }

        //根据测站大地高计算针对该测站的大地高度数组及数组起始位置
        i=0;
        while( i<91) {
            if (hgtjihe[i] > dadi_hgt_station) {
                hgtjihe[i - 1] = dadi_hgt_station;
                hgtNo = i - 1;
                break;
            }
            i = i + 1;
        }






        //初始化标准高度数组
        press= new Integer[]{1000, 925, 850, 700, 500, 400, 300, 250, 200, 150, 100, 70, 50, 30, 20, 10};

        //各要素在各气压层上的值（用于fetch）
        hgt     = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        atk     = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        density = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};


        //插值所得要素
        pressins   =null;
        atkins     =null;
        densityins =null;

        //经纬度
        //因为同一站的经纬度在原始表zh_cards_ele中不唯一，所以此处取平均值
        List<Object> vlatLon = tongjimapper.getUplatlon(vstation);
        vlatitude = Double.valueOf(vlatLon.get(0).toString());
        vlongitude = Double.valueOf(vlatLon.get(1).toString());


        //删除旧记录---------------------------
        upHeParam ucParam = new upHeParam();
        ucParam.setStation(vstation);
        ucParam.setSyear(vsyear);
        ucParam.setEyear(veyear);
        tongjimapper.deleteAllUpHeHisRecord(ucParam);



        //对各月-----------------------------------------------------------------------------------------
        for(vmonth = 1;vmonth<=12;vmonth++){

            //fetch出各气压层的要素---------------
            //将各气压层的要素值读入要素数组
            for(ip = 0;ip<16;ip++) {
                upAllParam uaParam = new upAllParam();
                uaParam.setStation(vstation);
                uaParam.setSyear(vsyear);
                uaParam.setEyear(veyear);
                uaParam.setMonth(vmonth);
                uaParam.setPress(press[ip]);
                List<Map<String,Object>> resList = tongjimapper.getUpFetch(uaParam);
                if(resList.size()>0){
                    hgt[ip] = Integer.valueOf(resList.get(0).get("m_hgt").toString());
                    atk[ip] = Integer.valueOf(resList.get(0).get("m_atk").toString());
                    density[ip] = Integer.valueOf(resList.get(0).get("m_d").toString());

                }

                //将几何高度换算成大地高度
                if (hgt[ip] != null) {
                    hgt[ip] = hgt[ip] + vhgtdelta;
                }
            }

            //用于在等压面数据不足的情况下，用最后一组可用数据进行插值。目前只用于对树插值----
            //记录最后两个可用的等压面标号
            vokpress1 =0;
            vokpress2 =0;

            //对各个高度层进行插值-------------------------------------------------
            for(ih=hgtNo;ih<=91;ih++) {

                //对气压和密度用对数插值-------------------------------------------------------------------------------------


                //如果当前是高度在1000米以下则用1000，850两个气压层数据进行插值
                if (hgtjihe[ih] <= 1000 && hgt[1] != null && hgt[3] != null) {
                    //气压
                    pressins = selftools.logarithm(hgtjihe[ih], press[1], hgt[1], press[3], hgt[3]);
                    //密度
                    if (density[1] != null && density[3] != null) {
                        //进行正常插值
                        densityins = selftools.logarithm(hgtjihe[ih], density[1], hgt[1], density[3], hgt[3]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 = 1;
                        vokpress2 = 3;
                    } else{
                        //进行非正常插值
                        if (vokpress1 != 0 && vokpress2 != 0) {
                            densityins = selftools.logarithm(hgtjihe[ih], density[vokpress1], hgt[vokpress1], density[vokpress2], hgt[vokpress2]);
                        }

                    }
                }

                //如果当前是高度在1000米以上，3000米（含）以下，则用850、700两个气压层数据进行插值
                if (hgtjihe[ih]>1000 && hgtjihe[ih]<=3000 && hgt[3] != null && hgt[4] != null){
                    //气压
                    pressins=selftools.logarithm(hgtjihe[ih],press[3],hgt[3],press[4],hgt[4]);
                    //密度
                    if (density[3] != null && density[4] != null) {
                        densityins = selftools.logarithm(hgtjihe[ih], density[3], hgt[3], density[4], hgt[4]);
                        //刷新最后两个可用的等压面标号
                        vokpress1 = 3;
                        vokpress2 = 4;
                    }else {
                        //进行非正常插值
                        if (vokpress1 != 0 && vokpress2 != 0) {
                            densityins = selftools.logarithm(hgtjihe[ih], density[vokpress1], hgt[vokpress1], density[vokpress2], hgt[vokpress2]);
                        }
                    }
                }


                //如果当前是高度在3000米以上，6500米（含）以下，则用700、400两个气压层数据进行插值
                if (hgtjihe[ih]>3000 && hgtjihe[ih]<=6500 && hgt[4] != null && hgt[6] != null) {
                    //气压
                    pressins = selftools.logarithm(hgtjihe[ih], press[4], hgt[4], press[6], hgt[6]);
                    //密度
                    if (density[4] != null && density[6] != null) {
                        densityins=selftools.logarithm(hgtjihe[ih], density[4], hgt[4], density[6], hgt[6]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 = 4;
                        vokpress2 = 6;
                    } else {
                        //进行非正常插值
                        if (vokpress1 != 0 && vokpress2 != 0) {
                            densityins = selftools.logarithm(hgtjihe[ih], density[vokpress1], hgt[vokpress1], density[vokpress2], hgt[vokpress2]);
                        }
                    }
                }

                //如果当前是高度在6500米以上，9000米（含）以下，则用400、300两个气压层数据进行插值
                if (hgtjihe[ih]>6500 && hgtjihe[ih]<=9000 && hgt[6] != null && hgt[7] != null ) {
                    //气压
                    pressins = selftools.logarithm(hgtjihe[ih], press[6], hgt[6], press[7], hgt[7]);
                    //密度
                    if (density[6] != null && density[7] != null) {
                        densityins = selftools.logarithm(hgtjihe[ih], density[6], hgt[6], density[7], hgt[7]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 = 6;
                        vokpress2 = 7;
                    } else {
                        //进行非正常插值
                        if (vokpress1 != 0 && vokpress2 != 0) {
                            densityins = selftools.logarithm(hgtjihe[ih], density[vokpress1], hgt[vokpress1], density[vokpress2], hgt[vokpress2]);
                        }
                    }
                }

                //如果当前是高度在9000米以上，10000米（含）以下，则用300、250两个气压层数据进行插值
                if(hgtjihe[ih]>9000 && hgtjihe[ih]<=10000 && hgt[7] != null && hgt[8] != null ){
                    //气压
                    pressins=selftools.logarithm(hgtjihe[ih],press[7],hgt[7],press[8],hgt[8]);
                    //密度
                    if(density[7] != null && density[8] != null ){
                        densityins=selftools.logarithm(hgtjihe[ih],density[7],hgt[7],density[8],hgt[8]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 =7; vokpress2 =8;
                    }else{
                        //进行非正常插值
                        if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtjihe[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
                    }
                }

                //如果当前是高度在10000米以上，12000米（含）以下，则用250、200两个气压层数据进行插值
                if(hgtjihe[ih]>10000 && hgtjihe[ih]<=12000 && hgt[8] != null && hgt[9] != null ){
                    //气压
                    pressins=selftools.logarithm(hgtjihe[ih],press[8],hgt[8],press[9],hgt[9]);
                    //密度
                    if(density[8] != null && density[9] != null ){
                        densityins=selftools.logarithm(hgtjihe[ih],density[8],hgt[8],density[9],hgt[9]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 =8; vokpress2 =9;
                    }else{
                        //进行非正常插值
                        if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtjihe[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
                    }
                }

                //如果当前是高度在12000米以上，13000米（含）以下，则用200、150两个气压层数据进行插值
                if(hgtjihe[ih]>12000 && hgtjihe[ih]<=13000 && hgt[9] != null && hgt[10] != null ){
                    //气压
                    pressins=selftools.logarithm(hgtjihe[ih],press[9],hgt[9],press[10],hgt[10]);
                    //密度
                    if(density[9] != null && density[10] != null ){
                        densityins=selftools.logarithm(hgtjihe[ih],density[9],hgt[9],density[10],hgt[10]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 =9; vokpress2 =10;
                    }else{
                        //进行非正常插值
                        if(vokpress1!= 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtjihe[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
                    }
                }

                //如果当前是高度在13000米以上，16000米（含）以下，则用150、100两个气压层数据进行插值
                if(hgtjihe[ih]>13000 && hgtjihe[ih]<=16000 && hgt[10] != null && hgt[11] != null ){
                    //气压
                    pressins=selftools.logarithm(hgtjihe[ih],press[10],hgt[10],press[11],hgt[11]);
                    //密度
                    if(density[10] != null && density[11] != null ){
                        densityins=selftools.logarithm(hgtjihe[ih],density[10],hgt[10],density[11],hgt[11]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 =10; vokpress2 =11;
                    }else{
                        //进行非正常插值
                        if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtjihe[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
                    }
                }

                //如果当前是高度在16000米以上，18000米（含）以下，则用100、70两个气压层数据进行插值
                if(hgtjihe[ih]>16000 && hgtjihe[ih]<=18000 && hgt[11] != null && hgt[12] != null ){
                    //气压
                    pressins=selftools.logarithm(hgtjihe[ih],press[11],hgt[11],press[12],hgt[12]);
                    //密度
                    if(density[11] != null && density[12] != null ){
                        densityins=selftools.logarithm(hgtjihe[ih],density[11],hgt[11],density[12],hgt[12]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 =11; vokpress2 =12;
                    }else{
                        //进行非正常插值
                        if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtjihe[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
                    }
                }

                //如果当前是高度在18000米以上，20000米（含）以下，则用70、50两个气压层数据进行插值
                if(hgtjihe[ih]>18000 && hgtjihe[ih]<=20000 && hgt[12] != null && hgt[13] != null ){
                    //气压
                    pressins=selftools.logarithm(hgtjihe[ih],press[12],hgt[12],press[13],hgt[13]);
                    //密度
                    if(density[12] != null && density[13] != null ){
                        densityins=selftools.logarithm(hgtjihe[ih],density[12],hgt[12],density[13],hgt[13]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 =12; vokpress2 =13;
                    }else{
                        //进行非正常插值
                        if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtjihe[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
                    }
                }

                //如果当前是高度在20000米以上，24000米（含）以下，则用50、30两个气压层数据进行插值
                if(hgtjihe[ih]>20000 && hgtjihe[ih]<=24000 && hgt[13] != null && hgt[14] != null ){
                    //气压
                    pressins=selftools.logarithm(hgtjihe[ih],press[13],hgt[13],press[14],hgt[14]);
                    //密度
                    if(density[13] != null && density[14] != null ){
                        densityins=selftools.logarithm(hgtjihe[ih],density[13],hgt[13],density[14],hgt[14]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 =13; vokpress2 =14;
                    }else{
                        //进行非正常插值
                        if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtjihe[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
                    }
                }

                //如果当前是高度在24000米以上，26000米（含）以下，则用30、20两个气压层数据进行插值
                if(hgtjihe[ih]>24000 && hgtjihe[ih]<=26000 && hgt[14] != null && hgt[15] != null ){
                    //气压
                    pressins=selftools.logarithm(hgtjihe[ih],press[14],hgt[14],press[15],hgt[15]);
                    //密度
                    if(density[14] != null && density[15] != null ){
                        densityins=selftools.logarithm(hgtjihe[ih],density[14],hgt[14],density[15],hgt[15]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 =14; vokpress2 =15;
                    }else{
                        //进行非正常插值
                        if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtjihe[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
                    }
                }

                //如果当前是高度在26000米以上，30000米（含）以下，则用20、10两个气压层数据进行插值
                if(hgtjihe[ih]>26000 && hgtjihe[ih]<=30000 && hgt[15] != null && hgt[16] != null ){
                    //气压
                    pressins=selftools.logarithm(hgtjihe[ih],press[15],hgt[15],press[16],hgt[16]);
                    //密度
                    if(density[15] != null && density[16] != null ){
                        densityins=selftools.logarithm(hgtjihe[ih],density[15],hgt[15],density[16],hgt[16]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 =15; vokpress2 =16;
                    }else{
                        //进行非正常插值
                        if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtjihe[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
                    }
                }

                //对其它要素用拉格朗日插值//////////////////////////////////////////////////////////////////////////////////////////

                //如果当前是高度在2000米以下则用1000，850和700三个气压层数据进行插值
                if(hgtjihe[ih]<=2000 && hgt[1] != null && hgt[3] != null && hgt[4] != null ){

                    //绝对温度
                    if(atk[1] != null && atk[3] != null && atk[4] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[1],hgt[1],atk[3],hgt[3],atk[4],hgt[4]);
                    }

                }

                //如果当前是高度在2000米以上，4500（含）以下则用850、700和500三个气压层数据进行插值
                if(hgtjihe[ih]>2000 && hgtjihe[ih] <=4500 && hgt[3] != null && hgt[4] != null && hgt[5] != null ){

                    //绝对温度
                    if(atk[3] != null && atk[4] != null && atk[5] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[3],hgt[3],atk[4],hgt[4],atk[5],hgt[5]);
                    }

                }

                //如果当前是高度在4500米以上，6000（含）以下则用700、500和400三个气压层数据进行插值
                if(hgtjihe[ih]>4500 && hgtjihe[ih] <=6000 && hgt[4] != null && hgt[5] != null && hgt[6] != null ){

                    //绝对温度
                    if(atk[4] != null && atk[5] != null && atk[6] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[4],hgt[4],atk[5],hgt[5],atk[6],hgt[6]);
                    }

                }

                //如果当前是高度在6000米以上，8500（含）以下则用500、400和300三个气压层数据进行插值
                if(hgtjihe[ih]>6000 && hgtjihe[ih] <=8500 && hgt[5] != null && hgt[6] != null && hgt[7] != null ){

                    //绝对温度
                    if(atk[5] != null && atk[6] != null && atk[7] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[5],hgt[5],atk[6],hgt[6],atk[7],hgt[7]);
                    }

                }

                //如果当前是高度在8500米以上，9500米（含）以下则用400、300和250三个气压层数据进行插值
                if(hgtjihe[ih]>8500 && hgtjihe[ih] <=9500 && hgt[6] != null && hgt[7] != null && hgt[8] != null ){

                    //绝对温度
                    if(atk[6] != null && atk[7] != null && atk[8] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[6],hgt[6],atk[7],hgt[7],atk[8],hgt[8]);
                    }

                }

                //如果当前是高度在9500米以上，10500米（含）以下则用300、250和200三个气压层数据进行插值
                if(hgtjihe[ih]>9500 && hgtjihe[ih] <=10500 && hgt[7] != null && hgt[8] != null && hgt[9] != null ){

                    //绝对温度
                    if(atk[7] != null && atk[8] != null && atk[9] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[7],hgt[7],atk[8],hgt[8],atk[9],hgt[9]);
                    }

                }


                //如果当前是高度在10500米以上，12500米（含）以下则用250、200和150三个气压层数据进行插值
                if(hgtjihe[ih]>10500 && hgtjihe[ih] <=12500 && hgt[8] != null && hgt[9] != null && hgt[10] != null ){

                    //绝对温度
                    if(atk[8] != null && atk[9] != null && atk[10] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[8],hgt[8],atk[9],hgt[9],atk[10],hgt[10]);
                    }

                }

                //如果当前是高度在12500米以上，14500米（含）以下则用200、150和100三个气压层数据进行插值
                if(hgtjihe[ih]>12500 && hgtjihe[ih] <=14500 && hgt[9] != null && hgt[10] != null && hgt[11] != null ){

                    //绝对温度
                    if(atk[9] != null && atk[10] != null && atk[11] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[9],hgt[9],atk[10],hgt[10],atk[11],hgt[11]);
                    }

                }

                //如果当前是高度在14500米以上，16500米（含）以下则用150、100和70三个气压层数据进行插值
                if(hgtjihe[ih]>14500 && hgtjihe[ih] <=16500 && hgt[10] != null && hgt[11] != null && hgt[12] != null ){

                    //绝对温度
                    if(atk[10] != null && atk[11] != null && atk[12] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[10],hgt[10],atk[11],hgt[11],atk[12],hgt[12]);
                    }

                }

                //如果当前是高度在16500米以上，19500米（含）以下则用100、70和50三个气压层数据进行插值
                if(hgtjihe[ih]>16500 && hgtjihe[ih] <=19500 && hgt[11] != null && hgt[12] != null && hgt[13] != null ){

                    //绝对温度
                    if(atk[11] != null && atk[12] != null && atk[13] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[11],hgt[11],atk[12],hgt[12],atk[13],hgt[13]);
                    }

                }


                //如果当前是高度在19500米以上，22500米（含）以下则用70、50和30三个气压层数据进行插值
                if(hgtjihe[ih]>19500 && hgtjihe[ih] <=22500 && hgt[12] != null && hgt[13] != null && hgt[14] != null ){

                    //绝对温度
                    if(atk[12] != null && atk[13] != null && atk[14] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[12],hgt[12],atk[13],hgt[13],atk[14],hgt[14]);
                    }

                }


                //如果当前是高度在22500米以上，25500米（含）以下则用50、30和20三个气压层数据进行插值
                if(hgtjihe[ih]>22500 && hgtjihe[ih] <=25500 && hgt[13] != null && hgt[14] != null && hgt[15] != null ){

                    //绝对温度
                    if(atk[13] != null && atk[14] != null && atk[15] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[13],hgt[13],atk[14],hgt[14],atk[15],hgt[15]);
                    }

                }


                //如果当前是高度在25500米以上，30000米（含）以下则用30、20和10三个气压层数据进行插值
                if(hgtjihe[ih]>25500 && hgtjihe[ih] <=30000 && hgt[14] != null && hgt[15] != null && hgt[16] != null ){

                    //绝对温度
                    if(atk[14] != null && atk[15] != null && atk[16] != null ){
                        atkins=selftools.lagrange(hgtjihe[ih],atk[14],hgt[14],atk[15],hgt[15],atk[16],hgt[16]);
                    }

                }


                //将插值结果记入ldb_stat_all_he_mn_hgt-------------------------------------------------------------------
                // 根据表结构设计，暂时不插入气压数据

                upHeParam tjparam = new upHeParam();
                tjparam.setStation(vstation);
                tjparam.setSyear(vsyear);
                tjparam.setEyear(veyear);
                tjparam.setMonth(vmonth);
                tjparam.setLatitude(vlatitude.intValue());
                tjparam.setLongitude(vlongitude.intValue());
                tjparam.setHgtjihe(hgtjihe[ih].doubleValue());
                tjparam.setM_atk(atkins);
                tjparam.setM_d(densityins);

                //开始插入
                tongjimapper.insertAllUpHeTJ(tjparam);

                //变量重新初始化---------------------------------
                //插值所得要素
                pressins   =null;
                atkins     =null;
                densityins =null;


            } //各层

            //变量重新初始化---------------------------------
            //当站、年代区间、月，各要素在各气压层上的值（用于fetch）
            hgt     = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
            atk     =  new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
            density =  new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};


        } //各月

        return Result.success("战略高空累年统计完成");
    }



    /*
     *  统计常规高空累年数据 由ldb_stat_all_up_mn_isb，对常规和巡航目标统计各标准大地高度层上的平均绝对温度、大气密度、纬向风、经向风和大气折射指数
     *  vstation 站号
     *  vsyear 年 起
     *  veyear 年 止
     */
    public Result doAllCGXHUpTongJi(Integer vstation,Integer vsyear,Integer veyear){

        //高度数组
        //type hgt_array is varray(41) of number;
        //标准大地高度数组
        Integer[] hgtdadi = new Integer[41];
        //大地高层变量
        Integer ih;
        //该站点的几何高度和大地高之差
        Integer vhgtdelta;
        //标准气压层数组
        Integer[] press = new Integer[16];
        //气压层变量
        Integer ip;

        //各要素在各气压层上的值（用于fetch）
        Integer[] hgt = new Integer[16];
        Integer[] atk = new Integer[16];
        Integer[] density = new Integer[16];
        Integer[] windu = new Integer[16];
        Integer[] windv = new Integer[16];
        Integer[] aste = new Integer[16];

        //月变量
        Integer vmonth;

        //游标
        //cursor sqlsel (vmonth in number, vpress in number ) is select m_hgt,m_atk,m_d,m_u,m_v,m_aste from zp_all_cards_mn where station=vstation and syear=vsyear and eyear=veyear and month=vmonth and press=vpress;

        //插值所得要素
        Double pressins;
        Double atkins;
        Double densityins;
        Double winduins;
        Double windvins;
        Double asteins;

        //经纬度
        Double vlatitude;
        Double vlongitude;

        //用于在等压面数据不足的情况下，用最后一组可用数据进行插值。目前只用于对树插值----
           //记录最后两个可用的等压面标号
        Integer vokpress1;
        Integer vokpress2;

        //该站的大地高数组的起始位置
        Integer hgtNo;

        //循环变量
        Integer i;

        //测站海拔（几何）高
        Integer haiba_hgt_station;

        //测站大地高
        Integer dadi_hgt_station;


        //初始化------------------------------------------------------------------------------

         //标准大地高度数组--0到30000米每隔1000米，0-10000米每隔500米
        hgtdadi = new Integer[]{0, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500, 5000, 5500, 6000, 6500, 7000, 7500, 8000, 8500, 9000, 9500, 10000, 11000, 12000, 13000, 14000, 15000, 16000, 17000, 18000, 19000, 20000, 21000, 22000, 23000, 24000, 25000, 26000, 27000, 28000, 29000, 30000};
        //标准大地高数组长度
        hgtNo = 1;

        //因为当前没有各站点大地高和海拔高之差，所以先将vhgtdelta置0
        //vhgtdelta:=0;

        //从base.base_station表中读取差异高记录
        List<Object> cyhgtObj = tongjimapper.getUpCyhgt(vstation.toString());
        vhgtdelta = Integer.valueOf(cyhgtObj.get(0).toString());

        //从base.base_station表中读取测站的海拔高
        List<Object> elevationObj = tongjimapper.getUpElevation(vstation.toString());
        haiba_hgt_station = Integer.valueOf(elevationObj.get(0).toString());

        //如果测站海拔高为9999，则为缺测，将测站大地高置为0，使用标准大地高数组，否则生成非标准大地高数组及数组起始位置
          //根据测站海拔高和差异高计算测站大地高
        if (haiba_hgt_station.equals(9999)) {
            dadi_hgt_station=0;
        }else {
            dadi_hgt_station=haiba_hgt_station + vhgtdelta;
        }

        //根据测站大地高计算针对该测站的大地高度数组及数组起始位置
          //0-10000米每隔500米
        i=0;
        while( i<41) {
            if (hgtdadi[i] > dadi_hgt_station) {
                hgtdadi[i - 1] = dadi_hgt_station;
                hgtNo = i - 1;
                break;
            }
            i = i + 1;
        }






        //初始化标准高度数组
        press= new Integer[]{1000, 925, 850, 700, 500, 400, 300, 250, 200, 150, 100, 70, 50, 30, 20, 10};

        //各要素在各气压层上的值（用于fetch）
        hgt     = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        atk     = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        density = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        windu   = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        windv   = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        aste    = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};


        //插值所得要素
        pressins   =null;
        atkins     =null;
        densityins =null;
        winduins   =null;
        windvins   =null;
        asteins    =null;

        //经纬度
         //因为同一站的经纬度在原始表zh_cards_ele中不唯一，所以此处取平均值
        List<Object> vlatLon = tongjimapper.getUplatlon(vstation);
        vlatitude = Double.valueOf(vlatLon.get(0).toString());
        vlongitude = Double.valueOf(vlatLon.get(1).toString());


        //删除旧记录---------------------------
        upCgxhParam ucParam = new upCgxhParam();
        ucParam.setStation(vstation);
        ucParam.setSyear(vsyear);
        ucParam.setEyear(veyear);
        tongjimapper.deleteAllUpCgxhHisRecord(ucParam);



        //对各月-----------------------------------------------------------------------------------------
        for(vmonth = 1;vmonth<=12;vmonth++){

        //fetch出各气压层的要素---------------
        //将各气压层的要素值读入要素数组
            for(ip = 0;ip<16;ip++) {
                upAllParam uaParam = new upAllParam();
                uaParam.setStation(vstation);
                uaParam.setSyear(vsyear);
                uaParam.setEyear(veyear);
                uaParam.setMonth(vmonth);
                uaParam.setPress(press[ip]);
                List<Map<String,Object>> resList = tongjimapper.getUpFetch(uaParam);
                if(resList.size()>0){
                    hgt[ip] = Integer.valueOf(resList.get(0).get("m_hgt").toString());
                    atk[ip] = Integer.valueOf(resList.get(0).get("m_atk").toString());
                    density[ip] = Integer.valueOf(resList.get(0).get("m_d").toString());
                    windu[ip] = Integer.valueOf(resList.get(0).get("m_u").toString());
                    windv[ip] = Integer.valueOf(resList.get(0).get("m_v").toString());
                    aste[ip] = Integer.valueOf(resList.get(0).get("m_aste").toString());
                }

                //将几何高度换算成大地高度
                if (hgt[ip] != null) {
                    hgt[ip] = hgt[ip] + vhgtdelta;
                }
            }

            //用于在等压面数据不足的情况下，用最后一组可用数据进行插值。目前只用于对树插值----
              //记录最后两个可用的等压面标号
            vokpress1 =0;
            vokpress2 =0;

            //对各个高度层进行插值-------------------------------------------------
            for(ih=hgtNo;ih<=41;ih++) {

                //对气压和密度用对数插值-------------------------------------------------------------------------------------


                //如果当前是高度在1000米以下则用1000，850两个气压层数据进行插值
                if (hgtdadi[ih] <= 1000 && hgt[1] != null && hgt[3] != null) {
                    //气压
                    pressins = selftools.logarithm(hgtdadi[ih], press[1], hgt[1], press[3], hgt[3]);
                    //密度
                    if (density[1] != null && density[3] != null) {
                        //进行正常插值
                        densityins = selftools.logarithm(hgtdadi[ih], density[1], hgt[1], density[3], hgt[3]);

                        //刷新最后两个可用的等压面标号
                        vokpress1 = 1;
                        vokpress2 = 3;
                    } else{
                        //进行非正常插值
                        if (vokpress1 != 0 && vokpress2 != 0) {
                            densityins = selftools.logarithm(hgtdadi[ih], density[vokpress1], hgt[vokpress1], density[vokpress2], hgt[vokpress2]);
                        }

                    }
                }

        //如果当前是高度在1000米以上，3000米（含）以下，则用850、700两个气压层数据进行插值
        if (hgtdadi[ih]>1000 && hgtdadi[ih]<=3000 && hgt[3] != null && hgt[4] != null){
            //气压
            pressins=selftools.logarithm(hgtdadi[ih],press[3],hgt[3],press[4],hgt[4]);
            //密度
            if (density[3] != null && density[4] != null) {
                densityins = selftools.logarithm(hgtdadi[ih], density[3], hgt[3], density[4], hgt[4]);
                //刷新最后两个可用的等压面标号
                vokpress1 = 3;
                vokpress2 = 4;
            }else {
                //进行非正常插值
                if (vokpress1 != 0 && vokpress2 != 0) {
                    densityins = selftools.logarithm(hgtdadi[ih], density[vokpress1], hgt[vokpress1], density[vokpress2], hgt[vokpress2]);
                }
            }
        }


        //如果当前是高度在3000米以上，6500米（含）以下，则用700、400两个气压层数据进行插值
        if (hgtdadi[ih]>3000 && hgtdadi[ih]<=6500 && hgt[4] != null && hgt[6] != null) {
            //气压
            pressins = selftools.logarithm(hgtdadi[ih], press[4], hgt[4], press[6], hgt[6]);
            //密度
            if (density[4] != null && density[6] != null) {
                densityins=selftools.logarithm(hgtdadi[ih], density[4], hgt[4], density[6], hgt[6]);

                //刷新最后两个可用的等压面标号
                vokpress1 = 4;
                vokpress2 = 6;
            } else {
                //进行非正常插值
                if (vokpress1 != 0 && vokpress2 != 0) {
                    densityins = selftools.logarithm(hgtdadi[ih], density[vokpress1], hgt[vokpress1], density[vokpress2], hgt[vokpress2]);
                }
            }
        }

        //如果当前是高度在6500米以上，9000米（含）以下，则用400、300两个气压层数据进行插值
        if (hgtdadi[ih]>6500 && hgtdadi[ih]<=9000 && hgt[6] != null && hgt[7] != null ) {
            //气压
            pressins = selftools.logarithm(hgtdadi[ih], press[6], hgt[6], press[7], hgt[7]);
            //密度
            if (density[6] != null && density[7] != null) {
                densityins = selftools.logarithm(hgtdadi[ih], density[6], hgt[6], density[7], hgt[7]);

                //刷新最后两个可用的等压面标号
                vokpress1 = 6;
                vokpress2 = 7;
            } else {
                //进行非正常插值
                if (vokpress1 != 0 && vokpress2 != 0) {
                    densityins = selftools.logarithm(hgtdadi[ih], density[vokpress1], hgt[vokpress1], density[vokpress2], hgt[vokpress2]);
                }
            }
        }

        //如果当前是高度在9000米以上，10000米（含）以下，则用300、250两个气压层数据进行插值
        if(hgtdadi[ih]>9000 && hgtdadi[ih]<=10000 && hgt[7] != null && hgt[8] != null ){
            //气压
            pressins=selftools.logarithm(hgtdadi[ih],press[7],hgt[7],press[8],hgt[8]);
            //密度
            if(density[7] != null && density[8] != null ){
            densityins=selftools.logarithm(hgtdadi[ih],density[7],hgt[7],density[8],hgt[8]);

            //刷新最后两个可用的等压面标号
            vokpress1 =7; vokpress2 =8;
            }else{
            //进行非正常插值
            if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtdadi[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
            }
        }

        //如果当前是高度在10000米以上，12000米（含）以下，则用250、200两个气压层数据进行插值
        if(hgtdadi[ih]>10000 && hgtdadi[ih]<=12000 && hgt[8] != null && hgt[9] != null ){
                //气压
            pressins=selftools.logarithm(hgtdadi[ih],press[8],hgt[8],press[9],hgt[9]);
            //密度
            if(density[8] != null && density[9] != null ){
            densityins=selftools.logarithm(hgtdadi[ih],density[8],hgt[8],density[9],hgt[9]);

            //刷新最后两个可用的等压面标号
            vokpress1 =8; vokpress2 =9;
            }else{
            //进行非正常插值
            if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtdadi[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
            }
        }

        //如果当前是高度在12000米以上，13000米（含）以下，则用200、150两个气压层数据进行插值
        if(hgtdadi[ih]>12000 && hgtdadi[ih]<=13000 && hgt[9] != null && hgt[10] != null ){
                //气压
            pressins=selftools.logarithm(hgtdadi[ih],press[9],hgt[9],press[10],hgt[10]);
            //密度
            if(density[9] != null && density[10] != null ){
            densityins=selftools.logarithm(hgtdadi[ih],density[9],hgt[9],density[10],hgt[10]);

            //刷新最后两个可用的等压面标号
            vokpress1 =9; vokpress2 =10;
            }else{
            //进行非正常插值
            if(vokpress1!= 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtdadi[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
            }
        }

        //如果当前是高度在13000米以上，16000米（含）以下，则用150、100两个气压层数据进行插值
        if(hgtdadi[ih]>13000 && hgtdadi[ih]<=16000 && hgt[10] != null && hgt[11] != null ){
                //气压
            pressins=selftools.logarithm(hgtdadi[ih],press[10],hgt[10],press[11],hgt[11]);
            //密度
            if(density[10] != null && density[11] != null ){
            densityins=selftools.logarithm(hgtdadi[ih],density[10],hgt[10],density[11],hgt[11]);

            //刷新最后两个可用的等压面标号
            vokpress1 =10; vokpress2 =11;
            }else{
            //进行非正常插值
            if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtdadi[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
            }
        }

        //如果当前是高度在16000米以上，18000米（含）以下，则用100、70两个气压层数据进行插值
        if(hgtdadi[ih]>16000 && hgtdadi[ih]<=18000 && hgt[11] != null && hgt[12] != null ){
                //气压
            pressins=selftools.logarithm(hgtdadi[ih],press[11],hgt[11],press[12],hgt[12]);
            //密度
            if(density[11] != null && density[12] != null ){
            densityins=selftools.logarithm(hgtdadi[ih],density[11],hgt[11],density[12],hgt[12]);

            //刷新最后两个可用的等压面标号
            vokpress1 =11; vokpress2 =12;
            }else{
            //进行非正常插值
            if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtdadi[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
            }
        }

        //如果当前是高度在18000米以上，20000米（含）以下，则用70、50两个气压层数据进行插值
        if(hgtdadi[ih]>18000 && hgtdadi[ih]<=20000 && hgt[12] != null && hgt[13] != null ){
                //气压
            pressins=selftools.logarithm(hgtdadi[ih],press[12],hgt[12],press[13],hgt[13]);
            //密度
            if(density[12] != null && density[13] != null ){
            densityins=selftools.logarithm(hgtdadi[ih],density[12],hgt[12],density[13],hgt[13]);

            //刷新最后两个可用的等压面标号
            vokpress1 =12; vokpress2 =13;
            }else{
            //进行非正常插值
            if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtdadi[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
            }
        }

        //如果当前是高度在20000米以上，24000米（含）以下，则用50、30两个气压层数据进行插值
        if(hgtdadi[ih]>20000 && hgtdadi[ih]<=24000 && hgt[13] != null && hgt[14] != null ){
                //气压
            pressins=selftools.logarithm(hgtdadi[ih],press[13],hgt[13],press[14],hgt[14]);
            //密度
            if(density[13] != null && density[14] != null ){
            densityins=selftools.logarithm(hgtdadi[ih],density[13],hgt[13],density[14],hgt[14]);

            //刷新最后两个可用的等压面标号
            vokpress1 =13; vokpress2 =14;
            }else{
            //进行非正常插值
            if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtdadi[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
            }
        }

        //如果当前是高度在24000米以上，26000米（含）以下，则用30、20两个气压层数据进行插值
        if(hgtdadi[ih]>24000 && hgtdadi[ih]<=26000 && hgt[14] != null && hgt[15] != null ){
                //气压
            pressins=selftools.logarithm(hgtdadi[ih],press[14],hgt[14],press[15],hgt[15]);
            //密度
            if(density[14] != null && density[15] != null ){
            densityins=selftools.logarithm(hgtdadi[ih],density[14],hgt[14],density[15],hgt[15]);

            //刷新最后两个可用的等压面标号
            vokpress1 =14; vokpress2 =15;
            }else{
            //进行非正常插值
            if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtdadi[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
            }
        }

        //如果当前是高度在26000米以上，30000米（含）以下，则用20、10两个气压层数据进行插值
        if(hgtdadi[ih]>26000 && hgtdadi[ih]<=30000 && hgt[15] != null && hgt[16] != null ){
                //气压
            pressins=selftools.logarithm(hgtdadi[ih],press[15],hgt[15],press[16],hgt[16]);
            //密度
            if(density[15] != null && density[16] != null ){
            densityins=selftools.logarithm(hgtdadi[ih],density[15],hgt[15],density[16],hgt[16]);

            //刷新最后两个可用的等压面标号
            vokpress1 =15; vokpress2 =16;
            }else{
            //进行非正常插值
            if(vokpress1 != 0 && vokpress2 != 0 ){ densityins=selftools.logarithm(hgtdadi[ih],density[vokpress1],hgt[vokpress1],density[vokpress2],hgt[vokpress2]); }
            }
        }

        //对其它要素用拉格朗日插值//////////////////////////////////////////////////////////////////////////////////////////

                //如果当前是高度在2000米以下则用1000，850和700三个气压层数据进行插值
        if(hgtdadi[ih]<=2000 && hgt[1] != null && hgt[3] != null && hgt[4] != null ){

                //绝对温度
            if(atk[1] != null && atk[3] != null && atk[4] != null ){
                atkins=selftools.lagrange(hgtdadi[ih],atk[1],hgt[1],atk[3],hgt[3],atk[4],hgt[4]);
            }
            //纬向风
            if(windu[1] != null && windu[3] != null && windu[4] != null ){
                winduins=selftools.lagrange(hgtdadi[ih],windu[1],hgt[1],windu[3],hgt[3],windu[4],hgt[4]);
            }
            //经向风
            if(windv[1] != null && windv[3] != null && windv[4] != null ){
                windvins=selftools.lagrange(hgtdadi[ih],windv[1],hgt[1],windv[3],hgt[3],windv[4],hgt[4]);
            }
            //折射率
            if(aste[1] != null && aste[3] != null && aste[4] != null ){
                asteins=selftools.lagrange(hgtdadi[ih],aste[1],hgt[1],aste[3],hgt[3],aste[4],hgt[4]);
            }
        }

        //如果当前是高度在2000米以上，4500（含）以下则用850、700和500三个气压层数据进行插值
        if(hgtdadi[ih]>2000 && hgtdadi[ih] <=4500 && hgt[3] != null && hgt[4] != null && hgt[5] != null ){

            //绝对温度
            if(atk[3] != null && atk[4] != null && atk[5] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[3],hgt[3],atk[4],hgt[4],atk[5],hgt[5]);
            }
            //纬向风
            if(windu[3] != null && windu[4] != null && windu[5] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[3],hgt[3],windu[4],hgt[4],windu[5],hgt[5]);
            }
            //经向风
            if(windv[3] != null && windv[4] != null && windv[5] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[3],hgt[3],windv[4],hgt[4],windv[5],hgt[5]);
            }
            //折射率
            if(aste[3] != null && aste[4] != null && aste[5] != null ){
            asteins=selftools.lagrange(hgtdadi[ih],aste[3],hgt[3],aste[4],hgt[4],aste[5],hgt[5]);
            }
        }

        //如果当前是高度在4500米以上，6000（含）以下则用700、500和400三个气压层数据进行插值
        if(hgtdadi[ih]>4500 && hgtdadi[ih] <=6000 && hgt[4] != null && hgt[5] != null && hgt[6] != null ){

             //绝对温度
            if(atk[4] != null && atk[5] != null && atk[6] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[4],hgt[4],atk[5],hgt[5],atk[6],hgt[6]);
            }
            //纬向风
            if(windu[4] != null && windu[5] != null && windu[6] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[4],hgt[4],windu[5],hgt[5],windu[6],hgt[6]);
            }
            //经向风
            if(windv[4] != null && windv[5] != null && windv[6] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[4],hgt[4],windv[5],hgt[5],windv[6],hgt[6]);
            }
            //折射率
            if(aste[4] != null && aste[5] != null && aste[6] != null ){
            asteins=selftools.lagrange(hgtdadi[ih],aste[4],hgt[4],aste[5],hgt[5],aste[6],hgt[6]);
            }
        }

        //如果当前是高度在6000米以上，8500（含）以下则用500、400和300三个气压层数据进行插值
        if(hgtdadi[ih]>6000 && hgtdadi[ih] <=8500 && hgt[5] != null && hgt[6] != null && hgt[7] != null ){

             //绝对温度
            if(atk[5] != null && atk[6] != null && atk[7] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[5],hgt[5],atk[6],hgt[6],atk[7],hgt[7]);
            }
            //纬向风
            if(windu[5] != null && windu[6] != null && windu[7] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[5],hgt[5],windu[6],hgt[6],windu[7],hgt[7]);
            }
            //经向风
            if(windv[5] != null && windv[6] != null && windv[7] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[5],hgt[5],windv[6],hgt[6],windv[7],hgt[7]);
            }
            //折射率
            if(aste[5] != null && aste[6] != null && aste[7] != null ){
            asteins=selftools.lagrange(hgtdadi[ih],aste[5],hgt[5],aste[6],hgt[6],aste[7],hgt[7]);
            }
        }

        //如果当前是高度在8500米以上，9500米（含）以下则用400、300和250三个气压层数据进行插值
        if(hgtdadi[ih]>8500 && hgtdadi[ih] <=9500 && hgt[6] != null && hgt[7] != null && hgt[8] != null ){

            //绝对温度
            if(atk[6] != null && atk[7] != null && atk[8] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[6],hgt[6],atk[7],hgt[7],atk[8],hgt[8]);
            }
            //纬向风
            if(windu[6] != null && windu[7] != null && windu[8] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[6],hgt[6],windu[7],hgt[7],windu[8],hgt[8]);
            }
            //经向风
            if(windv[6] != null && windv[7] != null && windv[8] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[6],hgt[6],windv[7],hgt[7],windv[8],hgt[8]);
            }
            //折射率
            if(aste[6] != null && aste[7] != null && aste[8] != null ){
            asteins=selftools.lagrange(hgtdadi[ih],aste[6],hgt[6],aste[7],hgt[7],aste[8],hgt[8]);
            }
        }

        //如果当前是高度在9500米以上，10500米（含）以下则用300、250和200三个气压层数据进行插值
        if(hgtdadi[ih]>9500 && hgtdadi[ih] <=10500 && hgt[7] != null && hgt[8] != null && hgt[9] != null ){

             //绝对温度
            if(atk[7] != null && atk[8] != null && atk[9] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[7],hgt[7],atk[8],hgt[8],atk[9],hgt[9]);
            }
            //纬向风
            if(windu[7] != null && windu[8] != null && windu[9] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[7],hgt[7],windu[8],hgt[8],windu[9],hgt[9]);
            }
            //经向风
            if(windv[7] != null && windv[8] != null && windv[9] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[7],hgt[7],windv[8],hgt[8],windv[9],hgt[9]);
            }
            //折射率
            if(aste[7] != null && aste[8] != null && aste[9] != null ){
            asteins=selftools.lagrange(hgtdadi[ih],aste[7],hgt[7],aste[8],hgt[8],aste[9],hgt[9]);
            }
        }


        //如果当前是高度在10500米以上，12500米（含）以下则用250、200和150三个气压层数据进行插值
        if(hgtdadi[ih]>10500 && hgtdadi[ih] <=12500 && hgt[8] != null && hgt[9] != null && hgt[10] != null ){

             //绝对温度
            if(atk[8] != null && atk[9] != null && atk[10] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[8],hgt[8],atk[9],hgt[9],atk[10],hgt[10]);
            }
            //纬向风
            if(windu[8] != null && windu[9] != null && windu[10] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[8],hgt[8],windu[9],hgt[9],windu[10],hgt[10]);
            }
            //经向风
            if(windv[8] != null && windv[9] != null && windv[10] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[8],hgt[8],windv[9],hgt[9],windv[10],hgt[10]);
            }
           /*
            //折射率
            if(aste[8] != null && aste[9] != null && aste[10] != null ){
              asteins=selftools.lagrange(hgtdadi[ih],aste[8],hgt[8],aste[9],hgt[9],aste[10],hgt[10]);
            }
          */
        }

        //如果当前是高度在12500米以上，14500米（含）以下则用200、150和100三个气压层数据进行插值
        if(hgtdadi[ih]>12500 && hgtdadi[ih] <=14500 && hgt[9] != null && hgt[10] != null && hgt[11] != null ){

            //绝对温度
            if(atk[9] != null && atk[10] != null && atk[11] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[9],hgt[9],atk[10],hgt[10],atk[11],hgt[11]);
            }
            //纬向风
            if(windu[9] != null && windu[10] != null && windu[11] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[9],hgt[9],windu[10],hgt[10],windu[11],hgt[11]);
            }
            //经向风
            if(windv[9] != null && windv[10] != null && windv[11] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[9],hgt[9],windv[10],hgt[10],windv[11],hgt[11]);
            }
           /*
            //折射率
            if(aste[9] != null && aste[10] != null && aste[11] != null ){
              asteins=selftools.lagrange(hgtdadi[ih],aste[9],hgt[9],aste[10],hgt[10],aste[11],hgt[11]);
            }
          */
        }

        //如果当前是高度在14500米以上，16500米（含）以下则用150、100和70三个气压层数据进行插值
        if(hgtdadi[ih]>14500 && hgtdadi[ih] <=16500 && hgt[10] != null && hgt[11] != null && hgt[12] != null ){

            //绝对温度
            if(atk[10] != null && atk[11] != null && atk[12] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[10],hgt[10],atk[11],hgt[11],atk[12],hgt[12]);
            }
            //纬向风
            if(windu[10] != null && windu[11] != null && windu[12] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[10],hgt[10],windu[11],hgt[11],windu[12],hgt[12]);
            }
            //经向风
            if(windv[10] != null && windv[11] != null && windv[12] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[10],hgt[10],windv[11],hgt[11],windv[12],hgt[12]);
            }
           /*
            //折射率
            if(aste[10] != null && aste[11] != null && aste[12] != null ){
              asteins=selftools.lagrange(hgtdadi[ih],aste[10],hgt[10],aste[11],hgt[11],aste[12],hgt[12]);
            }
          */
        }

        //如果当前是高度在16500米以上，19500米（含）以下则用100、70和50三个气压层数据进行插值
        if(hgtdadi[ih]>16500 && hgtdadi[ih] <=19500 && hgt[11] != null && hgt[12] != null && hgt[13] != null ){

             //绝对温度
            if(atk[11] != null && atk[12] != null && atk[13] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[11],hgt[11],atk[12],hgt[12],atk[13],hgt[13]);
            }
            //纬向风
            if(windu[11] != null && windu[12] != null && windu[13] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[11],hgt[11],windu[12],hgt[12],windu[13],hgt[13]);
            }
            //经向风
            if(windv[11] != null && windv[12] != null && windv[13] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[11],hgt[11],windv[12],hgt[12],windv[13],hgt[13]);
            }
           /*
            //折射率
            if(aste[11] != null && aste[12] != null && aste[13] != null ){
              asteins=selftools.lagrange(hgtdadi[ih],aste[11],hgt[11],aste[12],hgt[12],aste[13],hgt[13]);
            }
          */
        }


        //如果当前是高度在19500米以上，22500米（含）以下则用70、50和30三个气压层数据进行插值
        if(hgtdadi[ih]>19500 && hgtdadi[ih] <=22500 && hgt[12] != null && hgt[13] != null && hgt[14] != null ){

             //绝对温度
            if(atk[12] != null && atk[13] != null && atk[14] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[12],hgt[12],atk[13],hgt[13],atk[14],hgt[14]);
            }
            //纬向风
            if(windu[12] != null && windu[13] != null && windu[14] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[12],hgt[12],windu[13],hgt[13],windu[14],hgt[14]);
            }
            //经向风
            if(windv[12] != null && windv[13] != null && windv[14] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[12],hgt[12],windv[13],hgt[13],windv[14],hgt[14]);
            }
           /*
            //折射率
            if(aste[12] != null && aste[13] != null && aste[14] != null ){
              asteins=selftools.lagrange(hgtdadi[ih],aste[12],hgt[12],aste[13],hgt[13],aste[14],hgt[14]);
            }
          */
        }


        //如果当前是高度在22500米以上，25500米（含）以下则用50、30和20三个气压层数据进行插值
        if(hgtdadi[ih]>22500 && hgtdadi[ih] <=25500 && hgt[13] != null && hgt[14] != null && hgt[15] != null ){

             //绝对温度
            if(atk[13] != null && atk[14] != null && atk[15] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[13],hgt[13],atk[14],hgt[14],atk[15],hgt[15]);
            }
            //纬向风
            if(windu[13] != null && windu[14] != null && windu[15] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[13],hgt[13],windu[14],hgt[14],windu[15],hgt[15]);
            }
            //经向风
            if(windv[13] != null && windv[14] != null && windv[15] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[13],hgt[13],windv[14],hgt[14],windv[15],hgt[15]);
            }
           /*
            //折射率
            if(aste[13] != null && aste[14] != null && aste[15] != null ){
              asteins=selftools.lagrange(hgtdadi[ih],aste[13],hgt[13],aste[14],hgt[14],aste[15],hgt[15]);
            }
          */
        }


        //如果当前是高度在25500米以上，30000米（含）以下则用30、20和10三个气压层数据进行插值
        if(hgtdadi[ih]>25500 && hgtdadi[ih] <=30000 && hgt[14] != null && hgt[15] != null && hgt[16] != null ){

            //绝对温度
            if(atk[14] != null && atk[15] != null && atk[16] != null ){
            atkins=selftools.lagrange(hgtdadi[ih],atk[14],hgt[14],atk[15],hgt[15],atk[16],hgt[16]);
            }
            //纬向风
            if(windu[14] != null && windu[15] != null && windu[16] != null ){
            winduins=selftools.lagrange(hgtdadi[ih],windu[14],hgt[14],windu[15],hgt[15],windu[16],hgt[16]);
            }
            //经向风
            if(windv[14] != null && windv[15] != null && windv[16] != null ){
            windvins=selftools.lagrange(hgtdadi[ih],windv[14],hgt[14],windv[15],hgt[15],windv[16],hgt[16]);
            }
           /*
            //折射率
            if(aste[14] != null && aste[15] != null && aste[16] != null ){
              asteins=selftools.lagrange(hgtdadi[ih],aste[14],hgt[14],aste[15],hgt[15],aste[16],hgt[16]);
            }
          */
        }


        //将插值结果记入zp_all_cards_hgt_mn_cgxh-------------------------------------------------------------------
        upCgxhParam tjparam = new upCgxhParam();
        tjparam.setStation(vstation);
        tjparam.setSyear(vsyear);
        tjparam.setEyear(veyear);
        tjparam.setMonth(vmonth);
        tjparam.setLatitude(vlatitude.intValue());
        tjparam.setLongitude(vlongitude.intValue());
        tjparam.setHgtdadi(hgtdadi[ih].doubleValue());
        tjparam.setM_press(pressins.intValue());
        tjparam.setM_atk(atkins);
        tjparam.setM_d(densityins);
        tjparam.setM_u(winduins);
        tjparam.setM_v(windvins);
        tjparam.setM_aste(asteins);

        //开始插入
        tongjimapper.insertAllUpCgxhTJ(tjparam);

        //变量重新初始化---------------------------------
        //插值所得要素
        pressins   =null;
        atkins     =null;
        densityins =null;
        winduins   =null;
        windvins   =null;
        asteins    =null;

        } //各层

          //变量重新初始化---------------------------------
          //当站、年代区间、月，各要素在各气压层上的值（用于fetch）
        hgt     = new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        atk     =  new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        density =  new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        windu   =  new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        windv   =  new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
        aste    =  new Integer[]{null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};

        } //各月

        return Result.success("常规高空累年统计完成");
    }



    /*
     *  统计高空累年数据 从历年高空资料统计表zp_over_cards_mn统计累年数据。如果起止年份在实际存在的资料年份之外，则以最小、最大年份代替
     *  vstation 站号
     *  vsyear 年 起
     *  veyear 年 止
     */
    public Result doAllUpTongJi(Integer vstation,Integer vsyear,Integer veyear){


        //用于记录实际的最大最小年份
        Integer vrsyear;
        Integer vreyear;

        //标准气压层数组
        Integer[] press = new Integer[16];
        //循环变量
        Integer vmonth;
        Integer i;

        //初始化变量------------------------------------------------------

        press = new Integer[]{1000, 925, 850, 700, 500, 400, 300, 250, 200, 150, 100, 70, 50, 30, 20, 10};

        //循环变量
        vmonth=0;
        i=0;


        //计算起止年份------------------------------------------------------------------
        //找出现有数据的实际最大最小年份
        vrsyear = tongjimapper.getUpOverMinYear(vstation);
        vreyear = tongjimapper.getUpOverMaxYear(vstation);

        //如果指定的起止年不在资料的最小和最大年份范围内，则作相应调整
        if (vsyear>=vrsyear && vsyear<=vreyear) {
            vrsyear = vsyear;
        }
        if (veyear>=vrsyear && veyear<=vreyear) {
            vreyear=veyear;
        }


        //删除已有的记录-------------------------------------------------------
        upAllParam param_d = new upAllParam();
        param_d.setStation(vstation);
        param_d.setSyear(vrsyear);
        param_d.setEyear(vreyear);
        tongjimapper.deleteAllUpHisRecord(param_d);


        //进行累年统计---------------------------------------------------------
        for(int ii=1;ii<=12;ii++) {
            vmonth = ii;
            //for vmonth in 1..12 loop
            for (i = 1; i <= 16; i++) {



                //向ldb_stat_all_up_mn_isb表插入给定区站、年代区间的各月统计结果------------------------------
                //如果确实从ldb_stat_over_up_mn_isb表中读出了数据则进行统计并插入到ldb_stat_all_up_mn_isb中
                if (vrsyear != null && vreyear != null) {

                    upAllParam param_all = new upAllParam();
                    param_all.setStation(vstation);
                    param_all.setSyear(vrsyear);
                    param_all.setEyear(vreyear);
                    param_all.setMonth(vmonth);
                    param_all.setPress(press[i]);
                    List<Object> sum_all = tongjimapper.getAllUpTJData(param_all);

                    upAllParam tjparam = new upAllParam();
                    tjparam.setStation(vstation);
                    tjparam.setSyear(vrsyear);
                    tjparam.setEyear(vreyear);
                    tjparam.setMonth(vmonth);
                    tjparam.setLatitude(Integer.valueOf(sum_all.get(0).toString()));
                    tjparam.setLongitude(Integer.valueOf(sum_all.get(1).toString()));
                    tjparam.setPress(press[i]);
                    tjparam.setM_hgt(Double.valueOf(sum_all.get(2).toString()));
                    tjparam.setM_atk(Double.valueOf(sum_all.get(3).toString()));
                    tjparam.setM_d(Double.valueOf(sum_all.get(4).toString()));
                    tjparam.setM_u(Double.valueOf(sum_all.get(5).toString()));
                    tjparam.setM_v(Double.valueOf(sum_all.get(6).toString()));
                    tjparam.setM_aste(Double.valueOf(sum_all.get(7).toString()));

                    //开始插入
                    tongjimapper.insertAllUpTJ(tjparam);
                }
            }
        }

        return Result.success("高空累年统计完成！");
    }

    //高空历年统计 循环调用站点和年份
    public Result doOverUpTongJi(){

        List<Map<String,Object>> years = tongjimapper.getUpYears();
        List<Map<String,Object>> stations = tongjimapper.getUpStations();
        if(years.size() > 0 && stations.size() > 0 ){
            //遍历所有年份
            for(int i = 0 ,len=years.size(); i<len;i++) {
                //遍历所有站号
                for(int j = 0 ,lenj=years.size(); j<lenj;j++) {

                    Object year_o = years.get(i).get("year");
                    Object station_o = stations.get(j).get("station");
                    if(year_o == null || station_o == null)continue;//跳过空的
                    Integer year = Integer.parseInt(year_o.toString());//年
                    Integer station = Integer.parseInt(station_o.toString());//年
                    doUpTongJi(station,year);//调用统计程序
                }
            }
        }else{
            return Result.error("数据年份或站点号全为空");
        }

        return Result.success("高空历年统计完成");
    }

    /*
     *  高空等压层历年各月统计
     *  station 站号
     *  year 年
     */
    public Result doUpTongJi(Integer vstation,Integer viyear) {

        //查询条件
        tempParam param = new tempParam();
        param.setStation(String.valueOf(vstation));
        param.setYear(viyear);
        //查询一个站点的一年的数据
        List<Map<String,Object>> mapList = tongjimapper.getUpTongJiData(param);//获取统计数据
        if(mapList.size() > 0){

            //统计之前先将统计表中历史数据删除
            upTongJiParam param1 = new upTongJiParam();
            param1.setStation(vstation);
            param1.setYear(viyear);
            tongjimapper.deleteUpHisRecord(param1);//获取统计数据

            //初始化标准高度数组
            Integer[] press_array = {1000,925,850,700,500,400,300,250,200,150,100,70,50,30,20,10};

            //循环气压层*************************************************************************
            for(int press_a=0; press_a<16;press_a++) {

                //变量初始化------------------------------------------------
                 //新旧记录的月、日 和新记录的时、经纬度
                Integer vmonth = -1,imonth = -1 , vday = -1,iday = -1,vhour = -1,  vlatitude =-1 , vlongitude =-1;


                //fetch into变量
                Integer vhgt = -1, vat = -1, vtd = -1, vwd = -1, vws = -1;
                String voq_hgt = "", voq_at = "",  voq_td = "",  voq_wind = "";

                //计算所得变量
                Integer vhgtjihe =-1; //几何高度
                double vatk = -1;  //绝对温度
                Integer vd = -1; //密度
                double vw_u = -1; //纬向风
                double vw_v = -1; //经向风
                Integer vaste = -1; //大气折射率


                //合计变量。考虑到日、月合计可能超过单个值的10、100倍，所以适当提高变量精度
                  //日合计变量
                Integer vsumhgt = 0;
                double vsumatk = 0;
                Integer vsumd = 0;
                double vsumwu = 0;
                double vsumwv = 0;
                Integer vsumaste = 0;
                //月合计变量
                Integer vmnsumhgt = 0;
                double vmnsumatk = 0;
                Integer vmnsumd = 0;
                double vmnsumwu = 0;
                double vmnsumwv = 0;
                Integer vmnsumaste = 0;
                //计数变量
                  //日计数变量
                Integer vcthgt = 0, vctatk = 0, vctd = 0, vctwu = 0, vctwv = 0, vctaste = 0;
                //月计数变量
                Integer vmncthgt = 0, vmnctatk = 0, vmnctd = 0, vmnctwu = 0, vmnctwv = 0, vmnctaste = 0;

                //时次和日计数器，注意一日中可能有4个标准时次，但可能有的时次没有能对日平均有贡献，设定这三个变量只是用来在进入日、旬、月交替判断的时候设置一个门槛
                   //一日中有多少符合标准时次的原始记录
                Integer i=0 ;
                //一月中有多少有效日
                Integer j=0 ;

                //记录计数变量
                   //插入的月记录数
                Integer icnt =0 ;

                //判断是否是处理此次气压循环的第一条记录
                Integer iisfirst =1;

                //判断是否到达最后一条记录
                Integer iislast =0;

                //遍历所有数据开始统计
                for(int a = 0 ,len=mapList.size(); a<len;a++) {
                    //判断是最后一条
                    if(a == len-1){iislast = 1;}

                    try {

                        vmonth = Integer.parseInt(mapList.get(a).get("month").toString());//
                        vday = Integer.parseInt(mapList.get(a).get("day").toString());//
                        vhour = Integer.parseInt(mapList.get(a).get("hour").toString());//
                        vwd = Integer.parseInt(mapList.get(a).get("wd").toString());//
                        vws = Double.valueOf(mapList.get(a).get("ws").toString()).intValue();//
                        vat = Double.valueOf(mapList.get(a).get("at").toString()).intValue();//
                        vtd = Double.valueOf(mapList.get(a).get("td").toString()).intValue();//

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    //旧日期的初次赋值
                    if (iisfirst == 1) {imonth=vmonth;iday=vday;iisfirst = 0;}


                    ////日循环@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                    //判断是一天遍历结束 通过判断上一次的日期跟这一次不一样 或最后一条数据
                    if((!iday.equals(vday) || !imonth.equals(vmonth) || iislast == 1 ) && i != 0) {

                        //几何高度
                        if (vcthgt>=1){
                            vsumhgt=vsumhgt / vcthgt;
                            //月
                            vmnsumhgt=vmnsumhgt + vsumhgt;
                            vmncthgt=vmncthgt + 1;
                        }else {
                            vsumhgt=null;
                        }

                        //绝对温度
                        if (vctatk>=1) {
                            vsumatk=vsumatk / vctatk;
                            //月
                            vmnsumatk=vmnsumatk + vsumatk;
                            vmnctatk=vmnctatk + 1;
                        }else {
                            vsumatk= -1;
                        }

                        //密度
                        if (vctd>=1) {
                            vsumd = vsumd / vctd;
                            //月
                            vmnsumd = vmnsumd + vsumd;
                            vmnctd = vmnctd + 1;
                        }else {
                            vsumd=null;
                        }

                        //纬向风
                        if (vctwu>=1) {
                            vsumwu = vsumwu / vctwu;
                            //月
                            vmnsumwu = vmnsumwu + vsumwu;
                            vmnctwu = vmnctwu + 1;
                        }else {
                            vsumwu= -1;
                        }

                        //经向风
                        if (vctwv>=1) {
                            vsumwv = vsumwv / vctwv;
                            //月
                            vmnsumwv = vmnsumwv + vsumwv;
                            vmnctwv = vmnctwv + 1;
                        }else {
                            vsumwv= -1;
                        }

                        //大气折射指数
                        if (vctaste>=1) {
                            vsumaste=vsumaste / vctaste;
                            //月
                            vmnsumaste=vmnsumaste + vsumaste;
                            vmnctaste=vmnctaste + 1;
                        }else {
                            vsumaste=null;
                        }


                        //更新月中的有效天数计数
                        j=j+1;
                        //更新旧日变量
                        iday=vday;

                        //日变量重新初始化-----------------------------
                           //日合计变量
                        vsumhgt = 0; vsumatk = 0; vsumd = 0; vsumwu = 0; vsumwv = 0; vsumaste = 0;
                        //日计数变量
                        vcthgt = 0; vctatk = 0; vctd = 0; vctwu = 0; vctwv = 0; vctaste = 0;


                        //日循环结束
                    }


                    ////月循环@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                    //判断是一月遍历结束 通过判断上一次的日期跟这一次不一样 或最后一条数据
                    if((!imonth.equals(vmonth) || iislast == 1 ) && j != 0){


                        //求各平均量的月值
                        if (vmncthgt>=1) { //几何高度
                            vmnsumhgt=vmnsumhgt / vmncthgt;
                        }else {
                            vmnsumhgt=null;
                        }

                        if (vmnctatk>=1) { //绝对温度
                            vmnsumatk=vmnsumatk / vmnctatk;
                        }else {
                            vmnsumatk= -1;
                        }

                        if (vmnctd>=1 ) { //密度
                            vmnsumd=vmnsumd / vmnctd;
                        }else {
                            vmnsumd=null;
                        }

                        if (vmnctwu>=1 ) {    //纬向风
                            vmnsumwu=vmnsumwu / vmnctwu;
                        }else {
                            vmnsumwu= -1;
                        }

                        if (vmnctwv>=1 ) {     //经向风
                            vmnsumwv=vmnsumwv / vmnctwv;
                        }else {
                            vmnsumwv= -1;
                        }

                        if (vmnctaste>=1 ) {       //大气折射指数
                            vmnsumaste=vmnsumaste / vmnctaste;
                        }else {
                            vmnsumaste=null;
                        }


                        /**数据库入库**/

                        //判断计算的统计项有不为空的就启动入库
                        if(vmnsumhgt != null || vmnsumd !=null || vmnsumatk !=0 || vmnsumwu !=0 || vmnsumwv !=0 || vmnsumaste !=null){
                            upTongJiParam tjparam = new upTongJiParam();
                            tjparam.setStation(vstation);
                            tjparam.setYear(viyear);
                            tjparam.setMonth(imonth);
                            tjparam.setLatitude(vlatitude);
                            tjparam.setLongitude(vlongitude);
                            tjparam.setPress(press_array[press_a]);
                            tjparam.setM_hgt(vmnsumhgt);
                            tjparam.setM_atk(vmnsumatk);
                            tjparam.setM_d(vmnsumd);
                            tjparam.setM_u(vmnsumwu);
                            tjparam.setM_v(vmnsumwv);
                            tjparam.setM_aste(vmnsumaste);


                            //开始插入
                            tongjimapper.insertUpYueTJ(tjparam);

                        }

                        //重新初始化------------

                         //月合计变量
                        vmnsumhgt = 0; vmnsumatk = 0; vmnsumd = 0; vmnsumwu = 0; vmnsumwv = 0; vmnsumaste = 0;
                        //月计数变量
                        vmncthgt = 0; vmnctatk = 0; vmnctd = 0; vmnctwu = 0; vmnctwv = 0; vmnctaste = 0;

                        //更新旧月值
                        imonth=vmonth;
                        j=0;

                        //月循环结束
                    }



                    ////！！！！！！！！！！！！！！！！！时次循环 变量赋值！！！！！！！！！！！！！！！！！！！！！！！

                    //时次-----------------------------------------------------------------------
                    //这一段放在所有日、旬、月交替判断的前面是为了避免不同日的时次，如1日的12时和2日的18时被误认为是相邻时次

                    //计算几何高度
                    List<String> aaa = Lists.newArrayList("01","04","05");
                    if (aaa.indexOf(voq_hgt)>-1) {
                        vhgtjihe = selftools.tojihehgt(vhgt, vlatitude / 100000);
                    }else{
                        vhgtjihe = null;
                    }


                    //计算绝对温度
                    List<String> bbb = Lists.newArrayList("01","04","05");
                    if (bbb.indexOf(voq_at)>-1) {
                        vatk = vat/10 + 273.16;
                    }else {
                        //vatk= NULL;
                        vatk= -1;
                    }

                    //计算纬向风、经向风
                    List<String> ccc = Lists.newArrayList("01","04","05");
                    if (ccc.indexOf(voq_wind)>-1 && vws != null && vwd != null) {
                        vw_u=vws * sin((vwd / 180 - 1) * 3.1415927) / 10;
                        vw_v=vws * cos((vwd / 180 - 1) * 3.1415927) / 10;
                    }else {
                        vw_u= -1;
                        vw_v= -1;
                    }

                    //计算折射指数
                    List<String> ddd = Lists.newArrayList("01","04","05");
                    if (ddd.indexOf(voq_at)>-1&& ddd.indexOf(voq_td)>-1) {
                        vaste = selftools.getaste(vat, vat - vtd, press_array[press_a]);
                    }else {
                        vaste=null;
                    }

                    //计算密度
                    List<String> eee = Lists.newArrayList("01","04","05");
                    if (eee.indexOf(voq_td)>-1&& eee.indexOf(voq_at)>-1) {
                        vd = selftools.getd(vat, vat - vtd, press_array[press_a] * 10);
                    }else {
                        vd=null;
                    }


                    //进行日合计-------------------------------------------------
                      //几何高度
                    if (vhgtjihe != null) {
                        vsumhgt=vsumhgt + vhgtjihe;
                        vcthgt=vcthgt + 1;
                    }
                    //绝对温度
                    if (vatk != -1) {
                        vsumatk=vsumatk + vatk;
                        vctatk=vctatk + 1;
                    }
                    //大气密度
                    if (vd != null) {
                        vsumd=vsumd + vd;
                        vctd=vctd + 1;
                    }
                    //纬向风
                    if (vw_u != -1) {
                        vsumwu=vsumwu + vw_u;
                        vctwu=vctwu + 1;
                    }
                    //经向风
                    if (vw_v != -1) {
                        vsumwv=vsumwv + vw_v;
                        vctwv=vctwv + 1;
                    }
                    //大气折射率
                    if (vaste != null) {
                        vsumaste=vsumaste + vaste;
                        vctaste=vctaste + 1;
                    }


                    //更新一日内的时次计数
                    i=i+1;
                    //时次统计结束-------------------------------------------------------------------------------


                 //内部for循环结束
                }


            //气压层for循环结束
            }

        }else{
            return Result.error("查询结果为空！");
        }


        return Result.success("");
    }






    /*
     *  统计地面累年数据 从历年地面资料统计表ldb_stat_over_surf_dkm。如果起止年份在实际存在的资料年份之外，则以最小、最大年份代替
     *  vstation 站号
     *  vsyear 年 起
     *  veyear 年 止
     */
    public Result doAllSurfTongJi(Integer vstation,Integer vsyear,Integer veyear){


        //用于记录实际的最大最小年份
        Integer vrsyear;
        Integer vreyear;

        //用于fetch出的风向频率计数
        Integer vc_wd_all;
        //所有的记次统计都记录在数组中。因为风向统计需要17个记次变量。所以定一个长度为17得数组。如果一个站点50年来某个月的所有时次都吹一种风，那么大约是10000多次计数。所以整数位至少有5位。另外为了放置计算所得的频率，还需要一位小树位。
        Integer[] vc_wd = new Integer[16];

        //记录最多风向及其频率
        String vmax_wd;
        Integer vmax_wd_f;

        //循环变量
        Integer vmonth;
        Integer vdekad;
        Integer i;
        //初始化变量------------------------------------------------------

        //循环变量
        vmonth=0;
        vdekad=0;
        i=0;


        //计算起止年份------------------------------------------------------------------
        //找出现有数据的实际最大最小年份
        vrsyear = tongjimapper.getSurfOverMinYear(vstation);
        vreyear = tongjimapper.getSurfOverMaxYear(vstation);

        //如果指定的起止年不在资料的最小和最大年份范围内，则作相应调整
        if (vsyear>=vrsyear && vsyear<=vreyear) {
            vrsyear = vsyear;
        }
        if (veyear>=vrsyear && veyear<=vreyear) {
            vreyear=veyear;
        }


        //删除已有的记录-------------------------------------------------------
        surfAllParam param_d = new surfAllParam();
        param_d.setStation(vstation);
        param_d.setSyear(vrsyear);
        param_d.setEyear(vreyear);
        tongjimapper.deleteAllSurfHisRecord(param_d);


        //进行累年统计---------------------------------------------------------
        for(int ii=1;ii<=12;ii++) {
            vmonth = ii;
            //for vmonth in 1..12 loop
            for (int jj = 0; jj <= 3; jj++) {
                vdekad = jj;
                //计算最多风向及其频率------------------------------------------------------------

                //重新初始化--------------
                //记录最多风向及其频率
                vmax_wd = "";
                vmax_wd_f = 0;
                //用于select出的风向频率计数
                vc_wd_all = 0;
                vc_wd = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                surfAllParam param_wd = new surfAllParam();
                param_wd.setStation(vstation);
                param_wd.setVrsyear(vrsyear);
                param_wd.setVreyear(vreyear);
                param_wd.setMonth(vmonth);
                param_wd.setDekad(vdekad);
                List<Integer> sum_wd = tongjimapper.getVCWD(param_wd);
                vc_wd_all = sum_wd.get(0);
                if(sum_wd.size()>1)vc_wd[0] = sum_wd.get(1);
                if(sum_wd.size()>2)vc_wd[1] = sum_wd.get(2);
                if(sum_wd.size()>3)vc_wd[2] = sum_wd.get(3);
                if(sum_wd.size()>4)vc_wd[3] = sum_wd.get(4);
                if(sum_wd.size()>5)vc_wd[4] = sum_wd.get(5);
                if(sum_wd.size()>6)vc_wd[5] = sum_wd.get(6);
                if(sum_wd.size()>7)vc_wd[6] = sum_wd.get(7);
                if(sum_wd.size()>8)vc_wd[7] = sum_wd.get(8);
                if(sum_wd.size()>9)vc_wd[8] = sum_wd.get(9);
                if(sum_wd.size()>10)vc_wd[9] = sum_wd.get(10);
                if(sum_wd.size()>11)vc_wd[10] = sum_wd.get(11);
                if(sum_wd.size()>12)vc_wd[11] = sum_wd.get(12);
                if(sum_wd.size()>13)vc_wd[12] = sum_wd.get(13);
                if(sum_wd.size()>14)vc_wd[13] = sum_wd.get(14);
                if(sum_wd.size()>15)vc_wd[14] = sum_wd.get(15);
                if(sum_wd.size()>16)vc_wd[15] = sum_wd.get(16);
                if(sum_wd.size()>17)vc_wd[16] = sum_wd.get(17);


                for (i = 0; i < 17; i++) {
                    //计算各风向的频率
                    vc_wd[i] = vc_wd[i] * 100 / vc_wd_all;
                    //记录最大频率
                    if (vc_wd[i] > vmax_wd_f) {
                        vmax_wd_f = vc_wd[i];
                    }
                }
                //把与最大频率相差上下0.005以内的风向频率也记入最多风向
                for (i = 0; i < 17; i++) {

                    if (vc_wd[i] - vmax_wd_f < 0.5 && vc_wd[i] - vmax_wd_f > -0.5) {
                        if (i == 1 && vmax_wd == null) {
                            vmax_wd = "N";
                        } else {
                            vmax_wd = vmax_wd + ", N";
                        }
                        if (i == 2 && vmax_wd == null) {
                            vmax_wd = "NNE";
                        } else {
                            vmax_wd = vmax_wd + ", NNE";
                        }
                        if (i == 3 && vmax_wd == null) {
                            vmax_wd = "NE";
                        } else {
                            vmax_wd = vmax_wd + ", NE";
                        }
                        if (i == 4 && vmax_wd == null) {
                            vmax_wd = "ENE";
                        } else {
                            vmax_wd = vmax_wd + ", ENE";
                        }
                        if (i == 5 && vmax_wd == null) {
                            vmax_wd = "E";
                        } else {
                            vmax_wd = vmax_wd + ", E";
                        }
                        if (i == 6 && vmax_wd == null) {
                            vmax_wd = "ESE";
                        } else {
                            vmax_wd = vmax_wd + ", ESE";
                        }
                        if (i == 7 && vmax_wd == null) {
                            vmax_wd = "SE";
                        } else {
                            vmax_wd = vmax_wd + ", SE";
                        }
                        if (i == 8 && vmax_wd == null) {
                            vmax_wd = "SSE";
                        } else {
                            vmax_wd = vmax_wd + ", SSE";
                        }
                        if (i == 9 && vmax_wd == null) {
                            vmax_wd = "S";
                        } else {
                            vmax_wd = vmax_wd + ", S";
                        }
                        if (i == 10 && vmax_wd == null) {
                            vmax_wd = "SSW";
                        } else {
                            vmax_wd = vmax_wd + ", SSW";
                        }
                        if (i == 11 && vmax_wd == null) {
                            vmax_wd = "SW";
                        } else {
                            vmax_wd = vmax_wd + ", SW";
                        }
                        if (i == 12 && vmax_wd == null) {
                            vmax_wd = "WSW";
                        } else {
                            vmax_wd = vmax_wd + ", WSW";
                        }
                        if (i == 13 && vmax_wd == null) {
                            vmax_wd = "W";
                        } else {
                            vmax_wd = vmax_wd + ", W";
                        }
                        if (i == 14 && vmax_wd == null) {
                            vmax_wd = "WNW";
                        } else {
                            vmax_wd = vmax_wd + ", WNW";
                        }
                        if (i == 15 && vmax_wd == null) {
                            vmax_wd = "NW";
                        } else {
                            vmax_wd = vmax_wd + ", NW";
                        }
                        if (i == 16 && vmax_wd == null) {
                            vmax_wd = "NNW";
                        } else {
                            vmax_wd = vmax_wd + ", NNW";
                        }
                        if (i == 17 && vmax_wd == null) {
                            vmax_wd = "C";
                        } else {
                            vmax_wd = vmax_wd + ", C";
                        }
                    }
                }

                //如果最大风向频率仍为空，则将其置回空值-
                if (vmax_wd_f == 0) {
                    vmax_wd_f = null;
                }

                //向zp_all_gts_dkmn表插入给定区站、年代区间的各月旬统计结果------------------------------
                //如果确实从zp_over_gts_dkmn表中读出了数据则进行统计并插入到zp_all_gts_dkmn中
                if (vrsyear != null && vreyear != null) {

                    surfAllParam param_all = new surfAllParam();
                    param_all.setStation(vstation);
                    param_all.setVrsyear(vrsyear);
                    param_all.setVreyear(vreyear);
                    param_all.setMonth(vmonth);
                    param_all.setDekad(vdekad);
                    List<Object> sum_all = tongjimapper.getAllTJData(param_wd);

                    surfAllParam tjparam = new surfAllParam();
                    tjparam.setStation(vstation);
                    tjparam.setSyear(vrsyear);
                    tjparam.setEyear(vreyear);
                    tjparam.setMonth(vmonth);
                    tjparam.setDekad(vdekad);
                    tjparam.setM_slp(Double.valueOf(sum_all.get(0).toString()));
                    tjparam.setM_ws(Double.valueOf(sum_all.get(1).toString()));
                    tjparam.setM_at(Double.valueOf(sum_all.get(2).toString()));
                    tjparam.setM_rh(Double.valueOf(sum_all.get(3).toString()));
                    tjparam.setM_n(Double.valueOf(sum_all.get(4).toString()));
                    tjparam.setM_nh(Double.valueOf(sum_all.get(5).toString()));
                    tjparam.setM_d(Double.valueOf(sum_all.get(6).toString()));
                    tjparam.setM_max_at(Double.valueOf(sum_all.get(7).toString()));
                    tjparam.setM_min_at(Double.valueOf(sum_all.get(8).toString()));
                    tjparam.setMax_slp(Double.valueOf(sum_all.get(9).toString()));
                    tjparam.setMax_ws(Double.valueOf(sum_all.get(10).toString()));
                    tjparam.setMax_at(Double.valueOf(sum_all.get(11).toString()));
                    tjparam.setMax_wd(Integer.valueOf(sum_all.get(12).toString()));
                    tjparam.setMin_slp(Double.valueOf(sum_all.get(13).toString()));
                    tjparam.setMin_at(Double.valueOf(sum_all.get(14).toString()));
                    tjparam.setMin_rh(Double.valueOf(sum_all.get(15).toString()));
                    tjparam.setSd_slp(Double.valueOf(sum_all.get(16).toString()));
                    tjparam.setSd_ws(Double.valueOf(sum_all.get(17).toString()));
                    tjparam.setSd_at(Double.valueOf(sum_all.get(18).toString()));
                    tjparam.setCloudy(Double.valueOf(sum_all.get(19).toString()));
                    tjparam.setFine(Double.valueOf(sum_all.get(20).toString()));
                    tjparam.setFog(Double.valueOf(sum_all.get(21).toString()));
                    tjparam.setThunder(Double.valueOf(sum_all.get(22).toString()));
                    tjparam.setF_h_100(Double.valueOf(sum_all.get(23).toString()));
                    tjparam.setF_h_200(Double.valueOf(sum_all.get(24).toString()));
                    tjparam.setF_h_300(Double.valueOf(sum_all.get(25).toString()));
                    tjparam.setF_h_600(Double.valueOf(sum_all.get(26).toString()));
                    tjparam.setF_h_1500(Double.valueOf(sum_all.get(27).toString()));
                    tjparam.setF_h_2500(Double.valueOf(sum_all.get(28).toString()));
                    tjparam.setF_h_2500p(Double.valueOf(sum_all.get(29).toString()));
                    tjparam.setF_nh_1(Double.valueOf(sum_all.get(30).toString()));
                    tjparam.setF_nh_3(Double.valueOf(sum_all.get(31).toString()));
                    tjparam.setF_nh_6(Double.valueOf(sum_all.get(32).toString()));
                    tjparam.setF_nh_8(Double.valueOf(sum_all.get(33).toString()));
                    tjparam.setF_nh_10(Double.valueOf(sum_all.get(34).toString()));
                    tjparam.setF_vis_1(Double.valueOf(sum_all.get(35).toString()));
                    tjparam.setF_vis_4(Double.valueOf(sum_all.get(36).toString()));
                    tjparam.setF_vis_10(Double.valueOf(sum_all.get(37).toString()));
                    tjparam.setF_vis_10p(Double.valueOf(sum_all.get(38).toString()));
                    tjparam.setMost_wd(vmax_wd);
                    tjparam.setF_most_wd(Double.valueOf(vmax_wd_f));

                    //开始插入
                    tongjimapper.insertAllSurfTJ(tjparam);
                }
            }
        }

        return Result.success("地面累年统计完成！");
    }


    //地面历年统计 循环调用站点和年份
    public Result doOverSurfTongJi(){

        List<Map<String,Object>> years = tongjimapper.getSurfYears();
        List<Map<String,Object>> stations = tongjimapper.getSurfStations();
        if(years.size() > 0 && stations.size() > 0 ){
            //遍历所有年份
            for(int i = 0 ,len=years.size(); i<len;i++) {
                //遍历所有站号
                for(int j = 0 ,lenj=years.size(); j<lenj;j++) {

                    Object year_o = years.get(i).get("year");
                    Object station_o = stations.get(j).get("station");
                    if(year_o == null || station_o == null)continue;//跳过空的
                    Integer year = Integer.parseInt(year_o.toString());//年
                    Integer station = Integer.parseInt(station_o.toString());//年
                    //doSurfTongJi(58968,2011,1,4,4,30);
                    doSurfTongJi(station,year,1,4,4,30);//调用统计程序
                }
            }
        }else{
            return Result.error("数据年份或站点号全为空");
        }

        return Result.success("地面历年统计完成");
    }


    /*
     *  地面历年各月旬统计
     *  station 站号
     *  year 年
     *  key 控制是否进行相邻时次插值的开关 1是启动
     *  daymiss 最少有效天数
     *  dekadmiss 最少有效旬数
     *  monthmiss 最少有效月数
     */
    public Result doSurfTongJi(Integer vstation,Integer viyear,Integer key,Integer daymiss,Integer dekadmiss,Integer monthmiss) {

        //查询条件
        surfParam param = new surfParam();
        param.setStation(String.valueOf(vstation));
        param.setYear(viyear);
        //查询一个站点的一年的数据
        List<Map<String,Object>> mapList = tongjimapper.getSurfTongJiData(param);//获取统计数据
        if(mapList.size() > 0){

            //统计之前先将统计表中历史数据删除
            surfTongJiParam param1 = new surfTongJiParam();
            param1.setStation(vstation);
            param1.setYear(viyear);
            tongjimapper.deleteSurfHisRecord(param1);//获取统计数据

            //fetchinto变量
            Integer vslp=-1, vwd=-1, vws=-1, vat=-1, vn=-1, vnh=-1, vtd=-1, vmaxat=-1, vminat=-1, vqslp=-1, vqwd=-1, vqws=-1, vqat=-1, vqn=-1,
                    vqnh=-1, vqtd=-1, vqmaxat=-1, vqminat=-1, vh=-1, vvis=-1, vww=-1, vqh=-1, vqvis=-1, vqww=-1, vw1=-1, vw2=-1, vqw1=-1, vqw2=-1;
            //合计和计数变量
            Integer vsumslp=0,vsumws=0,vsumat=0,vsumn=0,vsumnh=0,vsumd=0,vsumrh=0,vsummaxat=0,vsumminat=0,
                    vctslp=0,vctws=0,vctat=0,vctn=0,vctnh=0,vctd=0,vctrh=0,vctmaxat=0,vctminat=0,
                    vdksumslp=0,vdksumws=0,vdksumat=0,vdksumn=0,vdksumnh=0,vdksumd=0,vdksumrh=0,vdksummaxat=0,vdksumminat=0,vdksumslpqr=0,vdksumwsqr=0,vdksumatqr=0,
                    vdkctslp=0,vdkctws=0,vdkctat=0,vdkctn=0,vdkctnh=0,vdkctd=0,vdkctrh=0,vdkctmaxat=0,vdkctminat=0,  vdkctfinecloudy =0,
                    vmnsumslp=0,vmnsumws=0,vmnsumat=0,vmnsumn=0,vmnsumnh=0,vmnsumd=0,vmnsumrh=0,vmnsummaxat=0,vmnsumminat=0,vmnsumslpqr=0,vmnsumwsqr=0,vmnsumatqr=0,
                    vmnctslp=0,vmnctws=0,vmnctat=0,vmnctn=0,vmnctnh=0,vmnctd=0,vmnctrh=0,vmnctmaxat=0,vmnctminat=0,  vmnctfinecloudy =0,   wmnctweather=0;
            //用于记录一日内各标准时次是否有数据来的变量（0表示没有数据或质控不过关，1表示有数据）
            Integer[] vhslp   = {0,0,0,0};
            Integer[] vhwd    = {0,0,0,0};
            Integer[] vhws    = {0,0,0,0};
            Integer[] vhat    = {0,0,0,0};
            Integer[] vhn     = {0,0,0,0};
            Integer[] vhnh    = {0,0,0,0};
            Integer[] vhtd    = {0,0,0,0};
            Integer[] vhmaxat = {0,0,0,0};
            Integer[] vhminat = {0,0,0,0};
            Integer[] vhh     = {0,0,0,0};
            Integer[] vhrh    = {0,0,0,0};
            Integer[] vhd     = {0,0,0,0};
            //一日内是否有雾和雷暴的判断量
            Integer iisfog = 0;
            Integer iisthunder = 0;
            //一日内是否成功的进行了天气现象观测
            Integer iisweatherchecked = 0;
            //记录一日内00时和12时的海平面气压、风速、温度、总云量、低云量、最高温度、最低温度、大气密度、相对湿度
            Integer vslp00 =0 , vd00 =0, vws00 =0, vat00 =0, vn00 =0, vnh00 =0,  vrh00 =0,   vmaxat00 =0, vminat00 =0,
                    vslp06 =0 , vd06 =0, vws06 =0, vat06 =0, vn06 =0, vnh06 =0,  vrh06 =0,   vmaxat06 =0, vminat06 =0;
            //极值
            Integer vdkmaxat=-9999,vdkminat=9999,vdkmaxwd=-999,vdkmaxws=-9999,vdkminrh=999,vdkmaxslp=-99999,vdkminslp=99999,
                    vmnmaxat=-9999,vmnminat=9999,vmnmaxwd=-999,vmnmaxws=-9999,vmnminrh=999,vmnmaxslp=-99999,vmnminslp=99999;
            //用于统计晴天阴天的计数变量
            Integer vdkfine =0;
            Integer vmnfine =0;
            Integer vdkcloudy =0;
            Integer vmncloudy =0;
            //用于统计雾日数和雷暴日数的变量
            Integer vmnfog=0;
            Integer vmnthunder=0;
            //各种用于频率统计的计数变量
            Integer vc_h_all =0;
            Integer vc_nh_all =0;
            Integer vc_vis_all =0;
            Integer vc_wd_all =0;
            Integer[] vc_nh ={0,0,0,0,0};
            Integer[] vc_h  ={0,0,0,0,0,0,0};
            Integer[] vc_vis={0,0,0,0};
            Integer[] vc_wd ={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            //用于解码的变量
            Integer vjwd=-1;
            Integer vjnh=-1;
            //计算所得变量
            Integer vrh=-1,vd=-1,vqrh=-1,vqd=-1;
            //过去天气
            Integer vw2char = -1;
            //标准差
            Integer vsdslp=-1,vsdws=-1,vsdat=-1;
            //缺测规定，日最少有效测量次数
            Integer dayless=4-daymiss;
            //缺测规定：旬，月最少有效缺侧数
            Integer dekadless=3;
            Integer monthless=30;

            //计数器:日,旬,月
            Integer i=0,j=0,k=0;
            //判断是否是处理第一条记录
            Integer iisfirst = 1;
            //判断是否到达最后一条记录
            Integer iislast = 0;

            Map<String,String> pre_mapList = null;//存前一次循环的值
            //新旧记录的月、旬、日和新记录的时
            Integer vmonth = -1,imonth = -1,vdekad = -1 , idekad = -1 , vday = -1,iday = -1,vhour = -1;

            //遍历所有数据开始统计
            for(int a = 0 ,len=mapList.size(); a<len;a++) {
                //判断是最后一条
                if(a == len-1){iislast = 1;}

                try {

                    vslp = Double.valueOf(mapList.get(a).get("slp").toString()).intValue();//海平面气压
                    vctslp = vslp;
                    vmonth = Integer.parseInt(mapList.get(a).get("month").toString());//
                    vday = Integer.parseInt(mapList.get(a).get("day").toString());//
                    vhour = Integer.parseInt(mapList.get(a).get("hour").toString());//
                    vwd = Integer.parseInt(mapList.get(a).get("wd").toString());//
                    vws = Double.valueOf(mapList.get(a).get("ws").toString()).intValue();//
                    vat = Double.valueOf(mapList.get(a).get("at").toString()).intValue();//
                    vn = Integer.parseInt(mapList.get(a).get("n").toString());//
                    vnh = Integer.parseInt(mapList.get(a).get("nh").toString());//
                    vtd = Double.valueOf(mapList.get(a).get("td").toString()).intValue();//
                    vmaxat = Double.valueOf((mapList.get(a).get("max_at")== null ? "0":mapList.get(a).get("max_at")).toString()).intValue();//
                    vminat = Double.valueOf(mapList.get(a).get("min_at").toString()).intValue();//
                    vqslp = Integer.parseInt(mapList.get(a).get("q_slp").toString());//
                    vqwd = Integer.parseInt(mapList.get(a).get("q_wd").toString());//
                    vqws = Integer.parseInt(mapList.get(a).get("q_ws").toString());//
                    vqat = Integer.parseInt(mapList.get(a).get("q_at").toString());//
                    vqn = Integer.parseInt(mapList.get(a).get("q_n").toString());//
                    vqnh = Integer.parseInt(mapList.get(a).get("q_nh").toString());//
                    vqtd = Integer.parseInt(mapList.get(a).get("q_td").toString());//
                    vqmaxat = Integer.parseInt((mapList.get(a).get("q_max_at")== null ? "0":mapList.get(a).get("q_max_at")).toString());//
                    vqminat = Integer.parseInt((mapList.get(a).get("q_min_at")== null ? "0":mapList.get(a).get("q_min_at")).toString());//
                    vh = Integer.parseInt(mapList.get(a).get("h").toString());//
                    vvis = Integer.parseInt(mapList.get(a).get("vis").toString());//
                    vww = Integer.parseInt(mapList.get(a).get("ww").toString());//
                    vqh = Integer.parseInt(mapList.get(a).get("q_h").toString());//
                    vqvis = Integer.parseInt(mapList.get(a).get("q_vis").toString());//
                    vqww = Integer.parseInt(mapList.get(a).get("q_ww").toString());//
                    vw1 = Integer.parseInt(mapList.get(a).get("w1").toString());//
                    String vw2char_p = mapList.get(a).get("w2").toString();
                    if(!vw2char_p.equals(""))vw2char = Integer.parseInt(vw2char_p);//
                    vqw1 = Integer.parseInt(mapList.get(a).get("q_w1").toString());//
                    vqw2 = Integer.parseInt(mapList.get(a).get("q_w2").toString());//
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }


                //判断过去天气w2
                ArrayList<Integer> wwv= Lists.newArrayList(0,1,2,3,4,5,6,7,8,9);
                if (vqw2==0 && vw2char !=null && wwv.indexOf(vw2char)>-1){
                    vw2=Integer.valueOf(vw2char);
                }else{
                    vqw2=1;
                }
                //旧日期的初次赋值
                if (iisfirst == 1) {imonth=vmonth;iday=vday;iisfirst = 0;}
                //旬变量赋值
                if(vday <= 10){vdekad=1 ;}else if(vday > 20){vdekad=3 ;}else {vdekad=2;}
                //对于第一条记录，旧旬值等于新旬值
                if (idekad==-1){idekad = vdekad;}


                ////日循环@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                //判断是一天遍历结束 通过判断上一次的日期跟这一次不一样 或最后一条数据
                if((!iday.equals(vday) || !imonth.equals(vmonth) || iislast == 1 ) && i != 0) {

                    //海平面气压
                    if (vctslp >= dayless) {
                        if(vctslp!=0)vsumslp = vsumslp / vctslp;
                        //旬
                        vdksumslp = vdksumslp + vsumslp;
                        vdksumslpqr = vdksumslpqr + vsumslp * vsumslp;
                        vdkctslp = vdkctslp + 1;
                        //月
                        vmnsumslp = vmnsumslp + vsumslp;
                        vmnsumslpqr = vmnsumslpqr + vsumslp * vsumslp;
                        vmnctslp = vmnctslp + 1;
                    } else {
                        vsumslp = null;
                    }

                    //风速
                    if (vctws >= dayless) {
                        if(vctws!=0)vsumws = vsumws / vctws;
                        //旬
                        vdksumws = vdksumws + vsumws;
                        vdksumwsqr = vdksumwsqr + vsumws * vsumws;
                        vdkctws = vdkctws + 1;
                        //月
                        vmnsumws = vmnsumws + vsumws;
                        vmnsumwsqr = vmnsumwsqr + vsumws * vsumws;
                        vmnctws = vmnctws + 1;

                    } else {
                        vsumws = null;
                    }

                    //温度
                    if (vctat >= dayless) {
                        if(vctat!=0)vsumat = vsumat / vctat;
                        //旬
                        vdksumat = vdksumat + vsumat;
                        vdksumatqr = vdksumatqr + vsumat * vsumat;
                        vdkctat = vdkctat + 1;
                        //月
                        vmnsumat = vmnsumat + vsumat;
                        vmnsumatqr = vmnsumatqr + vsumat * vsumat;
                        vmnctat = vmnctat + 1;

                    } else {
                        vsumat = null;
                    }

                    //总云量 和阴晴天计数
                    if (vctn >= dayless) {
                        if(vctn!=0)vsumn = vsumn / vctn;
                        //旬
                        vdksumn = vdksumn + vsumn;
                        vdkctn = vdkctn + 1;
                        //月
                        vmnsumn = vmnsumn + vsumn;
                        vmnctn = vmnctn + 1;

                        //统计旬月的晴天阴天日数
                        //旬月中有效观测日数加1
                        vdkctfinecloudy = vdkctfinecloudy + 1;
                        vmnctfinecloudy = vmnctfinecloudy + 1;

                        if (vsumn <= 3) {
                            vdkfine = vdkfine + 1;
                            vmnfine = vmnfine + 1;
                        }
                        if (vsumn >= 8) {
                            vdkcloudy = vdkcloudy + 1;
                            vmncloudy = vmncloudy + 1;
                        }

                    } else {
                        vsumn = null;
                    }

                    //低云量
                    if (vctnh >= dayless) {
                        if(vctnh!=0)vsumnh = vsumnh / vctnh;
                        //旬
                        vdksumnh = vdksumnh + vsumnh;
                        vdkctnh = vdkctnh + 1;
                        //月
                        vmnsumnh = vmnsumnh + vsumnh;
                        vmnctnh = vmnctnh + 1;

                    } else {
                        vsumnh = null;
                    }

                    //相对湿度
                    if (vctrh >= dayless) {
                        if(vctrh!=0)vsumrh = vsumrh / vctrh;
                        //旬
                        vdksumrh = vdksumrh + vsumrh;
                        vdkctrh = vdkctrh + 1;
                        //月
                        vmnsumrh = vmnsumrh + vsumrh;
                        vmnctrh = vmnctrh + 1;

                    } else {
                        vsumrh = null;
                    }

                    //密度
                    if (vctd >= dayless){
                        if(vctd!=0)vsumd = vsumd / vctd;
                        //旬
                        vdksumd=vdksumd + vsumd;
                        vdkctd=vdkctd + 1;
                        //月
                        vmnsumd=vmnsumd + vsumd;
                        vmnctd=vmnctd + 1;
                    }else {
                        vsumd=null;
                    }

                    //平均最高温度
                    if (vctmaxat==1) {
                        if(vctmaxat!=0)vsummaxat = vsummaxat / vctmaxat;
                        //旬
                        vdksummaxat=vdksummaxat + vsummaxat;
                        vdkctmaxat=vdkctmaxat + 1;
                        //月
                        vmnsummaxat=vmnsummaxat + vsummaxat;
                        vmnctmaxat=vmnctmaxat + 1;

                    }else {
                        vsummaxat=null;
                    }

                    //平均最低温度
                    if (vctminat==1) {
                        if(vctminat!=0)vsumminat=vsumminat / vctminat;
                        //旬
                        vdksumminat=vdksumminat + vsumminat;
                        vdkctminat=vdkctminat + 1;
                        //月
                        vmnsumminat=vmnsumminat + vsumminat;
                        vmnctminat=vmnctminat + 1;

                    }else {
                        vsumminat=null;
                    }


                    //更新雾和雷暴日计数
                    //这时iisfog 和 iisthunder保留的是上一个时次的数据，如果当前是00时且是当站、当月次日，那么保留的就是上一日的数据。这时如果00时的过去天气现象指出有雾或雷暴，就应该更新iisfog或iisthunder为1。
                    if (vmonth == imonth && vday == iday + 1 && vhour == 0) {
                        if (vqw1 == 0 || vqw2 == 0) {
                            iisweatherchecked = 1;
                        }
                        if (vqw1 == 0 && (selftools.getw12(vw1) == 2) || (vqw2 == 0 && selftools.getw12(vw2) == 2)) {
                            iisfog = 1;
                        }
                        if (vqw1 == 0 && (selftools.getw12(vw1) == 7) || (vqw2 == 0 && selftools.getw12(vw2) == 7)) {
                            iisthunder = 1;
                        }
                    }

                    if (iisweatherchecked == 1) {
                        wmnctweather = wmnctweather + 1;
                    }

                    if (iisfog == 1){
                        vmnfog=vmnfog + 1;
                    }
                    if (iisthunder==1) {
                        vmnthunder=vmnthunder + 1;
                    }

                    //更新旬月中的有效天数计数
                    j=j+1; k=k+1;
                    //更新旧日变量
                    iday=vday;

                    //日变量重新初始化-----------------------------
                    //清空日中的时次计数
                    i=0;
                    //日合计变量
                    vsumslp=0;vsumd=0;vsumws=0;vsumat=0;vsumn=0;vsumnh=0;vsumrh=0;vsummaxat=0;vsumminat=0;
                    vctslp=0;vctd=0;vctws=0;vctat=0;vctn=0;vctnh=0;vctrh=0;vctmaxat=0;vctminat=0;
                    //当天有无雾和雷暴的判断量
                    iisfog = 0;
                    iisthunder = 0;
                    //一日内是否成功的进行了天气现象观测
                    iisweatherchecked = 0;

                    //用于记录一日内各标准时次是否有数据来的变量（0表示没有数据或质控不过关，1表示有数据）

                    vhslp   = new Integer[]{0,0,0,0};
                    vhwd    = new Integer[]{0,0,0,0};
                    vhws    = new Integer[]{0,0,0,0};
                    vhat    = new Integer[]{0,0,0,0};
                    vhn     = new Integer[]{0,0,0,0};
                    vhnh    = new Integer[]{0,0,0,0};
                    vhtd    = new Integer[]{0,0,0,0};
                    vhmaxat = new Integer[]{0,0,0,0};
                    vhminat = new Integer[]{0,0,0,0};
                    vhh     = new Integer[]{0,0,0,0};
                    vhrh    = new Integer[]{0,0,0,0};
                    vhd     = new Integer[]{0,0,0,0};
                    //记录一日内00时和12时的海平面气压、风速、温度、总云量、低云量、最高温度、最低温度、大气密度、相对湿度
                    vslp00 =0 ; vd00 =0; vws00 =0; vat00 =0; vn00 =0; vnh00 =0;  vrh00 =0;   vmaxat00 =0; vminat00 =0;
                    vslp06 =0 ; vd06 =0; vws06 =0; vat06 =0; vn06 =0; vnh06 =0;  vrh06 =0;   vmaxat06 =0; vminat06 =0;

                    //日循环结束
                }


                ////旬循环@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                //判断是一旬遍历结束 通过判断上一次的日期跟这一次不一样 或最后一条数据
                if((!idekad.equals(vdekad) || !imonth.equals(vmonth) || iislast == 1 ) && i != 0){

                    //计算该旬的最少有效测量数
                    if (idekad==1 || idekad==2){
                        dekadless = 10-dekadmiss+1;
                    }else if (idekad==3){
                        if (imonth==1 || imonth ==3 || imonth ==5 || imonth ==7 || imonth ==8 || imonth ==10 || imonth ==12){
                            dekadless = 11-dekadmiss+1;
                        }else if (imonth == 2) {
                            dekadless = 8 - dekadmiss + 1;
                        }else {dekadless = 10-dekadmiss+1;}
                    }

                    //求各平均量的旬值==========================================

                    //海平面气压
                    if (vdkctslp>=dekadless) {
                        vsdslp = vdksumslp;
                        vdksumslp = vdksumslp / vdkctslp;
                    }else {
                        vdksumslp = null;
                        vsdslp = null;
                    }
                    //密度
                    if (vdkctd>=dekadless){
                        vdksumd=vdksumd / vdkctd;
                    }else {
                        vdksumd=null;
                    }
                    //风速
                    if (vdkctws>=dekadless) {
                        vsdws=vdksumws;
                        vdksumws=vdksumws / vdkctws;
                    }else{
                        vdksumws = null;
                        vsdws=null;
                    }
                    //气温
                    if (vdkctat>=dekadless){
                        vsdat=vdksumat;
                        vdksumat=vdksumat/vdkctat;
                    }else {
                        vdksumat=null;
                        vsdat=null;
                    }
                    //总云量
                    if (vdkctn>=dekadless){
                        vdksumn=vdksumn/vdkctn;
                    }else {
                        vdksumn=null;
                    }
                    //低云量
                    if (vdkctnh>=dekadless){
                        vdksumnh=vdksumnh/vdkctnh ;
                    }else {
                        vdksumnh=null;
                    }
                    //相对湿度
                    if (vdkctrh>=dekadless){
                        vdksumrh=vdksumrh/vdkctrh;
                    }else {
                        vdksumrh=null;
                    }
                    //最高温度
                    if (vdkctmaxat>=dekadless){
                        vdksummaxat=vdksummaxat/vdkctmaxat;
                    }else {
                        vdksummaxat=null;
                    }
                    //最低温度
                    if (vdkctminat>=dekadless){
                        vdksumminat=vdksumminat/vdkctminat;
                    }else {
                        vdksumminat=null;
                    }


                    //求各极值的旬值
                    if (vdkmaxslp==-99999){vdkmaxslp=null;}
                    if (vdkminslp==99999){vdkminslp=null;}
                    if (vdkmaxat==-9999){vdkmaxat=null;}
                    if (vdkminat==9999){vdkminat=null;}
                    if (vdkmaxws==-9999){vdkmaxws=null;}
                    if (vdkmaxwd==-999){vdkmaxwd=null;}
                    if (vdkminrh==999){vdkminrh=null;}

                    //如果各平均量旬值的计数器不足最少有效日数，则将该平均量旬值的平方赋为null
                    if (vdkctslp<dekadless){vdksumslpqr=null;}
                    if (vdkctws<dekadless){vdksumwsqr=null;}
                    if (vdkctat<dekadless){vdksumatqr=null;}

                    //将旬中的晴阴天日数通过有效观测次数换算到旬上来
                    List<Integer> aa = Lists.newArrayList(1,2);
                    List<Integer> bb = Lists.newArrayList(1,3,5,7,8,10,12);
                    List<Integer> cc = Lists.newArrayList(4,6,9,11);
                    if (vdkctfinecloudy >=dekadless) {
                        if (aa.indexOf(idekad) > -1) {
                            vdkfine = vdkfine * 10 / vdkctfinecloudy;
                            vdkcloudy = vdkcloudy * 10 / vdkctfinecloudy;
                        }
                        if (idekad == 3) {
                            if (bb.indexOf(imonth) > -1) {
                                vdkfine = vdkfine * 11 / vdkctfinecloudy;
                                vdkcloudy = vdkcloudy * 11 / vdkctfinecloudy;
                            }
                            if (imonth == 2) {
                                vdkfine = vdkfine * 8 / vdkctfinecloudy;
                                vdkcloudy = vdkcloudy * 8 / vdkctfinecloudy;
                            }
                            if (cc.indexOf(imonth) > -1) {
                                vdkfine = vdkfine * 10 / vdkctfinecloudy;
                                vdkcloudy = vdkcloudy * 10 / vdkctfinecloudy;
                            }
                        }
                    }else {
                        vdkfine=null;
                        vdkcloudy=null;
                    }


                    /**数据库入库**/

                    //判断计算的统计项有不为空的就启动入库
                    if  (
                            vdksumslp !=null || vdksumd !=null || vdksumws !=null || vdksumat !=null || vdksumn !=null ||
                                    vdksumnh !=null || vdksumrh !=null || vdksummaxat !=null || vdksumminat !=null ||
                                    vdkmaxslp !=null || vdkminslp !=null || vdkmaxws !=null || vdkmaxwd !=null || vdkminat !=null ||
                                    vdkminrh !=null || vdkcloudy !=null || vdkfine !=null
                    ){
                        surfTongJiParam tjparam = new surfTongJiParam();
                        tjparam.setStation(vstation);
                        tjparam.setYear(viyear);
                        tjparam.setMonth(imonth);
                        tjparam.setDekad(idekad);
                        tjparam.setM_slp(vdksumslp);
                        tjparam.setM_d(vdksumd);
                        tjparam.setM_ws(vdksumws);
                        tjparam.setM_at(vdksumat);
                        tjparam.setM_max_at(vdksummaxat);
                        tjparam.setM_min_at(vdksumminat);
                        tjparam.setM_rh(vdksumrh);
                        tjparam.setM_n(vdksumn);
                        tjparam.setM_nh(vdksumnh);
                        tjparam.setMax_slp(vdkmaxslp);
                        tjparam.setMin_slp(vdkminslp);
                        tjparam.setMax_ws(vdkmaxws);
                        tjparam.setMax_wd(vdkmaxwd);
                        tjparam.setMax_at(vdkmaxat);
                        tjparam.setMin_at(vdkminat);
                        tjparam.setMin_rh(vdkminrh);
                        tjparam.setSd_slp(vsdslp);
                        tjparam.setSd_ws(vsdws);
                        tjparam.setSd_at(vsdat);
                        tjparam.setSd_slp_qr(vdksumslpqr);
                        tjparam.setSd_ws_qr(vdksumwsqr);
                        tjparam.setSd_at_qr(vdksumatqr);
                        tjparam.setC_sd_slp(vdkctslp);
                        tjparam.setC_sd_ws(vdkctws);
                        tjparam.setC_sd_at(vdkctat);
                        tjparam.setMcount(j);
                        tjparam.setCloudy(vdkcloudy);
                        tjparam.setFine(vdkfine);
                        //开始插入
                        tongjimapper.insertXunTJ(tjparam);

                    }


                    //重新初始化------------
                    //旬最值
                    vdkmaxws=-9999;vdkmaxwd=-999;vdkmaxat=-9999;vdkminat=9999;vdkminrh=999;vdkmaxslp=-99999;vdkminslp=99999;
                    //旬累加变量
                    vdksumslp=0;vdksumd=0;vdksumws=0;vdksumat=0;vdksumn=0;vdksumnh=0;vdksumrh=0;vdksummaxat=0;vdksumminat=0;vdksumslpqr=0;vdksumwsqr=0;vdksumatqr=0;vsdslp=0;vsdws=0;vsdat=0;
                    //旬计数变量
                    vdkctslp=0;vdkctd=0;vdkctws=0;vdkctat=0;vdkctn=0;vdkctnh=0;vdkctrh=0;vdkctmaxat=0;vdkctminat=0;
                    //旬晴天和阴天日数
                    vdkfine=0; vdkcloudy=0;
                    vdkctfinecloudy =0;
                    //更新旬变量
                    idekad=vdekad;

                    //更新旬中的日计数
                    j=0;

                    //旬循环结束
                }


                ////月循环@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
                //判断是一月遍历结束 通过判断上一次的日期跟这一次不一样 或最后一条数据
                if((!imonth.equals(vmonth) || iislast == 1 ) && k != 0){

                    //计算该月的最少有效测量数
                    if (imonth == 1 || imonth == 3 || imonth == 5 || imonth == 7 || imonth == 8 || imonth == 10 || imonth == 12){
                        monthless = 31 - monthmiss + 1;
                    }else if (imonth ==2) {
                        monthless = 28 - monthmiss + 1;
                    }else{
                        monthless = 30-monthmiss+1;
                    }

                    //求各平均量的月值=============================================

                    //海平面气压
                    if (vmnctslp>=monthless) {
                        vsdslp = vmnsumslp;
                        if(vmnctslp!=0)vmnsumslp = vmnsumslp / vmnctslp;
                    }else {
                        vmnsumslp = null;
                        vsdslp = null;
                    }
                    //密度
                    if (vmnctd>=monthless) {
                        if(vmnctd!=0)vmnsumd=vmnsumd / vmnctd;
                    }else {
                        vmnsumd=null;
                    }
                    //风速
                    if (vmnctws>=monthless) {
                        vsdws=vmnsumws;
                        if(vmnctws!=0)vmnsumws=vmnsumws / vmnctws;
                    }else {
                        vmnsumws=null;
                        vsdws=null;
                    }
                    //气温
                    if (vmnctat>=monthless) {
                        vsdat=vmnsumat;
                        if(vmnctat!=0)vmnsumat=vmnsumat / vmnctat;
                    }else {
                        vmnsumat=null;
                        vsdat=null;
                    }
                    //总云量
                    if (vmnctn>=monthless) {
                        if(vmnctn!=0)vmnsumn=vmnsumn / vmnctn;
                    }else {
                        vmnsumn=null;
                    }
                    //低云量
                    if (vmnctnh>=monthless) {
                        if(vmnctnh!=0)vmnsumnh=vmnsumnh / vmnctnh;
                    }else {
                        vmnsumnh=null;
                    }
                    //相对湿度
                    if (vmnctrh>=monthless) {
                        if(vmnctrh!=0)vmnsumrh=vmnsumrh / vmnctrh;
                    }else {
                        vmnsumrh=null;
                    }
                    //最高温度
                    if (vmnctmaxat>=monthless) {
                        if(vmnctmaxat!=0)vmnsummaxat=vmnsummaxat / vmnctmaxat;
                    }else {
                        vmnsummaxat=null;
                    }
                    //最低温度
                    if (vmnctminat>=monthless) {
                        if(vmnctminat!=0)vmnsumminat=vmnsumminat / vmnctminat;
                    }else {
                        vmnsumminat=null;
                    }

                    //求各极值的月值
                    if (vmnmaxslp==-99999){vmnmaxslp=null;}
                    if (vmnminslp==99999){vmnminslp=null;}
                    if (vmnmaxat==-9999){vmnmaxat=null;}
                    if (vmnminat==9999){vmnminat=null;}
                    if (vmnmaxws==-9999){vmnmaxws=null; }
                    if (vmnmaxwd==-999){vmnmaxwd=null;}
                    if (vmnminrh==999){vmnminrh=null;}

                    //如果各平均量月值的计数器不足最少有效日数，则将该平均量月值的平方赋为null
                    if (vmnctslp<monthless){ vmnsumslpqr=null;}
                    if (vmnctws<monthless){ vmnsumwsqr=null;}
                    if (vmnctat<monthless){ vmnsumatqr=null;}

                    //将月中的晴阴天日数通过有效观测次数换算到旬上来
                    List<Integer> z11 = Lists.newArrayList(1,3,5,7,8,10,12);
                    List<Integer> z12 = Lists.newArrayList(4,6,9,11);
                    if (vmnctfinecloudy >= monthless) {
                        if(z11.indexOf(imonth)>-1){
                            vmnfine=vmnfine * 31 / vmnctfinecloudy;
                            vmncloudy=vmncloudy * 31 / vmnctfinecloudy;
                        }
                        if(imonth == 2){
                            vmnfine=vmnfine * 28 / vmnctfinecloudy;
                            vmncloudy=vmncloudy * 28 / vmnctfinecloudy;
                        }
                        if(z12.indexOf(imonth)>-1){
                            vmnfine=vmnfine * 30 / vmnctfinecloudy;
                            vmncloudy=vmncloudy * 30 / vmnctfinecloudy;
                        }
                    }else {
                        vmnfine=null;
                        vmncloudy=null;
                    }

                    //将月中的雾和雷暴日数通过有效观测次数换算到旬上来
                    List<Integer> y11 = Lists.newArrayList(1,3,5,7,8,10,12);
                    List<Integer> y12 = Lists.newArrayList(4,6,9,11);
                    if (wmnctweather >= monthless && wmnctweather!=0){
                        if(y11.indexOf(imonth)>-1){
                            vmnfog=vmnfog*31/wmnctweather;
                            vmnthunder=vmnthunder*31/wmnctweather;
                        }
                        if(imonth == 2){
                            vmnfog=vmnfog*28/wmnctweather;
                            vmnthunder=vmnthunder*28/wmnctweather;
                        }
                        if(y12.indexOf(imonth)>-1){
                            vmnfog=vmnfog*30/wmnctweather;
                            vmnthunder=vmnthunder*30/wmnctweather;
                        }
                    }else {
                        vmnthunder=null;
                        vmnfog=null;
                    }


                    /**数据库入库**/

                    //判断计算的统计项有不为空的就启动入库
                    if  (
                            vmnsumslp != null || vmnsumd !=null || vmnsumws !=null || vmnsumat !=null || vmnsumn !=null ||
                                    vmnsumnh !=null || vmnsumrh !=null || vmnsummaxat !=null || vmnsumminat !=null ||
                                    vmnmaxslp !=null || vmnminslp !=null || vmnmaxws !=null || vmnmaxwd !=null || vmnmaxat !=null ||
                                    vmnminat !=null || vmnminrh !=null || vmncloudy !=null || vmnfine !=null || vc_h_all>0
                                    || vc_nh_all>0 || vc_vis_all>0 || vc_wd_all>0
                    ){
                        surfTongJiParam tjparam = new surfTongJiParam();
                        tjparam.setStation(vstation);
                        tjparam.setYear(viyear);
                        tjparam.setMonth(imonth);
                        tjparam.setDekad(0);
                        tjparam.setM_slp(vmnsumslp);
                        tjparam.setM_d(vmnsumd);
                        tjparam.setM_ws(vmnsumws);
                        tjparam.setM_at(vmnsumat);
                        tjparam.setM_max_at(vmnsummaxat);
                        tjparam.setM_min_at(vmnsumminat);
                        tjparam.setM_rh(vmnsumrh);
                        tjparam.setM_n(vmnsumn);
                        tjparam.setM_nh(vmnsumnh);
                        tjparam.setMax_slp(vmnmaxslp);
                        tjparam.setMin_slp(vmnminslp);
                        tjparam.setMax_ws(vmnmaxws);
                        tjparam.setMax_wd(vmnmaxwd);
                        tjparam.setMax_at(vmnmaxat);
                        tjparam.setMin_at(vmnminat);
                        tjparam.setMin_rh(vmnminrh);
                        tjparam.setSd_slp(vsdslp);
                        tjparam.setSd_ws(vsdws);
                        tjparam.setSd_at(vsdat);
                        tjparam.setSd_slp_qr(vmnsumslpqr);
                        tjparam.setSd_ws_qr(vmnsumwsqr);
                        tjparam.setSd_at_qr(vmnsumatqr);
                        tjparam.setC_sd_slp(vmnctslp);
                        tjparam.setC_sd_ws(vmnctws);
                        tjparam.setC_sd_at(vmnctat);
                        tjparam.setMcount(k);
                        tjparam.setCloudy(vmncloudy);
                        tjparam.setFine(vmnfine);
                        tjparam.setC_h_all(vc_h_all);
                        tjparam.setC_nh_all(vc_nh_all);
                        tjparam.setC_vis_all(vc_vis_all);
                        tjparam.setC_wd_all(vc_wd_all);
                        tjparam.setC_h_100(vc_h[0]);
                        tjparam.setC_h_200(vc_h[1]);
                        tjparam.setC_h_300(vc_h[2]);
                        tjparam.setC_h_600(vc_h[3]);
                        tjparam.setC_h_1500(vc_h[4]);
                        tjparam.setC_h_2500(vc_h[5]);
                        tjparam.setC_h_2500p(vc_h[6]);
                        tjparam.setC_nh_1(vc_nh[0]);
                        tjparam.setC_nh_3(vc_nh[1]);
                        tjparam.setC_nh_6(vc_nh[2]);
                        tjparam.setC_nh_8(vc_nh[3]);
                        tjparam.setC_nh_10(vc_nh[4]);
                        tjparam.setC_vis_1(vc_vis[0]);
                        tjparam.setC_vis_4(vc_vis[1]);
                        tjparam.setC_vis_10(vc_vis[2]);
                        tjparam.setC_vis_10p(vc_vis[3]);
                        tjparam.setC_wd_1 (vc_wd[0]);
                        tjparam.setC_wd_2(vc_wd[1]);
                        tjparam.setC_wd_3(vc_wd[2]);
                        tjparam.setC_wd_4(vc_wd[3]);
                        tjparam.setC_wd_5(vc_wd[4]);
                        tjparam.setC_wd_6(vc_wd[5]);
                        tjparam.setC_wd_7(vc_wd[6]);
                        tjparam.setC_wd_8(vc_wd[7]);
                        tjparam.setC_wd_9(vc_wd[8]);
                        tjparam.setC_wd_10(vc_wd[9]);
                        tjparam.setC_wd_11(vc_wd[10]);
                        tjparam.setC_wd_12(vc_wd[11]);
                        tjparam.setC_wd_13(vc_wd[12]);
                        tjparam.setC_wd_14(vc_wd[13]);
                        tjparam.setC_wd_15(vc_wd[14]);
                        tjparam.setC_wd_16(vc_wd[15]);
                        tjparam.setC_wd_17(vc_wd[16]);
                        tjparam.setFog(vmnfog);
                        tjparam.setThunder(vmnthunder);

                        //开始插入
                        tongjimapper.insertSurfYueTJ(tjparam);

                    }


                    //重新初始化------------
                    //月最值
                    vmnmaxws=-9999;vmnmaxwd=-999;vmnmaxat=-9999;vmnminat=9999;vmnminrh=999;vmnmaxslp=-99999;vmnminslp=99999;
                    //月累加变量
                    vmnsumslp=0;vmnsumd=0;vmnsumws=0;vmnsumat=0;vmnsumn=0;vmnsumnh=0;vmnsumrh=0;vmnsummaxat=0;vmnsumminat=0;vmnsumslpqr=0;vmnsumwsqr=0;vmnsumatqr=0;vsdslp=0;vsdws=0;vsdat=0;
                    //月计数变量
                    vmnctslp=0;vmnctd=0;vmnctws=0;vmnctat=0;vmnctn=0;vmnctnh=0;vmnctrh=0;vmnctmaxat=0;vmnctminat=0;
                    //月的雾日数和雷暴日数
                    vmnfog=0;
                    vmnthunder=0;
                    wmnctweather =0;
                    //月的晴天阴天日数
                    vmnfine=0; vmncloudy=0;
                    vmnctfinecloudy =0;
                    //月各级低云量、低云高、各风向、能见度记数变量
                    vc_nh = new Integer[]{0, 0, 0, 0, 0};
                    vc_h  = new Integer[]{0, 0, 0, 0, 0, 0, 0};
                    vc_vis= new Integer[]{0, 0, 0, 0};
                    vc_wd = new Integer[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                    vc_h_all=0;
                    vc_nh_all=0;
                    vc_vis_all=0;
                    vc_wd_all=0;

                    imonth=vmonth;
                    k=0;


                    //月循环结束
                }



                ////！！！！！！！！！！！！！！！！！时次循环 变量赋值！！！！！！！！！！！！！！！！！！！！！！！

                //时次-----------------------------------------------------------------------
                //这一段放在所有日、旬、月交替判断的前面是为了避免不同日的时次，如1日的12时和2日的18时被误认为是相邻时次

                //计算相对湿度
                if (vqtd==0 && vqat==0) {
                    vrh=selftools.getrh(vat, vtd);
                    vqrh=0;
                }else {
                    vrh=null;
                    vqrh=1;
                }

                //计算密度
                if (vqtd==0 && vqat==0 && vqslp==0) {
                    vd = selftools.getd(vat, vtd, vslp);
                    vqd = 0;
                }else {
                    vd=null;
                    vqd=1;
                }

                //统计低云量记数
                if (vqnh == 0){
                    vjnh=selftools.getd_nh(vnh);
                    switch(vjnh) {
                        case 1 : vc_nh[0]=vc_nh[0] + 1;vc_nh_all=vc_nh_all + 1;break;
                        case 2 : vc_nh[1]=vc_nh[1] + 1;vc_nh_all=vc_nh_all + 1;break;
                        case 3 : vc_nh[2]=vc_nh[2] + 1;vc_nh_all=vc_nh_all + 1;break;
                        case 4 : vc_nh[3]=vc_nh[3] + 1;vc_nh_all=vc_nh_all + 1;break;
                        case 5 : vc_nh[4]=vc_nh[4] + 1;vc_nh_all=vc_nh_all + 1;break;
                    }
                }

                //统计低云高记数
                if (vqh == 0){
                    vh=selftools.getd_h(vh);
                    switch(vh) {
                        case 1:vc_h[0] = vc_h[0] + 1; vc_h_all = vc_h_all + 1;break;
                        case 2:vc_h[1] = vc_h[1] + 1; vc_h_all = vc_h_all + 1;break;
                        case 3:vc_h[2] = vc_h[2] + 1; vc_h_all = vc_h_all + 1;break;
                        case 4:vc_h[3] = vc_h[3] + 1; vc_h_all = vc_h_all + 1;break;
                        case 5:vc_h[4] = vc_h[4] + 1; vc_h_all = vc_h_all + 1;break;
                        case 6:vc_h[5] = vc_h[5] + 1; vc_h_all = vc_h_all + 1;break;
                        case 7:vc_h[6] = vc_h[6] + 1; vc_h_all = vc_h_all + 1;break;
                    }
                }

                //统计最多风向计数
                if (vqwd == 0 && vqws == 0) {
                    vjwd=selftools.get16wd(vwd, vws);
                    switch (vjwd) {
                        case 0:vc_wd[16] = vc_wd[16] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 1:vc_wd[0] = vc_wd[0] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 2:vc_wd[1] = vc_wd[1] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 3:vc_wd[2] = vc_wd[2] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 4:vc_wd[3] = vc_wd[3] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 5:vc_wd[4] = vc_wd[4] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 6:vc_wd[5] = vc_wd[5] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 7:vc_wd[6] = vc_wd[6] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 8:vc_wd[7] = vc_wd[7] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 9:vc_wd[8] = vc_wd[8] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 10:vc_wd[9] = vc_wd[9] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 11:vc_wd[10] = vc_wd[10] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 12:vc_wd[11] = vc_wd[11] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 13:vc_wd[12] = vc_wd[12] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 14:vc_wd[13] = vc_wd[13] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 15:vc_wd[14] = vc_wd[14] + 1;vc_wd_all = vc_wd_all + 1;break;
                        case 16:vc_wd[15] = vc_wd[15] + 1;vc_wd_all = vc_wd_all + 1;break;
                    }
                }

                //统计能见度计数
                if (vqvis == 0) {
                    vvis=selftools.getd_vis(vvis);
                    switch (vvis) {
                        case 1:vc_vis[0] = vc_vis[0] + 1;vc_vis_all = vc_vis_all + 1;break;
                        case 2:vc_vis[1] = vc_vis[1] + 1;vc_vis_all = vc_vis_all + 1;break;
                        case 3:vc_vis[2] = vc_vis[2] + 1;vc_vis_all = vc_vis_all + 1;break;
                        case 4:vc_vis[3] = vc_vis[3] + 1;vc_vis_all = vc_vis_all + 1;break;
                    }
                }

                //旬月极值判断--因为极值不一定从4个标准时次中选，所以游标不能写成只选择标准时次
                if (vqmaxat==0 && vdkmaxat<vmaxat){ vdkmaxat=vmaxat;}
                if (vqminat==0 && vdkminat>vminat ){ vdkminat=vminat; }
                if (vqat==0    && vdkminat>vat    ){ vdkminat=vat   ; }
                if (vqat==0    && vdkmaxat<vat    ){ vdkmaxat=vat   ; }
                if (vqmaxat==0 && vmnmaxat<vmaxat ){ vmnmaxat=vmaxat; }
                if (vqminat==0 && vmnminat>vminat ){ vmnminat=vminat; }
                if (vqat==0    && vmnminat>vat    ){ vmnminat=vat   ; }
                if (vqat==0    && vmnmaxat<vat    ){ vmnmaxat=vat   ; }
                if (vqslp==0   && vdkmaxslp<vslp  ){ vdkmaxslp=vslp; }
                if (vqslp==0   && vdkminslp>vslp  ){ vdkminslp=vslp; }
                if (vqslp==0   && vmnmaxslp<vslp  ){ vmnmaxslp=vslp; }
                if (vqslp==0   && vmnminslp>vslp  ){ vmnminslp=vslp; }
                if (vqws==0  && vqwd==0  && vdkmaxws<vws    ){ vdkmaxws=vws;  vdkmaxwd=vwd;  }
                if (vqws==0  && vqwd==0  && vmnmaxws<vws    ){ vmnmaxws=vws;  vmnmaxwd=vwd;  }
                if (vqrh==0    && vdkminrh>vrh    ){ vdkminrh=vrh; }
                if (vqrh==0    && vmnminrh>vrh    ){ vmnminrh=vrh; }




                ////----------------判断当前是0 6 12 18四个时次的数据  数据累加------------------------------
                if(vhour.equals(0) || vhour.equals(6) || vhour.equals(12) || vhour.equals(18)){

                    //海平面气压
                    if (vqslp == 0) {
                        vsumslp = vsumslp + vslp;
                        vctslp = vctslp + 1;
                        if (key == 1) {
                            switch(vhour) {
                                case 0: vhslp[1]=1;vslp00=vslp;break;
                                case 600: vhslp[2]=1;vslp06=vslp;break;
                                case 1200:vhslp[3]=1;
                                    //如果需要且可以对06时插值
                                    if (vhslp[1] == 1 && vhslp[2] == 0){
                                        vsumslp = vsumslp + (vslp00 + vslp) / 2;
                                        vctslp = vctslp + 1;
                                    }
                                    break;
                                case 1800:vhslp[4]=1;
                                    //如果需要且可以对12时插值
                                    if (vhslp[2] == 1 && vhslp[3] == 0){
                                        vsumslp = vsumslp + (vslp06 + vslp) / 2;
                                        vctslp = vctslp + 1;
                                    }
                                    break;
                            }
                        }
                    }

                    //风速
                    if (vqws==0){
                        vsumws=vsumws+vws;
                        vctws=vctws+1;
                        if (key==1) {
                            switch (vhour) {
                                case 0:
                                    vhws[1] = 1;
                                    vws00 = vws;
                                    break;
                                case 600:
                                    vhws[2] = 1;
                                    vws06 = vws;
                                    break;
                                case 1200:
                                    vhws[3] = 1;
                                    //如果需要且可以对06时插值
                                    if (vhws[1] == 1 && vhws[2] == 0) {
                                        vsumws = vsumws + (vws00 + vws) / 2;
                                        vctws = vctws + 1;
                                    }
                                    break;
                                case 1800:
                                    vhws[4] = 1;
                                    //如果需要且可以对12时插值
                                    if (vhws[2] == 1 && vhws[3] == 0) {
                                        vsumws = vsumws + (vws06 + vws) / 2;
                                        vctws = vctws + 1;
                                    }
                                    break;
                            }
                        }
                    }
                    //密度
                    if (vqd==0){
                        vsumd=vsumd+vd;
                        vctd=vctd+1;
                        if (key==1) {
                            switch (vhour) {
                                case 0:
                                    vhd[1]=1; vd00=vd;
                                    break;
                                case 600:
                                    vhd[2]=1; vd06=vd;
                                    break;
                                case 1200:
                                    vhd[3] = 1;
                                    //如果需要且可以对06时插值
                                    if (vhd[1] == 1 && vhd[2] == 0) {
                                        vsumd=vsumd+(vd00+vd)/2;
                                        vctd=vctd+1;
                                    }
                                    break;
                                case 1800:
                                    vhd[4] = 1;
                                    //如果需要且可以对12时插值
                                    if (vhd[2] == 1 && vhd[3] == 0) {
                                        vsumd=vsumd+(vd06+vd)/2;
                                        vctd=vctd+1;
                                    }
                                    break;
                            }
                        }
                    }
                    //气温
                    if (vqat==0){
                        vsumat = vsumat + vat ;
                        vctat = vctat + 1 ;
                        if (key==1) {
                            switch (vhour) {
                                case 0:
                                    vhat[1]=1; vat00=vat;
                                    break;
                                case 600:
                                    vhat[2]=1; vat06=vat;
                                    break;
                                case 1200:
                                    vhat[3] = 1;
                                    //如果需要且可以对06时插值
                                    if (vhat[1] == 1 && vhat[2] == 0) {
                                        vsumat=vsumat+(vat00+vat)/2;
                                        vctat=vctat+1;
                                    }
                                    break;
                                case 1800:
                                    vhat[4] = 1;
                                    //如果需要且可以对12时插值
                                    if (vhat[2] == 1 && vhat[3] == 0) {
                                        vsumat=vsumat+(vat06+vat)/2;
                                        vctat=vctat+1;
                                    }
                                    break;
                            }
                        }
                    }


                    //总云量和低云量——因为总云量编码为9时表示“无法判断”所以即使质控通过只要编码为9（即函数JieMa_YunLiang返回-1），也不记入日合计和有效时次次数
                    //先对云量进行解码
                    if (vqn==0) {
                        vn = selftools.JieMa_YunLiang(vn);
                        if (vn < 0) {
                            vqn=1;
                        }
                    }
                    if (vqnh==0) {
                        vnh=selftools.JieMa_YunLiang(vnh);
                        if (vnh < 0) {
                            vqnh=1;
                        }
                    }



                    //对缺测数据的处理
                    //如果低云量缺测而总云量为0，则低云量置为0
                    if (vqnh!=0 && vqn==0 && vn==0 ) {
                        vnh=0;
                        vqnh=0;
                    }
                    //如果低云量缺测而总云量不为0，则低云量和总云量都认为缺测
                    if (vqnh!=0 && vqn==0 && vn>0) {
                        vqn=1;
                    }
                    //如果总云量缺测而低云量不缺测，则总云量等于低云量
                    if (vqnh==0 && vqn!=0) {
                        vn=vnh;
                        vqn=0;
                    }
                    //如果总云量和低云量都可用，且总云量低于低云量，则将低云量置为总云量
                    if (vqnh==0 && vqn==0 && vn<vnh) {
                        vnh=vn;
                    }


                    //对总云量和低云量
                    //①总云量
                    if (vqn==0 && vqnh==0){
                        vsumn=vsumn+vn;
                        vctn=vctn+1;
                        if (key==1) {
                            switch (vhour) {
                                case 0:
                                    vhn[1]=1; vn00=vn;
                                    break;
                                case 600:
                                    vhn[2]=1; vn06=vn;
                                    break;
                                case 1200:
                                    vhn[3] = 1;
                                    //如果需要且可以对06时插值
                                    if (vhn[1] == 1 && vhn[2] == 0) {
                                        vsumn=vsumn+(vn00+vn)/2;
                                        vctn=vctn+1;
                                    }
                                    break;
                                case 1800:
                                    vhn[4] = 1;
                                    //如果需要且可以对12时插值
                                    if (vhn[2] == 1 && vhn[3] == 0) {
                                        vsumn=vsumn+(vn06+vn)/2;
                                        vctn=vctn+1;
                                    }
                                    break;
                            }
                        }
                        //②低云量
                        vsumnh=vsumnh+vnh;
                        vctnh=vctnh+1;
                        if (key == 1) {
                            switch (vhour) {
                                case 0:
                                    vhnh[1] = 1;
                                    vnh00 = vnh;
                                    break;
                                case 600:
                                    vhnh[2] = 1;
                                    vnh06 = vnh;
                                    break;
                                case 1200:
                                    vhnh[3] = 1;
                                    //如果需要且可以对06时插值
                                    if (vhnh[1] == 1 && vhnh[2] == 0) {
                                        vsumnh=vsumnh+(vnh00+vnh)/2;
                                        vctnh=vctnh+1;
                                    }
                                    break;
                                case 1800:
                                    vhnh[4] = 1;
                                    //如果需要且可以对12时插值
                                    if (vhnh[2] == 1 && vhnh[3] == 0) {
                                        vsumnh=vsumnh+(vnh06+vnh)/2;
                                        vctnh=vctnh+1;
                                    }
                                    break;
                            }
                        }
                    }



                    //相对湿度
                    if (vqrh==0) {
                        vsumrh=vsumrh+vrh;
                        vctrh=vctrh+1;
                        if (key == 1) {
                            switch (vhour) {
                                case 0:
                                    vhrh[1] = 1;
                                    vrh00 = vrh;
                                    break;
                                case 600:
                                    vhrh[2] = 1;
                                    vrh06 = vrh;
                                    break;
                                case 1200:
                                    vhrh[3] = 1;
                                    //如果需要且可以对06时插值
                                    if (vhrh[1] == 1 && vhrh[2] == 0) {
                                        vsumrh=vsumrh+(vrh00+vrh)/2;
                                        vctrh=vctrh+1;
                                    }
                                    break;
                                case 1800:
                                    vhrh[4] = 1;
                                    //如果需要且可以对12时插值
                                    if (vhrh[2] == 1 && vhrh[3] == 0) {
                                        vsumrh=vsumrh+(vrh06+vrh)/2;
                                        vctrh=vctrh+1;
                                    }
                                    break;
                            }
                        }
                    }

                    //平均最高气温
                    if (vqmaxat==0) {
                        vsummaxat=vsummaxat+vmaxat;
                        vctmaxat=vctmaxat+1;
                        if (key == 1) {
                            switch (vhour) {
                                case 0:
                                    vhmaxat[1] = 1;
                                    vmaxat00 = vmaxat;
                                    break;
                                case 600:
                                    vhmaxat[2] = 1;
                                    vmaxat06 = vmaxat;
                                    break;
                                case 1200:
                                    vhmaxat[3] = 1;
                                    //如果需要且可以对06时插值
                                    if (vhmaxat[1] == 1 && vhmaxat[2] == 0) {
                                        vsummaxat=vsummaxat+(vmaxat00+vmaxat)/2;
                                        vctmaxat=vctmaxat+1;
                                    }
                                    break;
                                case 1800:
                                    vhmaxat[4] = 1;
                                    //如果需要且可以对12时插值
                                    if (vhmaxat[2] == 1 && vhmaxat[3] == 0) {
                                        vsummaxat=vsummaxat+(vmaxat06+vmaxat)/2;
                                        vctmaxat=vctmaxat+1;
                                    }
                                    break;
                            }
                        }
                    }
                    //平均最低气温
                    if (vqminat==0) {
                        vsumminat=vsumminat+vminat;
                        vctminat=vctminat+1;
                        if (key == 1) {
                            switch (vhour) {
                                case 0:
                                    vhminat[1] = 1;
                                    vminat00 = vminat;
                                    break;
                                case 600:
                                    vhminat[2] = 1;
                                    vminat06 = vminat;
                                    break;
                                case 1200:
                                    vhminat[3] = 1;
                                    //如果需要且可以对06时插值
                                    if (vhminat[1] == 1 && vhminat[2] == 0) {
                                        vsumminat=vsumminat+(vminat00+vminat)/2;
                                        vctminat=vctminat+1;
                                    }
                                    break;
                                case 1800:
                                    vhminat[4] = 1;
                                    //如果需要且可以对12时插值
                                    if (vhminat[2] == 1 && vhminat[3] == 0) {
                                        vsumminat=vsumminat+(vminat06+vminat)/2;
                                        vctminat=vctminat+1;
                                    }
                                    break;
                            }
                        }
                    }




                    //四个时次判断结束
                }

                //是否有雾和雷暴---------------------------------
                //为了使日交替时iisfog和iisthunder中的数据为上一日的数据，判断本日有无雾和雷暴的语句，放在日交替判断的后面。
                //用过去天气现象w1和w2判断
                if (vhour != 0){
                    if (vqw1 == 0 || vqw2 == 0){iisweatherchecked=1;}
                    if (vqw1 == 0 && (selftools.getw12(vw1) == 2) || (vqw2 == 0 && selftools.getw12(vw2) == 2)){ iisfog=1;}
                    if (vqw1 == 0 && (selftools.getw12(vw1) == 7) || (vqw2 == 0 && selftools.getw12(vw2) == 7)){ iisthunder=1;}
                }
                //用现在天气现象ww判断
                if (vqww==0){iisweatherchecked=1;}
                if (vqww==0 && selftools.getww(vww)==17){ iisfog=1;}
                if (vqww==0 && selftools.getww(vww)==10){iisthunder=1;}
                //是否有雾和雷暴---------------------------------判断结束

                //更新一日内的时次计数
                i=i+1;
                //时次统计结束----------------------------------


                //for循环结束
            }

        }else{
            return Result.error("查询结果为空！");
        }


        return Result.success("");
    }










    //end
}

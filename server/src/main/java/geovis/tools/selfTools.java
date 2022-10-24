package geovis.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

import java.io.*;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.cos;
import static java.lang.Math.exp;

/**
 * 内部使用的自研工具方法
 */
@Data
public class selfTools {


    /*
    * 地面和高空统计调用方法集合
    *
    *
     */



    //拉格朗日插值
    //X1\2\3需要插枝的气象要素（除气压和密度）
    //h1\2\3对应的几何高度
    //h所要插值出的要素的几何高度
    public Double lagrange(Integer h, Integer X1, Integer h1, Integer X2, Integer h2, Integer X3, Integer h3){
        Integer res = (h-h2)*(h-h3)*X1/((h1-h2)*(h1-h3))+(h-h1)*(h-h3)*X2/((h2-h1)*(h2-h3))+(h-h2)*(h-h1)*X3/((h3-h2)*(h3-h1));
        return  res.doubleValue();
    }

    //对数插值
    //对气压、密度进行对数插值
    public Double logarithm(Integer h, Integer X1, Integer h1, Integer X2, Integer h2){
        return exp(Math.log(X1)-Math.log(X1/X2)*(h-h1)/(h2-h1));
    }

    //判断云高
    public Integer getd_h(Integer vh){
        Integer result = 0;
        if(vh == 0 || vh == 1){result=1;}//<100米
        else if(vh == 2){result=2;}//<200米
        else if(vh == 3){result=3;}//<300米
        else if(vh == 4){result=4;}//<600米
        else if(vh == 5 || vh == 6){result=5;}//<1500米
        else if(vh == 7 || vh == 8){result=6;}//<2500米
        else if(vh == 9){result=7;}//>=2500米
        return result;
    }

    //判断低云量成数属于哪个等级
    public Integer getd_nh(Integer vnh){
        Integer result = 0;
        if(vnh<2){result=1;}//0-1成
        else if(vnh==2){result=2;}//2-3成
        else if(vnh<6){result=3;}//4-6成
        else if(vnh<7){result=4;}//7-8成
        else if(vnh<9){result=5;}//9-10成
        return result;
    }

    //判断能见度
    public Integer getd_vis(Integer vvis){
        Integer result = 0;
        if(vvis<10 || vvis==90 || vvis==91 || vvis==92 || vvis==93){result=1;}//<1km
        if(vvis<40 || vvis==94 || vvis==95){result=2;}//1-4km
        if(vvis<51 || vvis>55 && vvis<60 || vvis==96){result=3;}//4-10km
        if(vvis<100 || vvis>59){result=4;}//>=10km
        return result;
    }

    //将10度间隔的风向转化为16方位风向
    //由于数据质量问题，有很多风向不为0而风速为0的情况，对这种情况处理为静风
    //注意风向为0而风速不为0的情况是北风
    public Integer get16wd(Integer vwd,Integer vws){
        Integer result = -1;
        if(vws==0){result=0;}//C
        if(vwd>=0 && vwd<11.25 && vws!=0){result=1;}//N
        if(vwd>=11.25 && vwd<33.75 && vws!=0){result=2;}//NNE
        if(vwd>=33.75 && vwd<56.25 && vws!=0){result=3;}//NE
        if(vwd>=56.25 && vwd<78.75 && vws!=0){result=4;}//ENE
        if(vwd>=78.75 && vwd<101.25 && vws!=0){result=5;}//E
        if(vwd>=101.25 && vwd<123.75 && vws!=0){result=6;}//ESE
        if(vwd>=123.75 && vwd<146.25 && vws!=0){result=7;}//SE
        if(vwd>=146.25 && vwd<168.75 && vws!=0){result=8;}//SSE
        if(vwd>=168.75 && vwd<191.25 && vws!=0){result=9;}//S
        if(vwd>=191.25 && vwd<213.75 && vws!=0){result=10;}//SSW
        if(vwd>=213.75 && vwd<236.25 && vws!=0){result=11;}//SW
        if(vwd>=236.25 && vwd<258.75 && vws!=0){result=12;}//WSW
        if(vwd>=258.75 && vwd<281.25 && vws!=0){result=13;}//W
        if(vwd>=281.25 && vwd<303.75 && vws!=0){result=14;}//WNW
        if(vwd>=303.75 && vwd<326.25 && vws!=0){result=15;}//NW
        if(vwd>=326.25 && vwd<348.75 && vws!=0){result=16;}//NNW
        if(vwd>=348.75 && vwd<=360   && vws!=0){result=1;}//N
        return result;
    }

    //过去天气现象w1,w2解码
    public Integer getw12(Integer vw){
        Integer result = 0;
        if(vw == 3){result=1;}//沙尘暴、吹雪、雪暴
        if(vw == 4){result=2;}//雾
        if(vw == 5){result=3;}//毛毛雨
        if(vw == 6){result=4;}//雨
        if(vw == 7){result=5;}//雪或雨夹雪
        if(vw == 8){result=6;}//阵雨
        if(vw == 9){result=7;}//雷暴
        return result;
    }

    //现在天气现象解码
    public Integer getww(Integer vww){
        Integer result = 0;
        //只判断是否有雾和雷暴
        if(vww==28 || (vww>=40 && vww<=49)){result=17;}//雾
        if(vww == 17 || vww == 29 || vww>=91 && vww<=99){result=10;}//雷暴
        /*
        //因为有多种天气现象同时出现，所以需要用位图或者数组存放返回值
        if(vww == 4){result=1;}//烟
        if(vww == 5){result=2;}//霾
        if(vww == 6){result=3;}//浮尘
        if(vww == 7){result=4;}//扬沙
        if(vww == 8){result=5;}//沙尘
        if(vww == 9 || vww == 30 || vww == 31 || vww == 32 || vww == 33 || vww == 34 || vww == 35 || vww == 98){result=6;}//沙尘暴
        if(vww == 10 || vww == 11 || vww == 12){result=7;}//轻雾
        if(vww == 13){result=8;}//闪电
        if(vww == 14 || vww == 15 || vww == 16 || vww>=50 && vww<=99){result=9;}//降水
        if(vww == 17 || vww == 19 || vww>=91 && vww<=99){result=10;}//雷暴
        if(vww == 18 || vww == 87 || vww == 88 || vww == 93 || vww == 94 || vww == 96 || vww == 99){result=11;}//散
        if(vww == 19){result=12;}//龙卷
        if(vww == 20 || vww == 21 || vww == 23 || vww == 24 || vww == 25 || vww == 80 || vww == 81 || vww == 82 || vww == 83 || vww == 84 || vww == 93 || vww == 94 || vww == 97 || vww == 98 || vww>=50 && vww<=69){result=13;}//雨
        if(vww == 22 || vww == 23 || vww == 26 || vww == 83 || vww == 84 || vww == 85 || vww == 86 || vww == 93 || vww == 94 || vww == 97 || vww>=68 && vww<=79){result=14;}//雪
        if(vww == 36 || vww == 37 || vww == 38 || vww == 39){result=15;}//吹雪
        if(vww == 27 || vww == 89 || vww == 90 || vww == 93 || vww == 94 || vww == 96 || vww == 99){result=16;}//冰雹
        if(vww == 28 || vww>=40 && vww<=49){result=17;}//雾
         */
        return result;
    }


    //将位势高度转化成几何高度，h米，latitude度
    public Integer tojihehgt(Integer h,Integer latitude){

        //将角度换算成弧度
        Double latitude_arc=latitude*3.1415927/180;
        //计算该纬度上的自由落体加速度
        Double g=9.80616*(1-0.002637*cos(2*latitude_arc)+0.0000059*cos(2*latitude_arc)*cos(2*latitude_arc));
        //计算该纬度上的地球有效半径
        Double r=2*g/(3.085462E-6+cos(2*latitude_arc)*2.27E-9);
        //将位势高度转化成几何高度
        Double result=(r*h)/(r*g/9.80665-h);
        return result.intValue();
    }

    //计算空气密度，vat，vtd 0.1度，vslp 0.1百帕
    public Integer getd(Integer vat,Integer vtd,Integer vslp){

        //参数组类型定义
        //常数
        //Double[][] pa = new Double[2][3];
        //变量
        Integer vi=1;
        Double vvat=1.00;
        Double vvtd=1.00;
        int vvslp=  1;
        //参数初始化
        Double ce=6.11139;
        Double ct0=273.16;
        Double cr=287.0528;
        Double[][] pa = {{19.802, 17.885, 0.2311E-3},{23.662, -5.5087, 0.1098E-3}};
        if (vat/10>0) {
            vi=0;
        }else {
            vi=1;
        }

        //温度换算为绝对温度K
        vvat = vat / 10 + ct0;
        vvtd = vtd / 10 + ct0;
        //气压换算为百帕
        vvslp = vslp / 10;
        Double result = 100000 * vvslp / (cr * vvat * (1 + 0.37082 * (ce * exp(pa[vi][0] * (vvtd - ct0) / (vvtd - pa[vi][1] + pa[vi][2] * vvtd * vvtd))/vvslp)));

        return result.intValue();
    }

    //计算大气折射指数，vat,vtd 0.1度，vslp 百帕
    public Integer getaste(Integer vat,Integer vtd,Integer vpress){

        //参数组类型定义
        //变量
        Integer vi=1;
        Double vvat=1.00;
        Double vvtd=1.00;
        int vvslp=  1;
        //参数初始化
        Double ce = 6.11139;
        Double ct0 = 273.16;

        Double[][] pa = {{19.802, 17.885, 0.2311E-3},{23.662, -5.5087, 0.1098E-3}};
        if (vat/10>0) {
            vi=0;
        }else {
            vi=1;
        }

        //温度换算为绝对温度K
        vvat = vat / 10 + ct0;
        vvtd = vtd / 10 + ct0;
        //计算水气压
        Double ve=ce*exp(pa[vi][0]*(vvtd-ct0)/(vvtd-pa[vi][1]+pa[vi][2]*vvtd*vvtd));
        Double result=77.6*vpress/vvat +3.73*100000*ve/(vvat*vvat);

        return result.intValue();
    }

    //计算相对湿度
    public Integer getrh(Integer vat,Integer vtd){

        //参数组类型定义
        //变量
        Integer vi=1;
        Double vvat=1.00;
        Double vvtd=1.00;
        //参数初始化
        Double ct0 = 273.16;

        Double[][] pa = {{19.802,17.885,0.0002311},{23.662,-5.5087,0.0001098}};
        if (vat/10>0) {
            vi=0;
        }else {
            vi=1;
        }

        //温度换算为绝对温度K
        vvat = vat / 10 + ct0;
        vvtd = vtd / 10 + ct0;
        //计算相对湿度
        Double result = 100*exp(pa[vi][0]*(vvtd-ct0)/(vvtd-pa[vi][1]+pa[vi][2]*vvtd*vvtd)-pa[vi][0]*(vvat-ct0)/(vvat-pa[vi][1]+pa[vi][2]*vvat*vvat));

        return result.intValue();
    }


    //云量进行解码
    public Integer JieMa_YunLiang(Integer vMa){
        Double result = -2.0;
        switch(vMa){
            case 0:result=0.0;break;
            case 1:result=1.0;break;
            case 2:result=2.5;break;
            case 3:result=4.0;break;
            case 4:result=5.0;break;
            case 5:result=6.0;break;
            case 6:result=7.5;break;
            case 7:result=9.5;break;
            case 8:result=10.0;break;
            case 9:result=-1.0;break;
            case 99:result=-1.0;break;
            default:result=-2.0;break;
        }
        return result.intValue();
    }




    //方法一：用JAVA自带的函数 判断字符串是否是数字类型
    public Boolean isNumber(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
    //判断字符串是否是数字类型 可以判断正负、整数小数
    public Boolean isNumber1(String str){
        // ?:0或1个, *:0或多个, +:1或多个
        Boolean strResult = false;
        if(str != null && str != "" && !str.equals(""))strResult = str.matches("-?[0-9]+.？[0-9]*");
        return strResult;
    }

    //json对象转json字符串 转成"{\"requestDt\":20210903050000, \"data\":[{\"lon\":2.0, \"lat\":1.0}]}"
    public String formatJson(Object json){
        if(null == json) return "";
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        String jsonStr = gson.toJson(json);
        return jsonStr;
    }

    //从json文件中获取key对应的值 返回string类型值
    public String readJsonForKeyToString(File jsonFile,String key){
        String res = "";
        try {
            String jsonString = FileUtils.readFileToString(jsonFile,"utf-8");
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            res = jsonObject.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    //读取json文件成map对象
    public Map readJsonToMap(String json_url) {
        String jsonStr = "";
        try {
            File jsonFile = new File(json_url);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            Gson gson = new Gson();
            Map<String, Object> map = new HashMap<String, Object>();
            map = gson.fromJson(jsonStr, map.getClass());
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //获取当前服务器ip
    public String getIp(){
        try {
            InetAddress ia=InetAddress.getLocalHost();
            String localip=ia.getHostAddress();
            return localip;
        } catch (Exception e) {
            e.printStackTrace();
            return "localhost";
        }
    }
    //时间格式化 20181216 2013010100 2000010403
    public String formatTime(String str){
        if(str.equals("") || str.equals(null) || str.indexOf("-")>-1)return str;
        int len = str.length() > 14 ? 14 :str.length() ;//控制最大长度14
        switch(len){
            case 5 : str = str.substring(0,4)+"-0"+str.substring(4,5)+"-00 00:00:00";break;
            case 6 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-00 00:00:00";break;
            case 7 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-0"+str.substring(6,7)+" 00:00:00";break;
            case 8 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" 00:00:00";break;
            case 9 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" 0"+str.substring(8,9)+":00:00";break;
            case 10 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" "+str.substring(8,10)+":00:00";break;
            case 11 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" "+str.substring(8,10)+":0"+str.substring(10,11)+":00";break;
            case 12 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" "+str.substring(8,10)+":"+str.substring(10,12)+":00";break;
            case 13 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" "+str.substring(8,10)+":"+str.substring(10,12)+":0"+str.substring(12,13);break;
            case 14 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" "+str.substring(8,10)+":"+str.substring(10,12)+":"+str.substring(12,14);break;
        }
        return str;
    }
    //时间格式化 只保留日期和小时 后面填充0
    public String formatTime3(String str){
        if(str.equals("") || str.equals(null) || str.indexOf("-")>-1)return str;
        int len = str.length() > 14 ? 14 :str.length() ;//控制最大长度14
        switch(len){
            case 5 : str = str.substring(0,4)+"-0"+str.substring(4,5)+"-00 00:00:00";break;
            case 6 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-00 00:00:00";break;
            case 7 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-0"+str.substring(6,7)+" 00:00:00";break;
            case 8 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" 00:00:00";break;
            case 9 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" 0"+str.substring(8,9)+":00:00";break;
            case 10 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" "+str.substring(8,10)+":00:00";break;
            case 11 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" "+str.substring(8,10)+":00:00";break;
            case 12 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" "+str.substring(8,10)+":00:00";break;
            case 13 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" "+str.substring(8,10)+":00:00";break;
            case 14 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8)+" "+str.substring(8,10)+":00:00";break;
        }
        return str;
    }
    //时间格式化 20181216 2013010100 2000010403
    public String formatTime2(String str){
        if(str.equals("") || str.equals(null) || str.indexOf("-")>-1)return str;
        int len = str.length() > 14 ? 14 :str.length() ;//控制最大长度14
        switch(len){
            case 5 : str = str.substring(0,4)+"-0"+str.substring(4,5)+"-00";break;
            case 6 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-00";break;
            case 7 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-0"+str.substring(6,7);break;
            case 8 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8);break;
            case 9 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8);break;
            case 10 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8);break;
            case 11 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8);break;
            case 12 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8);break;
            case 13 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8);break;
            case 14 : str = str.substring(0,4)+"-"+str.substring(4,6)+"-"+str.substring(6,8);break;
        }
        return str;
    }

    /**
     @param: 入参是当前时间2020-03-01
     @return:返参是前一天的日期,理应为2020-02-29(闰年)
     */
    public String getBeforeDay(String dateTime,Integer num){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try{
            date=simpleDateFormat.parse(dateTime);
        }catch (ParseException e){
            //logger.error("String转Date日期失败:",e);
        }
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        //往前一天
        calendar.add(Calendar.DAY_OF_MONTH,num);
        return simpleDateFormat.format(calendar.getTime());
    }


    /*
     * 将时间转换为时间戳
     */
    public Long dateToStamp(String time){
        if(time.equals(""))return null;
        if(time.indexOf("-")<0)return null;
        Long res = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            res = sdf.parse(time).getTime()/1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res;
    }

    /*
     * 将时间转换为时间戳
     */
    public Long dateToStamp2(String time){
        if(time.equals(""))return null;
        if(time.indexOf("-")<0)return null;
        Long res = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            res = sdf.parse(time).getTime()/1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res;
    }

    //获取当前日期前n天的日期
    public String getNowBeforeDay(Integer n){
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(new Date());
        //拿到前一天
        calendar.add(Calendar.DAY_OF_MONTH,-n);
        //获取后一天
        //calendar.add(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String date_now = simpleDateFormat.format(calendar.getTime());
        return date_now;
    }
    //时间去掉符号 2022-01-04 改为 20220104
    public String getdateFromat(String startTime){
        if (startTime != null && startTime != "" && startTime.indexOf("-") > -1) {
            startTime = startTime.replace("-", "");
            startTime = startTime.replace(" ", "");
            startTime = startTime.replace(":", "");
        }
        return startTime;
    }





}

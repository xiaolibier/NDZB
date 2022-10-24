package geovis.service;


import geovis.mapper.*;
import geovis.param.*;
import geovis.tools.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.DateUtils;

import javax.annotation.Resource;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class dataSourceService {


    @Value("${dataDir}")
    private String dataDir;
    @Resource
    private dataSourceMapper datasourcemapper;
    //@Autowired
    private selfTools selftools = new selfTools();
    private static final String FIX="\uFEFF";


    //获取所有站点列表
    public Result getStaticStation(stationParam param) {

        List<Map<String,String>> mapList = datasourcemapper.getStaticStation(param);//查询
        if(mapList.size() > 0){
        }else{
            return Result.error("查询结果为空！");
        }
        return Result.success(mapList);
    }

    //修改成果数据
    public Result saveChengGuo(chengGuoParam param) {

        datasourcemapper.saveChengGuo(param);//上传
        return Result.success("修改成功！");
    }

    //上传成果数据
    public Result uploadChengGuo(chengGuoParam param) {

        //字段赋值
        String id = String.valueOf(System.currentTimeMillis());//用时间戳加文件大小做id
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String create_time = df.format(new Date());//获取当前create_time
        param.setId(id);//设置id
        //param.setUpload_time(create_time);//记录上传时间
        param.setStat("上传成功");//记录上传时间
        datasourcemapper.uploadChengGuo(param);//上传
        return Result.success("上传成功！");
    }

    //删除一个成果记录
    public Result deleteChengGuoRecordById(chengGuoParam param) {
        String id = param.getId();
        //参数校验
        String errorMsg = "";
        if(id.equals(null) || id.equals("")){errorMsg = "【非空错误】参数id不能为空！";}
        if(errorMsg != "")return Result.error(errorMsg);
        datasourcemapper.deleteChengGuoRecordById(param);
        return Result.success("保存成功！");
    }

    //删除一个资料记录
    public Result deleteZiLiaoRecordById(dataSourceParam param) {
        String id = param.getId();
        //参数校验
        String errorMsg = "";
        if(id.equals(null) || id.equals("")){errorMsg = "【非空错误】参数id不能为空！";}
        if(errorMsg != "")return Result.error(errorMsg);
        datasourcemapper.deleteZiLiaoRecordById(param);
        return Result.success("保存成功！");
    }

    //资料文件上传日志入库
    public Result zlInsertToDB(dataSourceParam param) {

        //字段赋值
        String id = String.valueOf(System.currentTimeMillis());//用时间戳加文件大小做id
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String create_time = df.format(new Date());//获取当前create_time
        param.setId(id);//设置id
        param.setTime(create_time);//记录上传时间

        datasourcemapper.fileToDB(param);

        return Result.success("发送成功！");
    }

    //根据多个条件查询文件
    public Result getChengGuoList(chengGuoParam param) {

        Map<String,Object> data = new HashMap<>();
        String startTime = param.getStart_time();
        String endTime = param.getEnd_time();

        List<Map<String,String>> recordList = datasourcemapper.getChengGuoList(param);//查询是否已经存在当前记录
        Long date1 = null;
        Long date2 = null;
        if(startTime != null) {
            //将日期时间2021-09-03 00:00:00改为 20210903000000
            if (startTime != null && startTime != "" && startTime.indexOf("-") > -1) {
                startTime = startTime.replace("-", "");
                startTime = startTime.replace(" ", "");
                startTime = startTime.replace(":", "");
            }
            //存用于sql查询的时间
            if (startTime.length() >= 10) {
                String timeStr = startTime.substring(0, 8);//截取20210903 用于like查询
                param.setUpload_time(timeStr);//用于sql查询数据
            }
            startTime = selftools.formatTime(startTime);//格式化日期成2021-09-03 10:20:30格式
            date1 = selftools.dateToStamp(startTime);//转为时间戳
        }
        if(endTime != null) {
            date2 = selftools.dateToStamp(endTime);//转为时间戳
        }
        //recordList = doSearch(recordList, date1, date2, "upload_time");//时间搜索
        data.put("data",recordList);
        data.put("dataSource",dataDir);
        return Result.success(data);
    }

    //根据多个条件查询文件
    public Result getFileList(dataSourceParam param) {

        Map<String,Object> data = new HashMap<>();
        String startTime = param.getStart_time();
        String endTime = param.getEnd_time();

        List<Map<String,String>> recordList = datasourcemapper.getFileList(param);//查询是否已经存在当前记录
        Long date1 = null;
        Long date2 = null;
        if(startTime != null) {
            //将日期时间2021-09-03 00:00:00改为 20210903000000
            if (startTime != null && startTime != "" && startTime.indexOf("-") > -1) {
                startTime = startTime.replace("-", "");
                startTime = startTime.replace(" ", "");
                startTime = startTime.replace(":", "");
            }
            //存用于sql查询的时间
            if (startTime.length() >= 10) {
                String timeStr = startTime.substring(0, 8);//截取20210903 用于like查询
                param.setTime(timeStr);//用于sql查询数据
            }
            startTime = selftools.formatTime(startTime);//格式化日期成2021-09-03 10:20:30格式
            date1 = selftools.dateToStamp(startTime);//转为时间戳
        }
        if(endTime != null) {
            date2 = selftools.dateToStamp(endTime);//转为时间戳
        }
        recordList = doSearch(recordList, date1, date2, "time");//时间搜索
        data.put("data",recordList);
        data.put("dataSource",dataDir);
        return Result.success(data);
    }



    //按照开始时间和结束时间搜索
    private List<Map<String,String>> doSearch(List<Map<String,String>> dataAll,Long startTime,Long endTime,String seachStr){
        List<Map<String,String>> dataAll1 = new ArrayList<>();
        if(dataAll != null && dataAll.size() > 0){
            for(int i = 0,len= dataAll.size(); i<len;i++) {
                String time1 = dataAll.get(i).get(seachStr);
                //判断时间是否为空或者不在搜索的时间段范围内 删除此项 等同于时间查询
                Long date1 = null;
                if(time1 != null) {
                    time1 = selftools.formatTime(time1);//格式化日期成2021-09-03 00:00:00格式
                    if(!time1.equals("") && time1.indexOf("-")>-1) {
                        date1 = selftools.dateToStamp(time1);//转为时间戳
                    }
                }
                if(time1 != null && !time1.equals("") && (startTime != null && date1 > startTime || startTime == null) && (endTime != null && date1 < endTime || endTime == null) ){//判断时间不在范围内的删除
                    dataAll1.add(dataAll.get(i));
                }
            }
        }
        return dataAll1;
    }



    /**
     * 获取csv文件内容 并入库
     * @return 对象list
     */
    public List<Map<String,Object>> dataInDB(byte[] bate, String datatype) throws IOException {


        tempParam param = new tempParam();
        surfParam param2 = new surfParam();
        List<Map<String,Object>> allString = new ArrayList();
        Map<String,Object> callLogInfo ;
        List<String> list = new ArrayList();
        // 获取文件内容
        list = getSource(bate);
        //// 获取文件表头======================================
        List<String> title = Arrays.asList(list.get(0).split(","));
        String customerName = title.get(0).trim();//获取第n个表格字段名


        //// 循环内容 循环每一行 入库
        list.remove(0);//去掉第一行表头
        for(int i = 0; i<list.size();i++){
            List<String> content = Arrays.asList(list.get(i).split(","));
            // 当没有添加额外参数时
            if(content!=null){
                //遍历每一个字段 content.get(0)
                if(datatype != null && datatype.equals("1") && content.size()>0){//高空数据
                    param.setStation(content.get(0));
                    param.setYear(s2d(content.get(1)));
                    param.setMonth(s2d(content.get(2)));
                    param.setDay(s2d(content.get(3)));
                    param.setHour(s2d(content.get(4)));
                    param.setMinute(s2d(content.get(5)));
                    param.setOdate(s2date(content.get(6)));
                    param.setLdate(s2date2(content.get(7)));
                    param.setLtime(s2d(content.get(8)));
                    param.setLatitude(s2d(content.get(9)));
                    param.setLongitude(s2d(content.get(10)));
                    param.setType(s2d(content.get(12)));
                    param.setPress(s2d(content.get(13)));
                    param.setMean(s2d(content.get(14)));
                    param.setAt(s2d(content.get(16)));
                    param.setTd(s2d(content.get(17)));
                    param.setWd(s2d(content.get(18)));
                    param.setWs(s2d(content.get(19)));
                    param.setQ_mean(s2d(content.get(21)));
                    param.setQ_wd(s2d(content.get(25)));
                    param.setQ_ws(s2d(content.get(26)));
                    datasourcemapper.tempInToDB(param);
                }else if(datatype != null && datatype.equals("2") && content.size()>0){//地面数据

                    param2.setStation(content.get(0));
                    param2.setYear(s2int(content.get(1)));
                    param2.setMonth(s2int(content.get(2)));
                    param2.setDay(s2int(content.get(3)));
                    param2.setHour(s2int(content.get(4)));
                    param2.setCccc(content.get(9));
                    param2.setLatitude(s2d(content.get(10)));
                    param2.setLongitude(s2d(content.get(11)));
                    param2.setType(s2int(content.get(13)));
                    param2.setStn_type(s2int(content.get(14)));
                    param2.setH(s2int(content.get(18)));
                    param2.setVis(s2int(content.get(19)));
                    param2.setSlp(s2d(content.get(29)));
                    param2.setLp(s2d(content.get(26)));
                    param2.setWd(s2int(content.get(21)));
                    param2.setWs(s2d(content.get(22)));
                    param2.setAt(s2d(content.get(23)));
                    param2.setTd(s2d(content.get(24)));
                    param2.setMin_at(s2d(content.get(70)));
                    param2.setW2(content.get(34));
                    param2.setWw(s2int(content.get(32)));
                    param2.setW1(s2int(content.get(33)));
                    param2.setN(s2int(content.get(20)));
                    param2.setNh(s2int(content.get(35)));
                    param2.setCl(s2int(content.get(36)));
                    param2.setCm(s2int(content.get(37)));
                    param2.setCh(s2int(content.get(38)));
                    param2.setDp24(s2d(content.get(63)));
                    param2.setRain24(s2d(content.get(62)));
                    param2.setQ_slp(s2int(content.get(92)));
                    param2.setQ_lp(s2int(content.get(89)));
                    param2.setQ_wd(s2int(content.get(84)));
                    param2.setQ_ws(s2int(content.get(85)));
                    param2.setQ_at(s2int(content.get(86)));
                    param2.setQ_td(s2int(content.get(87)));
                    param2.setQ_min_at(s2int(content.get(133)));
                    param2.setQ_w2(s2int(content.get(97)));
                    param2.setQ_vis(s2int(content.get(82)));
                    param2.setQ_ww(s2int(content.get(95)));
                    param2.setQ_w1(s2int(content.get(96)));
                    param2.setQ_n(s2int(content.get(83)));
                    param2.setQ_nh(s2int(content.get(98)));
                    param2.setQ_cl(s2int(content.get(99)));
                    param2.setQ_h(s2int(content.get(81)));
                    param2.setQ_cm(s2int(content.get(100)));
                    param2.setQ_ch(s2int(content.get(101)));
                    param2.setQ_dp24(s2int(content.get(126)));
                    param2.setQ_rain24(s2int(content.get(125)));
                    param2.setRh(s2d(content.get(25)));
                    param2.setSpl(s2int(content.get(27)));
                    param2.setA3(s2d(content.get(28)));
                    param2.setDp_idx(s2int(content.get(31)));
                    param2.setN1(s2int(content.get(39)));
                    param2.setC1(s2int(content.get(40)));
                    param2.setH1(s2int(content.get(41)));
                    param2.setN2(s2int(content.get(42)));
                    param2.setC2(s2int(content.get(43)));
                    param2.setH2(s2int(content.get(44)));
                    param2.setN3(s2int(content.get(45)));
                    param2.setC3(s2int(content.get(46)));
                    param2.setH3(s2int(content.get(47)));
                    param2.setN4(s2int(content.get(48)));
                    param2.setC4(s2int(content.get(49)));
                    param2.setH4(s2int(content.get(50)));
                    param2.setCbn(s2int(content.get(51)));
                    param2.setCbt(s2int(content.get(52)));
                    param2.setCbh(s2int(content.get(53)));
                    param2.setRain01(s2d(content.get(54)));
                    param2.setRain02(s2d(content.get(55)));
                    param2.setRain03(s2d(content.get(56)));
                    param2.setRain06(s2d(content.get(57)));
                    param2.setRain09(s2d(content.get(58)));
                    param2.setRain12(s2d(content.get(59)));
                    param2.setRain15(s2d(content.get(60)));
                    param2.setRain18(s2d(content.get(61)));
                    param2.setDt24(s2d(content.get(64)));
                    param2.setMax_at12(s2d(content.get(67)));
                    param2.setMin_at12(s2d(content.get(68)));
                    param2.setSnowh(s2d(content.get(69)));
                    param2.setEva(s2d(content.get(71)));
                    param2.setAlr(s2d(content.get(72)));
                    param2.setSunp(s2d(content.get(73)));
                    param2.setGs(s2int(content.get(74)));
                    param2.setSw1(s2int(content.get(75)));
                    param2.setSw2(s2int(content.get(76)));
                    param2.setSw3(s2int(content.get(77)));
                    param2.setSw4(s2int(content.get(78)));
                    param2.setSw5(s2int(content.get(79)));
                    param2.setSw6(s2int(content.get(80)));
                    param2.setQ_rh(s2int(content.get(88)));
                    param2.setQ_spl(s2int(content.get(90)));
                    param2.setQ_a3(s2int(content.get(91)));
                    param2.setQ_dp03(s2int(content.get(93)));
                    param2.setQ_dp_idx(s2int(content.get(94)));
                    param2.setQ_n1(s2int(content.get(102)));
                    param2.setQ_c1(s2int(content.get(103)));
                    param2.setQ_h1(s2int(content.get(104)));
                    param2.setQ_n2(s2int(content.get(105)));
                    param2.setQ_c2(s2int(content.get(106)));
                    param2.setQ_h2(s2int(content.get(107)));
                    param2.setQ_n3(s2int(content.get(108)));
                    param2.setQ_c3(s2int(content.get(109)));
                    param2.setQ_h3(s2int(content.get(110)));
                    param2.setQ_n4(s2int(content.get(111)));
                    param2.setQ_c4(s2int(content.get(112)));
                    param2.setQ_h4(s2int(content.get(113)));
                    param2.setQ_cbn(s2int(content.get(114)));
                    param2.setQ_cbt(s2int(content.get(115)));
                    param2.setQ_cbh(s2int(content.get(116)));
                    param2.setQ_rain01(s2int(content.get(117)));
                    param2.setQ_rain02(s2int(content.get(118)));
                    param2.setQ_rain03(s2int(content.get(119)));
                    param2.setQ_rain06(s2int(content.get(120)));
                    param2.setQ_rain09(s2int(content.get(121)));
                    param2.setQ_rain12(s2int(content.get(122)));
                    param2.setQ_rain15(s2int(content.get(123)));
                    param2.setQ_rain18(s2int(content.get(124)));
                    param2.setQ_dt24(s2int(content.get(127)));
                    param2.setQ_max_at24(s2int(content.get(128)));
                    param2.setQ_min_at24(s2int(content.get(129)));
                    param2.setQ_max_at12(s2int(content.get(130)));
                    param2.setQ_min_at12(s2int(content.get(131)));
                    param2.setQ_snowh(s2int(content.get(132)));
                    param2.setQ_eva(s2int(content.get(134)));
                    param2.setQ_alr(s2int(content.get(135)));
                    param2.setQ_sunp(s2int(content.get(136)));
                    param2.setQ_gs(s2int(content.get(137)));
                    param2.setQ_sw1(s2int(content.get(138)));
                    param2.setQ_sw2(s2int(content.get(139)));

                    //判断总长度 字段不存在会报错
                    if(content.size()>140)param2.setQ_sw3(s2int(content.get(140)));
                    if(content.size()>141)param2.setQ_sw4(s2int(content.get(141)));
                    if(content.size()>142)param2.setQ_sw5(s2int(content.get(142)));
                    if(content.size()>143)param2.setQ_sw6(s2int(content.get(143)));
                    if(content.size()>144)param2.setRain032(s2d(content.get(144)));
                    if(content.size()>145)param2.setRain062(s2d(content.get(145)));
                    if(content.size()>146)param2.setRain122(s2d(content.get(146)));
                    if(content.size()>147)param2.setRain242(s2d(content.get(147)));
                    if(content.size()>148)param2.setStamp(s2T(content.get(148)));
                    datasourcemapper.surfInToDB(param2);
                }

            }
        }
        return allString;
    }



    //类型转换 String 转 Timestamp
    private java.sql.Timestamp s2T(String str){
        java.sql.Timestamp res = null;
        if(str != null && !str.equals("")){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d HH:mm:ss");
                // project获取到的是字符串类型的日期，需要转换成java.util.Date类型
                Date startDateUtil = sdf.parse(str);
                // java.util.Date类型的日期无法封装到SQL中，需要转换成java.sql.Date类型
                java.sql.Timestamp date = new java.sql.Timestamp(startDateUtil.getTime());
                res = date;
            } catch (NumberFormatException | ParseException e) {
                e.printStackTrace();
            }
        }else{

        }
        return res;
    }


    //类型转换 String 转 date
    private java.sql.Date s2date2(String str){
        java.sql.Date res = null;
        if(str != null && !str.equals("")){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
                // project获取到的是字符串类型的日期，需要转换成java.util.Date类型
                Date startDateUtil = sdf.parse(str);
                // java.util.Date类型的日期无法封装到SQL中，需要转换成java.sql.Date类型
                java.sql.Date date = new java.sql.Date(startDateUtil.getTime());
                res = date;
            } catch (NumberFormatException | ParseException e) {
                e.printStackTrace();
            }
        }else{

        }
        return res;
    }
    //类型转换 String 转 date
    private java.sql.Timestamp s2date(String str){
        java.sql.Timestamp res = null;
        if(str != null && !str.equals("")){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d H:mm");
                // project获取到的是字符串类型的日期，需要转换成java.util.Date类型
                Date startDateUtil = sdf.parse(str);
                // java.util.Date类型的日期无法封装到SQL中，需要转换成java.sql.Date类型
                java.sql.Timestamp date = new java.sql.Timestamp(startDateUtil.getTime());
                res = date;
            } catch (NumberFormatException | ParseException e) {
                e.printStackTrace();
            }
        }else{

        }
        return res;
    }

    //类型转换 String 转 Interger
    private int s2int(String str){
        int res = 0;
        if(str != null && !str.equals("")){
            try {
                res = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }else{
            res = 0;
        }
        return res;
    }

    //类型转换 String 转 double
    private double s2d(String str){
        double res = 0;
        if(str != null && !str.equals("") && str != ""){
            try {
                res = Double.parseDouble(str);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }else{
            res = 0;
        }
        return res;
    }

    // back 获取csv文件内容 备份
    public static List<Map<String,Object>> getResource11(byte[] bate) throws IOException {
        List<Map<String,Object>> allString = new ArrayList();
        Map<String,Object> callLogInfo ;
        List<String> list = new ArrayList();
        // 获取文件内容
        list = getSource(bate);
        //// 获取文件表头======================================
        List<String> title = Arrays.asList(list.get(0).split(","));
        String customerName = title.get(0).trim();
        String customerNo = title.get(1).trim();
        // 头部会带有"\uFEFF"值
        if(customerName.startsWith(FIX)){
            customerName = customerName.replace(FIX, "");
        }
        callLogInfo = new HashMap();
        callLogInfo.put("param1",customerName);
        callLogInfo.put("param2",customerNo);
        allString.add(callLogInfo);

        //// 循环内容===========================================
        list.remove(0);//去掉第一行表头
        for(int i = 0; i<list.size();i++){
            List<String> content = Arrays.asList(list.get(i).split(","));
            // 当没有添加额外参数时
            if(content!=null){
                callLogInfo = new HashMap();
                callLogInfo.put("param1",content.get(0));
                callLogInfo.put("param2",content.get(1));
                allString.add(callLogInfo);
            }
        }
        return allString;
    }


    /**
     * 读文件数据
     */
    public static List<String> getSource(byte[] bate) throws IOException {
        BufferedReader br = null;
        ByteArrayInputStream fis=null;
        InputStreamReader isr = null;
        try {
            fis = new ByteArrayInputStream(bate);
            //指定以UTF-8编码读入
            isr = new InputStreamReader(fis,"UTF-8");
            br = new BufferedReader(isr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String line;
        String everyLine ;
        List<String> allString = new ArrayList<>();
        try {
            //读取到的内容给line变量
            while ((line = br.readLine()) != null){
                everyLine = line;
                allString.add(everyLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis != null){
                fis.close();
            }
            if(isr != null){
                isr.close();
            }
        }
        return allString;
    }

    //登录
    public Result login(userParam param) {
        String login_name = param.getLogin_name().trim();
        String password = param.getPassword().trim();
        //参数校验
        String errorMsg = "";
        if(login_name.equals(null) || login_name.equals("")){errorMsg = "【非空错误】参数login_name不能为空！";}
        if(password.equals(null) || password.equals("")){errorMsg = "【非空错误】参数password不能为空！";}
        if(errorMsg != "")return Result.error(errorMsg);
        List<Map<String,String>> mapList = datasourcemapper.login(param);//查询
        if(mapList.size() > 0){
            //校验用户激活状态
            for(int i = 0 ,len=mapList.size(); i<len;i++) {
                String user_static = mapList.get(i).get("user_static");
                if(user_static.equals(null) || user_static.equals("") || user_static.equals("0")){
                    errorMsg = "【用户错误】该用户还未激活，请联系管理员激活！";
                }
            }
            if(errorMsg != "")return Result.error(errorMsg);

        }else{
            return Result.error("用户名/密码错误！");
        }
        return Result.success(mapList);
    }

    //添加用户
    public Result addUser(userParam param) {

        String login_name = param.getLogin_name();
        //校验用户是否存在
        if(!login_name.equals(null) && !login_name.equals("")){
            userParam check = new userParam();
            check.setLogin_name(login_name);
            List<Map<String,String>> mapList = datasourcemapper.getUserList(check);//查询
            if(mapList.size() > 0){
                return Result.error("当前登录名已存在，请更换其他登录名再注册！");
            }
        }
        String id = String.valueOf(System.currentTimeMillis());//用时间戳加文件大小做id
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String create_time = df.format(new Date());//获取当前create_time
        param.setId(id);//设置id
        param.setCreate_time(create_time);//记录上传时间
        datasourcemapper.addUser(param);
        return Result.success("保存成功！");
    }
    //删除一个用户
    public Result deleteUserById(userParam param) {
        String id = param.getId();
        //参数校验
        String errorMsg = "";
        if(id.equals(null) || id.equals("")){errorMsg = "【非空错误】参数id不能为空！";}
        if(errorMsg != "")return Result.error(errorMsg);
        datasourcemapper.deleteUserById(param);
        return Result.success("保存成功！");
    }
    //编辑保存用户信息
    public Result saveUser(userParam param) {

        //参数校验
        String errorMsg = "";

        if(errorMsg != "")return Result.error(errorMsg);
        datasourcemapper.saveUser(param);//上传
        return Result.success("保存成功！");
    }

    //获取用户列表
    public Result getUserList(userParam param) {

        List<Map<String,String>> mapList = datasourcemapper.getUserList(param);//查询
        if(mapList.size() > 0){
        }else{
            return Result.error("查询结果为空！");
        }
        return Result.success(mapList);
    }
















    //end
}

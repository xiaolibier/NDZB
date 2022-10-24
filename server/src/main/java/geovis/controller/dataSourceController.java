package geovis.controller;


import geovis.param.*;
import geovis.service.*;
import geovis.tools.Result;
import geovis.tools.selfTools;
import geovis.tools.wordUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


/***
 * 创建：lxq 2022-8-9
 * 作用：资源数据的处理
 */

@Api(tags = "对外接口列表")
@RestController
public class dataSourceController {

    @Value("${dataDir}")
    private String dataDir;
    @Autowired
    private dataSourceService datasourceservice;
    @Autowired
    private tongJiService tongjiservice;

    private static final String FIX="\uFEFF";
    private wordUtil wordutil = new wordUtil();



    @ApiOperation("获取所有站点列表")
    @RequestMapping(value = "/getStaticStation", method = {RequestMethod.GET,RequestMethod.POST})
    public Result getStaticStation(stationParam param) {
        Result res = datasourceservice.getStaticStation(param);
        return res;
    }

    @ApiOperation("修改成果数据")
    @RequestMapping(value = "/saveChengGuo", method = {RequestMethod.GET,RequestMethod.POST})
    public Result saveChengGuo( chengGuoParam param) {
        Result res = datasourceservice.saveChengGuo(param);
        return res;
    }
    @ApiOperation("上传成果数据")
    @RequestMapping(value = "/uploadChengGuo", method = {RequestMethod.GET,RequestMethod.POST})
    public Result uploadChengGuo( chengGuoParam param) {
        Result res = datasourceservice.uploadChengGuo(param);
        return res;
    }

    @ApiOperation("获取成果列表")
    @RequestMapping(value = "/getChengGuoList", method = {RequestMethod.GET,RequestMethod.POST})
    public Result getChengGuoList(chengGuoParam param) {
        Result res = datasourceservice.getChengGuoList(param);
        return res;
    }

    @ApiOperation("删除一个成果记录")
    @RequestMapping(value = "/deleteChengGuoRecordById", method = {RequestMethod.GET,RequestMethod.POST})
    public Result deleteChengGuoRecordById( chengGuoParam param) {
        Result res = datasourceservice.deleteChengGuoRecordById(param);
        return res;
    }
    @ApiOperation("删除一个资料记录")
    @RequestMapping(value = "/deleteZiLiaoRecordById", method = {RequestMethod.GET,RequestMethod.POST})
    public Result deleteYuBaoById( dataSourceParam param) {
        Result res = datasourceservice.deleteZiLiaoRecordById(param);
        return res;
    }

    @ApiOperation("获取续补资料列表")
    @RequestMapping(value = "/getFileList", method = {RequestMethod.GET,RequestMethod.POST})
    public Result getFileList(dataSourceParam param) {
        Result res = datasourceservice.getFileList(param);
        return res;
    }

    //用户接口
    @ApiOperation("登录")
    @RequestMapping(value = "/login", method = {RequestMethod.GET,RequestMethod.POST})
    public Result login( userParam param) {
        Result res = datasourceservice.login(param);
        return res;
    }

    @ApiOperation("添加一个用户 注册用户")
    @RequestMapping(value = "/addUser", method = {RequestMethod.GET,RequestMethod.POST})
    public Result addUser( userParam param) {
        Result res = datasourceservice.addUser(param);
        return res;
    }
    @ApiOperation("删除一个用户")
    @RequestMapping(value = "/deleteUserById", method = {RequestMethod.GET,RequestMethod.POST})
    public Result deleteUserById( userParam param) {
        Result res = datasourceservice.deleteUserById(param);
        return res;
    }
    @ApiOperation("编辑保存用户信息")
    @RequestMapping(value = "/saveUser", method = {RequestMethod.GET,RequestMethod.POST})
    public Result saveUser( userParam param) {
        Result res = datasourceservice.saveUser(param);
        return res;
    }
    @ApiOperation("获取用户列表 带查询")
    @RequestMapping(value = "/getUserList", method = {RequestMethod.GET,RequestMethod.POST})
    public Result getUserList( userParam param) {
        Result res = datasourceservice.getUserList(param);
        return res;
    }

    @ApiOperation("上传成果")
    @PostMapping("/uploadChengGuoFile")
    public Result uploadChengGuo(MultipartFile file, String datatype){
        String res = null;
        String buffer = "";
        chengGuoParam param = new chengGuoParam();
        if (file != null && !file.isEmpty()) {

            String fileName = file.getOriginalFilename();  // 文件名
            String fileOriginalName = file.getOriginalFilename();  // 原始文件名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));  // 后缀名
            fileName = UUID.randomUUID() + suffixName; // 新文件名
            String url = "/ChengGuo/"+fileName;
            String dir = dataDir+"/ChengGuo/";
            long size = file.getSize(); //kb 文件大小
            String size1 = String.valueOf(size); //kb
            if(size>1024)size1 = String.valueOf(size / 1024); //kb

            param.setFile_url(url);
            param.setName(fileName);//存新文件名
            param.setFile_type(suffixName);
            param.setType(datatype);
            param.setFile_size(size1);
            File tmpFile = new File(dir);
            if (!tmpFile.exists()) {
                tmpFile.mkdirs();
            }
            try {
                File dest = new File(dir  + fileName);
                file.transferTo(dest);
                //文件创建成功时 入库操作
                param.setStat("入库成功");
            } catch (IOException e) {
                e.printStackTrace();
            }

            String path = dir + fileName;
            buffer = wordutil.wordToHtmlString(path,path);
            param.setT_doc(buffer);
            /*
            try {
                if (path.endsWith(".doc")) {
                    FileInputStream is = new FileInputStream(path);
                    WordExtractor ex = new WordExtractor(is);
                    buffer = ex.getText();
                    param.setT_doc(buffer);
                    is.close();
                } else if (path.endsWith("docx")) {
                    OPCPackage opcPackage = POIXMLDocument.openPackage(path);
                    POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
                    buffer = extractor.getText();
                    param.setT_doc(buffer);
                    opcPackage.close();
                } else {
                    return Result.error("文件不是word文件");
                }
            } catch (Exception e) {
                //e.printStackTrace();
                return Result.error("读取word文件失败"+e.getMessage());
            }
            */


        }
        //return Result.success(param);
        return Result.success(param);
    }


    @ApiOperation("上传文件返回路径和其他文件相关信息")
    @PostMapping("/uploadFile")
    public Result uploadFile(MultipartFile file, String datatype){
        String res = null;
        dataSourceParam param = new dataSourceParam();
        if (file != null && !file.isEmpty()) {

            String fileName = file.getOriginalFilename();  // 文件名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));  // 后缀名
            fileName = UUID.randomUUID() + suffixName; // 新文件名
            String url = "/ChengGuo/"+fileName;
            String dir = dataDir+"/ChengGuo/";
            long size = file.getSize(); //kb 文件大小
            String size1 = String.valueOf(size); //kb
            if(size>1024)size1 = String.valueOf(size / 1024); //kb

            param.setUrl(url);
            param.setName(fileName);//存新文件名
            param.setFile_type(suffixName);
            param.setType(datatype);
            param.setFile_size(size1);
            File tmpFile = new File(dir);
            if (!tmpFile.exists()) {
                tmpFile.mkdirs();
            }
            try {
                File dest = new File(dir  + fileName);
                file.transferTo(dest);
                //文件创建成功时 入库操作
                param.setStat("入库成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Result.success(param);
    }


    @ApiOperation("上传资料")
    @PostMapping("/uploadZiLiao")
    public Result uploadZiLiao(MultipartFile file, String datatype){
        String res = null;
        dataSourceParam param = new dataSourceParam();
        if (file != null && !file.isEmpty()) {

            String fileName = file.getOriginalFilename();  // 文件名
            String oldFileName = file.getOriginalFilename();  // 文件名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));  // 后缀名
            fileName = UUID.randomUUID() + suffixName; // 新文件名
            String url = "/ZiLiao/"+fileName;
            String dir = dataDir+"/ZiLiao/";
            long size = file.getSize(); //kb 文件大小
            String size1 = String.valueOf(size); //kb
            if(size>1024)size1 = String.valueOf(size / 1024); //kb
            byte[] bate = null;
            List<Map<String, Object>> list = new ArrayList<>();
            try{
                bate =file.getBytes();
                datasourceservice.dataInDB(bate,datatype);//csv文件读取并循环每一行入库
            } catch (IOException e) {
                e.printStackTrace();
            }

            //处理文件本身存储到服务器
            param.setUrl(url);
            param.setName(oldFileName);//存新文件名
            param.setFile_type(suffixName);
            param.setType(datatype);
            param.setFile_size(size1);
            File tmpFile = new File(dir);
            if (!tmpFile.exists()) {
                tmpFile.mkdirs();
            }
            try {
                File dest = new File(dir  + fileName);
                file.transferTo(dest);
                res = "upload successFully!url="+url  + fileName;
                //文件创建成功时 入库操作
                param.setStat("入库成功");
                datasourceservice.zlInsertToDB(param);
            } catch (IOException e) {
                e.printStackTrace();
                res = "upload error!";
            }


        }
        return Result.success(param);
    }






// end
}
